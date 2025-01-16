package com.px.constants;

import com.pig4cloud.pig.common.core.constant.enums.BaseEnum;

/**
 * 支付订单类型
 *
 * @author 品讯科技
 */
public enum ActivityStateEnum implements BaseEnum {

    WAIT(0, "未开始"),
    STARTED(1, "进行中"),
    FINISH(2, "已结束");

    private Integer value;
    private String desc;

    private ActivityStateEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }
}
