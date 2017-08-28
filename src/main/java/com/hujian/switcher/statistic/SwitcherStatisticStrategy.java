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

package com.hujian.switcher.statistic;


/**
 * Created by hujian06 on 2017/8/22.
 */
public enum SwitcherStatisticStrategy {
    STATISTIC_LAST_TIME_EXCEPTION(1, ""),
    STATISTIC_FIRST_TIME_EXCEPTION(2, ""),
    STATISTIC_FULL_TIME_EXCEPTION(3,""),
    STATISTIC_LAST_TIME_PARAMS(1, ""),
    STATISTIC_FIRST_TIME_PARAMS(2, ""),
    STATISTIC_FULL_TIME_PARAMS(3,"");

    private int index;
    private String name;

    SwitcherStatisticStrategy(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public static String getName(int index) {
        for (SwitcherStatisticStrategy c : SwitcherStatisticStrategy.values()) {
            if (c.getIndex() == index) {
                return c.name;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
