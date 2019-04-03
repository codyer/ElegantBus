package com.cody.live.event.bus.app;

/**
 * Created by xu.yi. on 2019/4/3.
 * LiveEventBus
 */
public class TestBean {
    private String name;
    private String code;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TestBean{");
        sb.append("name='").append(name).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append('}');
        return sb.toString();
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
