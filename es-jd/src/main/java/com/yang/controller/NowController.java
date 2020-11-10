package com.yang.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NowController {

    @GetMapping
    public String index(){
        return "index";
    }

}
