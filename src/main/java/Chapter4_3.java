import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

class Chapter4_3 {

    Chapter4_3() {
//        publishSubjectExample();
        bufferingPublishSubjectExample();
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
}
