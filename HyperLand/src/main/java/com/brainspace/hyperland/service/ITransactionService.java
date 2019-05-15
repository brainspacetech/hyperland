package com.brainspace.hyperland.service;

import com.brainspace.hyperland.bo.RestResponse;

import java.util.List;
import java.util.Map;

public interface ITransactionService {
    RestResponse createBooking(Object requestObject,String createdBy);
    public RestResponse createPayment(Map paymentMap, String type,String createdBy);
    public RestResponse approvePayment(String id,String type,String approvedBy);
    public RestResponse updateTransaction(Map restRequest,String type,String id,String createdBy);
    public RestResponse generateRecieptNumber();
    public RestResponse generatePrintReceipt(Integer bookingId, Integer paymentId) ;
    public RestResponse getAllChequeEntries();
    public RestResponse udpateChequeEntry(String id);

}
