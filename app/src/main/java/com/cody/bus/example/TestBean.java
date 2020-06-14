/*
 * ************************************************************
 * 文件：TestBean.java  模块：app  项目：DoveBus
 * 当前修改时间：2020年06月14日 23:12:17
 * 上次修改时间：2020年06月14日 23:11:57
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：app
 * Copyright (c) 2020
 * ************************************************************
 */

package com.cody.bus.example;

/**
 * Created by xu.yi. on 2019/4/3.
 * DoveBus
 */
public class TestBean {
    private String name;
    private String code;

    @Override
    public String toString() {
        return "TestBean{" + "name='" + name + '\'' +
                ", code='" + code + '\'' +
                '}';
    }

    public TestBean(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
