package cn.lori.demo;

import cn.lori.bean.populator.annotation.PopulatorCategory;
import cn.lori.bean.populator.annotation.PopulatorItem;
import com.google.common.base.MoreObjects;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
@Qualifier("demoWhatEverSpringBeanOther")
@PopulatorCategory(codes = {"WHY", "WHERE", "WHEN"}, populateAnnotatedFieldsOnly = true)
public class DemoWhatEverSpringBeanOther {


    @PopulatorItem(categoryCode = "WHEN", code = "WHEN_2_GO", target = "itemDesc")
    private String when2Go;


    @PopulatorItem
    private List<String> WHERE_2_GO;

    private Map<String, String> WHY_2_GO;

    private List<Integer> WHAT_2_GO;

    @PostConstruct
    public void init() {

        log.info("demoWhatEverSpringBeanOther.PostConstruct all null");

        log.info("demoWhatEverSpringBeanOther.PostConstruct.when2Go: " + when2Go);
        log.info("demoWhatEverSpringBeanOther.PostConstruct.WHERE_2_GO: " + WHERE_2_GO);
        log.info("demoWhatEverSpringBeanOther.PostConstruct.WHY_2_GO: " + WHY_2_GO);
        log.info("demoWhatEverSpringBeanOther.PostConstruct.WHAT_2_GO: " + WHAT_2_GO);

    }

    @PreDestroy
    public void preDestroy() {

        log.info("demoWhatEverSpringBeanOther.PreDestroy.when2Go: " + when2Go);
        log.info("demoWhatEverSpringBeanOther.PreDestroy.WHERE_2_GO: " + WHERE_2_GO);
        log.info("demoWhatEverSpringBeanOther.PreDestroy.WHY_2_GO: " + WHY_2_GO);
        log.info("demoWhatEverSpringBeanOther.PreDestroy.WHAT_2_GO: " + WHAT_2_GO);

    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("when2Go", when2Go)
                .add("WHERE_2_GO", WHERE_2_GO)
                .add("WHY_2_GO", WHY_2_GO)
                .add("WHAT_2_GO", WHAT_2_GO)
                .toString();
    }
}
