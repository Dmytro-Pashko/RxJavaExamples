import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * The first chapter of the book "Reactive Programming with RxJava"
 * Samples implemented using RxJava2.
 */

class Chapter1 {

    Chapter1() {
//        observableExample();
//        singleExample();
//        timerExample(5);
//        testNever();
//        exampleWithoutLambdas();
//        fewSubscribers();
//        cachingHttpRequestExample();
//        infinityStreamExample();
//        testCallable();
//        timerExample();
//        intervalExample();
    }

    private void observableExample() {
        Logger.log("Started.");
        Observable<String> collector = Observable.merge(getAsyncRequest("A"), getAsyncRequest("B"));
        collector.subscribe(System.out::println);
        Logger.log("Finished");
    }

    private Observable<String> getAsyncRequest(String name) {
        return Observable.create(emitter -> {
            try {
                long waitTime = new Random().nextInt(5) * 1000;
                Thread.sleep(waitTime);
                emitter.onNext(String.format("%s request completed [%d ms] %s...", name, waitTime, Thread.currentThread()));
                emitter.onComplete();
            } catch (InterruptedException e) {
                emitter.onError(e);
            }
        }).map(v -> (String) v);
    }

    private void singleExample() {
        Logger.log("Started.");
        Single.create(emitter -> {
            try {
                long waitTime = new Random().nextInt(5) * 1000;
                Thread.sleep(waitTime);
                emitter.onSuccess(String.format("Rrequest completed [%d ms] %s...", waitTime, Thread.currentThread()));
            } catch (InterruptedException e) {
                emitter.onError(e);
            }
        }).subscribe(System.out::println);
        Logger.log("Finished.");
    }

    private void timerExample(int ticks) {
        Observable.range(1, ticks)
                .zipWith(Observable.interval(1, TimeUnit.SECONDS), (io.reactivex.functions.BiFunction<Integer, Long, Object>) (integer, aLong) -> integer)
                .observeOn(Schedulers.io())
                .blockingNext().forEach(tick -> Logger.log("%d",tick));
    }

    private void testNever() {
        Observable.empty().subscribe(o -> Logger.log("Completed."));
    }

    private void exampleWithoutLambdas() {
        Logger.log("Before.");
        Observable.unsafeCreate(new ObservableSource<Integer>() {
            @Override
            public void subscribe(Observer<? super Integer> observer) {
                Logger.log("Generation Started.");
                observer.onNext(1);
                observer.onNext(2);
                observer.onNext(3);
                Logger.log("Generation Finished.");
                observer.onComplete();
            }
        }).subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer integer) {
                Logger.log("%d", integer);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                Logger.log("Completed.");
            }
        });

        Logger.log("After.");
    }

    private void fewSubscribers() {
        Observable<Integer> observable = Observable.create(emitter -> {
            Logger.log("Creating");
            emitter.onNext(42);
            emitter.onComplete();
        });
        observable.subscribe(value -> Logger.log("A = %d", value));
        observable.subscribe(value -> Logger.log("B = %d", value));
    }

    private void cachingHttpRequestExample() {
        HttpClient client = new HttpClient();
        Observable<String> networkCall = Observable.unsafeCreate(emitter -> {
            try {
                String response = client.sendGetRequest("https://www.wikipedia.org");
                emitter.onNext(response);
                emitter.onComplete();
            } catch (IOException e) {
                emitter.onError(e);
            }
        }).cache().cast(String.class);
        long start = System.currentTimeMillis();
        networkCall.subscribe();
        Logger.log("First request takes %d ms.", System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        networkCall.subscribe();
        Logger.log("Second request takes %d ms.", System.currentTimeMillis() - start);
    }


    private void infinityStreamExample() {
        new DisposableExample().test();
    }

    class DisposableExample {

        private Disposable subscription;

        void test() {
            Logger.log("Infinity stream created.");
            Observable<BigInteger> observable = Observable.create(emitter -> {
                Runnable r = () -> {
                    BigInteger i = BigInteger.ZERO;
                    while (!emitter.isDisposed()) {
                        emitter.onNext(i);
                        i = i.add(BigInteger.ONE);
                    }
                };
                new Thread(r).start();
            });
            subscription = observable.subscribe(value -> {
                Logger.log("%d", value);
                if (value.intValue() > 65535) {
                    subscription.dispose();
                }
            });
        }
    }

    private void testCallable() {
        HttpClient client = new HttpClient();
        Observable<String> networkCall = Observable.fromCallable(() -> client.sendGetRequest("https://www.wikipedia.org"))
                .cache().cast(String.class);
        long start = System.currentTimeMillis();
        networkCall.subscribe();
        Logger.log("First request takes %d ms", System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        networkCall.subscribe();
        Logger.log("Second request takes %d ms", System.currentTimeMillis() - start);
    }

    private void timerExample() {
        Logger.log("Timer started.");
        Observable.timer(1, TimeUnit.SECONDS)
                .blockingSubscribe((zero) -> Logger.log("%d", zero));
        Logger.log("Timer stopped.");
    }


    private void intervalExample() {
        Logger.log("Interval timer started.");
        Observable.interval(1_000_000/60, TimeUnit.MICROSECONDS)
                .blockingSubscribe((i) -> Logger.log("%d", i));
        Logger.log("Interval  stopped.");
    }
}