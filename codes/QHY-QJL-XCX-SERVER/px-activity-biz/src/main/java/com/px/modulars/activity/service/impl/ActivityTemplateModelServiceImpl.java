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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.px.modulars.activity.entity.ActivityTemplateModel;
import com.px.modulars.activity.mapper.ActivityTemplateModelMapper;
import com.px.modulars.activity.service.ActivityTemplateModelService;
import com.px.basic.alone.core.base.BaseServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 *
 * @author 品讯科技
 * @date 2024-08
 */
@Service
public class ActivityTemplateModelServiceImpl extends ServiceImpl<ActivityTemplateModelMapper,ActivityTemplateModel> implements ActivityTemplateModelService {

    @Override
    public List<ActivityTemplateModel> getTemplateModelList(ActivityTemplateModel activityTemplateModel) {
        QueryWrapper query = new QueryWrapper();
        query.like("title", activityTemplateModel.getTitle());
        query.last("limit 0,10");
        return super.baseMapper.flowList(query);
    }
}
