package com.px.modulars.mini.controller;

import com.pig4cloud.pig.common.core.util.R;
import com.px.modulars.mini.entity.MiniConfig;
import com.px.modulars.mini.service.MiniConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/miniConfig")
public class MiniConfigController {


    @Autowired
    private MiniConfigService miniConfigService;



    @RequestMapping("/getConfig")
    public R getConfig(){
       List<MiniConfig> configList =  miniConfigService.list();
       if (configList!=null && configList.size()>0){
           return R.ok(configList.get(0));
       }else {
           return R.ok(new MiniConfig());
       }
    }


    @PostMapping("/update")
    public R update(@RequestBody MiniConfig config){
        MiniConfig miniConfig = new MiniConfig();
        List<MiniConfig> configList =  miniConfigService.list();
        if (configList!=null && configList.size()>0){
            miniConfig.setId(configList.get(0).getId());
            miniConfig.setContactPhone(config.getContactPhone());
            miniConfig.setAboutUs(config.getAboutUs());
            miniConfig.setClearnDays(config.getClearnDays());
            miniConfig.setAdminAudit(config.getAdminAudit());


            miniConfig.setTextAudit(config.getTextAudit());
            miniConfig.setImgAudit(config.getImgAudit());
            miniConfig.setVoiceAudit(config.getVoiceAudit());

            miniConfigService.updateById(miniConfig);
        }else {
            miniConfig.setContactPhone(config.getContactPhone());
            miniConfig.setAboutUs(config.getAboutUs());
            miniConfig.setClearnDays(config.getClearnDays());
            miniConfig.setAdminAudit(config.getAdminAudit());


            miniConfig.setTextAudit(config.getTextAudit());
            miniConfig.setImgAudit(config.getImgAudit());
            miniConfig.setVoiceAudit(config.getVoiceAudit());

            miniConfigService.save(miniConfig);
        }
        return R.ok(miniConfig);
    }

}
