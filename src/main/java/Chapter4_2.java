import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

class Chapter4_2 {

    Chapter4_2() {
//        timeoutExample();
        exampleBooking();
    }

    private void timeoutExample() {
        HttpClient client = new HttpClient();
        Single.create(emitter -> {
            emitter.onSuccess(client.sendGetRequest("https://github.com"));
        }).timeout(100, TimeUnit.MILLISECONDS)
                .subscribe(body -> {
                    Logger.log("%s", body);
                }, error -> {
                    Logger.log("%s", error);
                });
    }


    private void exampleBooking() {
        Single<Flight> flight = rxLookupFlight("LOT 783")
                .doOnSuccess(f -> Logger.log("Flight found."))
                .subscribeOn(Schedulers.io());
        Single<Passenger> passenger = rxLookupPassenger("Dmitry Pashko")
                .doOnSuccess(p -> Logger.log("Passenger found."))
                .subscribeOn(Schedulers.io());

        flight.zipWith(passenger, this::rxBookTicket)
                //Instead Pair
                .flatMap(ob -> ob)
                .toObservable()
                .blockingSubscribe(ticket -> Logger.log("Ticket booked :%s", ticket.toString()));
    }

    private Single<Flight> rxLookupFlight(String number) {
        return Single.just(new Flight(number)).delay(1280, TimeUnit.MILLISECONDS);
    }

    private Single<Passenger> rxLookupPassenger(String name) {
        return Single.just(new Passenger(name)).delay(430, TimeUnit.MILLISECONDS);
    }

    private Single<Ticket> rxBookTicket(Flight flight, Passenger passenger) {
        return Single.just(new Ticket(flight, passenger)).delay(765, TimeUnit.MILLISECONDS);
    }

    private class Ticket {

        private final Flight flight;
        private final Passenger passenger;

        Ticket(Flight flight, Passenger passenger) {

            this.flight = flight;
            this.passenger = passenger;
        }

        @Override
        public String toString() {
            return "Ticket " + flight.toString() + " : " + passenger.toString();
        }
    }

    private class Flight {

        private final String number;

        Flight(String number) {
            this.number = number;
        }

        @Override
        public String toString() {
            return "Flight № " + number;
        }
    }

    private class Passenger {

        private final String name;

        Passenger(String name) {

            this.name = name;
        }

        @Override
        public String toString() {
            return "Passenger [" + name + "]";
        }
    }
}
