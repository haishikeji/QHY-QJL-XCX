/*
 *    Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the pig4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * @author 品讯科技
 */
package com.px.modulars.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.px.apis.activity.vo.PersonnelListVo;
import com.px.fastfile.service.FastfileService;
import com.px.modulars.activity.entity.*;
import com.px.modulars.activity.mapper.ActivityRecordMapper;
import com.px.modulars.activity.service.*;
import com.px.utils.Excel.Column;
import com.px.utils.Excel.ExcelTool;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 活动报名记录
 *
 * @author 品讯科技
 * @date 2024-08
 */
@Service
public class ActivityRecordServiceImpl extends ServiceImpl<ActivityRecordMapper, ActivityRecord> implements ActivityRecordService {
    @Autowired
    private ActivityValueService valueService;
    @Autowired
    private ActivityRecordService activityRecordService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ActivityFieldService activityFieldService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private FastfileService fastfileService;

    @Value("${localFile.systemPath}")
    private String systemPath;

    @Value("${localFile.savePath}")
    private String savePath;


    @Value("${localFile.zipSavePath}")
    private String zipSavePath;

    @Value("${localFile.recordFilesPath}")
    private String recordFilesPath;


    @Override
    public Page<ActivityRecord> getRecordPage(Page page, ActivityRecord activityRecord, Boolean all) {
        QueryWrapper query = new QueryWrapper();
        query.eq("a.aid", activityRecord.getAid());
        query.eq(activityRecord.getUid() != null, "a.uid", activityRecord.getUid());
        query.eq(activityRecord.getShState() != null, "a.sh_state", activityRecord.getShState());
        query.eq(activityRecord.getState() != null, "a.state", activityRecord.getState());
        if (!all) {
            query.eq("a.state", 1);
        }
        query.orderByDesc("a.create_time");
        Page<ActivityRecord> result = baseMapper.getRecordPage(page, query);
        for (ActivityRecord a : result.getRecords()) {
            a.setValues(valueService.lambdaQuery().eq(ActivityValue::getRid, a.getId()).list());
        }
        return result;
    }

    @Override
    public Page<ActivityRecord> getRecordPage1(Page page, ActivityRecord activityRecord, Boolean all) {
        QueryWrapper query = new QueryWrapper();
        query.eq("a.aid", activityRecord.getAid());
        query.eq(activityRecord.getUid() != null, "a.uid", activityRecord.getUid());
        query.ne(activityRecord.getShState() != null, "a.sh_state", activityRecord.getShState());
        query.eq(activityRecord.getState() != null, "a.state", activityRecord.getState());

        query.like(activityRecord.getNickname() != null, "b.nickname", activityRecord.getNickname());
        if (!all) {
            query.eq("a.state", 1);
        }
        query.orderByDesc("a.create_time");
        Page<ActivityRecord> result = baseMapper.getRecordPage(page, query);
        for (ActivityRecord a : result.getRecords()) {
            a.setValues(valueService.lambdaQuery().eq(ActivityValue::getRid, a.getId()).list());
        }
        return result;
    }

    @Override
    public String getTitle(Integer id) {
        List<Integer> list = getJson(id);
        Map<String, String> map = new HashMap<String, String>();
        map.put("昵称", "name");
        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("状态", "state");
        List<Map<String, String>> titleList = new ArrayList<>();
        titleList.add(map);

        AtomicInteger i = new AtomicInteger();
        list.forEach(fid -> {
            i.getAndIncrement();
            ActivityField activityField = activityFieldService.getById(fid);
            Map<String, String> map2 = new HashMap<String, String>();
            map2.put(activityField.getName(), "map" + i);
            titleList.add(map2);
        });
        titleList.add(map1);
        AtomicInteger n = new AtomicInteger();
        List<ActivityRecord> activityRecordList = activityRecordService.lambdaQuery().eq(ActivityRecord::getState, 1)
                .eq(ActivityRecord::getShState, 1).eq(ActivityRecord::getAid, id).orderByDesc(ActivityRecord::getCreateTime).list();
        List<Map<String, String>> rowList = new ArrayList<>();
        activityRecordList.forEach(activityRecord -> {

            Map m = new HashMap<String, String>();
            Users users = usersService.getById(activityRecord.getUid());
            if (users != null) {
                m.put("name", users.getNickname());
            }
            m.put("state", "通过");
            list.forEach(a -> {
                n.getAndIncrement();
                ActivityValue activityValue = valueService.lambdaQuery().eq(ActivityValue::getRid, activityRecord.getId()).eq(ActivityValue::getFid, a).one();
                if (activityValue == null) {
                    m.put("map" + n, "");
                } else {
                    m.put("map" + n, activityValue.getValue());
                }

            });
            n.set(0);
            rowList.add(m);
        });
        ExcelTool excelTool = new ExcelTool("单级表头的表格", 15, 20);
        List<Column> titleData = excelTool.columnTransformer(titleList);
        try {
            excelTool.exportExcel(titleData, rowList, "/outExcel.xls", true, false);
            File file = new File("/outExcel.xls");
            Map<String, Object> upResult = this.fastfileService.uploadFile("ali-jipfqf", file);
            System.out.println(upResult.get("kpath"));
            if (upResult.size() > 0) {
                return upResult.get("kpath").toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    public List<Integer> getJson(Integer id) {
        Activity activity = activityService.getById(id);
        List<Integer> list = new ArrayList<>();
        if (activity != null) {
            if (StringUtils.isNotEmpty(activity.getField())) {
                JSONArray jsonArray = new JSONArray(activity.getField());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int fid = jsonObject.getInt("id");
                    list.add(fid);
                }
            }
        }
        return list;
    }

    @Override
    public String getTitle1(Integer id, LocalDate startTime, LocalDate endTime) {
        List<Integer> list = getJson(id);
        Map<String, String> map = new HashMap<String, String>();
        map.put("昵称", "name");
        Map<String, String> map4 = new HashMap<String, String>();
        map4.put("提交时间", "createTime");
        Map<String, String> map5 = new HashMap<String, String>();
        map5.put("报名状态", "bmState");
        List<Map<String, String>> titleList = new ArrayList<>();
        titleList.add(map);
        titleList.add(map4);
        titleList.add(map5);
        AtomicInteger i = new AtomicInteger();


        Map<Integer, Boolean> fileFieldMap = new HashMap();

        list.forEach(fid -> {
            i.getAndIncrement();
            ActivityField activityField = activityFieldService.getById(fid);
            if (activityField != null) {
                Map<String, String> map6 = new HashMap<String, String>();
                map6.put(activityField.getName(), "map" + i);
                titleList.add(map6);

                if (activityField.getFieldType() == 4 || activityField.getFieldType() == 5) {
                    fileFieldMap.put(fid, true);
                }
            }
        });
        AtomicInteger n = new AtomicInteger();
        List<ActivityRecord> activityRecordList = activityRecordService.lambdaQuery()
                .eq(ActivityRecord::getAid, id)
                .eq(ActivityRecord::getState, 1)
                .apply(startTime != null && endTime != null,
                        "date(create_time) >= date( '" + startTime + "') and date(create_time) <= date( '" + endTime + "')")
                .orderByDesc(ActivityRecord::getCreateTime).list();
        List<Map<String, String>> rowList = new ArrayList<>();
        activityRecordList.forEach(activityRecord -> {
            Map m = new HashMap<String, String>();
            Users users = usersService.getById(activityRecord.getUid());
            if (users != null) {
                m.put("name", users.getNickname());
            }
            if (activityRecord.getState() == 1) {
                m.put("bmState", "已参加");

            } else {
                m.put("bmState", "已取消");
            }
            Users users1 = usersService.getById(activityRecord.getShUid());
            if (users1 != null) {
                m.put("uname", users1.getNickname());
            }

            DateTimeFormatter dfDateTime = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");

            m.put("createTime", dfDateTime.format(activityRecord.getCreateTime()));

            list.forEach(a -> {
                n.getAndIncrement();
                ActivityValue activityValue = valueService.lambdaQuery().eq(ActivityValue::getRid, activityRecord.getId()).eq(ActivityValue::getFid, a).one();
                if (activityValue == null) {
                    m.put("map" + n, "");
                } else {
                    if (fileFieldMap.get(a)!=null && fileFieldMap.get(a)) {

                        if (StringUtils.isNotEmpty(activityValue.getValue())){
                            m.put("map" + n, "HYPERLINK(\""+recordFilesPath+"?rid="+ activityRecord.getId()+ "&fid="+ a +"\",\"查看附件\")");
                        }else {
                            m.put("map" + n, activityValue.getValue());
                        }

                    } else {
                        m.put("map" + n, activityValue.getValue());
                    }
                }
            });
            n.set(0);
            rowList.add(m);
        });
        ExcelTool excelTool = new ExcelTool("单级表头的表格", 15, 20);
        List<Column> titleData = excelTool.columnTransformer(titleList);
        try {
            String fileName = System.currentTimeMillis() + id + ".xls";

            excelTool.exportExcel(titleData, rowList, savePath + systemPath + zipSavePath + "/" + fileName, true, false);
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public Integer getSignCount(Integer aid) {
        return baseMapper.getSignCount(aid);
    }

    @Override
    public Integer getActivitySignCount(Integer aid) {
        return baseMapper.getActivitySignCount(aid);
    }

    @Override
    public Page<PersonnelListVo> getPersonnelPage(Page page,Integer aid) {
        return baseMapper.getPersonnelPage(page,aid);
    }


}


