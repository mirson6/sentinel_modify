package com.alibaba.csp.sentinel.dashboard.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>Description: </p>
 * @author Mirson 
 */
@RestController
@RequestMapping(value = "/test")
public class TestController {

    @ResponseBody
    @RequestMapping("/query")
    public String query(final String app) {
        return "test: " + app;
    }
}
