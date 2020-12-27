package com.zkdlu.circuitbreaker;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OtherController {
    @GetMapping("/test")
    public String test(String key) {
        if (!StringUtils.equals("test", key)) {
            throw new RuntimeException("Invalid Key");
        }

        return "OK";
    }
}
