package com.hujian.switcher.schedulers;

import com.google.common.base.Preconditions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by hujian06 on 2017/8/29.
 */
public abstract class AbstractScheduleRunner<T> implements ScheduleRunner<T>{

    private Executor _executor; // the executor

    @Override
    public CompletableFuture<T> queue() {
        Preconditions.checkArgument(_executor != null,
                "Executor is null");
        try {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return realRun();
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, _executor);
        } catch (RejectedExecutionException e) {
            try {
                return CompletableFuture.completedFuture(fallback(e));
            } catch (Exception ep) {
                CompletableFuture<T> result = new CompletableFuture<>();
                result.completeExceptionally(ep);
                return result;
            }
        }
    }

    @Override
    public T execute() throws Exception {
        try {
            return queue().get();
        } catch (Exception e) {
            //todo : more elaborate solution with exception 'e'
            return fallback(e);
        }
    }

    //**********************************//
    //       actual work here           //
    //**********************************//

    protected abstract T realRun();
    protected abstract T fallback(Exception e);

    /**
     * the method you can assign the executor.
     * @param _executor the executor.
     */
    public void set_executorService(Executor _executor) {
        this._executor = _executor;
    }
}
