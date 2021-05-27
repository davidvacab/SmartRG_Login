package StaticControl;

public class ThreadRunner {
    public static void runTask(Runnable task, Thread.UncaughtExceptionHandler exCatch){
        Thread thread = new Thread(task);
        thread.setUncaughtExceptionHandler(exCatch);
        thread.start();
    }
}
