import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

import java.util.concurrent.TimeUnit;

class Chapter2 {

    Chapter2() {
//        exampleOuterAPI();
//        publishSubjectExample();
//        asyncSubjectExample();
//        replySubjectExample();
//        refCountExample();
        publishExample();
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

    /**
     * Creating API object when disposable will be subscribed.
     * RefCount check count of subscribers and then when subscribers count changed from 0
     * will be subscribing to observable. Subscribers count other than 1 will be ignored.
     * The same to *.share()
     */
    private void refCountExample() {
        Observable<Integer> observable = Observable.create(emitter -> {
            OutAPI outAPI = new OutAPI();
            outAPI.addListener(data -> {
                if (!emitter.isDisposed()) {
                    emitter.onNext(data);
                } else {
                    outAPI.shutdown();
                }
            });
        }).publish().refCount().map(e -> (Integer) e);

        Disposable disposableA = observable.subscribe(d -> Logger.log("A %d", d));
        Logger.log("Subscribed A.");
        Disposable disposableB = observable.subscribe(d -> Logger.log("B %d", d));
        Logger.log("Subscribed B.");
        Observable.timer(2, TimeUnit.SECONDS).subscribe(b -> {
            disposableA.dispose();
            Logger.log("Removed subscriber A.");
        });

        Observable.timer(4, TimeUnit.SECONDS).subscribe(c -> {
            disposableB.dispose();
            Logger.log("Removed subscriber B.");
        });
    }

    private void publishExample() {
        ConnectableObservable<Integer> observable = Observable.create(emitter -> {
            OutAPI outAPI = new OutAPI();
            outAPI.addListener(data -> {
                if (!emitter.isDisposed()) {
                    emitter.onNext(data);
                } else {
                    outAPI.shutdown();
                }
            });
        }).map(e -> (Integer) e).publish();

        Observable.timer(2, TimeUnit.SECONDS).subscribe(c -> {
            Logger.log("A subscribed.");
                observable.subscribe(e -> Logger.log("A %d.", e));

        });

        Observable.timer(4, TimeUnit.SECONDS).subscribe(c -> {
            Logger.log("B subscribed.");
            observable.subscribe(e -> Logger.log("B %d.", e));
        });
        observable.connect();
    }
}