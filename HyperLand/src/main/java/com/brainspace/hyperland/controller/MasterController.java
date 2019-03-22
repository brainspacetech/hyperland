package com.brainspace.hyperland.controller;

import com.brainspace.hyperland.service.IMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.brainspace.hyperland.bo.RestResponse;


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
        RestResponse response = masterService.addData(type,request);
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
}
