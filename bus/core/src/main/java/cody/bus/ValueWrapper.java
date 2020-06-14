/*
 * ************************************************************
 * 文件：ValueWrapper.java  模块：core  项目：DoveBus
 * 当前修改时间：2020年06月14日 23:12:17
 * 上次修改时间：2020年06月14日 23:11:57
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：core
 * Copyright (c) 2020
 * ************************************************************
 */

package cody.bus;

import androidx.annotation.NonNull;

/**
 * Created by xu.yi. on 2019/3/31.
 * mutableLiveData 值包裹类
 */
final class ValueWrapper<T> {
    // 每个被观察的事件数据都有一个序号，只有产生的事件数据在观察者加入之后才通知到观察者
    // 即事件数据序号要大于观察者序号
    final int sequence;
    @NonNull
    final
    T value;

    ValueWrapper(@NonNull T value, int sequence) {
        this.sequence = sequence;
        this.value = value;
    }
}
