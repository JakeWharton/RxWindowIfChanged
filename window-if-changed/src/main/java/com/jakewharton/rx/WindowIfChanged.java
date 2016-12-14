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
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;

/**
 * Divides an {@link Observable} that emits items into an {@link Observable} that emits
 * {@link Observable Observables}, each one of which emits some subset of the items from the
 * original source. The items in each inner {@link Observable} all have a common "key" based on a
 * key selector function. A new inner {@link Observable} is emitted and the previous inner
 * {@link Observable} completes each time the key changes from the previous item.
 */
public final class WindowIfChanged {
  public static <T, K> Observable<GroupedObservable<K, T>> create(Observable<T> upstream,
      Function<? super T, ? extends K> keySelector) {
    return new WindowIfChangedObservable<>(upstream, keySelector);
  }

  private WindowIfChanged() {
    throw new AssertionError("No instances.");
  }
}
