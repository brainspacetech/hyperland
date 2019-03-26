package com.brainspace.hyperland.service;

import com.brainspace.hyperland.bo.*;
import com.brainspace.hyperland.dao.ITransactionDAO;
import com.brainspace.hyperland.utils.ConfigReader;
import com.brainspace.hyperland.utils.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TransactionService implements ITransactionService {

    @Autowired
    private ITransactionDAO transactionDAO;

    @Override
    public RestResponse createBooking(Object requestObject) {
        List<Map> installmentList = null;
        ConfigBO configBO = ConfigReader.getConfig();
        Transactions transactions = configBO.getTransactions();
        String insertQuery;
        Map<String, Object> mainObject = (Map) requestObject;
        Map<String, Object> customerDetails = (Map<String, Object>) mainObject.get("customerDetails");
        Map<String, Object> bookingDetails = (Map<String, Object>) mainObject.get("bookingDetails");
        //insert blank data in booking details to get booking id;
        String bookingQuery = "INSERT INTO BookingDetails(CustomerId,CreatedOn) values (?,SYSDATE()) ";
        BigInteger bookingId = (BigInteger) transactionDAO.getBookingId(bookingQuery, new Object[]{customerDetails.get("customerId")}, new int[]{Types.VARCHAR});

        for (int i = 0; i < transactions.getTransaction().length; i++) {
            PropertyMapping propertyMapping = transactions.getTransaction()[i].getPropertyMapping();
            Property property[] = propertyMapping.getProperty();
            ServiceUtils serviceUtils = new ServiceUtils();
            Map<String, List> jsonColumnMap = serviceUtils.jsonColumnNameMapper(property);
            if (transactions.getTransaction()[i].getId().equalsIgnoreCase("booking")) {
                String updateQuery = transactions.getTransaction()[i].getUpdateQuery();

                bookingDetails.put("customerId", bookingId + "_P_1");
                bookingDetails.put("bookingId", bookingId);
                Double totalAmount = 0.00;
                if (bookingDetails.get("baseSellingPrice") != null) {
                    Double bsp = Double.valueOf(bookingDetails.get("baseSellingPrice").toString());
                    Double bspTax = bsp * 5 / 100;
                    bookingDetails.put("baseSellingPriceTax", bspTax);
                    totalAmount += bsp;
                    totalAmount += bspTax;
                }
                Double plcChargesAmount = 0.0;
                if (bookingDetails.get("plcCharges") != null && bookingDetails.get("plcChargesType") != null) {
                    String plcChargesType = (String) bookingDetails.get("plcChargesType");
                    Double plcCharges = Double.valueOf(bookingDetails.get("plcCharges").toString());
                    if (plcChargesType.equalsIgnoreCase("Percent")) {
                        Double bsp = Double.valueOf(bookingDetails.get("baseSellingPrice").toString());
                        plcChargesAmount = bsp * plcCharges / 100;
                    } else {
                        plcCharges = Double.valueOf(bookingDetails.get("plcCharges").toString());
                        plcChargesAmount = plcCharges;
                    }
                    totalAmount += plcChargesAmount;
                    Double plcChargesTax = plcChargesAmount * 5 / 100;
                    bookingDetails.put("plcChargesTax", plcChargesTax);
                    totalAmount += plcChargesTax;
                }
                if (bookingDetails.get("additionalCharges") != null) {
                    Double additionalCharges = Double.valueOf(bookingDetails.get("additionalCharges").toString());
                    totalAmount += additionalCharges;
                    Double additionalChargesTax = additionalCharges * 5 / 100;
                    bookingDetails.put("additionalChargesTax", additionalChargesTax);
                    totalAmount += additionalChargesTax;
                }
                bookingDetails.put("createdBy", "System");
                bookingDetails.put("totalAmount", totalAmount);
                Map bookingMap = serviceUtils.customerMap(bookingDetails, jsonColumnMap);
                transactionDAO.updateData(updateQuery, bookingMap, "BookingId");


            } else if (transactions.getTransaction()[i].getId().equalsIgnoreCase("customer")) {
                insertQuery = transactions.getTransaction()[i].getInsertQuery();
                List<Map> customerList = new ArrayList<>();
                customerDetails.put("customerId", bookingId + "_P_1");
                Map customerMap = serviceUtils.customerMap(customerDetails, jsonColumnMap);
                customerList.add(customerMap);
                if (mainObject.get("coApplicantDetails") != null) {
                    customerMap = new HashMap();
                    List coApplicantList = (List) mainObject.get("coApplicantDetails");
                    for (int count = 0; count < coApplicantList.size(); count++) {
                        Map coAppMap = (Map) coApplicantList.get(count);
                        coAppMap.put("customerId", bookingId + "_C_" + (count + 1));
                        customerMap = serviceUtils.customerMap(coAppMap, jsonColumnMap);
                        customerMap.put("customerId", bookingId + "_C_" + (count + 1));
                        customerList.add(customerMap);
                    }
                }
                transactionDAO.insertDataBatch(insertQuery, customerList);
            }

            else if (transactions.getTransaction()[i].getId().equalsIgnoreCase("payment")) {
                Map paymentDetails = (Map)mainObject.get("paymentDetails");
                installmentList = new ArrayList<>();
                insertQuery = transactions.getTransaction()[i].getInsertQuery();
                Map installmentMap = new HashMap();
                installmentMap.put("customerId", bookingId + "_P_1");
                installmentMap.put("bookingId", bookingId);
                installmentMap.put("status", "Paid");
                installmentMap.put("paymentType", "Booking");
                installmentMap.put("collectedBy"        , "66565"  );
                installmentMap.put("paymentMode"        , paymentDetails.get("paymentMode") );
                installmentMap.put("chequeNo"           , paymentDetails.get("chequeNo"));
                installmentMap.put("transactionId"      , paymentDetails.get("transactionId"));
                installmentMap.put("paymentDate"        , paymentDetails.get("paymentDate") );
                installmentMap.put("receiptNo"          , paymentDetails.get("receiptNo")   );
                installmentMap.put("bankName"           , paymentDetails.get("bankName") );
                installmentMap.put("installmentAmount"   , bookingDetails.get("bookingAmount") );
                installmentList.add(   serviceUtils.customerMap(installmentMap,jsonColumnMap));
                Double pendingAmount = 0.0;
                Double totalAmount = 0.00;

                if(bookingDetails.get("totalAmount") != null){
                    totalAmount = Double.valueOf(bookingDetails.get("totalAmount").toString());
                }
                if (bookingDetails.get("bookingAmount") != null) {
                    Double bookingAmount = Double.valueOf(bookingDetails.get("bookingAmount").toString());
                    pendingAmount = totalAmount - bookingAmount;

                }

                if (((String) bookingDetails.get("paymentType")).equalsIgnoreCase("Installment")) {
                    int numberOfInstallment = Integer.valueOf((String) bookingDetails.get("numberOfInstallment"));

                    Double installmentAmount = pendingAmount/numberOfInstallment;
                    String installlmentDueDate = (String)bookingDetails.get("installmentStartDate");
                    DateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar cal = null;
                    try {
                        Date dt = sourceDateFormat.parse(installlmentDueDate);
                        cal = Calendar.getInstance();
                        cal.setTime(dt);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
/*
                    for (int k = 0; k < numberOfInstallment; k++) {
                        installmentMap = new HashMap();
                        installmentMap.put("customerId", bookingId + "_P_1");
                        installmentMap.put("bookingId", bookingId);
                        installmentMap.put("installmentAmount", installmentAmount);
                        installmentMap.put("dueDate", installlmentDueDate);
                        installmentMap.put("status", "Pending");
                        installmentMap.put("paymentType", "Installment");
                        cal.add(Calendar.MONTH,1);
                        Date currentDatePlusOne = cal.getTime();
                        installlmentDueDate = sourceDateFormat.format(currentDatePlusOne);
                        installmentList.add(   serviceUtils.customerMap(installmentMap,jsonColumnMap));
                    }*/
                }
                transactionDAO.insertDataBatch(insertQuery, installmentList);
            }

        }

        //add booking details
        //property details

        return null;
    }
}