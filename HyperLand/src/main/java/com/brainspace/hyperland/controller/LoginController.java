package com.brainspace.hyperland.controller;

import com.brainspace.hyperland.bo.RestResponse;
import com.brainspace.hyperland.service.IMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {
    @Autowired
    private IMasterService masterService;
    @GetMapping(value = "/get/roleMenuConfig")
    public ResponseEntity<RestResponse> getRoleMenuConfig() {
        RestResponse response = masterService.getRoleMenuConfig();
        return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
    }
    @GetMapping(value = "/get/menuConfig")
    public ResponseEntity<RestResponse> getMenuConfig() {
        RestResponse response = masterService.getMenuConfig();
        return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
    }
}

