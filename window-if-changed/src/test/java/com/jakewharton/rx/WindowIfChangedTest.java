package com.jakewharton.rx;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public final class WindowIfChangedTest {
  private final Function<Message, String> userSelector = new Function<Message, String>() {
    @Override public String apply(Message message) {
      return message.user;
    }
  };

  @Test public void splits() {
    Observable<Message> messages = Observable.just( //
        new Message("Bob", "Hello"), //
        new Message("Bob", "World"), //
        new Message("Alice", "Hey"), //
        new Message("Bob", "What's"), //
        new Message("Bob", "Up?"), //
        new Message("Eve", "Hey") //
    );
    final AtomicInteger seen = new AtomicInteger();
    WindowIfChanged.create(messages, userSelector)
        .switchMap(
            new Function<GroupedObservable<String, Message>, Observable<Notification<String>>>() {
              @Override public Observable<Notification<String>> apply(
                  GroupedObservable<String, Message> group) {
                final int count = seen.incrementAndGet();
                return group.map(new Function<Message, String>() {
                  @Override public String apply(Message message) throws Exception {
                    return count + " " + message;
                  }
                }).materialize();
              }
            })
        .test()
        .assertValues( //
            Notification.createOnNext("1 Bob Hello"), //
            Notification.createOnNext("1 Bob World"), //
            Notification.<String>createOnComplete(), //
            Notification.createOnNext("2 Alice Hey"), //
            Notification.<String>createOnComplete(), //
            Notification.createOnNext("3 Bob What's"), //
            Notification.createOnNext("3 Bob Up?"), //
            Notification.<String>createOnComplete(), //
            Notification.createOnNext("4 Eve Hey"), //
            Notification.<String>createOnComplete()); //
  }

  @Test public void completeCompletesInner() {
    Observable<Message> messages = Observable.just(new Message("Bob", "Hello"));
    final AtomicInteger seen = new AtomicInteger();
    WindowIfChanged.create(messages, userSelector)
        .switchMap(
            new Function<GroupedObservable<String, Message>, Observable<Notification<String>>>() {
              @Override public Observable<Notification<String>> apply(
                  GroupedObservable<String, Message> group) {
                final int count = seen.incrementAndGet();
                return group.map(new Function<Message, String>() {
                  @Override public String apply(Message message) throws Exception {
                    return count + " " + message;
                  }
                }).materialize();
              }
            })
        .test()
        .assertValues( //
            Notification.createOnNext("1 Bob Hello"), //
            Notification.<String>createOnComplete()) //
        .assertComplete();
  }

  @Test public void errorCompletesInner() {
    RuntimeException error = new RuntimeException("boom!");
    Observable<Message> messages = Observable.just( //
        Notification.createOnNext(new Message("Bob", "Hello")),
        Notification.createOnError(error)
    ).dematerialize();
    final AtomicInteger seen = new AtomicInteger();
    WindowIfChanged.create(messages, userSelector)
        .switchMap(
            new Function<GroupedObservable<String, Message>, Observable<Notification<String>>>() {
              @Override public Observable<Notification<String>> apply(
                  GroupedObservable<String, Message> group) {
                final int count = seen.incrementAndGet();
                return group.map(new Function<Message, String>() {
                  @Override public String apply(Message message) throws Exception {
                    return count + " " + message;
                  }
                }).materialize();
              }
            })
        .test()
        .assertValues( //
            Notification.createOnNext("1 Bob Hello"), //
            Notification.<String>createOnComplete()) //
        .assertError(error);
  }
}
