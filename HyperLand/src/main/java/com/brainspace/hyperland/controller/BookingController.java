package com.brainspace.hyperland.controller;

import com.brainspace.hyperland.bo.RestResponse;
import com.brainspace.hyperland.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
public class BookingController {
    @Autowired
    private ITransactionService transactionService;

    @RequestMapping(value = "/newBook", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> createBooking(@RequestBody Object restRequest) {
        System.out.println(restRequest);
        String createdBy = "";
        transactionService.createBooking(restRequest,createdBy);
        return null;
    }

    @RequestMapping(value = "/{value}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> payment(@RequestBody Object restRequest,@PathVariable(name="value") String type) {
        System.out.println(restRequest);
        String createdBy = "";
        transactionService.makePayment((Map) restRequest,type,createdBy);
        return null;
    }

}
