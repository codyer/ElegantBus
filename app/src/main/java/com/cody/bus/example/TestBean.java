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
