package LoadGen;

import Common.Util;

import java.util.Arrays;
import java.util.Map;

public class LoadInstruction implements Cloneable {
    private String service;

    private String command;
    private String method;

    private String[] values;

    private int expectedStatusCode = 0;
    private String shouldContain = null;

    private String line = "";

    private int lineNumber;

    private int loopCounter;

    private Map<String, String> headers;

    public LoadInstruction(String service, String command, String method, String[] values) {
        this.service = service;
        this.command = command;
        this.method = method;
        this.values = values;
    }

    public LoadInstruction(String service, String command, String method, String[] values, int expectedStatusCode, String shouldContain) {
        this.service = service;
        this.command = command;
        this.method = method;
        this.values = values;
        this.expectedStatusCode = expectedStatusCode;
        this.shouldContain = shouldContain;
    }

    private LoadInstruction(String line, int lineNumber, LoadGen.LoadVars loadVars) {

        this.line = line;
        this.lineNumber = lineNumber;
        String instruction = line;
        String expCode = null;
        String expContains = null;


        if (line.contains("====")) {
            String[] inst_test_split = line.split("====");
            instruction = inst_test_split[0].strip();
            expCode = inst_test_split[1].strip();
            if (expCode.contains("|")) {
                String[] testPart_split = expCode.split("\\|");
                expCode = testPart_split[0].strip();
                expContains = testPart_split[1].strip();
            }
        }

        if (expCode != null) {
            this.expectedStatusCode = Integer.parseInt(expCode);
        }
        this.shouldContain = expContains;


        String[] lineParts = instruction.split(" ");
        this.service = lineParts[0].strip().toUpperCase();
        this.command = lineParts[1].strip().toLowerCase();
        if (this.command.equals("info"))
            this.command = "get";

        if (this.command.equals("get") || this.command.equals("purchased"))
            this.method = "GET";
        else
            this.method = "POST";

        if (this.command.equals("place"))
            this.command = "place order";

        this.values = Arrays.copyOfRange(lineParts, 2, lineParts.length);

        try {
            for (int i = 0; i < this.values.length; i++) {
                if(this.values[i].startsWith("var.")){
                    switch (this.values[i].split("\\.")[1].strip()) {
                        case "a++" -> this.values[i] = String.valueOf(loadVars.a++);
                        case "b++" -> this.values[i] = String.valueOf(loadVars.b++);
                        case "c++" -> this.values[i] = String.valueOf(loadVars.c++);
                        case "a" -> this.values[i] = String.valueOf(loadVars.a);
                        case "b" -> this.values[i] = String.valueOf(loadVars.b);
                        case "c" -> this.values[i] = String.valueOf(loadVars.c);
                        case "a--" -> this.values[i] = String.valueOf(loadVars.a--);
                        case "b--" -> this.values[i] = String.valueOf(loadVars.b--);
                        case "c--" -> this.values[i] = String.valueOf(loadVars.c--);
                    }
                }
                if (this.values[i].startsWith("rnd.")) {
                    String[] rndParts = this.values[i].substring(4).split(":");
                    int min = Integer.parseInt(rndParts[0]);
                    int max = Integer.parseInt(rndParts[1]);
                    int randomNum = (int) (Math.random() * ((max - min) + 1)) + min;
                    this.values[i] = String.valueOf(randomNum);
                }
            }
        } catch (Exception e) {
            Util.print("Exception during execution of line (PARALLEL mode).", e);
        }
    }

    public static LoadInstruction fromString(String loadLine, int lineNumber, LoadGen.LoadVars loadVars) {
        return new LoadInstruction(loadLine, lineNumber, loadVars);
    }

    public static LoadInstruction fromString(String loadLine, int lineNumber, int loopNumber, LoadGen.LoadVars loadVars) {
        return new LoadInstruction(loadLine, lineNumber, loadVars).setLoopCounter(loopNumber);
    }

    public String getService() {
        return service;
    }

    public String getCommand() {
        return command;
    }

    public String[] getValues() {
        return values;
    }


    public String getMethod() {
        return method;
    }

    public int getExpectedStatusCode() {
        return expectedStatusCode;
    }

    public String getShouldContain() {
        return shouldContain;
    }

    public String getLine() {

        return line;
    }

    public String getLineWithValues() {

        return this.service + " " + command + " " + String.join(" ", values);
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getLoopCounter() {
        return loopCounter;
    }

    public LoadInstruction setLoopCounter(int loopCounter) {
        this.loopCounter = loopCounter;
        return this;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "LoadInstruction{" +
                "service='" + service + '\'' +
                ", command='" + command + '\'' +
                ", method='" + method + '\'' +
                ", values=" + Arrays.toString(values) +
                ", expectedStatusCode=" + expectedStatusCode +
                ", shouldContain='" + shouldContain + '\'' +
                ", line='" + line + '\'' +
                ", lineNumber=" + lineNumber +
                ", loopCounter=" + loopCounter +
                '}';
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public LoadInstruction setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public boolean hasHeaders() {
        return this.headers != null;
    }
}


