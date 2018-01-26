import com.sun.media.sound.InvalidFormatException;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;
import org.jsoup.Jsoup;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.stream.IntStream;

class Chapter3 {

    Chapter3() {
//        filterExample();
//        flatMapExample();
//        flatMapExampleConcurrency();
//        mergeExample();
        zipExample();
    }

    private void filterExample() {
        Observable.just(6, 8, 9, 10)
                .filter(i -> i % 3 > 0)
                .map(i -> "#" + i * 10)
                .filter(s -> s.length() < 4)
                .subscribe(value -> Logger.log(value));
    }

    /**
     * Async parsing title from HTML body for each link.
     */
    private void flatMapExample() {
        links().flatMap(this::getBody)
                .flatMap(this::getTitle)
                .subscribe(title -> Logger.log("[%s]", title));
    }

    private Observable<String> links() {
        return Observable.just("https://www.facebook.com",
                "https://www.wikipedia.org",
                "https://www.google.com",
                "https://twitter.com",
                "https://www.amazon.com");
    }

    private Observable<String> getBody(String link) {
        HttpClient client = new HttpClient();
        return Observable.fromCallable(() -> client.sendGetRequest(link))
                .observeOn(Schedulers.io())
                .cast(String.class);
    }

    private Observable<String> getTitle(String body) {
        return Observable.just(Jsoup.parse(body).getElementsByTag("title"))
                .filter(elements -> !elements.isEmpty())
                .map(elements -> elements.get(0).text());

    }

    /**
     * Async parsing title from HTML body for each link, at one time not more that 3 requests.
     */
    private void flatMapExampleConcurrency() {
        Disposable d = links().flatMap(this::getBody, 5)
                .flatMap(this::getTitle)
                .subscribe(title -> Logger.log("[%s]", title));
        //Waiting for threads.
        while (!d.isDisposed()) {
        }
    }

    private void mergeExample() {
        //Merge with [1..4] Observables.
        Single.merge(getHash("goodvin1709"), getHash("Java"), getHash("RxJava"))
                .subscribe(hash -> Logger.log("%s", hash));
        Logger.log("=====================");
        //Merge Observable with another.
        getHash("goodvin1709").mergeWith(getHash("RxJava")).subscribe(hash -> Logger.log("%s", hash));

    }

    private Single<String> getHash(String value) {
        return Single.create(emitter -> {
            if (value != null) {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
                digest.reset();
                String result = IntStream.range(0, hash.length)
                        .mapToObj(b -> Integer.toHexString(0xFF & hash[b]))
                        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                        .toString();
                emitter.onSuccess(result);
            } else {
                emitter.onError(new InvalidFormatException("Invalid input value."));
            }
        });
    }

    /**
     * Holy f**king shit :)
     */
    private void zipExample() {
        String[] values = {"Goodvin1709", "Java", "RxJava"};

        Observable.zip(Observable.fromArray(values),
                Observable.fromArray(values).flatMap(val -> getHash(val).toObservable()),
                (s, s2) -> String.format("value[%s] - hash[%s]", s, s2))
                .subscribe(v -> Logger.log("%s", v));
    }
}