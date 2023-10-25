package no.ntnu.tools;

public class WaitTask implements Runnable {
    private int seconds;

    public WaitTask(int seconds) {
        this.seconds = seconds;
    }

    @Override
    public void run() {
        try {
            System.out.println("WaitTask started");
            Thread.sleep(seconds * 1000); // Sleep for the specified number of seconds
            System.out.println("WaitTask was interrupted");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
