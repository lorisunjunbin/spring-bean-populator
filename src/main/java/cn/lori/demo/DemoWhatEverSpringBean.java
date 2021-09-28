package cn.lori.demo;

import cn.lori.bean.populator.annotation.PopulatorCategory;
import cn.lori.bean.populator.annotation.PopulatorItem;
import com.google.common.base.MoreObjects;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@Qualifier("demoWhatEverSpringBean")
@PopulatorCategory(codes = {"WHY", "WHERE", "WHEN"}, autoSync = true)
public class DemoWhatEverSpringBean {

    @PopulatorItem
    private String WHEN_2_GO;

    @PopulatorItem
    private List<String> WHERE_2_GO;

    private Map<String, String> WHY_2_GO;

    @PopulatorItem(defaultValue = "[1,2,3,4,5]")
    private List<Integer> WHAT_2_GO;

    @PostConstruct
    public void init() {

        log.info("demoWhatEverSpringBean.PostConstruct all null");

        log.info("demoWhatEverSpringBean.PostConstruct.WHEN_2_GO: " + WHEN_2_GO);
        log.info("demoWhatEverSpringBean.PostConstruct.WHERE_2_GO: " + WHERE_2_GO);
        log.info("demoWhatEverSpringBean.PostConstruct.WHY_2_GO: " + WHY_2_GO);
        log.info("demoWhatEverSpringBean.PostConstruct.WHAT_2_GO: " + WHAT_2_GO);

    }

    @PreDestroy
    public void preDestroy() {

        log.info("demoWhatEverSpringBean.PreDestroy.WHEN_2_GO: " + WHEN_2_GO);
        log.info("demoWhatEverSpringBean.PreDestroy.WHERE_2_GO: " + WHERE_2_GO);
        log.info("demoWhatEverSpringBean.PreDestroy.WHY_2_GO: " + WHY_2_GO);
        log.info("demoWhatEverSpringBean.PreDestroy.WHAT_2_GO: " + WHAT_2_GO);

    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("WHEN_2_GO", WHEN_2_GO)
                .add("WHERE_2_GO", WHERE_2_GO)
                .add("WHY_2_GO", WHY_2_GO)
                .add("WHAT_2_GO", WHAT_2_GO)
                .toString();
    }
}
