package net.danlew.sample;

import rx.Observable;

import java.util.concurrent.TimeUnit;

public class Sample {

    public static void main(String[] args) {
        Sources sources = new Sources();

        // Create our sequence for querying best available data
        Observable<Data> source = Observable.concat(
                sources.memory(),
                sources.disk(),
                sources.network()
            )
            .take(1);

        // "Request" latest data once a second
        Observable.interval(1, TimeUnit.SECONDS)
            .flatMap(__ -> source)
            .subscribe(data -> System.out.println("Received: " + data.value));

        // Occasionally clear memory (as if app restarted) so that we must go to disk
        Observable.interval(3, TimeUnit.SECONDS)
            .subscribe(__ -> sources.clearMemory());

        // Java will quit unless we idle
        sleep(15 * 1000);
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            // Ignore
        }
    }

}
