/**
 * Copyright (c) 2017 hujian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hujian.switcher.reactive;

/**
 * Created by hujian06 on 2017/8/24.
 */

import com.hujian.switcher.reactive.functions.Cancellable;

/**
 * The onNext, onError and onComplete methods should be called
 * in a sequential manner, just like the Observer's methods.
 * Use {@link #serialize()} if you want to ensure this.
 * The other methods are thread-safe.
 *
 * @param <T> the value type to emit
 */
public interface ObservableEmitter<T> {

    /**
     * Signal a normal value.
     * @param value the value to signal, not null
     */
    void onNext(T value);

    /**
     * Signal a Throwable exception.
     * @param error the Throwable to signal, not null
     */
    void onError(Throwable error);

    /**
     * Signal a completion.
     */
    void onComplete();

    /**
     * Sets a Disposable on this emitter; any previous Disposable
     * or Cancellation will be unsubscribed/cancelled.
     * @param d the disposable, null is allowed
     */
    void setDisposable(Disposable d);

    /**
     * Sets a Cancellable on this emitter; any previous Disposable
     * or Cancellation will be unsubscribed/cancelled.
     * @param c the cancellable resource, null is allowed
     */
    void setCancellable(Cancellable c);

    /**
     * Returns true if the downstream disposed the sequence.
     * @return true if the downstream disposed the sequence
     */
    boolean isDisposed();

    /**
     * Ensures that calls to onNext, onError and onComplete are properly serialized.
     * @return the serialized ObservableEmitter
     */
    ObservableEmitter<T> serialize();

    /**
     * Attempts to emit the specified {@code Throwable} error if the downstream
     * hasn't cancelled the sequence or is otherwise terminated, returning false
     * if the emission is not allowed to happen due to lifecycle restrictions.
     * @param t the throwable error to signal if possible
     * @return true if successful, false if the downstream is not able to accept further
     * events
     */
    boolean tryOnError(Throwable t);
}
