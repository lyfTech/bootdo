package com.bootdo.inverst.controller;


import com.bootdo.inverst.service.YatangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "inverst/yatang")
public class YatangController {

    @Autowired
    YatangService yatangService;


    @GetMapping(value = "monthInverst")
    String gotoYatang(){
        return "inverst/yatang/index";
    }

}
