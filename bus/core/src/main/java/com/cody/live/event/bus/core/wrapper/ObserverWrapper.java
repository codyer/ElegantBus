/*
 * ************************************************************
 * 文件：ObserverWrapper.java  模块：core  项目：CleanFramework
 * 当前修改时间：2019年03月31日 22:53:16
 * 上次修改时间：2019年03月31日 22:53:16
 * 作者：Cody.yi   https://github.com/codyer
 *
 * Copyright (c) 2019
 * ************************************************************
 */

package com.cody.live.event.bus.core.wrapper;

import androidx.lifecycle.Observer;
import androidx.annotation.Nullable;

/**
 * Created by xu.yi. on 2019/3/31.
 * 不要主动改变属性值
 */
public abstract class ObserverWrapper<T> {
    int sequence;
    Observer<ValueWrapper<T>> observer;

    public abstract void onChanged(@Nullable T t);
}
