import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Chapter4_3 {

    Chapter4_3() throws Exception {
//        publishSubjectExample();
//        bufferingPublishSubjectExample();
//        schedulerExample();
//        ioExample();
        computationExample();
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

    /**
     * This Scheduler better for asynchronously performing blocking IO.
     * Worker instances must be disposed manually.
     */
    private void ioExample() {
        executeTasks(getTaskList(20), Schedulers.io());
        Observable.just(1).delay(10, TimeUnit.SECONDS)
                .blockingSubscribe();
    }

    /**
     * Create active thread count equals to number of processors.
     * This type of Scheduler(Executor) better to handle computation work, and handling callbacks.
     */
    private void computationExample() {
        executeTasks(getTaskList(20), Schedulers.computation());
        Observable.just(1).delay(10, TimeUnit.SECONDS)
                .blockingSubscribe();
    }

    private void executeTasks(final List<Runnable> tasks, final Scheduler scheduler) {
        for (int i = 0; i < tasks.size(); i++) {
            scheduler.scheduleDirect(tasks.get(i));
            Logger.log("%d Scheduled.", i);
        }
    }

    private List<Runnable> getTaskList(int count) {
        return IntStream.range(0, count)
                .mapToObj(this::getTask)
                .collect(Collectors.toList());
    }

    private Runnable getTask(int number) {
        return () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Logger.log("%d Destroyed.", number);
        };
    }
}
