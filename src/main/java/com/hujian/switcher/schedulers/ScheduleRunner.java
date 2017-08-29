package com.hujian.switcher.schedulers;

import java.util.concurrent.CompletableFuture;

/**
 * Created by hujian06 on 2017/8/29.
 */
public interface ScheduleRunner<T> {

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

}
