package cn.lori.demo.service;

import cn.lori.bean.populator.model.PopulatorResource;
import cn.lori.bean.populator.service.impl.BasePopulateService;
import cn.lori.demo.model.DemoResource;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j2
@Component
public class DemoPopulateService extends BasePopulateService {

    @Override
    public Integer syncIntervalInMinutes() {
        return 1;
    }

    @Override
    public List<PopulatorResource> loadItems(String categoryCode) {
        log.info("successfully load resources for " + categoryCode);
        return switch (categoryCode) {
            case "WHEN" -> getDummyWhen(categoryCode);
            case "WHERE" -> getDummyWhere(categoryCode);
            case "WHY" -> getDummyWhy(categoryCode);
            case "BINGBANG" -> getDummyBingBang(categoryCode);
            case "DemoSampleConfig" -> getDemoSampleConfig(categoryCode);
            default -> Lists.newArrayList();
        };
    }

    private List<PopulatorResource> getDemoSampleConfig(String categoryCode) {
        return Lists.newArrayList(
                new DemoResource("SETTING_1", "SETTING_1_VALUE", "SETTING_1_DESC"),
                new DemoResource("SETTING_2", "SETTING_2_VALUE", "SETTING_2_DESC"),
                new DemoResource("SETTING_3", "SETTING_3_VALUE", "SETTING_3_DESC" + System.currentTimeMillis())
        );
    }

    private List<PopulatorResource> getDummyBingBang(String categoryCode) {
        return Lists.newArrayList(
                new DemoResource("love2Eat", "小鱼干", "xiaoyugan"),
                new DemoResource("lovePlayWith", "jindou", "金豆儿"),
                new DemoResource("color", "gray", "灰色@" + System.currentTimeMillis())
        );
    }

    private List<PopulatorResource> getDummyWhy(String cc) {
        return Lists.newArrayList(
                new DemoResource("WHY_2_GO", "{\"reason\":\"MISSING YOU\", \"reason1\":\"MISSING..." + System.currentTimeMillis() + "\"}", "itemDesc-" + System.currentTimeMillis())
        );
    }

    private List<PopulatorResource> getDummyWhere(String cc) {
        return Lists.newArrayList(
                new DemoResource("WHERE_2_GO", "[\"DALIAN_CHINA\",\"SHENZHEN_CHINA\",\"" + System.currentTimeMillis() + "\"]", "itemDesc-" + System.currentTimeMillis())
        );
    }

    private List<PopulatorResource> getDummyWhen(String cc) {
        return Lists.newArrayList(
                new DemoResource("WHEN_2_GO", "2021-10-01 now:" + System.currentTimeMillis(), "[\"itemDesc-" + System.currentTimeMillis() + "\"]"),
                new DemoResource("WHAT_2_GO", "[134,7865,3323]", "[\"itemDesc-" + System.currentTimeMillis() + "\"]")
        );
    }
}
