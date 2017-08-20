package com.hujian.switcher;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Created by hujian06 on 2017/8/20.
 */
public interface SwitcherRunner<T> {

    /**
     * async call
     * @return
     */
    CompletableFuture<T> queue();

    /**
     * sync call
     * @return
     */
    T execute() throws Exception;

    /**
     * you can set the executor.or the switcher will use the current switcher to run the job.
     * also,you can not call this function,the switcher will call this function auto-ly.
     * @param executorService
     */
    void setExecutorService(ExecutorService executorService);

}
