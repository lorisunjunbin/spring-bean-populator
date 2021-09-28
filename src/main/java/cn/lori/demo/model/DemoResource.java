package cn.lori.demo.model;

import cn.lori.bean.populator.model.PopulatorResource;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DemoResource implements PopulatorResource {

    private String itemCode;
    private String itemValue;
    private String itemDesc;

}
