package cn.lori.demo.config;

import cn.lori.bean.populator.annotation.PopulatorCategory;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.ManagedBean;

@ManagedBean
@Qualifier("bingBangConfig")
@PopulatorCategory(codes = "BINGBANG", autoSync = true)
public class BingBangConfig {
    private String color;
    private String love2Eat;
    private String lovePlayWith;
}
