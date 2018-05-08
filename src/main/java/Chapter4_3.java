import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Chapter4_3 {

    Chapter4_3() {
//        publishSubjectExample();
//        bufferingPublishSubjectExample();
        try {
            schedulerExample();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hot observer
     */
    private void publishSubjectExample() {
        PublishSubject<String> subject = PublishSubject.create();

        subject.onNext("one");
        subject.onNext("two");

        subject.subscribe(value -> {
            Logger.log("%s", value);
        });

        subject.onNext("three");
        subject.onNext("four");
    }

    /**
     * Hot observer with buffering
     */
    private void bufferingPublishSubjectExample() {
        ReplaySubject<String> subject = ReplaySubject.create();

        subject.onNext("one");
        subject.onNext("two");

        subject.subscribe(value -> {
            Logger.log("%s", value);
        });

        subject.onNext("three");
        subject.onNext("four");
    }

    private void schedulerExample() throws InterruptedException {
        final Thread mainThread = Thread.currentThread();
        final long startTime = Schedulers.newThread().now(TimeUnit.MILLISECONDS);
        Logger.log("Start time : %d", startTime);
        Schedulers.newThread()
                .scheduleDirect(() -> {
                    long endTime = System.currentTimeMillis();
                    Logger.log("End time : %d", endTime);
                    Logger.log("Process takes : %d", endTime - startTime);
                    synchronized (mainThread) {
                        mainThread.notify();
                    }
                }, 5000, TimeUnit.MILLISECONDS);
        synchronized (mainThread) {
            Thread.currentThread().wait();
        }
    }

}
