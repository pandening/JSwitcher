package com.hujian.switcher;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by hujian06 on 2017/8/20.
 */
public class SwitcherResultfulEntry<T> {

    private CompletableFuture<T> completableFutureData;
    private T rawData;
    private Boolean isCompletableFutureResult;

    public static SwitcherResultfulEntry emptyEntry() {
        return new SwitcherResultfulEntry();
    }

    private SwitcherResultfulEntry() {

    }

    public T getResultfulData() throws ExecutionException, InterruptedException {
        T data;
        if (isCompletableFutureResult) {
            data = completableFutureData.get();
        } else {
            data = rawData;
        }
        return data;
    }

    public T getResultfulData(T defaultValue) {
        T data;
        if (isCompletableFutureResult) {
            data = completableFutureData.getNow(defaultValue);
        } else {
            data = rawData;
        }
        return data;
    }

    public T getResultfulData(long timeout, TimeUnit timeUnit)
            throws InterruptedException, ExecutionException, TimeoutException {
        T data;
        if (isCompletableFutureResult) {
            data = completableFutureData.get(timeout,timeUnit);
        } else {
            data = rawData;
        }
        return data;
    }

    public SwitcherResultfulEntry(CompletableFuture<T> completableFutureData) {
        this.completableFutureData = completableFutureData;
        this.isCompletableFutureResult = true;
    }

    public SwitcherResultfulEntry(T rawData) {
        this.rawData = rawData;
        this.isCompletableFutureResult = false;
    }

    public CompletableFuture<T> getCompletableFutureData() {
        return completableFutureData;
    }

    public void setCompletableFutureData(CompletableFuture<T> completableFutureData) {
        this.completableFutureData = completableFutureData;
        this.isCompletableFutureResult = true;
    }

    public Boolean getCompletableFutureResult() {
        return isCompletableFutureResult;
    }

    public T getRawData() {
        return rawData;
    }

    public void setRawData(T rawData) {
        this.rawData = rawData;
        this.isCompletableFutureResult = false;
    }
}
