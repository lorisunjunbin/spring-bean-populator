package cn.lori.demo.config;

import cn.lori.bean.populator.annotation.PopulatorCategory;

import javax.annotation.ManagedBean;

@ManagedBean
@PopulatorCategory
public class DemoSampleConfig {
    private String SETTING_1;
    private String SETTING_2;
    private String SETTING_3;
}
