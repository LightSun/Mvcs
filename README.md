# Mvcs
这是一个Mvcs的框架。主要是管理多状态 和 辅助我们开发项目的功能.

<img src="/imgs/mvcs_android_base.gif" alt="Demo Screen Capture" width="287px" height="518px" />

## Mvcs框架的来源
我们都知道mvc, mvp模式（细节请百度）。m指的是模型 module. v 指 视图view ， c指控制器controller. p 指presenter.
这里的Mvcs中 mvc与其一致。 s代表状态state. 所以我才命名为mvcs.  另外设计模式中有个状态模式。实际上我这个框架就是由
mvc模式 和 状态模式衍生而来(他跟普通的状态机有很大不同).

## 概念理解
- [click here](https://github.com/LightSun/Mvcs/wiki/%E7%9B%B8%E5%85%B3%E6%A6%82%E5%BF%B5%E7%9A%84%E7%90%86%E8%A7%A3)

## 核心特征 (java SE平台就支持)
- ###  支持多状态共存和互斥。
- ###  支持状态对象的缓存，提高内存使用
- ###  支持状态栈以便返回。
- ###  支持多状态之间共享参数
- ###  支持对状态的各个操作。 eg: 'add'/'get'/'clear'/'set'/'remove'
- ###  支持状态更新和销毁
- ###  支持锁定/解锁事件。以方便我们处理app中的事件互斥。
- ###  新增(v1.1.6): 消息的处理。包括发送，移除，更新，回执等消息处理。
- ###  <h3>1.2.0 新增: 关于状态的团队管理和团队回调处理。 >>> 这个对游戏非常有用哟</h3> 
   - #### 1, 可以对团队或者成员进行 ‘增删改查’.
   - #### 2, 团队的更新update. (不同于controller.的更新).
   - #### 3, 分发团队消息.
   - #### 4, 团队成员分2类：正式 (formal)和外围(outer)。 只有正式的成员可以通知其他人(callback)。外围成员只能接收callback.

## android 平台扩展特性
- ### 支持 以 ViewHelper来链接所有的view. 还有toast.
- ### 支持状态的保存. see: android controller.onSaveInstanceState(Bundle outState).
- ### 将会对 动画和adapter提供支持。 Will
- ### thinking other.

## 限制
- 因为多状态实现原理是通过2进制计算来的。且所有的状态必须为2的n次方，多状态则为任意多个状态(flag)之和. 而flag以int来表示的， 所以以单个状态来说。
  最多支持31种状态(2^31是int类型下2^n的的最大值)。

## Gradle Usage
in jcenter.
```java
  //java 版本
  compile 'com.heaven7.java.mvcs:Mvcs-java:<Released-version>'
  //android版本
  compile 'com.heaven7.android.mvcs:mvcs-android:<Released-android-version>''

```

## Sample
 - [Demos](https://github.com/LightSun/Mvcs/wiki/Samples)

## API指南
 - [所有Api](https://github.com/LightSun/Mvcs/wiki/Api%E6%8C%87%E5%8D%97).

## Help us
   * if you have any question or good suggestion about this, please tell me... Thanks!
   
## Contribution
 * you can pull request, but your code must be a google style, eg:  google style.
   
## About Us
   * heaven7 
   * email: donshine723@gmail.com or 978136772@qq.com   

## License

    Copyright 2017  
                    heaven7(donshine723@gmail.com)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


