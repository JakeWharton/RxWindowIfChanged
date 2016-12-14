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
import io.reactivex.observables.GroupedObservable;
import io.reactivex.subjects.UnicastSubject;

final class WindowGroupedObservable<K, T> extends GroupedObservable<K, T> {
  final UnicastSubject<T> state = UnicastSubject.create();

  WindowGroupedObservable(K key) {
    super(key);
  }

  @Override protected void subscribeActual(Observer<? super T> observer) {
    state.subscribe(observer);
  }
}
