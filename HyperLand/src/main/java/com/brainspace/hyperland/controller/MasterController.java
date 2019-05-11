package com.brainspace.hyperland.controller;

import com.brainspace.hyperland.service.IMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.brainspace.hyperland.bo.RestResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.annotation.MultipartConfig;
import java.util.Map;


@RestController
@RequestMapping("/master")
public class MasterController {
    @Autowired
    private IMasterService masterService;

    @RequestMapping(value = "/getAll/{type}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<RestResponse> getAll(@PathVariable(name = "type") String type) {
        RestResponse response = null;
        try {
            response = masterService.getAllData(type);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
    }



    @PostMapping(value = "/add/{type}")
    public ResponseEntity<RestResponse> add(@PathVariable(name = "type") String type,@RequestBody Object request) {
        RestResponse response = masterService.addData(type,request,"");
        return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
    }



    @PostMapping(value = "/create/Firm")

    public ResponseEntity<RestResponse> createFirm(@RequestParam("logoFile") MultipartFile logoFile) {
     //   String firmName = (String) ((Map)requestBody).get("firmName");
        System.out.println(logoFile);
       // System.out.println(firmName);
     //   RestResponse response = masterService.addData(type,request,"");
       // return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        return null;
    }

    @PostMapping(value = "/addLand")
    public ResponseEntity<RestResponse> addLand(@RequestBody Object request) {
        RestResponse response = masterService.addLandData(request,"");
        return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/add/menuConfig")
    public ResponseEntity<RestResponse> addMenuConfig(@RequestBody Object request) {
        RestResponse response = masterService.addMenuConfig((String) ((Map)request).get("menuConfig"));
        return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
    }


    @GetMapping(value = "/get/{type}/{id}")
    public ResponseEntity<RestResponse> get(@PathVariable(name = "type") String type,@PathVariable("id") int id) {
        RestResponse response = masterService.getDataById(type,id);
        return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/update/{type}/{id}")
    public ResponseEntity<RestResponse> update(@PathVariable(name = "type") String type,@PathVariable("id") int id,@RequestBody Object request) {
        RestResponse response = masterService.updateData(type,id, request);
        return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
    }
    @PostMapping(value = "/delete/{type}/{id}")
    public ResponseEntity<RestResponse> delete(@PathVariable(name = "type") String type,@PathVariable("id") int id) {
        RestResponse response = masterService.deleteData(type,id);
        return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
    }
    @GetMapping(value = "/getLand/{id}")
    public ResponseEntity<RestResponse> getLand(@PathVariable("id") int id) {
        RestResponse response = masterService.getLandDataById("land",id);
        return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/add/roleMenuConfig")
    public ResponseEntity<RestResponse> addRoleMenuConfig(@RequestBody Object request) {
        RestResponse response = masterService.addRoleMenuConfig((String) ((Map)request).get("roleMenuConfig"),(String)((Map)request).get("role"));
        return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
    }


}
