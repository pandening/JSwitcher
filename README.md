what is JSwitch?
====================
```
     JSwitcher is a Convenient tool to switch schedule base on RxJava, it's easy to switch to 
 Another schedule from current schedule. you just need to care about your bussiness, using 'switchTo'
 Operator to switch to the longing schedlue when you want to do the work on the suitable schedule.
 There are some especial schedules for you, like I/O Bound Schedule, Compute Bound Schedule, And Single 
 Schedule, etc, if you want to create a extra schedule by yourself, it's ok for JSwitcher, and it's 
 very easy to do this .And the most important thing is the jswitch support 'chain operator', that means
 you can switch to a schedule, then fit on this schedule some works, then you can do switch operator continue
 from current position, or you can just fit another work on current schedule, and jswitcher has terminal 
 operator like 'waitAndShutdown', after do the operator, you can not do 'chain operator' anymore. and the
 jswitcher will wait some time and shutdown all of schedule. 
  

```


how JSwitcher works?
=====================
how to peogram with JSwitcher?
=============================


why choose JSwitcher?
=====================


update note
====================


Developer
===================


License
==================

```java
Copyright 2017 HuJian

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
