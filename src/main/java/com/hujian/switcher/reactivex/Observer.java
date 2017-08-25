/**
 * Copyright 2017 hujian
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

package com.hujian.switcher.reactivex;


public interface Observer<T> {

    /**
     * Provides the Observer with the means of cancelling (disposing) the
     * connection (channel) with the Observable in both
     * synchronous (from within {@link #onNext(Object)}) and asynchronous manner.
     * @param d the Disposable instance whose {@link Disposable#dispose()} can
     * be called anytime to cancel the connection
     */
    void onSubscribe(Disposable d);

    /**
     * Provides the Observer with a new item to observe.
     * The {@code Observable} will not call this method again after it calls either
     * @param t the item emitted by the Observable
     */
    void onNext(T t);

    /**
     * Notifies the Observer that the Observable has experienced an error condition.
     * @param e the exception encountered by the Observable
     */
    void onError(Throwable e);

    /**
     * Notifies the Observer that the Observable has finished sending push-based notifications.
     * The Observable will not call this method if it calls
     */
    void onComplete();

}
