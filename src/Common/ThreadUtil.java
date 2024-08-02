package Common;

import java.util.concurrent.*;

public class ThreadUtil {
    public static class VirtualThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            return Thread.ofVirtual().unstarted(r);
        }
    }

    public static class MainRunsWhenFullPolicy implements RejectedExecutionHandler {
        private boolean ignoreShutdown = false;
        public MainRunsWhenFullPolicy(boolean ignoreShutdown) { this.ignoreShutdown = ignoreShutdown; }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (ignoreShutdown || !e.isShutdown()) {
                r.run();
            }
        }
    }
    public static ThreadPoolExecutor createVThreadExecutor(int maxSize) {
        return createVThreadExecutor(maxSize, maxSize, maxSize);
    }

    public static ThreadPoolExecutor createVThreadExecutor(int queueSize, int corePoolSize, int maximumPoolSize) {
        //return Executors.newFixedThreadPool(config.getMaxParallelRequests());
        long keepAliveTime = 0L;
        TimeUnit unit = TimeUnit.MILLISECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(queueSize);
        ThreadFactory threadFactory = new VirtualThreadFactory();
        RejectedExecutionHandler handler = new MainRunsWhenFullPolicy(true);
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }
}
