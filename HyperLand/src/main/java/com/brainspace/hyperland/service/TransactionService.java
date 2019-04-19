package com.brainspace.hyperland.service;

import com.brainspace.hyperland.bo.*;
import com.brainspace.hyperland.dao.ITransactionDAO;
import com.brainspace.hyperland.utils.ConfigReader;
import com.brainspace.hyperland.utils.ServiceUtils;
import com.brainspace.hyperland.utils.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class TransactionService implements ITransactionService {

    @Autowired
    private ITransactionDAO transactionDAO;

    @Override
    public RestResponse createBooking(Object requestObject, String createdBy) {
        List<Map> installmentList = null;
        ConfigBO configBO = ConfigReader.getConfig();
        Transactions transactions = configBO.getTransactions();
        String insertQuery;
        Map<String, Object> mainObject = (Map) requestObject;
        Map<String, Object> customerDetails = (Map<String, Object>) mainObject.get("customerDetails");
        Map<String, Object> bookingDetails = new HashMap<>();
        //insert blank data in booking details to get booking id;
        String bookingQuery = "INSERT INTO BookingDetails(receiptNo,CreatedOn) values (?,SYSDATE()) ";
        BigInteger bookingId = (BigInteger) transactionDAO.getBookingId(bookingQuery, new Object[]{mainObject.get("receiptNo")}, new int[]{Types.VARCHAR});

        for (int i = 0; i < transactions.getTransaction().length; i++) {
            PropertyMapping propertyMapping = transactions.getTransaction()[i].getPropertyMapping();
            Property property[] = propertyMapping.getProperty();
            ServiceUtils serviceUtils = new ServiceUtils();
            Map<String, List> jsonColumnMap = serviceUtils.jsonColumnNameMapper(property);
            if (transactions.getTransaction()[i].getId().equalsIgnoreCase("booking")) {
                String updateQuery = transactions.getTransaction()[i].getUpdateQuery();
                bookingDetails.put("firmId",mainObject.get("firmId"));
                bookingDetails.put("firmName",mainObject.get("firmName"));
                bookingDetails.put("projectId",mainObject.get("projectId"));
                bookingDetails.put("projectName",mainObject.get("projectName"));
                bookingDetails.put("propertyTypeId",mainObject.get("propertyTypeId"));
                bookingDetails.put("propertyType",mainObject.get("propertyType"));
                bookingDetails.put("blockId",mainObject.get("blockId"));
                bookingDetails.put("block",mainObject.get("block"));
                bookingDetails.put("agentId",mainObject.get("agentId"));
                bookingDetails.put("plotNumber",mainObject.get("plotNumber"));
                bookingDetails.put("paymentType",mainObject.get("paymentType"));
                bookingDetails.put("bookingAmount",mainObject.get("bookingAmount"));
                bookingDetails.put("bookingDate",mainObject.get("bookingDate"));
                bookingDetails.put("baseSellingPrice",mainObject.get("baseSellingPrice"));
                bookingDetails.put("plcChargesType",mainObject.get("plcChargesType"));
                bookingDetails.put("plcCharges",mainObject.get("plcCharges"));
                bookingDetails.put("bookingType",mainObject.get("bookingType"));
                bookingDetails.put("numberOfInstallment",mainObject.get("numberOfInstallment"));
                bookingDetails.put("additionalCharges",mainObject.get("additionalCharges"));
                bookingDetails.put("installmentStartDate",mainObject.get("installmentStartDate"));
                bookingDetails.put("totalAmount",mainObject.get("totalAmount"));
                bookingDetails.put("discount",mainObject.get("discount"));
                bookingDetails.put("discountType",mainObject.get("discountType"));
                bookingDetails.put("customerId", bookingId + "_P_1");
                bookingDetails.put("bookingId", bookingId);
                bookingDetails.put("createdBy", createdBy);
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
            } else if (transactions.getTransaction()[i].getId().equalsIgnoreCase("payment")) {
                Map paymentDetails = new HashMap<>();

                insertQuery = transactions.getTransaction()[i].getInsertQuery();
                Map paymentMap = new HashMap();
                paymentMap.put("customerId", bookingId + "_P_1");
                paymentMap.put("bookingId", bookingId);
                paymentMap.put("paymentType", "Booking");
                paymentMap.put("collectedBy", createdBy);
                paymentMap.put("paymentMode", paymentDetails.get("paymentMode"));
                paymentMap.put("chequeNo", paymentDetails.get("chequeNo"));
                paymentMap.put("transactionId", paymentDetails.get("transactionId"));
                paymentMap.put("paymentDate", paymentDetails.get("paymentDate"));
                paymentMap.put("receiptNo", paymentDetails.get("receiptNo"));
                paymentMap.put("bankName", paymentDetails.get("bankName"));
                paymentMap.put("amount", bookingDetails.get("bookingAmount"));
                installmentList.add(serviceUtils.customerMap(paymentMap, jsonColumnMap));
                Double pendingAmount = 0.0;
                Double totalAmount = 0.00;

                if (bookingDetails.get("totalAmount") != null) {
                    totalAmount = Double.valueOf(bookingDetails.get("totalAmount").toString());
                }
                if (bookingDetails.get("bookingAmount") != null) {
                    Double bookingAmount = Double.valueOf(bookingDetails.get("bookingAmount").toString());
                    pendingAmount = totalAmount - bookingAmount;

                }

            }
            if (((String) bookingDetails.get("paymentType")).equalsIgnoreCase("installment")) {
                insertQuery = transactions.getTransaction()[i].getInsertQuery();
                String installlmentDueDate = (String) mainObject.get("installmentStartDate");
                DateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal = null;
                int numberOfInstallment = Integer.parseInt(bookingDetails.get("numberOfInstallment").toString());
                try {
                    Date dt = sourceDateFormat.parse(installlmentDueDate);
                    cal = Calendar.getInstance();
                    cal.setTime(dt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                installmentList = new ArrayList<>();
                Map installmentMap = new HashMap();
                Double installmentAmount = Double.valueOf(mainObject.get("installmentAmount").toString());
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
                    installmentList.add(serviceUtils.customerMap(installmentMap,jsonColumnMap));
                }
                TransactionUtils transactionUtils = new TransactionUtils();
                Double bookingAmount = Double.valueOf(bookingDetails.get("bookingAmount").toString());
                String particulars = bookingDetails.get("customerName") + " - " + bookingDetails.get("bookingAmount") + " - ";
                transactionUtils.addBalanceEntry(transactionDAO, (String) bookingDetails.get("paymentMode"), bookingAmount, particulars, null, createdBy);
                transactionDAO.insertDataBatch(insertQuery, installmentList);
            }
        }

        //add booking details
        //property details

        return null;
    }

    //used for Farmer and Agent Payment Entry
    public void createPayment(Map paymentMap, String type,String createdBy) {
        ConfigBO configBO = ConfigReader.getConfig();
        Transactions transactions = configBO.getTransactions();

        ServiceUtils serviceUtils = new ServiceUtils();

        for (int i = 0; i < transactions.getTransaction().length; i++) {
            Transaction transaction = transactions.getTransaction()[i];
            if (transaction.getId().equalsIgnoreCase(type)) {
                String sql = transaction.getInsertQuery();
                String params = sql.substring(sql.indexOf('(') + 1, sql.indexOf(')'));
                String paramsArr[] = params.split(",");
                PropertyMapping propertyMapping = transaction.getPropertyMapping();
                Property property[] = propertyMapping.getProperty();
                Map<String, List> jsonColumnMap = serviceUtils.propertyMapper(property);
                String value = "";
                Object arguments[] = new Object[paramsArr.length];
                int argumentTypes[] = new int[paramsArr.length];
                for (int j = 0; j < paramsArr.length; j++) {
                    System.out.println("paramsArr[j].trim() -- " + paramsArr[j].trim());
                    List jsonColTypeList = jsonColumnMap.get(paramsArr[j].trim());
                    Object colValue = paymentMap.get(jsonColTypeList.get(0));
                    arguments[j] = colValue;
                    argumentTypes[j] = Integer.parseInt((String) jsonColTypeList.get(1));
                    if(argumentTypes[j] == 93 && arguments[j]!=null )
                    {
                        Instant instant = Instant.parse((String)arguments[j]);
                        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Kolkata"));
                        arguments[j] = new java.sql.Date(Date.from(zonedDateTime.toInstant()).getTime());
                    }
                    if(paramsArr[j].trim().equalsIgnoreCase("CreatedBy")) {
                        arguments[j] = createdBy;
                    }
                    else if(paramsArr[j].trim().equalsIgnoreCase("CreatedOn"))
                    {
                        java.sql.Date currentDate = new java.sql.Date(new Date().getTime());
                        arguments[j] =  currentDate;
                    }



                }
                transactionDAO.addData(sql, arguments, argumentTypes); // add entry in table
                //In case of farmer payment update paid amount in LandMaster
                if (type.equalsIgnoreCase("farmerPayment")) {
                    Double paidAmount = Double.valueOf(paymentMap.get("paymentAmount").toString());
                    String updateQuery = "UPDATE LandMaster SET PaidAmount = CASE WHEN PaidAmount = 0.00  THEN "+paidAmount+"  ELSE PaidAmount + " + paidAmount + " END WHERE Id = " + paymentMap.get("landId");
                    String updateFarmerMasterQuery = "UPDATE FarmerMaster SET PaidAmount = CASE WHEN PaidAmount = 0.00  THEN "+paidAmount+"  ELSE PaidAmount + " + paidAmount + " END WHERE Id = " + paymentMap.get("farmerId");
                     transactionDAO.insertDataBatch(new String[]{updateQuery,updateFarmerMasterQuery}) ;
                }
                else if (type.equalsIgnoreCase("agent")) {
                    Double paidAmount = Double.valueOf(paymentMap.get("paymentAmount").toString());
                    String updateQuery = " UPDATE AgentMaster SET PaidAmount = CASE WHEN PaidAmount = 0.00 THEN "+paidAmount+"  ELSE PaidAmount " + paidAmount + " END WHERE Id = " + paymentMap.get("agentId");
                    transactionDAO.updateData(updateQuery);
                }


            }

        }

    }


    //used for Daily Expense / Farmer Payment / Agent Payment Approval / Property Cancellation Payment - > make and entry in Day book Entry table
     public void approvePayment(String id,String type,String approvedBy) {
         ConfigBO configBO = ConfigReader.getConfig();
         Transactions transactions = configBO.getTransactions();
         String updateQuery;
         for (Transaction transaction: transactions.getTransaction()) {
             if(transaction.getId().equalsIgnoreCase(type)){
                 updateQuery = transaction.getUpdateQuery();
                 updateQuery = updateQuery.replace("{1}","'"+approvedBy+"'");
                 updateQuery = updateQuery.replace("{2}",id);
                 transactionDAO.updateData(updateQuery);
                 // after payment make an entry in DaybookEntry table

             }
         }
    }

    // cancel property, update IScancelled = 'Y' in BookingDetails table
    public void cancelProperty(String bookingId)
    {

    }

}
