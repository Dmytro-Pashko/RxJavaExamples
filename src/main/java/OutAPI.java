import java.util.Random;

public class OutAPI {
    private OutApiListener listener;
    private boolean isInterrupted;

    OutAPI() {
        new Thread(() -> {
            Logger.log("Outer API instance created.");
            while (!isInterrupted) {
                try {
                    Thread.sleep(1000);
                    if (listener != null) {
                        listener.onDataChanged(new Random().nextInt(100));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Logger.log("Outer API instance stopped.");
        }).start();
    }

    void addListener(OutApiListener listener) {
        this.listener = listener;
    }

    void shutdown() {
        this.isInterrupted = true;
    }

    interface OutApiListener {

        void onDataChanged(int data);
    }
}
