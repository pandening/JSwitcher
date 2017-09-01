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

package com.hujian.switcher;

import com.google.common.base.Preconditions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by hujian06 on 2017/8/30.
 *
 * The Result Future of switch #apply#
 */
@SuppressWarnings(value = "unchecked")
public class SwitcherResultFuture<T> extends AtomicBoolean{

    /**
     * the future, you should assign the future after submit runner
     * in executor
     */
    private Future<?> future;

    /**
     * the real data, for syncMode result.
     */
    private T data;

    /**
     * the default-constructor here
     */
    public SwitcherResultFuture() {
        set(false); // the initial value
    }

    /**
     * initial the future.
     * @param future
     *          the future from executor
     */
    @SuppressWarnings(value = "unchecked")
    public SwitcherResultFuture(Future future) throws ExecutionException, InterruptedException {
        Preconditions.checkArgument(future != null, "the future is null");
        this.future = future;
        for (;;) {
            Boolean curStatus = get();
            if (!curStatus) {
                break;
            }
            if (compareAndSet(true, false)) {
                break;
            }
        }
    }

    /**
     * initial the future
     * @param data
     *          the data for sync mode result
     */
    public SwitcherResultFuture(T data) {
        Preconditions.checkArgument(data != null, "the data is null");
        this.data = data;
        for (;;) {
            Boolean curStatus = get();
            if (curStatus) {
                break;
            }
            if (compareAndSet(false, true)) {
                break;
            }
        }
    }

    /**
     * sample utils to get real data
     *
     * get() == true  => get the real data from
     * {@link SwitcherResultFuture#data#}
     * get() == false => get the real data from
     * {@link SwitcherResultFuture#future#}
     *
     * @return the real data
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public T fetchResult() throws ExecutionException, InterruptedException {
        if (get()) {
            return getData();
        } else {
            return fetch();
        }
    }

    /**
     * get the data
     * @return the real result data
     * @throws ExecutionException throw exception #ExecutionException#
     * @throws InterruptedException throw exception #InterruptedException#
     */
    public T fetch() throws ExecutionException, InterruptedException {
        Preconditions.checkArgument(future != null, "the future is null");
        T data = null;
        try {
            data = (T) future.get();
        } catch (Exception e) {
            ScheduleHooks.onError(e);
        }
        return data;
    }

    /**
     * get data from future
     * @param time the wait time
     * @param unit time unit
     * @return the real result data
     * @throws InterruptedException throw #InterruptedException#
     * @throws ExecutionException throw #ExecutionException#
     * @throws TimeoutException throw #TimeoutException#
     */
    public T fetch(long time, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        Preconditions.checkArgument(future != null, "the future is null");
        return (T) future.get(time, unit);
    }

    /**
     * get the result future
     * @return future {Result}
     */
    public Future<?> getFuture() {
        return future;
    }

    /**
     * you can set the future again
     * @param future the assign-pre future
     */
    public void setFuture(Future<?> future) {
        this.future = future;
        for (;;) {
            Boolean curStatus = get();
            if (!curStatus) {
                break;
            }
            if (compareAndSet(true, false)) {
                break;
            }
        }
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
        for (;;) {
            Boolean curStatus = get();
            if (curStatus) {
                break;
            }
            if (compareAndSet(false, true)) {
                break;
            }
        }
    }
}
