import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import sun.rmi.runtime.Log;

import java.util.Random;
import java.util.concurrent.TimeUnit;

class Application {

    Application() {
        //observableExample();
        //singleExample();
        //timerExample(5);
        testNever();
        exampleWithoutLambdas();
    }

    private void singleExample() {
        System.out.println("Start " + Thread.currentThread());
        Single.create(emitter -> {
            try {
                long waitTime = new Random().nextInt(5) * 1000;
                Thread.sleep(waitTime);
                emitter.onSuccess(String.format("Rrequest completed [%d ms] %s...", waitTime, Thread.currentThread()));
            } catch (InterruptedException e) {
                emitter.onError(e);
            }
        }).subscribe(System.out::println);
        System.out.println("Finish " + Thread.currentThread());
    }

    private void observableExample() {
        System.out.println("Start " + Thread.currentThread());
        Observable<String> collector = Observable.merge(getAsyncRequest("A"), getAsyncRequest("B"));
        collector.subscribe(System.out::println);
        System.out.println("Finish " + Thread.currentThread());
    }

    private Observable<String> getAsyncRequest(String name) {
        return Observable.create(emitter -> {
            try {
                long waitTime = new Random().nextInt(5) * 1000;
                Thread.sleep(waitTime);
                emitter.onNext(String.format("%s request completed [%d ms] %s...", name, waitTime, Thread.currentThread()));
                emitter.onCompleted();
            } catch (InterruptedException e) {
                emitter.onError(e);
            }
        }).map(v -> (String) v);
    }

    private void timerExample(int ticks) {
        Observable.range(1, ticks)
                .zipWith(Observable.interval(1, TimeUnit.SECONDS), (Func2<Integer, Long, Object>) (integer, aLong) -> integer)
                .observeOn(Schedulers.io())
                .toBlocking()
                .subscribe(System.out::println);
    }

    private void testNever() {
        Observable.empty().subscribe(o -> Logger.log("Completed."));
    }

    private void exampleWithoutLambdas() {
        Logger.log("Before.");
        Observable.unsafeCreate(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                Logger.log("Generation Started.");
                subscriber.onNext(1);
                subscriber.onNext(2);
                subscriber.onNext(3);
                Logger.log("Generation Finished.");
                subscriber.onCompleted();
            }
        }).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                Logger.log("Completed.");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Integer integer) {
                Logger.log("%d", integer);
            }
        });
        Logger.log("After.");
    }
}