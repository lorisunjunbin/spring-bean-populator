package cn.lori.demo.controller;

import cn.lori.bean.populator.model.Result;
import cn.lori.bean.populator.service.PopulateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/rest", produces = {"application/json"})
@ResponseStatus(HttpStatus.OK)
public class DemoConntroller {

    @Autowired
    PopulateService populateService;

    @RequestMapping(value = "refresh/{action}", method = RequestMethod.GET)
    public Result refresh(@PathVariable("action") String action) {
        return populateService.manuallyReload(action);
    }
}
