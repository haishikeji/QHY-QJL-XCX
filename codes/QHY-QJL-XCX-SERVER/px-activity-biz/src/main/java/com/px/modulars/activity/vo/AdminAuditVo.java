package com.px.modulars.activity.vo;

import lombok.Data;

@Data
public class AdminAuditVo {

    //活动id
    private Integer aid;

    //审核状态 1：审核通过 2：审核未通过
    private Integer state;

    //未通过原因
    private String content;

}
