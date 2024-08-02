package LoadGen;

import Common.Http;

import java.text.DecimalFormat;
import java.util.Formatter;

public class StopWatch {
    private long startTime = 0;
    private long stopTime = 0;
    private boolean running = false;
    private long startReqCount = 0;
    private long stopReqCount = 0;

    public void start() {
        this.startReqCount = Http.getRequestCount();
        this.running = true;
        this.startTime = System.currentTimeMillis();
    }

    public void stop() {
        this.stopTime = System.currentTimeMillis();
        this.running = false;
        this.stopReqCount = Http.getRequestCount();
    }

    public long getElapsed() {
        long elapsed;
        if (running) {
            elapsed = (System.currentTimeMillis() - startTime);
        } else {
            elapsed = (stopTime - startTime);
        }
        return elapsed;
    }

    public long getElapsedRequests() {
        long elapsed;
        if (running) {
            elapsed = (Http.getRequestCount() - startReqCount);
        } else {
            elapsed = (stopReqCount - startReqCount);
        }
        return elapsed;
    }

    @Override
    public String toString() {
        long elapsed = getElapsed();
        double elapsedSeconds = elapsed / (double)1000;
        long reqs = getElapsedRequests();
        long milliseconds = elapsed % 1000;
        long seconds = (elapsed / 1000) % 60;
        long minutes = (elapsed / (1000 * 60)) % 60;
        long hours = (elapsed / (1000 * 60 * 60)) % 24;
        double reqsPerSecond = (double) reqs / elapsedSeconds;
        String rpsFormatted = new DecimalFormat("#####.#").format(reqsPerSecond);

        return String.format("%02d:%02d:%02d.%03d - %d total Requests (%s requests/s)",
                hours, minutes, seconds, milliseconds, reqs, rpsFormatted);
    }
}