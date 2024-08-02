package Common;

public class Util {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_LIGHT_GRAY = "\u001B[37m";

    public static boolean PRINT_STACK_TRACE = true;

    public static void printlnRed(String text) {
        System.out.println(ANSI_RED + text + ANSI_RESET);
    }

    public static void print(Exception e) {
        printlnRed(e.getMessage());
        if (PRINT_STACK_TRACE) {
            System.out.print(ANSI_RED);
            e.printStackTrace();
            System.out.print(ANSI_RESET);
        }
    }

    public static void print(String message, Exception e) {
        printlnRed(message);
        print(e);
        printlnRed(e.getMessage());
        if (PRINT_STACK_TRACE) {
            System.out.print(ANSI_RED);
            e.printStackTrace();
            System.out.print(ANSI_RESET);
        }
    }

    public static void println(String text, String color) {
        System.out.println(color + text + ANSI_RESET);
    }

    public static void printlnGreen(String text) {
        System.out.println(ANSI_GREEN + text + ANSI_RESET);
    }

    public static void println(String text) {
        System.out.println(text);
    }

    public static String Green(String text) {
        return ANSI_GREEN + text + ANSI_RESET;
    }
    public static String Red(String text) {
        return ANSI_RED + text + ANSI_RESET;
    }
    public static String Yellow(String text) {
        return ANSI_YELLOW + text + ANSI_RESET;
    }

    public static void printlnYellow(String text) {
        System.out.println(Yellow(text));
    }

    public static class Logger {
        public static final int TRACE = 0;
        public static final int DEBUG = 1;
        public static final int INFO = 2;
        public static final int WARN = 3;
        public static final int ERROR = 4;
        public static final int FORCE = 100;
        public static int logLevel = INFO;

        public static void Log(int level, String message) {
            if (level >= logLevel) {
                switch (level) {
                    case TRACE:
                    case DEBUG:
                        System.out.println(ANSI_LIGHT_GRAY + message + ANSI_RESET);
                        break;
                    case WARN:
                        System.out.println(ANSI_YELLOW + message + ANSI_RESET);
                        break;
                    case ERROR:
                        System.out.println(ANSI_RED + message + ANSI_RESET);
                        break;
                    default:
                        System.out.println(message);
                        break;

                }
            }
        }

        public static void Log(int level, String fmessage, Object... objs) {
            if (level >= logLevel) {
                String formatted = String.format(fmessage, objs);
                Log(level, formatted);
            }
        }

        public static void Log(int level, String message, Exception e) {
            Log(level, message);
            Log(e);
        }

        public static void Log(int level, String message, String ansiColour) {
            Log(level,"%S%S%S", ansiColour, message, ANSI_RESET);
        }

        public static void Log(Exception e) {
            Log(ERROR, e.getMessage());
            if (logLevel <= DEBUG) {
                System.out.print(ANSI_RED);
                e.printStackTrace();
                System.out.print(ANSI_RESET);
            }
        }

        public static void Log(int level, Object obj) {
            Log(level, obj.toString());
        }

        public static void Logf(int level, String fMessage, Object... obj) {
            Log(level, fMessage, obj);
        }

        public static void Logf(int level, String color, String fMessage, Object... obj) {
            Log(level, String.format(fMessage, obj), color);
        }


        public static void Log(String message, Exception e) {
            Log(ERROR, message);
            Log(e);
        }

        public static void LogTrace(String s) {
            Log(TRACE, s);
        }
        public static void LogTrace(String s, Object... objs) {
            Logf(TRACE, s, objs);
        }

        public static void LogDebug(String s) {
            Log(DEBUG, s);
        }

        public static void LogDebug(String s, Object... objs) {
            Logf(DEBUG, s, objs);
        }

        public static void LogInfo(String s) {
            Log(INFO, s);
        }

        public static void Warn(String s) {
            Log(WARN, s);
        }

        public static void LogError(String s) {
            Log(ERROR, s);
        }
    }
}