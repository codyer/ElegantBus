/*
 * ************************************************************
 * 文件：ValueWrapper.java  模块：ElegantBus.core.main  项目：ElegantBus
 * 当前修改时间：2022年09月12日 17:58:58
 * 上次修改时间：2022年09月12日 17:47:29
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.core.main
 * Copyright (c) 2022
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
