package com.px.modulars.activity.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity_record_review")
public class ActivityRecordReview  extends Model<ActivityRecordReview> {

    private Integer id;

    private String content;

    private String img;

    private String tape;

    private LocalDateTime createTime;

    private Integer createBy;

    private Integer rid;

    @TableField(exist = false)
    private String createName;
    @TableField(exist = false)
    private String createHeadImg;

    private String  appid;

}
