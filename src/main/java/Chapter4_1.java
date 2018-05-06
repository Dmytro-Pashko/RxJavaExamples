import io.reactivex.Single;

import java.util.ArrayList;
import java.util.List;

class Chapter4_1 {

    Chapter4_1() {
        onErrorResume();
    }

    /**
     * The same like try -> catch but in reactive world.
     */
    private void onErrorResume() {
        Person person = new Person();
//        person.addHobbies("Programming");
        recommended(person.hobbies)
                .onErrorResumeNext(bestSeller())
                .subscribe(book -> {
                    Logger.log("%s", book.getName());
                });
    }

    private Single<Book> recommended(List<String> hobbies) {
        return Single.create(emitter -> {
            try {
                if (hobbies.contains("Programming")) {
                    emitter.onSuccess(new Book("Reactive Programming with RxJava."));
                }
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    private Single<Book> bestSeller() {
        Book bestSeller = new Book("TWISTED PREY by John Sandford.");
        return Single.just(bestSeller);
    }

    private class Person {

        List<String> hobbies;

        synchronized void addHobbies(String hobby) {
            if (hobbies == null) {
                hobbies = new ArrayList<>();
            }
            hobbies.add(hobby);
        }
    }

    private class Book {
        final String name;

        Book(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }
    }
}
