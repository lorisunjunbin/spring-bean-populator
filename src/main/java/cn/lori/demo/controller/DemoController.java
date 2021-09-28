package cn.lori.demo.controller;

import cn.lori.bean.populator.annotation.PopulatorCategory;
import cn.lori.bean.populator.annotation.PopulatorItem;
import cn.lori.bean.populator.model.Result;
import cn.lori.bean.populator.service.PopulateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@Qualifier("demoController")
@PopulatorCategory(codes = "BINGBANG", autoSync = true, populateAnnotatedFieldsOnly = true)
@RestController
@RequestMapping(value = "/rest", produces = {"application/json"})
@ResponseStatus(HttpStatus.OK)
public class DemoController {

    @PopulatorItem(code = "color", target = "itemDesc")
    private String bingBangColor;

    @Autowired
    PopulateService populateService;

    @RequestMapping(value = "refresh/{action}", method = RequestMethod.GET)
    public Result refresh(@PathVariable("action") String action) {
        return populateService.manuallyReload(action);
    }
}
