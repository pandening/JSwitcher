package com.hujian.switcher;

/**
 * Created by hujian06 on 2017/8/19.
 */
public interface RichnessSwitcherIface extends Switcher {

    /**
     * assign {@code executorName} for the current executorService
     * @param executorName
     * @return
     */
    RichnessSwitcherIface assignName(String executorName);

    /**
     * we can get some data from this.
     * @return {just for test}
     */
    SwitcherWithExtraData getSwitcherWithExtraData() throws InterruptedException;


    /**
     * switch to the executorService named "{@code executorName}"
     * @param executorName
     * @param isCreateMode
     * @param createExecutorType
     * @param magicOperator assign the "find" executor
     * @return
     */
    RichnessSwitcherIface switchTo(String executorName, Boolean magicOperator,
                                          Boolean isCreateMode, String createExecutorType) throws InterruptedException;

    /**
     * run the job on the executor named {@code executorName}
     * @param job
     * @param executorName
     * @param isCreateMode
     * @param createExecutorType
     * @return
     */
    RichnessSwitcherIface apply(Runnable job, String executorName, Boolean isCreateMode, String createExecutorType) throws InterruptedException, SwitchRunntimeException;

    /**
     * trans to resultful switcher
     * @return
     */
    ResultfulSwitcher transToResultfulSwitcher();

     class SwitcherWithExtraData<T> {
        private T data;
        private RichnessSwitcherIface richnessSwitcherIface;

        public SwitcherWithExtraData() {

        }

        public SwitcherWithExtraData(T data, RichnessSwitcherIface richnessSwitcherIface) {
            this.data = data;
            this.richnessSwitcherIface = richnessSwitcherIface;
        }

        public T getData() throws InterruptedException {
            richnessSwitcherIface.clear();
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public RichnessSwitcherIface getRichnessSwitcherIface() {
            return richnessSwitcherIface;
        }

        public void setRichnessSwitcherIface(RichnessSwitcherIface richnessSwitcherIface) {
            this.richnessSwitcherIface = richnessSwitcherIface;
        }
    }

}
