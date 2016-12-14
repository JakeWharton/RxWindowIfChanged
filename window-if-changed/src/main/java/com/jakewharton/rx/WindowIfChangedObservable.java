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

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;

final class WindowIfChangedObservable<T, K> extends Observable<GroupedObservable<K, T>> {
  private final Observable<T> upstream;
  private final Function<? super T, ? extends K> keySelector;

  WindowIfChangedObservable(Observable<T> upstream, Function<? super T, ? extends K> keySelector) {
    this.upstream = upstream;
    this.keySelector = keySelector;
  }

  @Override protected void subscribeActual(Observer<? super GroupedObservable<K, T>> observer) {
    upstream.subscribe(new WindowIfChangedObserver<>(keySelector, observer));
  }
}
