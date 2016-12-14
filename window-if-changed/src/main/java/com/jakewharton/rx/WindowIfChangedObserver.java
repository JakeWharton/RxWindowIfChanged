/*
 * Copyright (C) 2016 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jakewharton.rx;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;

final class WindowIfChangedObserver<T, K> implements Observer<T> {
  private static final Object NOT_SET = new Object();

  private final Function<? super T, ? extends K> keySelector;
  private final Observer<? super GroupedObservable<K, T>> observer;

  @SuppressWarnings("unchecked") // Safe because of erasure.
  private K key = (K) NOT_SET;
  private WindowGroupedObservable<K, T> window;

  WindowIfChangedObserver(Function<? super T, ? extends K> keySelector,
      Observer<? super GroupedObservable<K, T>> observer) {
    this.keySelector = keySelector;
    this.observer = observer;
  }

  @Override public void onSubscribe(Disposable d) {
    observer.onSubscribe(d);
  }

  @Override public void onNext(T value) {
    K nextKey;
    try {
      nextKey = keySelector.apply(value);
    } catch (Exception e) {
      onError(e);
      return;
    }

    K key = this.key;
    WindowGroupedObservable<K, T> window = this.window;

    if (key != null ? !key.equals(nextKey) : nextKey != null) {
      if (window != null) {
        window.state.onComplete();
      }
      this.key = nextKey;
      this.window = window = new WindowGroupedObservable<>(nextKey);
      observer.onNext(window);
    }
    window.state.onNext(value);
  }

  @Override public void onError(Throwable e) {
    WindowGroupedObservable<K, T> window = this.window;
    if (window != null) {
      window.state.onComplete();

      this.key = null;
      this.window = null;
    }

    observer.onError(e);
  }

  @Override public void onComplete() {
    WindowGroupedObservable<K, T> window = this.window;
    if (window != null) {
      window.state.onComplete();

      this.key = null;
      this.window = null;
    }

    observer.onComplete();
  }
}
