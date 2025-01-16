package com.px.modulars.plugin.baidu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.core.util.RedisHelper;
import com.px.basic.alone.core.base.BaseApiController;
import com.px.basic.alone.security.annotation.Inner;
import com.px.modulars.activity.entity.*;
import com.px.modulars.activity.service.ActivityTemplateModelService;
import com.px.modulars.activity.service.ActivityValueService;
import com.px.modulars.activity.service.UsersIntegralService;
import com.px.modulars.activity.service.UsersService;
import com.px.modulars.plugin.baidu.service.WenXinYiFanService;
import com.px.modulars.plugin.baidu.vo.Field;
import com.px.modulars.plugin.baidu.vo.JsMessage;
import com.px.modulars.plugin.baidu.vo.Message;
import com.px.vo.FieldVo;
import com.sun.org.apache.bcel.internal.generic.SWITCH;
import com.tencentcloudapi.cms.v20190321.models.User;
import io.swagger.models.auth.In;
import me.chanjar.weixin.common.service.WxService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/antiPorn")
public class AntiPornController extends BaseApiController {


    @Autowired
    private ActivityValueService valueService;

    @Value("${localFile.savePath}")
    private String savePath;

    @Value("${localFile.saveResultPath}")
    private String saveResultPath;

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private WenXinYiFanService wenXinYiFanService;

    @Autowired
    private ActivityTemplateModelService templateModelService;

    @Autowired
    private UsersService userService;

    @Autowired
    private UsersIntegralService usersIntegralService;
    @Value("${baidu.wenxinyifan.fcid}")
    private String fcid;
    @Value("${baidu.wenxinyifan.cjid}")
    private String cjid;
    @Value("${baidu.wenxinyifan.jzfcid}")
    private String jzfcid;
    @Value("${baidu.wenxinyifan.jcid}")
    private String jcid;
    @Value("${baidu.wenxinyifan.jqcjid}")
    private String jqcjid;
    @Value("${baidu.wenxinyifan.zlid}")
    private String zlid;


    private String cjmb = "你现在是一款表单模板工具，需要你根据用户的问题，分析提炼出主题，并根据要求整理创建三个数据模版。 回复数据json类型：\"[{\"title\":\"\",\"description\":\"\",\"use\":\"\",\"fieldList\":[{\"name\":\"\",\"type\":\"\",\"enumValue\":\"\"},{\"name\":\"\",\"type\":\"\",\"enumValue\":\"\"},{\"name\":\"\",\"type\":\"\",\"enumValue\":\"\"}]},{\"title\":\"\",\"description\":\"\",\"use\":\"\",\"fieldList\":[{\"name\":\"\",\"type\":\"\",\"enumValue\":\"\"},{\"name\":\"\",\"type\":\"\",\"enumValue\":\"\"},{\"name\":\"\",\"type\":\"\",\"enumValue\":\"\"}]},{\"title\":\"\",\"description\":\"\",\"use\":\"\",\"fieldList\":[{\"name\":\"\",\"type\":\"\",\"enumValue\":\"\"},{\"name\":\"\",\"type\":\"\",\"enumValue\":\"\"},{\"name\":\"\",\"type\":\"\",\"enumValue\":\"\"}]}]\"要求回复标题，接龙介绍，模版用途，模版字段列表（字段名，字段类型，枚举值）这些内容。注：模版字段列表类型包括（文字，单选框，复选框，图片，音频，日期，位置），如果是单选和多选需要列举最少两个枚举值。要求生成的三个需要呈现不同的标题和字段内容。 例如用户的问题为 \"创建单位上班打卡情况\"，你的回答：[{\"title\": \"单位上下班打卡情况\",\"description\": \"该接龙用于记录单位员工上下班打卡情况，记录的内容包括打卡时间、地点、是否迟到、早退等信息。\",\"use\":\"用于统计员工上班情况，加强考勤管理。\",\"fieldList\": [{\"name\": \"打卡时间\", \"type\": \"日期\",\"enumValue\": \"\" }, {\"name\": \"打卡地点\",\"type\": \"位置\",\"enumValue\": \"\" },{\"name\": \"迟到/早退\",\"type\":\"复选框\",\"enumValue\":\"迟到,早退\"} ]},{\"title\": \"单位上班打卡统计\",\"description\": \"该接龙用于统计单位员工的上班打卡，是否迟到等信息。\",\"use\":\"用于统计员工上下班情况，方便整治迟到。\",\"fieldList\": [{\"name\": \"打卡时间\", \"type\": \"日期\",\"enumValue\": \"\" },{\"name\": \"是否迟到\",\"type\":\"单选框\",\"enumValue\":\"是,否\"} ]},{\"title\": \"单位上班打卡记录\",\"description\": \"该接龙用于记录单位上班打卡情况，记录内容包括打卡人姓名、打卡时间、地点、是否迟到、早退等信息。\",\"use\":\"用于统计员工上班情况，加强考勤管理。\",\"fieldList\": [{\"name\": \"打卡人\", \"type\": \"文字\",\"enumValue\": \"\" }, {\"name\": \"打卡时间\", \"type\": \"日期\",\"enumValue\": \"\" }, {\"name\": \"打卡方式\",\"type\": \"单选框\",\"enumValue\": \"指纹打卡,密码打卡,远程打卡\" },{\"name\": \"迟到/早退\",\"type\":\"复选框\",\"enumValue\":\"迟到,早退\"} ]}]\"。 用户的问题是\"";
    private String hd = "\"，你的回答：(回复内容只回复json数据，不回复任何介绍)";
   /* public static void main(String[] args) {
        String templateStr = "\n" +
                "[{\"title\": \"健康信息接龙\",\"description\": \"该接龙用于记录填写人员的健康信息，记录内容包括姓名、年龄、性别、身体状况、过敏史等信息。\",\"use\":\"用于统计和了解员工健康情况，便于做好防疫工作。\",\"fieldList\": [{\"name\": \"姓名\", \"type\": \"文字\",\"enumValue\": \"\" }, {\"name\": \"年龄\", \"type\": \"单选框\",\"enumValue\": \"18-25、26-35、36-45、46-55、56以上\" }, {\"name\": \"性别\", \"type\": \"单选框\",\"enumValue\": \"男、女\" }, {\"name\": \"身体状况\", \"type\": \"单选框\",\"enumValue\": \"健康、亚健康、疾病\" }, {\"name\": \"过敏史\", \"type\": \"复选框\",\"enumValue\":\"是否有过敏史\"} ]},{\"title\": \"健康信息统计\",\"description\": \"该接龙用于统计人员健康情况，包括人数、性别、年龄等基本信息。\",\"use\":\"便于快速了解健康状况，合理进行工作分配。\",\"fieldList\": [{\"name\": \"健康人数\", \"type\": \"数字\",\"enumValue\": \"\" } ]},{\"title\": \"健康信息记录\",\"description\": \"该接龙用于记录人员的健康信息，包括姓名、年龄、性别、身体状况、过敏史等信息。\",\"use\":\"便于统计和了解员工健康情况，便于做好防疫工作。\",\"fieldList\": [{\"name\": \"姓名\", \"type\": \"文字\",\"enumValue\": \"\" }, {\"name\": \"年龄\", \"type\": \"单选框\",\"enumValue\": \"18-25、26-35、36-45、46-55、56以上\" }, {\"name\": \"性别\", \"type\": \"单选框\",\"enumValue\": \"男、女\" }, {\"name\": \"身体状况\", \"type\": \"单选框\",\"enumValue\": \"健康、亚健康、疾病\" }, {\"name\": \"过敏史\", \"type\": \"复选框\",\"enumValue\":\"是否有过敏史\"} ]}]";
        //去空格和换行
        //templateStr = templateStr.replaceAll(" ", "").replaceAll("\n", "");
        templateStr = templateStr.replaceAll("\\s*", "");
        List<JsMessage> jsMessages = com.alibaba.fastjson.JSONObject.parseArray(templateStr, JsMessage.class);
        System.out.println(jsMessages);
        *//* String templateStr = "[\"标题\":\"公司员工信息\",\"接龙介绍\":\"该接龙用于填写公司员工信息，包括姓名、性别、年龄、联系方式等。\",\"模板用途\":\"用于公司管理员工信息，方便员工管理。\",\"模版字段列表\":\"[\"字段名\":\"姓名\",\"字段类型\":\"文字\",\"枚举值\":\"\"] ,[\"字段名\":\"性别\",\"字段类型\":\"文字\",\"枚举值\":\"\"] ,[\"字段名\":\"年龄\",\"字段类型\":\"数字\",\"枚举值\":\"\"] ,[\"字段名\":\"联系方式\",\"字段类型\":\"文字\",\"枚举值\":\"\"] \"]";
        //去空格去换行
        templateStr = templateStr.replaceAll(" ", "").replaceAll("\n", "");
        //templateStr = templateStr.substring(templateStr.indexOf("[") + 1, templateStr.indexOf("]"));
        //"标题":"上下班记录打卡时间","内容":"打卡,记录打卡时间"  取标题后的装引号内字符串
        String cjData = templateStr.substring(templateStr.indexOf("标题") + 5, templateStr.indexOf("接龙介绍") - 3);
        String jljs = templateStr.substring(templateStr.indexOf("接龙介绍") + 7, templateStr.indexOf("模板用途") - 3);
        String mbyt = templateStr.substring(templateStr.indexOf("模板用途") + 7, templateStr.indexOf("模版字段列表") - 3);
        String mbzd = templateStr.substring(templateStr.indexOf("模版字段列表") + 9, templateStr.length() - 2);
        String[] mbzds = mbzd.split("],");
        for (String mbzd1 : mbzds) {
            String zdm = mbzd1.substring(mbzd1.indexOf("字段名") + 6, mbzd1.indexOf("字段类型") - 3);
            String zdlx = mbzd1.substring(mbzd1.indexOf("字段类型") + 7, mbzd1.indexOf("枚举值") - 3);
            String mjz = mbzd1.substring(mbzd1.indexOf("枚举值") + 6, mbzd1.length() - 1);
            System.out.println(zdm);
            System.out.println(zdlx);
            System.out.println(mjz);
        }
        System.out.println(cjData);
        System.out.println(jljs);
        System.out.println(mbyt);
        System.out.println(mbzd);*//*
    }*/
   /* public static void main(String[] args) {
        String str = "1. 宿舍晚归人员统计\n" +
                "接龙介绍：该接龙用于统计宿舍晚归人员的信息，包括姓名、房间号、晚归时间等。\n" +
                "模版用途：用于宿舍管理员统计晚归学生信息，加强安全管理。\n" +
                "模版字段列表：\n" +
                "* 姓名（文字）\n" +
                "* 房间号（文字）\n" +
                "* 晚归时间（日期）\n" +
                "2. 商品库存接龙\n" +
                "接龙介绍：该接龙用于统计商品库存信息，包括商品名称、数量、库存状态等。\n" +
                "模版用途：用于商家管理商品库存，避免缺货或积压。\n" +
                "模版字段列表：\n" +
                "* 商品名称（文字）\n" +
                "* 数量（数字）\n" +
                "* 库存状态（单选框，枚举值：库存充足、库存不足、已售罄）\n" +
                "3. 接龙填写健康信息\n" +
                "接龙介绍：该接龙用于填写健康信息，包括基本信息、身体状况、联系方式等。\n" +
                "模版用途：用于疫情防控期间填写个人健康信息，保障公共安全。\n" +
                "模版字段列表：\n" +
                "* 姓名（文字）\n" +
                "* 身份证号码（数字）\n" +
                "* 手机号（文字）\n" +
                "* 当前身体状况（单选框，枚举值：健康、体温异常、去过疫情高风险地区）";
        //去掉空格和换行
        str = str.replaceAll("\\s*", "");
        //System.out.println(str);
       *//* String str1 = str.substring(str.indexOf("1.")+2,str.indexOf("2."));
        String strtitle1 = str1.substring(0,str1.indexOf("接龙介绍："));
        String strjljs1 = str1.substring(str1.indexOf("接龙介绍：")+5,str1.indexOf("模版用途："));
        String strmbyt1 = str1.substring(str1.indexOf("模版用途：")+5,str1.indexOf("模版字段列表："));
        String[] strmbzdlb1 = str1.substring(str1.indexOf("模版字段列表：")+8).split("\\*");
        //System.out.println(Arrays.toString(strmbzdlb1));
        for (String s : strmbzdlb1) {
            String name = s.substring(0,s.indexOf("（"));
            String attribute = s.substring(s.indexOf("（")+1,s.indexOf("）"));
            String type = "";
            String[] value = null;
            if(attribute.contains("，")){
                type = attribute.substring(0,attribute.indexOf("，"));
                value = attribute.substring(attribute.indexOf("枚举值：")+4).split("、");
            }else{
                type = attribute;
            }
            System.out.println(Arrays.toString(value));
        }
*//*
        //System.out.println(str1);
        String str2 = str.substring(str.indexOf("2.")+2,str.indexOf("3."));
        String strtitle2 = str2.substring(0,str2.indexOf("接龙介绍："));
        String strjljs2 = str2.substring(str2.indexOf("接龙介绍：")+5,str2.indexOf("模版用途："));
        String strmbyt2 = str2.substring(str2.indexOf("模版用途：")+5,str2.indexOf("模版字段列表："));
        String[] strmbzdlb2 = str2.substring(str2.indexOf("模版字段列表：")+8).split("\\*");
        //System.out.println(Arrays.toString(strmbzdlb2));
        ActivityTemplateModel model = new ActivityTemplateModel();
        model.setTitle(strtitle2);
        model.setContent(strjljs2);
        model.setIntroduce(strmbyt2);
        model.setTemplateType(18);
        model.setActStartEnd(0);
        model.setAllNum(5000);
        model.setSingleNum(1);
        model.setType(1);
        //model.setUid(uid);
        model.setActivityType(1);
        if(strtitle2.contains("打卡")){
            model.setActivityType(1);
        } else if (strjljs2.contains("反馈")) {
            model.setActivityType(2);
        } else if (strjljs2.contains("报名")||strjljs2.contains("活动")) {
            model.setActivityType(3);
        }

        List<ActivityField> fieldVos = new ArrayList<>();
        for (String s : strmbzdlb2) {
            String name = s.substring(0,s.indexOf("（"));
            String attribute = s.substring(s.indexOf("（")+1,s.indexOf("）"));
            String type = "";
            String value = null;
            if(attribute.contains("，")){
                type = attribute.substring(0,attribute.indexOf("，"));
                value = attribute.substring(attribute.indexOf("枚举值：")+4);
            }else{
                type = attribute;
            }
            FieldVo fieldVo = new FieldVo();
            fieldVo.setName(name);
            fieldVo.setFieldType(1);
            if(type.contains("文字")){
                fieldVo.setFieldType(1);
            } else if (type.contains("单选")) {
                fieldVo.setFieldType(2);
            }else if (type.contains("复选")||type.contains("多选")) {
                fieldVo.setFieldType(3);
            }else if(type.contains("图片")){
                fieldVo.setFieldType(4);
            }else if(type.contains("日期")) {
                fieldVo.setFieldType(5);
            }else if(type.contains("位置")) {
                fieldVo.setFieldType(6);
            }
            fieldVo.setRequired(0);
            fieldVo.setVal(value);
            //ActivityField f = wenXinYiFanService.getCustomField(fieldVo);
            //fieldVos.add(f);
        }
        System.out.println(fieldVos);
        Gson gson = new Gson();
        String fieldvos = gson.toJson(fieldVos);
        model.setField(fieldvos);
        //System.out.println(str2);
       *//* String str3 = str.substring(str.indexOf("3.")+2);
        String strtitle3 = str3.substring(0,str3.indexOf("接龙介绍："));
        String strjljs3 = str3.substring(str3.indexOf("接龙介绍：")+5,str3.indexOf("模版用途："));
        String strmbyt3 = str3.substring(str3.indexOf("模版用途：")+5,str3.indexOf("模版字段列表："));
        String[] strmbzdlb3 = str3.substring(str3.indexOf("模版字段列表：")+8).split("\\*");
        //System.out.println(Arrays.toString(strmbzdlb3));
        for (String s : strmbzdlb3) {
            String name = s.substring(0,s.indexOf("（"));
            String attribute = s.substring(s.indexOf("（")+1,s.indexOf("）"));
            String type = "";
            String[] value = null;
            if(attribute.contains("，")){
                type = attribute.substring(0,attribute.indexOf("，"));
                value = attribute.substring(attribute.indexOf("枚举值：")+4).split("、");
            }else{
                type = attribute;
            }
            System.out.println(Arrays.toString(value));
        }*//*


    }*/
   @Inner(value = false)
   @GetMapping("/text")
   public R text( String data){
       /*String str = "1. 宿舍晚归人员统计\n" +
               "接龙介绍：该接龙用于统计宿舍晚归人员的信息，包括姓名、房间号、晚归时间等。\n" +
               "模版用途：用于宿舍管理员统计晚归学生信息，加强安全管理。\n" +
               "模版字段列表：\n" +
               "* 姓名（文字）\n" +
               "* 房间号（文字）\n" +
               "* 晚归时间（日期）\n" +
               "2. 商品库存接龙\n" +
               "接龙介绍：该接龙用于统计商品库存信息，包括商品名称、数量、库存状态等。\n" +
               "模版用途：用于商家管理商品库存，避免缺货或积压。\n" +
               "模版字段列表：\n" +
               "* 商品名称（文字）\n" +
               "* 数量（数字）\n" +
               "* 库存状态（单选框，枚举值：库存充足、库存不足、已售罄）\n" +
               "3. 接龙填写健康信息\n" +
               "接龙介绍：该接龙用于填写健康信息，包括基本信息、身体状况、联系方式等。\n" +
               "模版用途：用于疫情防控期间填写个人健康信息，保障公共安全。\n" +
               "模版字段列表：\n" +
               "* 姓名（文字）\n" +
               "* 身份证号码（数字）\n" +
               "* 手机号（文字）\n" +
               "* 当前身体状况（单选框，枚举值：健康、体温异常、去过疫情高风险地区）";
       List<ActivityTemplateModel> list =formate(str,0);
       return R.ok(list);*/
       String cjData = "";
       String content = wenXinYiFanService.template(data,fcid);
       String result = wenXinYiFanService.chat(content);
       String templateStr = result;
       List<ActivityTemplateModel> list = new ArrayList<>();
       Set<ActivityTemplateModel> set = new HashSet<>();
       if (StringUtils.isNotEmpty(templateStr)) {
                /*if (templateStr.indexOf(":") > 0) {
                    templateStr = templateStr.substring(templateStr.indexOf(":") + 1, templateStr.length());
                }
                if (templateStr.indexOf("：") > 0) {
                    templateStr = templateStr.substring(templateStr.indexOf("：") + 1, templateStr.length());
                }*/
           //去空格去换行
           templateStr = templateStr.replaceAll(" ", "").replaceAll("\n", "");
           //截取中括号内容
           templateStr = templateStr.substring(templateStr.indexOf("[") + 1, templateStr.indexOf("]"));
           //"标题":"上下班记录打卡时间","内容":"打卡,记录打卡时间"  取标题后的装引号内字符串
           cjData = templateStr.substring(templateStr.indexOf("标题") + 5, templateStr.indexOf("内容") - 3);
           templateStr = templateStr.substring(templateStr.indexOf("内容") + 5, templateStr.length()-1);
           String[] arr = null;
           if (templateStr.indexOf(",") > 0) {
               arr = templateStr.split(",");
           }else if (templateStr.indexOf("，") > 0) {
               arr = templateStr.split("，");
           }
           if (arr != null && arr.length > 0) {
               for (String s : arr) {
                   if (StringUtils.isNotEmpty(s)) {
                       ActivityTemplateModel model = new ActivityTemplateModel();
                       model.setTitle(s);
                       List<ActivityTemplateModel> modelist = templateModelService.lambdaQuery().like(ActivityTemplateModel::getTitle, s).page(new Page<>(1, 10)).getRecords();
                           /* List<ActivityTemplateModel> modelist = templateModelService
                                    .lambdaQuery()
                                    .like(ActivityTemplateModel::getTitle,s)
                                    .page(new Page<>(1, 10)).getRecords();*/
                       list.addAll(modelist);
                   }
               }
               Integer i = 0;
               Random random = new Random();
               while (i < list.size() && set.size() < 3) {
                   i++;
                   set.add(list.get(random.nextInt(list.size())));
               }

           }
       }
       List<ActivityTemplateModel> list1 = new ArrayList<>();
       if(set.size()==0){
           if(StringUtils.isEmpty(cjData)&&StringUtils.isNotEmpty(templateStr)){
               cjData = templateStr;
           }
           String content1 = wenXinYiFanService.template(cjData,jqcjid);
           String result1 = wenXinYiFanService.chat(content1);
           if(StringUtils.isNotEmpty(result1)){
               list1 =formates(result1,0);
           }
       }
       Map<String, Object> map = new HashMap<>();
       map.put("AIresult", result);
       map.put("template", set);
       map.put("templateModel", list1);
       return R.ok(map);
   }
    public List<ActivityTemplateModel> formate(String str,int uid){
        str = str.replaceAll("\\s*", "");
        List<String> strList =new ArrayList<>();
        if(str.contains("1.")&&str.contains("2.")){
            String str1 = str.substring(str.indexOf("1.")+2,str.indexOf("2."));
            strList.add(str1);
        }
        if(str.contains("2.")&&str.contains("3.")){
            String str2 = str.substring(str.indexOf("2.")+2,str.indexOf("3."));
            strList.add(str2);
        }
        if(str.contains("3.")&&str.contains("4.")){
            String str3 = str.substring(str.indexOf("3.")+2,str.indexOf("4."));
            strList.add(str3);
        }
        if(str.contains("4.")&&str.contains("5.")){
            String str4 = str.substring(str.indexOf("4.")+2,str.indexOf("5."));
            strList.add(str4);
        }
        if(str.contains("5.")&&str.contains("6.")){
            String str5 = str.substring(str.indexOf("5.")+2,str.indexOf("6."));
            strList.add(str5);
        }
        List<ActivityTemplateModel> list = new ArrayList<>();
        for (String ss : strList) {
            String strtitle2 = ss.substring(0,ss.indexOf("接龙介绍："));
            String strjljs2 = ss.substring(ss.indexOf("接龙介绍：")+5,ss.indexOf("模版用途："));
            String strmbyt2 = ss.substring(ss.indexOf("模版用途：")+5,ss.indexOf("模版字段列表："));
            String[] strmbzdlb2 = ss.substring(ss.indexOf("模版字段列表：")+8).split("\\*");
            //System.out.println(Arrays.toString(strmbzdlb2));
            ActivityTemplateModel model = new ActivityTemplateModel();
            model.setTitle(strtitle2);
            model.setContent(strjljs2);
            model.setIntroduce(strmbyt2);
            model.setTemplateType(18);
            model.setActStartEnd(0);
            model.setAllNum(5000);
            model.setSingleNum(1);
            model.setType(1);
            model.setUid(uid);
            model.setActivityType(1);
            if(strtitle2.contains("打卡")){
                model.setActivityType(1);
            } else if (strjljs2.contains("反馈")) {
                model.setActivityType(2);
            } else if (strjljs2.contains("报名")||strjljs2.contains("活动")) {
                model.setActivityType(3);
            }

            List<ActivityField> fieldVos = new ArrayList<>();
            for (String s : strmbzdlb2) {
                String name = s.substring(0,s.indexOf("（"));
                String attribute = s.substring(s.indexOf("（")+1,s.indexOf("）"));
                String type = "";
                String value = null;
                if(attribute.contains("，")){
                    type = attribute.substring(0,attribute.indexOf("，"));
                    value = attribute.substring(attribute.indexOf("枚举值：")+4);
                }else{
                    type = attribute;
                }
                FieldVo fieldVo = new FieldVo();
                fieldVo.setName(name);
                fieldVo.setFieldType(1);
                fieldVo.setType(1);
                if(type.contains("文字")){
                    fieldVo.setFieldType(1);
                } else if (type.contains("单选")) {
                    fieldVo.setFieldType(2);
                }else if (type.contains("复选")||type.contains("多选")) {
                    fieldVo.setFieldType(3);
                }else if(type.contains("图片")){
                    fieldVo.setFieldType(4);
                }else if(type.contains("音频")) {
                    fieldVo.setFieldType(5);
                }else if(type.contains("日期")) {
                    fieldVo.setFieldType(6);
                }else if(type.contains("位置")) {
                    fieldVo.setFieldType(7);
                }
                fieldVo.setRequired(0);
                fieldVo.setVal(value);
                ActivityField f = wenXinYiFanService.getCustomField(fieldVo,uid);
                fieldVos.add(f);
            }
            System.out.println(fieldVos);
            Gson gson = new Gson();
            String fieldvos = gson.toJson(fieldVos);
            model.setField(fieldvos);
            list.add(model);
        }
        return list;
    }
    public List<ActivityTemplateModel> formates(String str,int uid){
        str = str.replaceAll("\\s*", "");
        List<String> strList =new ArrayList<>();
        strList.add(str);
        List<ActivityTemplateModel> list = new ArrayList<>();
        for (String templateStr : strList) {
            String strtitle2 = templateStr.substring(templateStr.indexOf("标题") + 5, templateStr.indexOf("接龙介绍") - 3);
            String strjljs2 = templateStr.substring(templateStr.indexOf("接龙介绍") + 7, templateStr.indexOf("模板用途") - 3);
            String strmbyt2 = templateStr.substring(templateStr.indexOf("模板用途") + 7, templateStr.indexOf("模版字段列表") - 3);
            String mbzd = templateStr.substring(templateStr.indexOf("模版字段列表") + 9, templateStr.length() - 2);
            String[] strmbzdlb2 = mbzd.split("],");
            //System.out.println(Arrays.toString(strmbzdlb2));
            ActivityTemplateModel model = new ActivityTemplateModel();
            model.setTitle(strtitle2);
            model.setContent(strjljs2);
            model.setIntroduce(strmbyt2);
            model.setTemplateType(18);
            model.setActStartEnd(0);
            model.setAllNum(5000);
            model.setSingleNum(1);
            model.setType(1);
            model.setUid(uid);
            model.setActivityType(1);
            if(strtitle2.contains("打卡")){
                model.setActivityType(1);
            } else if (strjljs2.contains("反馈")) {
                model.setActivityType(2);
            } else if (strjljs2.contains("报名")||strjljs2.contains("活动")) {
                model.setActivityType(3);
            }

            List<ActivityField> fieldVos = new ArrayList<>();
            for (String mbzd1 : strmbzdlb2) {
                String name = mbzd1.substring(mbzd1.indexOf("字段名") + 6, mbzd1.indexOf("字段类型") - 3);
                String type = mbzd1.substring(mbzd1.indexOf("字段类型") + 7, mbzd1.indexOf("枚举值") - 3);
                String value = mbzd1.substring(mbzd1.indexOf("枚举值") + 6, mbzd1.length() - 1);
                FieldVo fieldVo = new FieldVo();
                fieldVo.setName(name);
                fieldVo.setFieldType(1);
                fieldVo.setType(1);
                if(type.contains("文字")){
                    fieldVo.setFieldType(1);
                } else if (type.contains("单选")) {
                    fieldVo.setFieldType(2);
                }else if (type.contains("复选")||type.contains("多选")) {
                    fieldVo.setFieldType(3);
                }else if(type.contains("图片")){
                    fieldVo.setFieldType(4);
                }else if(type.contains("日期")) {
                    fieldVo.setFieldType(5);
                }else if(type.contains("位置")) {
                    fieldVo.setFieldType(6);
                }
                fieldVo.setRequired(0);
                fieldVo.setVal(value);
                ActivityField f = wenXinYiFanService.getCustomField(fieldVo,uid);
                fieldVos.add(f);
            }
            System.out.println(fieldVos);
            Gson gson = new Gson();
            String fieldvos = gson.toJson(fieldVos);
            model.setField(fieldvos);
            list.add(model);
        }
        return list;
    }

    public static void main(String[] args) {
        String str = "{\"title\":\"运动会签到情况\",\"description\":\"该模板用于记录运动会签到情况，包括签到时间、地点、运动员姓名等信息。\",\"use\":\"用于统计运动会签到情况，方便后续数据分析和统计。\",\"fieldList\":[{\"name\":\"签到时间\",\"type\":\"日期\",\"enumValue\":\"上午,下午\"},{\"name\":\"签到地点\",\"type\":\"位置\",\"enumValue\":\"\"},{\"name\":\"运动员姓名\",\"type\":\"文字\",\"enumValue\":\"\"}]}";
        if (str == null || str.length() == 0 || str.charAt(0) != '[') {
            str =  "[" + str + "]";
        }
        List<JsMessage> jsMessages = com.alibaba.fastjson.JSONObject.parseArray(str, JsMessage.class);
        System.out.println(jsMessages);

    }
    public List<ActivityTemplateModel> Jsformate(String str,int uid){
        str = str.replaceAll("\\s*", "");
        System.out.println("JsForMate:"+str);
        if (str == null || str.length() == 0 || str.charAt(0) != '[') {
            str =  "[" + str + "]";
        }
        List<JsMessage> jsMessages = com.alibaba.fastjson.JSONObject.parseArray(str, JsMessage.class);

        List<ActivityTemplateModel> list = new ArrayList<>();
        for (JsMessage templateStr : jsMessages) {
            String strtitle2 = templateStr.getTitle();
            String strjljs2 = templateStr.getDescription();
            String strmbyt2 = templateStr.getUse();
            ActivityTemplateModel model = new ActivityTemplateModel();
            model.setTitle(strtitle2);
            model.setContent(strjljs2);
            model.setIntroduce(strmbyt2);
            model.setTemplateType(18);
            model.setActStartEnd(0);
            model.setAllNum(5000);
            model.setSingleNum(1);
            model.setType(1);
            model.setUid(uid);
            model.setActivityType(2);
            if(strtitle2.contains("打卡")){
                model.setActivityType(3);
            } else if (strjljs2.contains("反馈")) {
                model.setActivityType(2);
            } else if (strjljs2.contains("报名")||strjljs2.contains("活动")) {
                model.setActivityType(1);
            }
            List<ActivityField> fieldVos = new ArrayList<>();
            if(templateStr.getFieldList()!=null&&templateStr.getFieldList().size()>0){
                for (Field mbzd1 : templateStr.getFieldList()) {
                    String name = mbzd1.getName();
                    String type = mbzd1.getType();
                    String value = mbzd1.getEnumValue();
                    FieldVo fieldVo = new FieldVo();
                    fieldVo.setName(name);
                    fieldVo.setFieldType(1);
                    fieldVo.setType(1);
                    if(type.contains("文字")){
                        fieldVo.setFieldType(1);
                    } else if (type.contains("单选")) {
                        fieldVo.setFieldType(2);
                    }else if (type.contains("复选")||type.contains("多选")) {
                        fieldVo.setFieldType(3);
                    }else if(type.contains("图片")){
                        fieldVo.setFieldType(4);
                    }else if(type.contains("音频")) {
                        fieldVo.setFieldType(5);
                    }else if(type.contains("日期")) {
                        fieldVo.setFieldType(6);
                    }else if(type.contains("位置")) {
                        fieldVo.setFieldType(7);
                    }
                    fieldVo.setRequired(0);
                    fieldVo.setVal(value);
                    ActivityField f = wenXinYiFanService.getCustomField(fieldVo,uid);
                    fieldVos.add(f);
                }
            }

            System.out.println(fieldVos);
            Gson gson = new Gson();
            String fieldvos = gson.toJson(fieldVos);
            model.setField(fieldvos);
            list.add(model);
        }
        return list;
    }
    @Inner(value = false)
    @RequestMapping("/callBack")
    public void callBack(@RequestBody String data){

        JSONObject jsonObject = new JSONObject(data);

        String vidStr = jsonObject.get("audioId")!=null?jsonObject.get("audioId").toString():null;
        if (StringUtils.isNotEmpty(vidStr)){
            Integer vid = Integer.valueOf(vidStr);
            if (vid!=null){
                ActivityValue value =  valueService.getById(vid);
                if (value!=null){
                    String state = jsonObject.get("conclusionType")!=null?jsonObject.get("conclusionType").toString():null;
                    if ("1".equals(state)){
                        value.setAuditState(1);
                    }else {
                        //审核失败 音频清空并删除文件
                        value.setAuditState(2);
                        String filePath = value.getValue().replaceAll(saveResultPath, savePath);
                        //删除文件
                        File localFile = new File(filePath);
                        if (localFile.exists() && localFile.isFile()) {
                            localFile.delete();
                        }
                        value.setValue("");
                    }
                    valueService.updateById(value);
                }
            }
        }
    }

    @Inner(value = false)
    @GetMapping("/updateRedis")
    public R updateWxtokenRedis(){
        String token = redisHelper.get("wxAccessToken")!=null? redisHelper.get("wxAccessToken").toString() : "";
        if (StringUtils.isNotEmpty(token)){
           redisHelper.del("wxAccessToken");
        }
        return R.ok();
    }

    @Inner(value = false)
    @GetMapping("/chat")
    public R chat( String data){
       //去空格
        //data = data.replaceAll(" ","").replaceAll("\n","");
        String result = wenXinYiFanService.chat(data);
        return R.ok(result);
    }
    @Inner(value = false)
    @GetMapping("/jctemplate")
    public R jctemplate( String data){
       //去空格
        //data = data.replaceAll(" ","").replaceAll("\n","");
        /*String result = wenXinYiFanService.template(data,jcid);
        String res = "0";
        if(StringUtils.isNotEmpty(result)){
            //获取result的第一个字
            String str = result.substring(0,1);
            if(StringUtils.isNotEmpty(str)&&str.equals("是")){
                res = "1";
            }
        }
        return R.ok(res);*/
        return R.ok("1");
    }
    @Inner(value = false)
    @GetMapping("/template")
    public R template( String data, String id){
        //去空格
        //data = data.replaceAll(" ","").replaceAll("\n","");
        String result = wenXinYiFanService.template(data,id);
        String res = "0";
        if(StringUtils.isNotEmpty(result)){
            //获取result的第一个字
            String str = result.substring(0,1);
            if(StringUtils.isNotEmpty(str)&&str.equals("是")){
                res = "1";
            }
        }
        return R.ok(res);
        //return R.ok("1");
    }
    @Inner(value = false)
    @GetMapping("/cjtemplate")
    public R cjtemplate( String data){
        Integer uid=super.getCurrentId();
        if (uid == null) {
            return R.failed().setCode(401);
        }
        Users user = userService.getById(uid);
        if(user!=null&&user.getIntegral()!=null&&user.getIntegral()>=1) {
            user.setIntegral(user.getIntegral() - 1);
            userService.updateById(user);
            LocalDateTime now = LocalDateTime.now();
            UsersIntegral usersIntegral = new UsersIntegral();
            usersIntegral.setUid(uid.toString());
            usersIntegral.setCreateTime(now);
            usersIntegral.setUpdateTime(now);
            usersIntegral.setAllIntegral(user.getIntegral().toString());//总积分
            usersIntegral.setMessage("调用百度文心一帆，使用1积分");//记录
            usersIntegral.setIntegral("1");//分值
            usersIntegral.setType("1");//类型
            usersIntegral.setOperator("0");//0减分
            usersIntegralService.save(usersIntegral);
            //data = data.replaceAll(" ","").replaceAll("\n","");
           /* String content = wenXinYiFanService.template(data,jqcjid);
            String result = wenXinYiFanService.chat(content);
            String content1 = wenXinYiFanService.template(result,zlid);
            String result1 = wenXinYiFanService.chat(content);*/
            String content1 = cjmb+data+hd;
            //System.out.println("文心千帆输入："+content1);
            String result1 = wenXinYiFanService.chat(content1);
            //System.out.println("文心千帆回复："+result1);
            String regex = "```json(.*?)```";
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(result1);
            String result2 = "";
            if (matcher.find()) {
                result2 = matcher.group(1);
                //System.out.println(result);
            }else if(isJson(result1)){
                result2 = result1;
            }
            System.out.println("文心千帆回复："+result2);
            List<ActivityTemplateModel> list = new ArrayList<>();
            if(StringUtils.isNotEmpty(result2)){
                list =Jsformate(result2,0);
            }
            return R.ok(list);
        }
        return R.ok();
    }
    @Inner(value = false)
    @GetMapping("/fctemplate")
    public R fctemplate( String data){
        if(StringUtils.isEmpty(data)){
            return R.failed("参数不能为空");
        }
        String cjData = new String();
        Integer uid=super.getCurrentId();
        if (uid == null) {
            return R.failed().setCode(401);
        }
        Users user = userService.getById(uid);
        if(user!=null&&user.getIntegral()!=null&&user.getIntegral()>=1) {
            user.setIntegral(user.getIntegral() - 1);
            userService.updateById(user);
            LocalDateTime now = LocalDateTime.now();
            UsersIntegral usersIntegral = new UsersIntegral();
            usersIntegral.setUid(uid.toString());
            usersIntegral.setCreateTime(now);
            usersIntegral.setUpdateTime(now);
            usersIntegral.setAllIntegral(user.getIntegral().toString());//总积分
            usersIntegral.setMessage("调用百度文心一帆，使用1积分");//记录
            usersIntegral.setIntegral("1");//分值
            usersIntegral.setType("1");//类型
            usersIntegral.setOperator("0");//0减分
            usersIntegralService.save(usersIntegral);
            //去空格和换行。
            //data = data.replaceAll(" ","").replaceAll("\n","");
            //String content = wenXinYiFanService.template(data,fcid);
            String content = wenXinYiFanService.template(data,jzfcid);
            String result = wenXinYiFanService.chat(content);
            String templateStr = result;
            List<ActivityTemplateModel> list = new ArrayList<>();
            Set<ActivityTemplateModel> set = new HashSet<>();
            if (StringUtils.isNotEmpty(templateStr)) {
                /*if (templateStr.indexOf(":") > 0) {
                    templateStr = templateStr.substring(templateStr.indexOf(":") + 1, templateStr.length());
                }
                if (templateStr.indexOf("：") > 0) {
                    templateStr = templateStr.substring(templateStr.indexOf("：") + 1, templateStr.length());
                }*/
                //去空格去换行
                templateStr = templateStr.replaceAll(" ", "").replaceAll("\n", "");
                //截取中括号内容
                templateStr = templateStr.substring(templateStr.indexOf("[") + 1, templateStr.indexOf("]"));
                //"标题":"上下班记录打卡时间","内容":"打卡,记录打卡时间"  取标题后的装引号内字符串
                cjData = templateStr.substring(templateStr.indexOf("标题") + 5, templateStr.indexOf("内容") - 3);
                templateStr = templateStr.substring(templateStr.indexOf("内容") + 5, templateStr.length()-1);
                String[] arr = null;
                if (templateStr.indexOf(",") > 0) {
                    arr = templateStr.split(",");
                }else if (templateStr.indexOf("，") > 0) {
                    arr = templateStr.split("，");
                }
                if (arr != null && arr.length > 0) {
                    for (String s : arr) {
                        if (StringUtils.isNotEmpty(s)) {
                            ActivityTemplateModel model = new ActivityTemplateModel();
                            model.setTitle(s);
                            List<ActivityTemplateModel> modelist = templateModelService.lambdaQuery().like(ActivityTemplateModel::getTitle, s).page(new Page<>(1, 10)).getRecords();
                           /* List<ActivityTemplateModel> modelist = templateModelService
                                    .lambdaQuery()
                                    .like(ActivityTemplateModel::getTitle,s)
                                    .page(new Page<>(1, 10)).getRecords();*/
                            list.addAll(modelist);
                        }
                    }
                    //list 按id去重
                    list = list.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ActivityTemplateModel::getId))), ArrayList::new));
                    //list 随机排序
                    Collections.shuffle(list);
                    list.stream().limit(3).forEach(set::add);
                }
            }
            List<ActivityTemplateModel> list1 = new ArrayList<>();
            if(set.size()==0){
                if(StringUtils.isEmpty(cjData)&&StringUtils.isNotEmpty(templateStr)){
                    cjData = templateStr;
                }
                //String content1 = wenXinYiFanService.template(cjData,jqcjid);
                String content1 = cjmb+cjData+hd;
                System.out.println("文心千帆输入："+content1);
                String result1 = wenXinYiFanService.chat(content1);
                //System.out.println("文心千帆回复："+result1);
                String regex = "```json(.*?)```";
                Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(result1);
                String result2 = "";
                if (matcher.find()) {
                    result2 = matcher.group(1);
                    System.out.println(result);
                }else if(isJson(result1)){
                    result2 = result1;
                }
                System.out.println("文心千帆回复："+result2);
                if(StringUtils.isNotEmpty(result2)){
                    list1 =Jsformate(result2,0);
                }
            }
            Map<String, Object> map = new HashMap<>();
            map.put("AIresult", result);
            map.put("template", set);
            map.put("templateModel", list1);
            return R.ok(map);
        }
        return R.ok("积分不足");
    }
    public static boolean isJson(String str) {
        try {
            new JSONObject(str);
        } catch (JSONException ex) {
            try {
                new JSONArray(str);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
    /*public static void main(String[] args) {
        String input = "```json\n" +
                "{\"title\":\"运动会签到情况\",\"description\":\"该模板用于记录运动会签到情况，包括签到时间、地点、运动员姓名等信息。\",\"use\":\"用于统计运动会签到情况，方便后续数据分析和统计。\",\"fieldList\":[{\"name\":\"签到时间\",\"type\":\"日期\",\"enumValue\":\"上午,下午\"},{\"name\":\"签到地点\",\"type\":\"位置\",\"enumValue\":\"\"},{\"name\":\"运动员姓名\",\"type\":\"文字\",\"enumValue\":\"\"}]}" +
                "]```";
            String regex = "```json(.*?)```";
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                String result = matcher.group(1);
                System.out.println(result);
            }
        }*/

}
