package com.hujian.switcher;

/**
 * Created by hujian06 on 2017/8/20.
 */
public interface ResultfulSwitcherIfac<T> extends RichnessSwitcherIface {

    /**
     * you can assign the runner and get the result in sync mode now.
     * @param runner
     * @param resultfulEntry
     * @return
     */
    ResultfulSwitcherIfac syncApply(SwitcherRunner<T> runner, SwitcherResultfulEntry resultfulEntry) throws InterruptedException;

    /**
     * you can assign the runner and get the result in async mode now.
     * @param runner
     * @param resultfulEntry
     * @return
     */
    ResultfulSwitcherIfac asyncApply(SwitcherRunner<T> runner, SwitcherResultfulEntry resultfulEntry) throws InterruptedException;

}
