package com.brainspace.hyperland.controller;

import com.brainspace.hyperland.bo.RestResponse;
import com.brainspace.hyperland.service.IMasterService;
import com.brainspace.hyperland.service.ITransactionService;
import com.brainspace.hyperland.utils.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
        String createdBy = new ServiceUtils().getUserName();
        RestResponse  restResponse = transactionService.createBooking(restRequest,createdBy);
        if(restResponse.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(restResponse, HttpStatus.OK);
        else
            return  new ResponseEntity<RestResponse>(restResponse, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(value = "/approve/{type}/{id}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> approvePayment(@PathVariable(name="id") String id,@PathVariable(name="type") String type,@RequestBody Object restRequest) {
        Map<String,Object> params = (Map<String,Object>)restRequest;
        String isApprovedOn = (String)params.get("isApproved");
        String approvedBy = new ServiceUtils().getUserName();
        RestResponse response = transactionService.approvePayment(id,type,approvedBy,isApprovedOn);
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return  new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(value = "/add/{type}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> createTransaction(@RequestBody Object restRequest,@PathVariable(name="type") String type) {
        System.out.println(restRequest);
        String createdBy = new ServiceUtils().getUserName();
        RestResponse response = transactionService.createPayment((Map)restRequest,type,createdBy);
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return  new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(value = "/update/{type}/{id}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> updateTransaction(@RequestBody Object restRequest,@PathVariable(name="type") String type,@PathVariable(name="id") String id) {
        System.out.println(restRequest);
        String createdBy =new ServiceUtils().getUserName();
        RestResponse response = transactionService.updateTransaction((Map)restRequest,type,id,createdBy);
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return  new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(value = "/get/receiptNumber", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<RestResponse> generateRecieptNumber()
    {
        return new ResponseEntity<RestResponse>(transactionService.generateRecieptNumber(), HttpStatus.OK);
    }

    @RequestMapping(value = "/get/printReceipt", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> generateReciept(@RequestBody Object restRequest)
    {
        String bookingId= ((Map)restRequest).get("bookingId").toString();
        Integer bookingIdInt = bookingId!=null?Integer.parseInt(bookingId):null;

        Object paymentId= ((Map)restRequest).get("paymentId");

        Integer paymentIdInt = paymentId!=null?Integer.parseInt(paymentId.toString()):null;
        RestResponse response = transactionService.generatePrintReceipt(bookingIdInt,paymentIdInt);
        if(response.getStatusCode().equalsIgnoreCase("1")) {
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/update/cheque/{id}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> udpateChequeEntry(@PathVariable(name="id") String id)
    {
        RestResponse response = transactionService.udpateChequeEntry(id);
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(value = "/get/chequeEntry", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<RestResponse> getAllChequeEntry()
    {
        RestResponse response = transactionService.getAllChequeEntries();
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @RequestMapping(value = "/create/{type}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> createDailyExpense(@RequestBody Object restRequest,@PathVariable(name="type") String type) {

        String createdBy = new ServiceUtils().getUserName();
        RestResponse response = transactionService.createDailyExpense((Map)restRequest,type,createdBy);
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return  new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(value = "/cancelProperty", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> cancelProperty(@RequestBody Object restRequest) {
        Map requestParams = (Map) restRequest;
        int firmId = Integer.parseInt(requestParams.get("firmId").toString());
        String firmName = requestParams.get("firmName").toString();
        String customerName = requestParams.get("customerName").toString();
        int bookingId = Integer.parseInt(requestParams.get("bookingId").toString());
        String paymentMode = requestParams.get("paymentMode").toString();
        Double paidAmount = Double.parseDouble(requestParams.get("paidAmount").toString());
        int projectId = Integer.parseInt(requestParams.get("projectId").toString());
        int blockId = Integer.parseInt(requestParams.get("blockId").toString());
        int plotNumber = Integer.parseInt(requestParams.get("plotNumber").toString());
         RestResponse response = transactionService.cancelProperty(firmId, firmName, bookingId, paymentMode, customerName, paidAmount, projectId, blockId, plotNumber);
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return  new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(value = "/get/reward", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> getRewards(@RequestBody Object restRequest)
    {
        Integer agentId = null;
        if(restRequest!=null){
           Map requestMap = (Map) restRequest;
            agentId = requestMap.get("AgentId")!=null ? (Integer)requestMap.get("AgentId"):null;
        }
        RestResponse response = transactionService.getRewards(agentId);
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(value = "/update/reward", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> updateRewards(@RequestBody Object restRequest)
    {
        String agentId = null;
        String rewardId = null;
        Map requestMap = (Map) restRequest;
        String issuedBy = new ServiceUtils().getUserName();

        agentId = requestMap.get("agentId")!=null ? (String)requestMap.get("agentId"):null;
        rewardId = requestMap.get("rewardId")!=null ? (String)requestMap.get("rewardId"):null;
       String rewardOpted = requestMap.get("rewardOpted")!=null ? (String)requestMap.get("rewardOpted"):null;

        RestResponse response = transactionService.updateRewards(agentId,rewardId,issuedBy,rewardOpted);
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(value = "/hold/property", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> holdProperty(@RequestBody Object restRequest)
    {

        Map requestMap = (Map) restRequest;
        List plotIdList = (List)requestMap.get("plotId");
        String userId = new ServiceUtils().getUserName();
        RestResponse response = transactionService.holdProperty(plotIdList,userId);
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(value = "/unhold/property", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<RestResponse> unHoldProperty(@RequestBody Object restRequest)
    {

        Map requestMap = (Map) restRequest;
        List plotIdList = (List)requestMap.get("plotId");
        RestResponse response = transactionService.unHoldProperty(plotIdList);
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);

    }


}
