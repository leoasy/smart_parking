package com.ruoyi.biz.controller;

import com.ruoyi.biz.service.IAiEventService;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/biz")
public class DashBoardController {

    @Autowired
    private IAiEventService aiEventService;

    @GetMapping("/dashboard")
    public AjaxResult dashboard() {
        return AjaxResult.success(aiEventService.dashboard());
    }
}
