package com.brainspace.hyperland.service;

import com.brainspace.hyperland.bo.RestResponse;

import java.util.List;
import java.util.Map;

public interface ITransactionService {
    RestResponse createBooking(Object requestObject,String createdBy);
    public RestResponse createPayment(Map paymentMap, String type,String createdBy);
    public RestResponse approvePayment(String id,String type,String approvedBy,String isApproved);
    public RestResponse updateTransaction(Map restRequest,String type,String id,String createdBy);
    public RestResponse generateRecieptNumber();
    public RestResponse generatePrintReceipt(Integer bookingId, Integer paymentId) ;
    public RestResponse getAllChequeEntries();
    public RestResponse udpateChequeEntry(String id);
    RestResponse createDailyExpense(Map paymentMap, String type, String createdBy) ;
    RestResponse  cancelProperty(int firmId,String firmName, int bookingId,String paymentMode, String customerName,Double paidAmount,int projectId, int blockId, int plotNumber) ;
    RestResponse getRewards(String agentId);
    RestResponse updateRewards(String agentId,String rewardId,String issuedBy,String rewardOpted);
}
