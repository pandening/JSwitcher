package com.hujian.switcher;

/**
 * Created by hujian06 on 2017/8/19.
 */
public final class SwitcherFactory {

    private static Switcher switcher;
    private static RichnessSwitcherIface richnessSwitcherIface;
    private static ResultfulSwitcherIfac resultfulSwitcherIfac;

    public static Switcher createShareSwitcher() {
        if(switcher == null) {
            synchronized (SwitcherFactory.class) {
                if (switcher == null) {
                    switcher = new SampleSwitcher();
                }
            }
        }
        return switcher;
    }

    public static RichnessSwitcherIface createShareRichnessSwitcher() {
        if(richnessSwitcherIface == null) {
            synchronized (SwitcherFactory.class) {
                if (richnessSwitcherIface == null) {
                    richnessSwitcherIface = new RichnessSwitcher();
                }
            }
        }
        return richnessSwitcherIface;
    }

    public static ResultfulSwitcherIfac createResultfulSwitcher() {
        if(resultfulSwitcherIfac == null) {
            synchronized (SwitcherFactory.class) {
                if (resultfulSwitcherIfac == null) {
                    resultfulSwitcherIfac = new ResultfulSwitcher();
                }
            }
        }
        return resultfulSwitcherIfac;
    }

    public static void shutdownSwitcher() throws InterruptedException {
        if (null != switcher) {
            switcher.clear();
        }
    }

    public static void shutdownRichnessSwitcher() throws InterruptedException {
        if (null != richnessSwitcherIface) {
            richnessSwitcherIface.clear();
        }
    }

    public static void shutdownResultfulSwitcher() throws InterruptedException {
        if (null != resultfulSwitcherIfac) {
            resultfulSwitcherIfac.clear();
        }
    }

    public static void shutdown() throws InterruptedException {
        shutdownSwitcher();;
        shutdownRichnessSwitcher();
        shutdownResultfulSwitcher();
    }

}
