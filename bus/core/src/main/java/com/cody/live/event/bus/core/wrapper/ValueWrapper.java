/*
 * ************************************************************
 * 文件：ValueWrapper.java  模块：core  项目：CleanFramework
 * 当前修改时间：2019年03月31日 22:54:01
 * 上次修改时间：2019年03月31日 22:54:01
 * 作者：Cody.yi   https://github.com/codyer
 *
 * Copyright (c) 2019
 * ************************************************************
 */

package com.cody.live.event.bus.core.wrapper;

import androidx.annotation.NonNull;

/**
 * Created by xu.yi. on 2019/3/31.
 * mutableLiveData 值包裹类
 */
final class ValueWrapper<T> {
    int sequence;
    @NonNull
    T value;

    ValueWrapper(@NonNull T value, int sequence) {
        this.sequence = sequence;
        this.value = value;
    }
}
