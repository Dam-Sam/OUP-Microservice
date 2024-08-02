package LoadGen;

import Common.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class LoadGen {

    private static final ArrayList<String> errorList = new ArrayList<>();
    private static int lineCounter;

    private static final Map<String, String> headers = new HashMap<>();

    private static boolean disableTests = false;
    private static int defaultTest_Status = 0;
    private static String defaultTest_Contains = "";
    private static boolean hasDefaultTest;

    public static class LoadVars {
        public int a = 0;
        public int b = 0;
        public int c = 0;

        private final Map<Character, Integer> customVars = new HashMap<>();

        public LoadVars(int a, int b, int c, Map<Character, Integer> customVars) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.customVars.putAll(customVars);
        }

        public LoadVars() {
        }

        public static LoadVars from(LoadVars from) {
            return new LoadVars(from.a, from.b, from.c, from.customVars);
        }

        public void reset() {
            a = 0;
            b = 0;
        }

        private void setCustom(char var, int value) {
            customVars.put(var, value);
        }

        private void set(String directiveLine, LoadVars loadVars) {
            String varAssignment = directiveLine.split(" ", 2)[1].strip();
            char var = varAssignment.charAt(0);
            int value = varAssignment.length() > 1 ? Integer.parseInt(varAssignment.substring(2)) : 0;
            switch (var) {
                case 'a':
                    loadVars.a = value;
                    break;
                case 'b':
                    loadVars.b = value;
                    break;
                case 'c':
                    loadVars.c = value;
                    break;
                default:
                    loadVars.setCustom(var, value);
            }
        }

        public int getCustom(char var) {
            return customVars.get(var);
        }
    }

    private static final List<String> VALID_COMMANDS = Arrays.asList("get", "create", "update", "delete", "place", "place order", "info", "wipe", "purchased");

    private static LoadGenConfig config;

    public static void main(String[] args) throws IOException, InterruptedException {


        if (args.length == 0) {
            System.out.println("Usage: java WorkloadParser <config_file_path>");
            System.exit(1);
        }


        String currentDirectory = "";
        try {
            currentDirectory = getCurrentDirectory();
        } catch (URISyntaxException e) {
            System.out.println("Error getting current executing directory: \n" + e);
            System.exit(2);
        }

        try {
            config = loadConfig(args[0]);
        } catch (IOException e) {
            System.err.println("Error loading config file: " + e.getMessage());
        }

        Util.Logger.logLevel = config.getLogLevel();


        Http.setTimeout(config.getHttpClientTimeout());

        //MicroserviceBase.checkDependencies(null, config.getUserServiceBaseUrl(), config.getProductServiceBaseUrl(), config.getOrderServiceBaseUrl());

        StopWatch timer = new StopWatch();
        timer.start();


        if (config.getMode() == 1) {
            executeAuto();
        } else {
            executeWorkload(String.valueOf(Paths.get(currentDirectory, config.getLoadFile())));
        }
        timer.stop();
        Util.println("Total elapsed time: " + timer);
        Util.println("Total requests: " + Http.getRequestCount());
    }

    private static String getCurrentDirectory() throws URISyntaxException {
        URL classUrl = LoadGenConfig.class.getProtectionDomain().getCodeSource().getLocation();
        return Paths.get(classUrl.toURI()).toString();
    }

    private static LoadGenConfig loadConfig(String configFile) throws IOException {
        String configFileContent = ServiceConfig.getConfig(configFile, "LoadGen", true);
        return new ObjectMapper().readValue(configFileContent, LoadGenConfig.class);
    }

    private static void executeAuto() {
        System.out.println("LoadGen executing in auto mode (randomly generated)");
    }

    private static boolean eof = false;

    private static void executeWorkload(String loadFIle) {
        System.out.println("LoadGen executing in file mode (load file)");
        System.out.println(loadFIle);
        LoadVars loadVars = new LoadVars();

        StopWatch mainTimer = new StopWatch();
        Map<String, StopWatch> timers = new HashMap<String, StopWatch>();
        timers.put("main", mainTimer);

        boolean noParallelOverride = false;


        try (
                BufferedReader reader = new BufferedReader(new FileReader(loadFIle));
        ) {
            ThreadPoolExecutor executor = ThreadUtil.createVThreadExecutor(config.getMaxParallelRequests());
            if (config.getMaxParallelRequests() <= 1)
                noParallelOverride = true;
            String line;
            lineCounter = 0;
            while (!(line = getLine(reader)).equals("END")) {
                try {
                    if (line.isBlank() || line.startsWith("#") || line.startsWith("@")) {
                        //Ignore whitespace, comments, and line/goto labels
                        continue;
                    } else if (line.startsWith("END")) {
                        break;
                    }
                    if (line.charAt(0) == '!') {
                        String directiveLine = line.substring(1).strip();
                        if (directiveLine.startsWith("NOTEST")) {
                            Util.printlnYellow("RESPONSE TESTING DISABLED");
                            disableTests = true;
                        } else if (directiveLine.startsWith("SETTEST")) {
                            String[] split = directiveLine.split(" ", 2);
                            if(split.length > 1) {
                                String test = split[1].strip();
                                defaultTest_Contains = "";
                                if (test.contains("|")) {
                                    String[] testPart_split = test.split("\\|");
                                    defaultTest_Status = Integer.parseInt(testPart_split[0].strip());
                                    defaultTest_Contains = testPart_split[1].strip();
                                } else {
                                    defaultTest_Status = Integer.parseInt(test);
                                    defaultTest_Contains = "";
                                }
                                hasDefaultTest = true;
                                Util.printlnYellow("TEST DEFAULT SET:");
                                Util.printlnYellow("Must return: "+defaultTest_Status);
                                Util.printlnYellow("Must contain:"+defaultTest_Contains);
                            } else {
                                hasDefaultTest = false;
                                Util.printlnYellow("TEST DEFAULT REMOVED");
                            }
                        } else if (directiveLine.startsWith("TEST")) {
                            Util.printlnYellow("RESPONSE TESTING ENABLED");
                            disableTests = false;
                        } else if (directiveLine.startsWith("GOTO")) {
                            String label = directiveLine.split(" ", 2)[1].strip();
                            Util.printlnYellow("GOTO: Jumping to " + label);
                            while (!eof) {
                                line = getLine(reader);
                                if (!line.isEmpty() && line.charAt(0) == '@' && line.substring(1).strip().equals(label))
                                    break;
                            }
                        } else if (directiveLine.startsWith("HEADER")) {
                            waitForTasksToComplete(executor);
                            String[] parts = directiveLine.split(" ", 2)[1].strip()
                                    .split("=", 2);
                            headers.put(parts[0].strip(), parts[1].strip());
                        } else if (directiveLine.startsWith("SIM")) {
                            Util.printlnYellow("ENTERING SIMULATION MODE");
                            waitForTasksToComplete(executor);
                            String status = directiveLine.split(" ", 2)[1].strip();
                            headers.put(ServiceHttpHandler.HEADER_X_SIM, status);
                        } else if (directiveLine.startsWith("LOOP")) {
                            int loopCount = Integer.parseInt(directiveLine.split(" ")[1].strip());
                            ArrayList<String> lines = new ArrayList<>();
                            while (true) {
                                line = getLine(reader);
                                printLine(line, lineCounter);
                                if (line.isBlank() || line.startsWith("#")) {
                                    continue;
                                }
                                if (!line.startsWith("!ENDLOOP")) {
                                    if (line.startsWith("!")) {
                                        Util.Logger.LogError("DIRECTIVE NOT ALLOWED IN LOOP! ("+lineCounter+")");
                                        System.exit(99);
                                    }
                                    lines.add(line);
                                } else {
                                    break;
                                }
                            }
                            for (int loopCounter = 0; loopCounter < loopCount; loopCounter++) {
                                ArrayList<LoadInstruction> finalLines = new ArrayList<>();
                                for (String fline : lines) {
                                    finalLines.add(LoadInstruction.fromString(fline, lineCounter, loopCounter, loadVars));
                                }
                                if (isInParallelMode && !noParallelOverride) {
                                    executor.submit(() -> {
                                        try {
                                            for (LoadInstruction instruction : finalLines) {
                                                ExecuteLine(instruction);
                                            }
                                        } catch (Exception e1) {
                                            Util.Logger.Log("Exception during execution of loop line (PARALLEL mode).", e1);
                                        }
                                    });
                                } else {
                                    for (LoadInstruction instruction : finalLines) {
                                        ExecuteLine(instruction);
                                    }
                                }
                            }
                        } else if (directiveLine.startsWith("PSTART")) {
                            isInParallelMode = true;
                            Util.printlnYellow("PSTART Parallel mode enabled");
                        } else if (directiveLine.startsWith("PSYNC")) {
                            waitForTasksToComplete(executor);
                        } else if (directiveLine.startsWith("PEND")) {
                            isInParallelMode = false;
                            if (waitForTasksToComplete(executor))
                                Util.printlnGreen("PEND Parallel mode disabled.");
                        } else if (directiveLine.startsWith("SETVAR")) {
                            waitForTasksToComplete(executor);
                            loadVars.set(directiveLine, loadVars);
                        } else if (directiveLine.startsWith("PRINT")) {
                            String toPrint = directiveLine.split(" ", 2)[1];
                            Util.printlnGreen(toPrint);
                        } else if (directiveLine.startsWith("DELAY")) {
                            try {
                                int sleepMs = Integer.parseInt(directiveLine.split(" ", 2)[1]);
                                Thread.sleep(sleepMs);
                            } catch (InterruptedException e1) {
                                Util.printlnRed("Interrupted while sleeping (!DELAY).");
                                Util.printlnRed(e1.toString());
                            }
                        } else if (directiveLine.startsWith("TIMER")) {
                            String[] timerDir = directiveLine.split(" ", 3);
                            String action = timerDir[1];
                            String name = timerDir.length > 2 ? timerDir[2] : "main";
                            StopWatch timer = timers.containsKey(name) ? timers.get(name) : new StopWatch();
                            timers.putIfAbsent(name, timer);

                            if (action.equalsIgnoreCase("START")) {
                                timer.start();
                            } else if (action.equalsIgnoreCase("STOP")) {
                                timer.stop();
                            } else if (action.equalsIgnoreCase("PRINT")) {
                                Util.printlnGreen("TIMER '" + name + "' elapsed: " + timer);
                            }
                        }
                    } else {
                        String finalLine = line;
                        int lineNum = lineCounter;
                        //LoadVars varCopy = LoadVars.from(loadVars);
                        LoadInstruction instruction = LoadInstruction.fromString(finalLine, lineNum, loadVars);
                        if (isInParallelMode && !noParallelOverride) {
                            executor.submit(() -> {
                                try {
                                    ExecuteLine(instruction);
                                } catch (Exception e1) {
                                    Util.Logger.Log("Exception during execution of line (PARALLEL mode).", e1);
                                }
                            });
                        } else {
                            try {
                                ExecuteLine(instruction);
                            } catch (Exception e1) {
                                Util.Logger.Log("Exception during execution of line (main thread).", e1);
                            }
                        }
                    }
                } catch (Exception e) {
                    Util.Logger.LogError("Line " + lineCounter + "(" + line + ") Unhandled exception:");
                    Util.Logger.Log(e);
                }

            }
            executor.shutdown();
            try {
                Util.printlnGreen("***Finished script. Waiting 10s for PARALLEL threads to complete***");
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Util.Logger.Log("Exception while waiting for parallel threads to complete. Now continuing execution.", e);
            } finally {
                executor.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!errorList.isEmpty() && !disableTests) {
            Util.printlnRed("Your script has " + errorList.size() + " failed tests.");
            Util.println("--------------------------------------------------------------------------------");

            for (int i = 0; i < errorList.size(); i++) {
                Util.println("FAILURE #" + i);
                Util.println(errorList.get(i));
                Util.println("--------------------------------------------------------------------------------");
            }
        }

        Util.printlnGreen("All timers in script:");
        for (Map.Entry<String, StopWatch> timer : timers.entrySet()) {
            Util.printlnGreen(timer.getKey() + " elapsed: " + timer.getValue());
        }

    }

    private static final int taskWaitTimeout = 60000;
    private static final int taskWaitInterval = 2;

    private static boolean waitForTasksToComplete(ThreadPoolExecutor executor) throws InterruptedException {

        return waitForTasksToComplete(executor, taskWaitInterval, taskWaitTimeout);
    }

    private static boolean waitForTasksToComplete(ThreadPoolExecutor executor, int interval, int taskWaitTimeout) throws InterruptedException {
        try {
            long start = System.currentTimeMillis();
            while (executor.getActiveCount() > 0) {
                if ((System.currentTimeMillis() - start) > taskWaitTimeout) {
                    Util.Logger.Warn("Timeout while waiting for parallel requests to complete.");
                    return false;
                }
                Thread.sleep(interval);
            }
            return true;
        } catch (InterruptedException e) {
            Util.Logger.Log("Interrupted while waiting for parallel requests to complete.", e);
            return true;
        }
    }

    private static boolean isInParallelMode = false;

    private static void printLine(String line, int lineCounter) {
        printLine(line, lineCounter, 0);
    }

    private static void printLine(LoadInstruction instruction) {
        printLine(instruction.getLine(), instruction.getLineNumber(), instruction.getLoopCounter(),
                Util.ANSI_LIGHT_GRAY + " ==> " + instruction.getLineWithValues());
    }

    private static void printLine(String line, int lineCounter, int loopNumber) {
        Util.Logger.Log(Util.Logger.INFO, "%d.%d: %s", lineCounter, loopNumber, line);
    }

    private static void printLine(String line, int lineCounter, int loopNumber, String extra) {
        Util.Logger.Log(Util.Logger.INFO, "%d.%d: %s %s", lineCounter, loopNumber, line, extra);
    }

    private static String getLine(BufferedReader reader) throws IOException {
        lineCounter++;
        String line = reader.readLine();
        if (line != null) {
            line = line.strip();
            printLine(line, lineCounter);
        } else {
            line = "END";
            eof = true;
        }
        return line;

    }


    private static void assignVar(String directiveLine, LoadVars loadVars) {
        String[] dirSplit = directiveLine.split(" ");
        String var = dirSplit[1].strip();
        switch (var) {
            case "a":
                loadVars.a = Integer.parseInt(dirSplit[2].strip());
                break;
            case "b":
                loadVars.b = Integer.parseInt(dirSplit[2].strip());
                break;
            case "c":
                loadVars.c = Integer.parseInt(dirSplit[2].strip());
                break;
            default:
                break;
        }
    }

    private static void ExecuteLine(LoadInstruction instruction) {
        String command = instruction.getCommand();

        try {
            if (!VALID_COMMANDS.contains(command)) {
                Util.printlnRed("Line " + lineCounter + " - Invalid command: " + command);
                return;
            }

            instruction.setHeaders(headers);
            ServiceSender sender = ServiceSender.createSender(instruction.getService(), config);
            Http.Response response = sender.sendCommand(instruction);
            String testResults = disableTests ? "" : getTestResults(response, instruction);

            printLine(instruction);
//            System.out.println(response);
            if (!testResults.isBlank()) {
//                Util.printlnRed(testResults);
                errorList.add(instruction + "\n" + response.getCode() + " - " + response.getBody() + "\n" +
                        Util.ANSI_RED + testResults + Util.ANSI_RESET);
            }
        } catch (Exception e) {
            errorList.add(Util.ANSI_RED + "ERROR: " + instruction + "\n" + e + Util.ANSI_RESET);
            Util.Logger.LogError("Unhandled exception during execution of line." + e);
        }
    }


    public static String getTestResults(Http.Response response, LoadInstruction instruction) {

        StringBuilder b = new StringBuilder();
        int expCode = hasDefaultTest ? defaultTest_Status : instruction.getExpectedStatusCode();
        String shouldContain = hasDefaultTest ? defaultTest_Contains : instruction.getShouldContain();

        if(hasDefaultTest){

        }

        if (expCode > 0 && response.getCode() != expCode) {
            b.append("*ERROR* Http Status not what was expected. Expected: ");
            b.append(expCode).append(" Actual: ").append(response.getCode());
        }
        if (shouldContain != null && !shouldContain.isBlank() && !response.getBody().contains(shouldContain)) {
            b.append("*ERROR* Response body does not contain expected text. Expected: ");
            b.append(shouldContain);
        }

        return b.toString();
    }


}
