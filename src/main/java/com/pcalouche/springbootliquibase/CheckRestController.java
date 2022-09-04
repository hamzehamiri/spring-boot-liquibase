package com.pcalouche.springbootliquibase;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CheckRestController {

    @GetMapping("/userinfo")
    public String user() {
        return "Test";
    }
}
