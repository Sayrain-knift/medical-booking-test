package com.sayrain.medicalbooking.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel配置类
 * 提供流控规则和降级规则的初始化配置
 */
@Configuration
public class SentinelConfig {

    /**
     * 注入Sentinel资源切面，启用@SentinelResource注解支持
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    /**
     * 初始化流控规则
     */
    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 认证相关接口流控规则
        FlowRule loginRule = new FlowRule();
        loginRule.setResource("login");
        loginRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        loginRule.setCount(10); // 每秒最多10次登录请求
        rules.add(loginRule);

        FlowRule registerRule = new FlowRule();
        registerRule.setResource("register");
        registerRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        registerRule.setCount(5); // 每秒最多5次注册请求
        rules.add(registerRule);

        // 预约相关接口流控规则
        FlowRule createAppointmentRule = new FlowRule();
        createAppointmentRule.setResource("createAppointment");
        createAppointmentRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        createAppointmentRule.setCount(20); // 每秒最多20次创建预约请求
        rules.add(createAppointmentRule);

        FlowRule getAppointmentRule = new FlowRule();
        getAppointmentRule.setResource("getAppointments");
        getAppointmentRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        getAppointmentRule.setCount(50); // 每秒最多50次查询预约请求
        rules.add(getAppointmentRule);

        // 医生查询接口流控规则
        FlowRule getDoctorsRule = new FlowRule();
        getDoctorsRule.setResource("getDoctors");
        getDoctorsRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        getDoctorsRule.setCount(30); // 每秒最多30次查询医生请求
        rules.add(getDoctorsRule);

        // AI咨询接口流控规则（相对保守，因为AI调用较重）
        FlowRule aiChatRule = new FlowRule();
        aiChatRule.setResource("aiChat");
        aiChatRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        aiChatRule.setCount(5); // 每秒最多5次AI咨询请求
        rules.add(aiChatRule);

        FlowRuleManager.loadRules(rules);
    }

    /**
     * 初始化降级规则
     */
    @PostConstruct
    public void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // AI咨询接口降级规则
        DegradeRule aiChatDegradeRule = new DegradeRule();
        aiChatDegradeRule.setResource("aiChat");
        aiChatDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        aiChatDegradeRule.setCount(3000); // RT超过3秒触发降级
        aiChatDegradeRule.setTimeWindow(10); // 降级时间窗口10秒
        aiChatDegradeRule.setMinRequestAmount(5); // 最小请求数
        aiChatDegradeRule.setSlowRatioThreshold(1.0); // 慢调用比例阈值
        rules.add(aiChatDegradeRule);

        // 预约创建接口降级规则
        DegradeRule createAppointmentDegradeRule = new DegradeRule();
        createAppointmentDegradeRule.setResource("createAppointment");
        createAppointmentDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        createAppointmentDegradeRule.setCount(0.5); // 异常比例超过50%触发降级
        createAppointmentDegradeRule.setTimeWindow(30); // 降级时间窗口30秒
        createAppointmentDegradeRule.setMinRequestAmount(10); // 最小请求数
        rules.add(createAppointmentDegradeRule);

        // 预约查询接口降级规则
        DegradeRule getAppointmentDegradeRule = new DegradeRule();
        getAppointmentDegradeRule.setResource("getAppointments");
        getAppointmentDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        getAppointmentDegradeRule.setCount(2000); // RT超过2秒触发降级
        getAppointmentDegradeRule.setTimeWindow(15); // 降级时间窗口15秒
        getAppointmentDegradeRule.setMinRequestAmount(10); // 最小请求数
        getAppointmentDegradeRule.setSlowRatioThreshold(1.0); // 慢调用比例阈值
        rules.add(getAppointmentDegradeRule);

        DegradeRuleManager.loadRules(rules);
    }
}
