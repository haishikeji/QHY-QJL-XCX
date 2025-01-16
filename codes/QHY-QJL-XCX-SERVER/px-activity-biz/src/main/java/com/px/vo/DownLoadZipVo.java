package com.px.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DownLoadZipVo {

    private Integer id;

    private LocalDate startTime;

    private LocalDate endTime;

    private  String date;

    private Integer pageNo;

    private Integer pageSize;

    private Integer timeState;


}
