package com.brainspace.hyperland.service;

import com.brainspace.hyperland.bo.RestResponse;

import java.util.List;
import java.util.Map;

public interface ITransactionService {
    RestResponse createBooking(Object requestObject,String createdBy);
    public void createPayment(Map paymentMap, String type,String createdBy);
    public void approvePayment(String id,String type,String approvedBy);
    public void updateTransaction(Map restRequest,String type,String id,String createdBy);
    public RestResponse generateRecieptNumber();
}
