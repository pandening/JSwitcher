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

package com.hujian.switcher.annotation;

import org.reflections.Reflections;

/**
 * Created by hujian06 on 2017/8/21.
 */
public class SwitchThenRunResolver extends RunThenSwitchResolver{

    public SwitchThenRunResolver() {
        super();
        setRunThenSwitchMode(false);
    }

    public SwitchThenRunResolver(String scanPath) {
        super(scanPath);
        setRunThenSwitchMode(false);
    }

    public SwitchThenRunResolver(String scanPath, Reflections reflections) {
        super(scanPath, reflections);
        setRunThenSwitchMode(false);
    }

}
