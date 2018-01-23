import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

import java.util.concurrent.TimeUnit;

class Chapter2 {

    Chapter2() {
//        exampleOuterAPI();
//        publishSubjectExample();
//        asyncSubjectExample();
        replySubjectExample();
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
        Disposable disposable = observable.subscribe(d -> Logger.log("%d", d));
        Observable.interval(5, TimeUnit.SECONDS).subscribe(e -> disposable.dispose());
    }

    /**
     * |   |   | A | A |AB |AB |ABC|ABC| BC|  BC|   C|   C|
     * ---------------------------------------------------|Finish
     * | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 |
     */
    private void publishSubjectExample() {
        PublishSubject<Integer> subject = PublishSubject.create();
        OutAPI api = new OutAPI();
        api.addListener(data -> {
            if (subject.hasObservers()) {
                subject.onNext(data);
            }
            if (subject.hasComplete()) {
                api.shutdown();
            }
        });

        Disposable disposableA = subject
                .delaySubscription(2, TimeUnit.SECONDS)
                .subscribe(e -> Logger.log("A %d.", e));

        Disposable disposableB = subject
                .delaySubscription(4, TimeUnit.SECONDS)
                .subscribe(e -> Logger.log("B %d.", e));

        Disposable disposableC = subject
                .delaySubscription(6, TimeUnit.SECONDS)
                .subscribe(e -> Logger.log("C %d.", e));

        Observable.timer(8, TimeUnit.SECONDS).subscribe(a -> {
            disposableA.dispose();
            Logger.log("Removed subscriber A.");
        });

        Observable.timer(10, TimeUnit.SECONDS).subscribe(b -> {
            disposableB.dispose();
            Logger.log("Removed subscriber B.");
        });

        Observable.timer(12, TimeUnit.SECONDS).subscribe(c -> {
            disposableC.dispose();
            Logger.log("Removed subscriber C.");
            subject.onComplete();
        });
    }


    /**
     * Waiting for generation number above that 95 and will be emitted.
     * Emmit only last value before onComplete.
     */
    private void asyncSubjectExample() {
        AsyncSubject<Integer> subject = AsyncSubject.create();
        OutAPI api = new OutAPI();
        api.addListener(data -> {
            if (subject.hasObservers()) {
                subject.onNext(data);
                if (data > 95) {
                    subject.onComplete();
                    api.shutdown();
                }
            } else {
                api.shutdown();
            }
        });

        subject.subscribe(e -> Logger.log("%d.", e));
        Logger.log("Waiting for generation.");
    }

    /**
     * Subject with caching previous data.
     */
    private void replySubjectExample() {

        OutAPI api = new OutAPI();
        ReplaySubject<Integer> subject = ReplaySubject.create();
        api.addListener(data -> {
            if (subject.hasObservers()) {
                subject.onNext(data);
            }
            if (subject.hasComplete()) {
                api.shutdown();
            }
        });

        Disposable disposableA = subject
                .delaySubscription(2, TimeUnit.SECONDS)
                .subscribe(e -> Logger.log("A %d.", e));

        Disposable disposableB = subject
                .delaySubscription(4, TimeUnit.SECONDS)
                .subscribe(e -> Logger.log("B %d.", e));

        Disposable disposableC = subject
                .delaySubscription(6, TimeUnit.SECONDS)
                .subscribe(e -> Logger.log("C %d.", e));

        Observable.timer(8, TimeUnit.SECONDS).subscribe(a -> {
            disposableA.dispose();
            Logger.log("Removed subscriber A.");
        });

        Observable.timer(10, TimeUnit.SECONDS).subscribe(b -> {
            disposableB.dispose();
            Logger.log("Removed subscriber B.");
        });

        Observable.timer(12, TimeUnit.SECONDS).subscribe(c -> {
            disposableC.dispose();
            Logger.log("Removed subscriber C.");
            subject.onComplete();
        });

    }
}