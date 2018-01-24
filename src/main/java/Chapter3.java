import io.reactivex.Observable;
import org.jsoup.Jsoup;

class Chapter3 {

    Chapter3() {
//        filterExample();
//        flatMapExample();
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
                .cast(String.class);
    }

    private Observable<String> getTitle(String body) {
        return Observable.just(Jsoup.parse(body).getElementsByTag("title"))
                .filter(elements -> !elements.isEmpty())
                .map(elements -> elements.get(0).text());

    }

}