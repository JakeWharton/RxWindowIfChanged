package com.jakewharton.rx;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.*;

import io.reactivex.*;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

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

  @Test public void take1Outer() {
      WindowIfChanged.create(Observable.just(1, 1, 1, 1, 1, 1), Functions.<Integer>identity())
      .take(1)
      .flatMap(Functions.<Observable<Integer>>identity())
      .test()
      .assertResult(1, 1, 1, 1, 1, 1);
  }

  @Test public void take1OuterKeyChanged() {
      WindowIfChanged.create(Observable.just(1, 1, 1, 1, 1, 1, 2, 2), Functions.<Integer>identity())
      .take(1)
      .flatMap(Functions.<Observable<Integer>>identity())
      .test()
      .assertResult(1, 1, 1, 1, 1, 1);
  }

  @Test public void take1WindowTake1() {
      PublishSubject<Integer> ps = PublishSubject.create();

      TestObserver<Integer> ts = WindowIfChanged.create(ps, Functions.<Integer>identity())
              .take(1)
              .flatMap(new Function<GroupedObservable<Integer, Integer>, ObservableSource<Integer>>() {
                @Override
                public ObservableSource<Integer> apply(GroupedObservable<Integer, Integer> w)
                        throws Exception {
                    return w.take(1);
                }
            })
            .test();

      Assert.assertTrue(ps.hasObservers());

      ps.onNext(1);

      Assert.assertFalse(ps.hasObservers());

      ts.assertResult(1);
  }
}
