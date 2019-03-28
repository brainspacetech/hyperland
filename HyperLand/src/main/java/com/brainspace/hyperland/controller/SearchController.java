package com.brainspace.hyperland.controller;

import com.brainspace.hyperland.bo.RestResponse;
import com.brainspace.hyperland.service.ISearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {
    @Autowired
    private ISearchService searchService;

    @RequestMapping(value = "/{type}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> searchDailyExpense(@RequestBody Object restRequest, @PathVariable String type) {
        RestResponse response  =   searchService.searchObject((Map) restRequest,type);
        return  new ResponseEntity<RestResponse>(response, HttpStatus.OK);
    }

}
