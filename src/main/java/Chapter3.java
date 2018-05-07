import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;
import org.jsoup.Jsoup;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

class Chapter3 {

    Chapter3() {
//        filterExample();
//        flatMapExample();
//        flatMapExampleConcurrency();
//        mergeExample();
//        zipExample();
//        scanExample();
//        reduceExample();
//        creatingListWithReduce();
//        distinctExample();
//        distinctUntilChangedExample();
//        takeExample();
//        skipExample();
//        firstLastExample();
//        takeUntilExample();
//        takeWhileExample();
//        elementAtExample();
        merge2Example();
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
                emitter.onError(new Exception("Invalid input value."));
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

    /**
     * Accumulate previous value and current and emmit all intermediate values
     */
    private void scanExample() {
        List<Integer> blocks = Arrays.asList(5, 5, 10, 10, 20, 50);
        Observable.fromIterable(blocks)
                .scan((accumulator, value) -> accumulator + value)
                .blockingSubscribe(v -> Logger.log("%d", v));
    }

    /**
     * Accumulate previous value and current and emmit only result
     * But not working with infinity sources.
     */
    private void reduceExample() {
        List<Integer> blocks = Arrays.asList(5, 5, 10, 10, 20, 50);
        Observable.fromIterable(blocks)
                .reduce((accumulator, value) -> accumulator + value)
                .toObservable()
                .blockingSubscribe(total -> Logger.log("%d", total));
    }

    /**
     * Creating list from emmited object, for accumulator used ArrayList
     * and Bifunction for each operation, required to return accumulator each time.
     */
    private void creatingListWithReduce() {
        Single<List<Integer>> totalList = Observable.range(10, 10)
                .reduce(new ArrayList<Integer>(), (list, current) -> {
                    list.add(current);
                    return list;
                });
        totalList.subscribe(list -> list.forEach(item -> Logger.log("%d", item)));
    }

    /**
     * Simplify creating total collected list using collect operator, where accumulator the same
     * for each action.
     */
    private void creatingListWithCollect() {
        Single<List<Integer>> totalList = Observable.range(10, 10)
                .collect(ArrayList::new, List::add);
        totalList.subscribe(list -> list.forEach(item -> Logger.log("%d", item)));
    }

    /**
     * Select only unique elements from all source.
     */
    private void distinctExample() {
        List<String> values = Arrays.asList("A", "B", "C", "D", "A", "C", "B", "B", "D");
        Observable.fromIterable(values)
                .distinct()
                .collect(ArrayList::new, List::add)
                .subscribe(list -> list.forEach(item -> Logger.log("%s", item)));

    }

    /**
     * Select emmit value when previous value not equals with current.
     */
    private void distinctUntilChangedExample() {
        List<String> values = Arrays.asList("A", "A", "A", "B", "C", "D", "A", "C", "B", "B", "D");
        Observable.fromIterable(values)
                .distinctUntilChanged()
                .collect(ArrayList::new, List::add)
                .subscribe(list -> list.forEach(item -> Logger.log("%s", item)));

    }

    /**
     * Emmit first n elements.
     * when n is below 0 then will be throw java.lang.IllegalArgumentException
     */
    private void takeExample() {
        Observable.range(1, 10).take(5)
                .collect(StringBuilder::new, (builder, value) -> builder.append(value).append(" "))
                .subscribe(list -> Logger.log("%s", list));
    }

    /**
     * Skip first n elements.
     * when n is below 0 then skip will be ignored.
     */
    private void skipExample() {
        Observable.range(1, 10).skip(-99)
                .collect(StringBuilder::new, (builder, value) -> builder.append(value).append(" "))
                .subscribe(list -> Logger.log("%s", list));
    }

    private Observable<Integer> source() {
        return Observable.range(1 << 6, 1 << 9);
    }

    private Single<String> joiner(Observable<Integer> source) {
        return source.collect(StringBuilder::new, (builder, value) -> builder.append(value).append(" "))
                .map(StringBuilder::toString);
    }

    private void firstLastExample() {
        source().first(-1)
                .map(f -> "First " + f)
                .mergeWith(source().last(-1)
                        .map(v -> "Last " + v))
                .subscribe(v -> Logger.log("%s", v));

    }

    private void takeUntilExample() {
        source().takeUntil(v -> v == 128)
                .subscribe(v -> Logger.log("%d", v));
    }

    private void takeWhileExample() {
        source().takeWhile(v -> v < 80)
                .subscribe(v -> Logger.log("%d", v));
    }

    private void elementAtExample() {
        List<Integer> ids = Arrays.asList(1, 3, 57, 62, 33, 24);
        Observable.fromIterable(ids)
                .elementAt(2)
                .subscribe(id -> Logger.log("%d", id));
    }

    private void merge2Example() {
        String quote1 = "Anything less than immortality is a complete waste of time.";
        String quote2 = "Listen to many, speak to a few.";
        String quote3 = "My words fly up, my thoughts remain below: Words without thoughts never to heaven go.";

//        Observable.merge(speak(quote1, 70), speak(quote2, 80), speak(quote3, 100))
//                .blockingSubscribe(word -> Logger.log("%s",word));

        Observable.concat(speak(quote1, 70), speak(quote2, 80), speak(quote3, 100))
                .blockingSubscribe(word -> Logger.log("%s", word));

    }

    private Observable<String> speak(String quote, long msPerChar) {
        String[] tokens = quote.replaceAll("[:,]", "").split(" ");
        Observable<String> words = Observable.fromArray(tokens);
        Observable<Long> wordDelay = words
                .map(String::length)
                .map(length -> length * msPerChar)
                .scan((total, current) -> total + current);

//        return words.zipWith(wordDelay.startWith(0L), Pair::of)
//                .flatMap(this::just);

        return words.zipWith(wordDelay.startWith(0L), this::just)
                .flatMap(o -> o);
    }

//    private Observable<String> just(Pair pair) {
//        return Observable.just((String) pair.getLeft())
//                .delay((long) pair.getRight(), TimeUnit.MILLISECONDS);
//    }

    private Observable<String> just(String word, Long time) {
        return Observable.just(word).delay(time, TimeUnit.MILLISECONDS);
    }
}