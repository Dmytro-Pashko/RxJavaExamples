import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Chapter2 {


    public Chapter2() {
//        exampleOuterAPI();
        subjectExample();
    }

    private void exampleOuterAPI() {

        Observable<Integer> observable = Observable.create(emitter -> {
            OutAPI outAPI = new OutAPI();
            outAPI.addListener(data -> {
                if (!emitter.isDisposed()) {
                    emitter.onNext(data);
                } else {
                    outAPI.shutdown();
                }
            });
        });
        Disposable disposable = observable.subscribe(e -> Logger.log("%d", e));
        Observable.interval(5, TimeUnit.SECONDS).subscribe(e -> disposable.dispose());
    }

    private class OutAPI {

        private OutApiListener listener;
        private boolean isInterrupted;

        OutAPI() {
            Logger.log("Outer API instance created.");
            new Thread(() -> {
                while (!isInterrupted) {
                    try {
                        Thread.sleep(500);
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
    }

    private interface OutApiListener {

        void onDataChanged(int data);
    }


    private void subjectExample() {
        PublishSubject<Integer> subject = PublishSubject.create();
        OutAPI api = new OutAPI();
        api.addListener(data -> {
            if (subject.hasObservers()) {
                subject.onNext(data);
            } else {
                api.shutdown();
            }
        });

        Disposable disposableA = subject.subscribe(e -> Logger.log("A, %d.", e));
        Disposable disposableB = subject.subscribe(e -> Logger.log("B, %d.", e));
        Disposable disposableC = subject.subscribe(e -> Logger.log("C, %d.", e));

        Observable.interval(5, TimeUnit.SECONDS).subscribe(a -> {
            disposableA.dispose();
            Logger.log("Removed subscriber A.");
        });

        Observable.interval(10, TimeUnit.SECONDS).subscribe(b -> {
            disposableB.dispose();
            Logger.log("Removed subscriber B.");
        });

        Observable.interval(15, TimeUnit.SECONDS).subscribe(c -> {
            disposableC.dispose();
            Logger.log("Removed subscriber C.");

        });
    }
}