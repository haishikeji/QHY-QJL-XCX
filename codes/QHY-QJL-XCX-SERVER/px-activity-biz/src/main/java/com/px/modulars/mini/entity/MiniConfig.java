package com.px.modulars.mini.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

@Data
@TableName("mini_config")
public class MiniConfig extends Model<MiniConfig> {

    private Integer id;

    /**
     *  关于我们
     */
    private String aboutUs;

    /**
     *  联系电话
     */
    private String contactPhone;


    /**
     *  文件清理天数
     */
    private Integer clearnDays;


    /**
     *  是否开启后台审核 1：开始 2:关闭
     */
    private Integer adminAudit;



    /**
     *  文本检测
     */
    private Integer textAudit;


    /**
     *  图片检测
     */
    private Integer imgAudit;


    /**
     *  音频检测
     */
    private Integer voiceAudit;

    private String  appid;

}
