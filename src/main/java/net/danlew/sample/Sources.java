package net.danlew.sample;

import com.google.common.base.Optional;
import rx.Observable;

/**
 * Simulates three different sources - one from memory, one from disk,
 * and one from network. In reality, they're all in-memory, but let's
 * play pretend.
 *
 * Observable.create() is used so that we always return the latest data
 * to the subscriber; if you use just() it will only return the data from
 * a certain point in time.
 */
public class Sources {

    // Memory cache of data
    private Data memory = null;

    // What's currently "written" on disk
    private Data disk = null;

    // Each "network" response is different
    private int requestNumber = 0;

    // In order to simulate memory being cleared, but data still on disk
    public void clearMemory() {
        System.out.println("Wiping memory...");
        memory = null;
    }

    public Observable<Optional<Data>> memory() {
        Observable<Optional<Data>> observable = Observable.create(subscriber -> {
            subscriber.onNext(Optional.fromNullable(memory));
            subscriber.onCompleted();
        });

        return observable.compose(logSource("MEMORY"));
    }

    public Observable<Optional<Data>> disk() {
        Observable<Optional<Data>> observable = Observable.create(subscriber -> {
            subscriber.onNext(Optional.fromNullable(disk));
            subscriber.onCompleted();
        });

        // Cache disk responses in memory
        return observable.doOnNext(data -> memory = data.orNull())
            .compose(logSource("DISK"));
    }

    public Observable<Optional<Data>> network() {
        Observable<Optional<Data>> observable = Observable.create(subscriber -> {
            requestNumber++;
            subscriber.onNext(Optional.of(new Data("Server Response #" + requestNumber)));
            subscriber.onCompleted();
        });

        // Save network responses to disk and cache in memory
        return observable.doOnNext(data -> {
                disk = data.get();
                memory = data.get();
            })
            .compose(logSource("NETWORK"));
    }

    // Simple logging to let us know what each source is returning
    Observable.Transformer<Optional<Data>, Optional<Data>> logSource(final String source) {
        return dataObservable -> dataObservable.doOnNext(data -> {
            if (!data.isPresent()) {
                System.out.println(source + " does not have any data.");
            }
            else if (!data.get().isUpToDate()) {
                System.out.println(source + " has stale data.");
            }
            else {
                System.out.println(source + " has the data you are looking for!");
            }
        });
    }

}
