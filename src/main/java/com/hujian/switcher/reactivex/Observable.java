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

/**
 * Created by hujian06 on 2017/8/24.
 */

/**
 * The Observable class is the non-backpressured, optionally multi-valued base reactive class that
 * offers factory methods, intermediate operators and the ability to consume synchronous
 * and/or asynchronous reactive dataflows.
 * @param <T>
 *            the type of the items emitted by the Observable
 */
public abstract class Observable<T> implements ObservableSource<T> {

    @Override
    public void subscribe(Observer<? super T> observer) {
        Exception exception = null;
        try {
            subscribeActual(observer);
        } catch (Exception e) {
            exception = e;
        } finally {
            if (exception != null) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Operator implementations (both source and intermediate) should implement this method that
     * performs the necessary business logic.
     * <p>There is no need to call any of the plugin hooks on the current Observable instance or
     * the Subscriber.
     * @param observer the incoming Observer, never null
     */
    protected abstract void subscribeActual(Observer<? super T> observer);

    /**
     * create observable
     * @param source the source
     * @param <T> type
     * @return the observable
     */
    public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
         return new ObservableCreate<T>(source);
    }
}