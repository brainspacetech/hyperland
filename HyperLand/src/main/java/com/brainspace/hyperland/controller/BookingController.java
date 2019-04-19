package com.brainspace.hyperland.controller;

import com.brainspace.hyperland.bo.RestResponse;
import com.brainspace.hyperland.service.IMasterService;
import com.brainspace.hyperland.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/transaction")
public class BookingController {
    @Autowired
    private ITransactionService transactionService;
    @Autowired
    private IMasterService masterService;

    @RequestMapping(value = "/newBook", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> createBooking(@RequestBody Object restRequest) {
        System.out.println(restRequest);
        String createdBy = "";
        transactionService.createBooking(restRequest,createdBy);
        return null;
    }

    @RequestMapping(value = "/approve/{type}/{id}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> approvePayment(@PathVariable(name="id") String id,@PathVariable(name="type") String type) {
        String approvedBy = "Vijay";
        transactionService.approvePayment(id,type,approvedBy);
        return null;
    }

    @RequestMapping(value = "/add/{type}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> createTransaction(@RequestBody Object restRequest,@PathVariable(name="type") String type) {
        System.out.println(restRequest);
        String createdBy = "";
        transactionService.createPayment((Map)restRequest,type,createdBy);
        return null;
    }




}
