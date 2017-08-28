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

package com.hujian.switcher.reactive.flowable;

/**
 * Created by hujian06 on 2017/8/28.
 */

import com.hujian.switcher.reactive.Disposable;
import com.hujian.switcher.reactive.functions.Cancellable;

/**
 * Abstraction over a Reactive Streams that allows associating
 * a resource with it and exposes the current number of downstream
 * requested amount.
 *
 * The onNext, onError and onComplete methods should be called
 * in a sequential manner, just like the Subscriber's methods.
 * Use {@link #serialize()} if you want to ensure this.
 * The other methods are thread-safe.
 *
 * @param <T> the value type to emit
 */
public interface FlowableEmitter<T> extends Emitter<T> {

    /**
     * Sets a Disposable on this emitter; any previous Disposable
     * or Cancellation will be disposed/cancelled.
     * @param s the disposable, null is allowed
     */
    void setDisposable(Disposable s);

    /**
     * Sets a Cancellable on this emitter; any previous Disposable
     * or Cancellation will be disposed/cancelled.
     * @param c the cancellable resource, null is allowed
     */
    void setCancellable(Cancellable c);

    /**
     * The current outstanding request amount.
     * <p>This method is thread-safe.
     * @return the current outstanding request amount
     */
    long requested();

    /**
     * Returns true if the downstream cancelled the sequence.
     * This method is thread-safe.
     * @return true if the downstream cancelled the sequence
     */
    boolean isCancelled();

    /**
     * Ensures that calls to onNext, onError and onComplete are properly serialized.
     * @return the serialized FlowableEmitter
     */
    FlowableEmitter<T> serialize();

    /**
     * Attempts to emit the specified {@code Throwable} error if the downstream
     * hasn't cancelled the sequence or is otherwise terminated, returning false
     * if the emission is not allowed to happen due to lifecycle restrictions.
     *
     * Unlike {@link #onError(Throwable)}, the {@code RxJavaPlugins.onError} is not called
     * if the error could not be delivered.
     * @param t the throwable error to signal if possible
     * @return true if successful, false if the downstream is not able to accept further
     * events
     */
    boolean tryOnError(Throwable t);
}
