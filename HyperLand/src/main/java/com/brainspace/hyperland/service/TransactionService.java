package com.brainspace.hyperland.service;

import com.brainspace.hyperland.bo.*;
import com.brainspace.hyperland.dao.IMasterDAO;
import com.brainspace.hyperland.dao.ITransactionDAO;
import com.brainspace.hyperland.utils.ConfigReader;
import com.brainspace.hyperland.utils.PrintReceiptTemplate;
import com.brainspace.hyperland.utils.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
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
    @Autowired
    private IMasterDAO masterDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResponse createBooking(Object requestObject, String createdBy) {
        RestResponse restReponse = null;
        String statusCode = "";
        String statusMessage = "";
        BigInteger bookingId = null;
        try {
            List<Map> installmentList = new ArrayList<>();
            ConfigBO configBO = ConfigReader.getConfig();
            Transactions transactions = configBO.getTransactions();
            String insertQuery;
            Map<String, Object> mainObject = (Map) requestObject;
            Map<String, Object> customerDetails = (Map<String, Object>) mainObject.get("customerDetails");
            Map<String, Object> bookingDetails = new HashMap<>();
            //insert blank data in booking details to get booking id;
            String bookingQuery = "INSERT INTO BookingDetails(receiptNo,CreatedOn) values (?,SYSDATE()) ";
            bookingId = (BigInteger) transactionDAO.getBookingId(bookingQuery, new Object[]{mainObject.get("receiptNo")}, new int[]{Types.VARCHAR});

            for (int i = 0; i < transactions.getTransaction().length; i++) {
                PropertyMapping propertyMapping = transactions.getTransaction()[i].getPropertyMapping();
                Property property[] = propertyMapping.getProperty();
                ServiceUtils serviceUtils = new ServiceUtils();

                if (transactions.getTransaction()[i].getId().equalsIgnoreCase("booking")) {
                    Map<String, List> jsonColumnMap = serviceUtils.jsonColumnNameMapper(property);
                    String updateQuery = transactions.getTransaction()[i].getUpdateQuery();
                    bookingDetails.put("firmId", mainObject.get("firmId"));
                    bookingDetails.put("firmName", mainObject.get("firmName"));
                    bookingDetails.put("projectId", mainObject.get("projectId"));
                    bookingDetails.put("projectName", mainObject.get("projectName"));
                    bookingDetails.put("propertyTypeId", mainObject.get("propertyTypeId"));
                    bookingDetails.put("propertyType", mainObject.get("propertyType"));
                    bookingDetails.put("blockId", mainObject.get("blockId"));
                    bookingDetails.put("block", mainObject.get("block"));
                    bookingDetails.put("agentId", mainObject.get("agentId"));
                    bookingDetails.put("plotNumber", mainObject.get("plotNumber"));
                    bookingDetails.put("paymentType", mainObject.get("paymentType"));
                    bookingDetails.put("bookingAmount", mainObject.get("bookingAmount"));
                    bookingDetails.put("bookingDate", mainObject.get("bookingDate"));
                    bookingDetails.put("baseSellingPrice", mainObject.get("baseSellingPrice"));
                    bookingDetails.put("plcChargesType", mainObject.get("plcChargesType"));
                    bookingDetails.put("plcCharges", mainObject.get("plcCharges"));
                    bookingDetails.put("bookingType", mainObject.get("bookingType"));
                    bookingDetails.put("numberOfInstallment", mainObject.get("numberOfInstallment"));
                    bookingDetails.put("additionalCharges", mainObject.get("additionalCharges"));
                    bookingDetails.put("installmentStartDate", mainObject.get("installmentStartDate"));
                    bookingDetails.put("totalAmount", mainObject.get("totalAmount"));
                    bookingDetails.put("totalPaidAmount", mainObject.get("totalPaidAmount"));
                    bookingDetails.put("discount", mainObject.get("discount"));
                    bookingDetails.put("discountType", mainObject.get("discountType"));
                    bookingDetails.put("customerId", bookingId + "_P_1");
                    bookingDetails.put("bookingId", bookingId);
                    bookingDetails.put("createdBy", createdBy);

                    Map bookingMap = serviceUtils.customerMap(bookingDetails, jsonColumnMap);
                    transactionDAO.updateData(updateQuery, bookingMap, "BookingId");
                    //update plot details table
                    String updatePlotDetails = "UPDATE PlotDetails SET Status = 'Booked' WHERE FirmId = " + mainObject.get("firmId") + " and ProjectId = " + mainObject.get("projectId") + " and BlockId = " + mainObject.get("blockId") + " and PlotNo = " + mainObject.get("plotNumber");
                    //insert into Plot transaction
                    System.out.println("updatePlotDetails " + updatePlotDetails);
                    String insertPlotTransaction = "INSERT INTO PlotTransaction (PlotId,BookingId,BookedBy,BookedOn,AgentId) VALUES (" + mainObject.get("plotId") + "," + bookingId + ",'" + bookingId + "_P_1" + "',now()," + mainObject.get("agentId") + ")";
                    System.out.println("insertPlotTransaction  -- " + insertPlotTransaction);
                    transactionDAO.insertDataBatch(new String[]{updatePlotDetails, insertPlotTransaction});
                    //if token amount paid and booking amount is pending then insert into installment details along with installment details
                    if(((String) bookingDetails.get("paymentType")).equalsIgnoreCase("installment")  && ((String) mainObject.get("amountType")).equalsIgnoreCase("Token") )
                    {
                        Double totalAmount = Double.parseDouble(mainObject.get("totalAmount").toString());
                        Double bookingAmount = totalAmount * 25 / 100;
                        Double remainingBookingAmount = 0.00;
                        if (((String) mainObject.get("amountType")).equalsIgnoreCase("Token") ) {
                            remainingBookingAmount = bookingAmount - Double.parseDouble(mainObject.get("tokenAmount").toString());
                            if (remainingBookingAmount > 0) {
                                Map installmentMap = new HashMap();
                                installmentMap.put("customerId", bookingId + "_P_1");
                                installmentMap.put("bookingId", bookingId);
                                installmentMap.put("installmentAmount", remainingBookingAmount);
                                installmentMap.put("dueDate", mainObject.get("installmentStartDate"));
                                installmentMap.put("status", "Pending");
                                installmentMap.put("paymentType", "Booking");
                                installmentList.add(installmentMap);
                                // add this entry in installment details
                                //transactionDAO.insertDataBatch("");
                            }
                        }
                    }
                    else  if(!((String) bookingDetails.get("paymentType")).equalsIgnoreCase("installment"))
                    {
                        Double totalAmount = Double.parseDouble(mainObject.get("totalAmount").toString());
                        Double bookingAmount = totalAmount * 25 / 100;
                        Double remainingBookingAmount = 0.00;
                        if (((String) mainObject.get("amountType")).equalsIgnoreCase("Token") ) {
                            remainingBookingAmount = totalAmount - Double.parseDouble(mainObject.get("tokenAmount").toString());
                        }
                        else{
                            remainingBookingAmount = totalAmount - bookingAmount;
                        }

                            if (remainingBookingAmount > 0) {
                                Map installmentMap = new HashMap();
                                installmentMap.put("customerId", bookingId + "_P_1");
                                installmentMap.put("bookingId", bookingId);
                                installmentMap.put("installmentAmount", remainingBookingAmount);
                                installmentMap.put("dueDate", mainObject.get("installmentStartDate"));
                                installmentMap.put("status", "Pending");
                                installmentMap.put("paymentType", "Booking");
                                installmentList.add(installmentMap);
                            }

                    }


                } else if (transactions.getTransaction()[i].getId().equalsIgnoreCase("customer")) {
                    Map<String, List> jsonColumnMap = serviceUtils.jsonColumnNameMapper(property);
                    insertQuery = transactions.getTransaction()[i].getInsertQuery();
                    List<Map> customerList = new ArrayList<>();
                    customerDetails.put("customerId", bookingId + "_P_1");
                    customerDetails.put("customerType", "Primary");
                    customerDetails.put("bookingId",bookingId);
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
                            customerDetails.put("customerType", "CoApplicant");
                            customerList.add(customerMap);
                        }
                    }

                    transactionDAO.insertDataBatch(insertQuery, customerList);
                    String password = "";
                    if (customerDetails.get("panNumber") != null) {
                        password += customerDetails.get("panNumber");
                    }
                    if (customerDetails.get("dateOfBirth") != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
                        Instant instant = Instant.parse((String) customerDetails.get("dateOfBirth"));
                        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Kolkata"));
                        password += zonedDateTime.getMonthValue() + "" + zonedDateTime.getMonthValue() + "" + zonedDateTime.getYear();
                    }
                    System.out.println("C" + bookingId.toString() + " === " + password);
                    createUserAndRole("C" + bookingId.toString(), password, "ROLE_CUSTOMER");
                } else if (transactions.getTransaction()[i].getId().equalsIgnoreCase("payment")) {
                    Map paymentDetails = (Map<String, Object>) mainObject.get("paymentDetails");
                    Map<String, List> jsonColumnMap = serviceUtils.jsonColumnNameMapper(property);
                    insertQuery = transactions.getTransaction()[i].getInsertQuery();
                    Map paymentMap = new HashMap();
                    paymentMap.put("customerId", bookingId + "_P_1");
                    paymentMap.put("bookingId", bookingId);
                    paymentMap.put("paymentType", mainObject.get("amountType"));
                    paymentMap.put("collectedBy", createdBy);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
                    String currentDate = sdf.format(new Date());
                    paymentMap.put("collectedOn", currentDate);
                    paymentMap.put("paymentMode", mainObject.get("paymentMode"));
                    paymentMap.put("chequeNo", mainObject.get("chequeNo"));
                    paymentMap.put("transactionId", mainObject.get("transactionId"));
                    paymentMap.put("paymentDate", currentDate);
                    paymentMap.put("receiptNo", mainObject.get("receiptNo"));
                    paymentMap.put("bank", mainObject.get("bank"));
                    if (((String) mainObject.get("amountType")).equalsIgnoreCase("Token")) {
                        paymentMap.put("amount", mainObject.get("tokenAmount"));
                    }
                    else{
                        paymentMap.put("amount", mainObject.get("bookingAmount"));
                    }

                    paymentMap.put("transactionDate", mainObject.get("transactionDate"));
                    //   installmentList.add(serviceUtils.customerMap(paymentMap, jsonColumnMap));

                    Map convertedPaymentMap = serviceUtils.customerMap(paymentMap, jsonColumnMap);
                    ArrayList paymentMapList = new ArrayList();
                    paymentMapList.add(convertedPaymentMap);
                    transactionDAO.insertDataBatch(insertQuery, paymentMapList);
                }
                if ((((String) bookingDetails.get("paymentType")).equalsIgnoreCase("installment") || installmentList.size() > 0 )&& transactions.getTransaction()[i].getId().equalsIgnoreCase("installment")) {
                    Map<String, List> jsonColumnMap = serviceUtils.jsonColumnNameMapper(property);
                    insertQuery = transactions.getTransaction()[i].getInsertQuery();
                    if (installmentList.size() > 0) {
                        Map tempObj = installmentList.get(0);
                        installmentList.remove(0);
                        installmentList.add(serviceUtils.customerMap(tempObj, jsonColumnMap));
                    }

                    if(mainObject.get("paymentType").toString().equalsIgnoreCase("installment"))
                    {
                        String installlmentDueDate = (String) mainObject.get("installmentStartDate");
                        int numberOfInstallment = Integer.parseInt(bookingDetails.get("numberOfInstallment").toString());
                        DateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
                        Calendar cal = null;
                        Date dt = null;
                        try {
                            dt = sourceDateFormat.parse(installlmentDueDate);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //  java.sql.Date installmentDueDateSql = ServiceUtils.convertStrToSQLDate(installlmentDueDate);

                        Map installmentMap = null;
                        Double installmentAmount = Double.valueOf(mainObject.get("installmentAmount").toString());
                        for (int k = 0; k < numberOfInstallment; k++) {
                            installmentMap = new HashMap();
                            installmentMap.put("customerId", bookingId + "_P_1");
                            installmentMap.put("bookingId", bookingId);
                            installmentMap.put("installmentAmount", installmentAmount);
                            installmentMap.put("dueDate", installlmentDueDate);
                            installmentMap.put("status", "Pending");
                            installmentMap.put("paymentType", "Installment");
                            cal = Calendar.getInstance();
                            cal.setTime(dt);
                            //   cal.setTimeInMillis(new Date(installlmentDueDate).getTime());
                            cal.add(Calendar.MONTH, 1);
                            dt = cal.getTime();
                            //  installmentDueDateSql = new java.sql.Date(cal.getTimeInMillis());
                            installlmentDueDate = sourceDateFormat.format(dt);
                            installmentList.add(serviceUtils.customerMap(installmentMap, jsonColumnMap));
                        }
                    }
                    transactionDAO.insertDataBatch(insertQuery, installmentList);
                 /*   TransactionUtils transactionUtils = new TransactionUtils();
                    Double entryAmount = null;
                    if(mainObject.get("amountType").toString().equalsIgnoreCase("Token"))
                    {
                        entryAmount = Double.valueOf(bookingDetails.get("tokenAmount").toString());
                    }
                    else{
                         entryAmount = Double.valueOf(bookingDetails.get("bookingAmount").toString());
                    }
                    Double bookingAmount = Double.valueOf(bookingDetails.get("bookingAmount").toString());
                    String particulars = bookingDetails.get("customerName") + " - " + bookingDetails.get("bookingAmount") + " - ";

*/
                }
            }
            if (bookingDetails.get("bookingType").toString().equalsIgnoreCase("MLM")) {

                agentCommissionCalculation(Integer.parseInt(bookingDetails.get("firmId").toString()), Integer.parseInt(bookingDetails.get("projectId").toString()), Double.parseDouble(bookingDetails.get("totalPaidAmount").toString()), Integer.parseInt(bookingDetails.get("agentId").toString()));

            }
            if(mainObject.get("paymentMode").toString().equalsIgnoreCase("Cheque")){
                String  transactionId = mainObject.get("transactionId")!=null?mainObject.get("transactionId").toString():null;
                String  transactionDate = mainObject.get("transactionDate")!=null?mainObject.get("transactionDate").toString():null;
                makeChequeEntry(transactionId,transactionDate, bookingId.intValue());
            }
            String firmName = mainObject.get("firmName")!=null?mainObject.get("firmName").toString():"";
            Double paidAmount = mainObject.get("totalPaidAmount")!=null?Double.parseDouble(mainObject.get("totalPaidAmount").toString()):0.00;
            dayBookEntry(Integer.parseInt(mainObject.get("firmId").toString()), firmName,mainObject.get("paymentMode").toString(),  paidAmount, "Credit", customerDetails.get("customerName").toString(), "New Booking");
            statusCode = "1";
            statusMessage = "Success";
    }
    catch(Exception e)
    {
        e.printStackTrace();
        statusCode = "0";
        statusMessage = "Failed";
    }
        restReponse = ServiceUtils.convertObjToResponse(statusCode, statusMessage, bookingId);
        return restReponse;
    }

    //used for Farmer and Agent Payment Entry
    public RestResponse createPayment(Map paymentMap, String type, String createdBy) {
        RestResponse restResponse = null;
        String statusMessage = "";
        String statusCode = "";

        try {
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
                        if (argumentTypes[j] == 93 && arguments[j] != null) {
                            Instant instant = Instant.parse((String) arguments[j]);
                            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Kolkata"));
                            arguments[j] = new java.sql.Date(Date.from(zonedDateTime.toInstant()).getTime());
                        }
                        if (paramsArr[j].trim().equalsIgnoreCase("CreatedBy")) {
                            arguments[j] = createdBy;
                        } else if (paramsArr[j].trim().equalsIgnoreCase("CreatedOn")) {
                            java.sql.Date currentDate = new java.sql.Date(new Date().getTime());
                            arguments[j] = currentDate;
                        }
                    }
                    transactionDAO.addData(sql, arguments, argumentTypes); // add entry in table
                    //In case of farmer payment update paid amount in LandMaster
                    if (type.equalsIgnoreCase("farmerPayment")) {
                        Double paidAmount = Double.valueOf(paymentMap.get("paymentAmount").toString());
                        String updateQuery = "UPDATE LandMaster SET PaidAmount = CASE WHEN PaidAmount = 0.00 || PaidAmount is null THEN " + paidAmount + "  ELSE PaidAmount + " + paidAmount + " END WHERE Id = " + paymentMap.get("landId");
                        String updateFarmerMasterQuery = "UPDATE FarmerMaster SET PaidAmount = CASE WHEN PaidAmount = 0.00  || PaidAmount is null  THEN " + paidAmount + "  ELSE PaidAmount + " + paidAmount + " END WHERE Id = " + paymentMap.get("farmerId");
                        transactionDAO.insertDataBatch(new String[]{updateQuery, updateFarmerMasterQuery});
                    } else if (type.equalsIgnoreCase("agentPayment")) {
                        Double paidAmount = Double.valueOf(paymentMap.get("paymentAmount").toString());
                        String updateQuery = "UPDATE AgentBusinessDetails SET AmountPaidTillNow = CASE WHEN AmountPaidTillNow = 0.00 || AmountPaidTillNow is null THEN " + paidAmount + "  ELSE AmountPaidTillNow +" + paidAmount + " END WHERE AgentId = " + paymentMap.get("agentId");
                        String udpateAgentMasterPaidTillNow = "UPDATE AgentMaster SET AmountPaidTillNow = CASE WHEN AmountPaidTillNow = 0.00 || AmountPaidTillNow is null THEN " + paidAmount + "  ELSE AmountPaidTillNow +" + paidAmount + " END WHERE AgentId = " + paymentMap.get("agentId");
                        transactionDAO.insertDataBatch(new String[]{udpateAgentMasterPaidTillNow,updateQuery});
                    }

                }

            }
            statusCode = "1";
            statusMessage = "Success";
        }
        catch(Exception e)
        {
            e.printStackTrace();
            statusCode = "0";
            statusMessage = "Failed";
        }
        restResponse = ServiceUtils.convertObjToResponse(statusCode, statusMessage, null);
        return restResponse;

    }



    //used for Daily Expense / Farmer Payment / Agent Payment Approval / Property Cancellation Payment - > make and entry in Day book Entry table
    public RestResponse approvePayment(String id, String type, String approvedBy,String isApproved) {
        RestResponse restResponse = null;
        String statusMessage = "";
        String statusCode = "";
        try {
            ConfigBO configBO = ConfigReader.getConfig();
            Transactions transactions = configBO.getTransactions();
            String updateQuery;
            for (Transaction transaction : transactions.getTransaction()) {
                if (transaction.getId().equalsIgnoreCase(type)) {
                    updateQuery = transaction.getUpdateQuery();
                    updateQuery = updateQuery.replace("{1}", "'" + approvedBy + "'");
                    updateQuery = updateQuery.replace("{2}", id);
                    updateQuery = updateQuery.replace("{3}", "'"+isApproved+"'");
                    if(isApproved.equalsIgnoreCase("Y"))
                        updateQuery = updateQuery.replace("{4}", "'"+"Approved"+"'");
                    else
                        updateQuery = updateQuery.replace("{4}", "'"+"Rejected"+"'");

                    transactionDAO.updateData(updateQuery);
                    // after payment make an entry in DaybookEntry table
                 }
            }
            if(isApproved.equalsIgnoreCase("Y")) {
                String fethQuery = "";
                if (type.equalsIgnoreCase("agentPayment")) {
                    fethQuery = "SELECT PaymentMode as paymentMode,PaymentAmount as paymentAmount,AgentName as agentName   from AgentPayment where id = ?";
                    Map data = masterDAO.getDataById(fethQuery, Integer.parseInt(id));
                    dayBookEntry(null, null, data.get("paymentMode").toString(), Double.parseDouble(data.get("paymentAmount").toString()), "Debit", data.get("agentName").toString(), "Agent Payment");
                } else if (type.equalsIgnoreCase("farmerPayment")) {
                    fethQuery = "SELECT PaymentMode as paymentMode,PaymentAmount as paymentAmount,FarmerName as farmerName   from FarmerPayment where id = ?";
                    Map data = masterDAO.getDataById(fethQuery, Integer.parseInt(id));
                    dayBookEntry(null, null, data.get("paymentMode").toString(), Double.parseDouble(data.get("paymentAmount").toString()), "Debit", data.get("farmerName").toString(), "Farmer Payment");
                } else if (type.equalsIgnoreCase("dailyExpense")) {

                    fethQuery = "SELECT FirmId as firmId, FirmName as firmName, PaymentMode as paymentMode,Amount as paymentAmount,PaidTo as paidTo,ExpenseCategory as expenseCategory  from ExpenseDetails where id = ?";
                    Map data = masterDAO.getDataById(fethQuery, Integer.parseInt(id));
                    dayBookEntry(Integer.parseInt(data.get("firmId").toString()), data.get("firmName").toString(), data.get("paymentMode").toString(), Double.parseDouble(data.get("paymentAmount").toString()), "Debit", data.get("paidTo").toString(), data.get("expenseCategory").toString());
                }
            }
                    statusCode = "1";
                   statusMessage = "Success";
        }
        catch(Exception e)
        {
            e.printStackTrace();
            statusCode = "0";
            statusMessage = "Failed";
        }
        restResponse = ServiceUtils.convertObjToResponse(statusCode, statusMessage, null);
        return restResponse;
    }

    // cancel property, update IsCancelled = 'Y' in BookingDetails table
    public RestResponse cancelProperty(int firmId,String firmName, int bookingId,String paymentMode, String customerName,Double paidAmount,int projectId, int blockId, int plotNumber) {
        // in case of installment update, update booking details table with total amount.
        RestResponse restResponse = null;
        String statusMessage = "";
        String statusCode = "";
        try {
            String updateBookingQuery = "UPDATE BookingDetails SET IsCancelled = 'Y' WHERE BookingId = " + bookingId;
            String updatePlotQuery = "UPDATE PlotDetails SET Status = 'Available' WHERE FirmId = " + firmId + " and ProjectId = " + projectId + " and BlockId = " + blockId + " and PlotNo =" + plotNumber;

            transactionDAO.insertDataBatch(new String[]{updateBookingQuery, updatePlotQuery});
            dayBookEntry(firmId, firmName, paymentMode, paidAmount, "Debit", customerName, "Property Cancellation");
            statusCode = "1";
            statusMessage = "Success";
        }
        catch(Exception e)
        {
            e.printStackTrace();
            statusCode = "0";
            statusMessage = "Failed";
        }
        //make entry in day book entry
        restResponse = ServiceUtils.convertObjToResponse(statusCode, statusMessage, null);
        return restResponse;
    }

    public RestResponse updateTransaction(Map restRequest, String type, String id, String createdBy) {
        // in case of installment update, update booking details table with total amount.
        RestResponse restResponse = null;
        String statusMessage = "";
        String statusCode = "";
        Object objectId =  null;
        try {
            restRequest.put("collectedBy", createdBy);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
            String currentDate = sdf.format(new Date());
            restRequest.put("collectedOn", currentDate);
            restRequest.put("id", Integer.parseInt(id));
            int bookingId = Integer.parseInt(restRequest.get("bookingId").toString());
            restRequest.remove("isDisabled");
            ConfigBO configBO = ConfigReader.getConfig();
            Transactions transactions = configBO.getTransactions();
            ServiceUtils serviceUtils = new ServiceUtils();
            for (int i = 0; i < transactions.getTransaction().length; i++) {
                Transaction transaction = transactions.getTransaction()[i];
                if (transaction.getId().equalsIgnoreCase("payment")) {
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
                        Object colValue = restRequest.get(jsonColTypeList.get(0));
                        System.out.println(jsonColTypeList.get(0) + " ------- " + colValue);
                        arguments[j] = colValue;

                        argumentTypes[j] = Integer.parseInt((String) jsonColTypeList.get(1));
                        if (argumentTypes[j] == 3 && arguments[j] != null)
                            arguments[j] = Double.parseDouble(arguments[j].toString());
                        if (argumentTypes[j] == 4 && arguments[j] != null)
                            arguments[j] = Integer.parseInt(arguments[j].toString());
                        if (argumentTypes[j] == 93 && arguments[j] != null) {
                            arguments[j] = serviceUtils.convertStrToSQLDate(arguments[j].toString());
                        }
                    }
                    objectId =  transactionDAO.addInstallmentData(sql, arguments, argumentTypes); // add entry in table
                }
            }
            if (type.equalsIgnoreCase("installment")) {
                Double totalAmount = Double.parseDouble(restRequest.get("totalAmount").toString());
                Double
                        totalAmountPaid = Double.parseDouble(restRequest.get("amount").toString());
                int totalEmi = 0;
                Double remainder = 0.00;
                Double installmentAmount = 0.00;
                java.sql.Timestamp paymentDate = null;
                if (totalAmountPaid > totalAmount) {
                    installmentAmount = Double.parseDouble(restRequest.get("installmentAmount").toString());
                    totalEmi = (int) ((totalAmountPaid - totalAmount) / installmentAmount);
                    remainder = (totalAmountPaid - totalAmount) % installmentAmount;
                }
                if (restRequest.get("paymentDate") != null) {
                    Instant instant = Instant.parse((String) restRequest.get("paymentDate"));
                    ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Kolkata"));
                    paymentDate = new java.sql.Timestamp(Date.from(zonedDateTime.toInstant()).getTime());
                }

                restRequest.remove("dueDate");
                for (int i = 0; i < configBO.getTransactions().getTransaction().length; i++) {
                    if (configBO.getTransactions().getTransaction()[i].getId().equalsIgnoreCase(type)) {

                            String sql = configBO.getTransactions().getTransaction()[i].getUpdateQuery();
                            PropertyMapping propertyMapping = configBO.getTransactions().getTransaction()[i].getPropertyMapping();
                            Property property[] = propertyMapping.getProperty();
                            Map<String, List> jsonColumnMap = new ServiceUtils().jsonColumnNameMapper(property);
                            restRequest.put("paymentDate", restRequest.get("paymentDate"));
                            if (restRequest.get("pendingAmount") != null) {
                                Double pendingAmount = Double.parseDouble(restRequest.get("pendingAmount").toString());
                                if (pendingAmount <=0.00) {
                                    restRequest.put("pendingAmount", pendingAmount);
                                }
                            }
                            Map convertedInstallmentMap = serviceUtils.customerMap(restRequest, jsonColumnMap);
                            List<Map> installmentList = new ArrayList<>();
                            installmentList.add(convertedInstallmentMap);
                            Map map = new HashMap();
                            int nextId = Integer.parseInt(id) + 1;
                            for (int count = 0; count < totalEmi; count++) {
                                map.put("status", "Completed");
                                map.put("interestPaid", 0.00);
                                map.put("interestWaiveOff", 0.00);
                                map.put("installmentAmount", restRequest.get("installmentAmount"));
                                map.put("pendingAmount", 0.00);
                                map.put("paymentDate", restRequest.get("paymentDate"));
                                map.put("receiptNo", restRequest.get("receiptNo"));
                                map.put("interest", 0.00);
                                map.put("amount", 0.00);
                                map.put("amountPaid", restRequest.get("installmentAmount"));
                                map.put("paymentMode", restRequest.get("paymentMode"));
                                map.put("transactionId", restRequest.get("transactionId"));
                                map.put("transactionDate", restRequest.get("transactionDate"));
                                map.put("totalAmount", restRequest.get("installment"));
                                map.put("collectedBy", createdBy);
                                map.put("collectedOn", currentDate);
                                map.put("bankName", restRequest.get("bankName"));
                                map.put("id", nextId);
                                nextId++;
                                installmentList.add(serviceUtils.customerMap(map, jsonColumnMap));
                            }
                            if (remainder > 0.00) {
                                map.put("status", "Pending");
                                map.put("interestPaid", 0.00);
                                map.put("amount", remainder);
                                map.put("interestWaiveOff", 0.00);
                                map.put("installmentAmount", restRequest.get("installmentAmount"));
                                map.put("pendingAmount", (installmentAmount - remainder));
                                map.put("paymentDate", restRequest.get("paymentDate"));
                                map.put("receiptNo", restRequest.get("receiptNo"));
                                map.put("interest", 0.00);
                                map.put("amountPaid", remainder);
                                map.put("paymentMode", restRequest.get("paymentMode"));
                                map.put("transactionId", restRequest.get("transactionId"));
                                map.put("transactionDate", restRequest.get("transactionDate"));
                                map.put("totalAmount", remainder);
                                map.put("collectedBy", createdBy);
                                map.put("collectedOn", currentDate);
                                map.put("bankName", restRequest.get("bankName"));
                                map.put("id", nextId);
                                installmentList.add(serviceUtils.customerMap(map, jsonColumnMap));
                            }
                            transactionDAO.updateDataBatch(sql, installmentList, Integer.parseInt(id));
                            //update booking details - last payment date and total paid amount
                            String updateBookingDetailsQuery = "UPDATE BookingDetails SET LastPaymentDate = '" + paymentDate + "' , TotalPaidAmount = CASE WHEN TotalPaidAmount is null || TotalPaidAmount = 0.00  THEN " + totalAmountPaid + "  ELSE TotalPaidAmount + " + totalAmountPaid + " END WHERE BookingId =" + bookingId;
                            transactionDAO.updateData(updateBookingDetailsQuery);
                            String selectQuery = "SELECT FirmId, ProjectId, AgentId,BookingType from BookingDetails WHERE BookingId = ?";
                            Map<String, Object> bookingData = masterDAO.getDataById(selectQuery, bookingId);
                            if (bookingData.get("BookingType").toString().equalsIgnoreCase("MLM")) {

                              agentCommissionCalculation(Integer.parseInt(bookingData.get("FirmId").toString()), Integer.parseInt(bookingData.get("projectId").toString()), totalAmountPaid, Integer.parseInt(bookingData.get("agentId").toString()));

                            }
                            if(restRequest.get("paymentMode").toString().equalsIgnoreCase("Cheque")){
                                String  transactionId = restRequest.get("transactionId")!=null?restRequest.get("transactionId").toString():null;
                                String  transactionDate = restRequest.get("transactionDate")!=null?restRequest.get("transactionDate").toString():null;
                                makeChequeEntry(transactionId,transactionDate, bookingId);
                            }
                            dayBookEntry(Integer.parseInt(bookingData.get("FirmId").toString()), null,restRequest.get("paymentMode").toString(),  Double.parseDouble(restRequest.get("amount").toString()), "Credit", Integer.toString(bookingId), "Installment");
                        break;
                    }
                }
            }
            statusCode = "1";
            statusMessage = "Success";

        }
        catch(Exception e)
        {
            e.printStackTrace();
            statusCode = "0";
            statusMessage = "Failed";
        }
        restResponse = ServiceUtils.convertObjToResponse(statusCode, statusMessage, objectId);
        return restResponse;
    }

    private void agentCommissionCalculation(int firmId, int projectId, Double amount, int agentId) throws Exception {
        // deduct percentage from amount. If property has 80% business value then calculate commission amount from (amount*80)

        String selectProjectMaster = "SELECT BusinessValue as businessValue from ProjectMaster WHERE FirmId = " + firmId + " and ProjectId =" + projectId;
        List projectList = masterDAO.getAllData(selectProjectMaster);
        Double busnessValuePercentage = Double.parseDouble(((Map) projectList.get(0)).get("businessValue").toString());
        Double businessValue = amount * busnessValuePercentage / 100;
        List<String> queries = null;
        int count = 0;
        Double chainBusiness = 0.00;
        int sellerAgentLevel = 0;
        Double sellerAgentCommission = 0.00;
        String sponsorId = null;
        // fetch all parent agents.
        String selectChainAgents = "WITH RECURSIVE category_path (AgentId, AgentName, SponsorId,Designation,SelfBusiness,TotalCommission,Commission,MaxTarget) AS " +
                "(" +
                "   SELECT am.AgentId, am.AgentName, am.SponsorId,am.Designation,ab.SelfBusiness,ab.TotalCommission,mp.Commission,mp.MaxTarget " +
                "    FROM  AgentMaster am INNER JOIN AgentBusinessDetails ab INNER JOIN MatrixPlan mp ON mp.Level =am.Designation and am.AgentId = ab.AgentId " +
                "    WHERE am.AgentId = " + agentId +
                "  UNION ALL" +
                "  SELECT c.AgentId, c.AgentName, c.SponsorId,c.Designation, ab.SelfBusiness,ab.TotalCommission,mp.Commission,mp.MaxTarget " +
                "    FROM category_path AS cp JOIN  AgentMaster AS c INNER JOIN AgentBusinessDetails  ab   INNER JOIN MatrixPlan mp ON mp.Level = c.Designation and ab.AgentId = cp.SponsorId and cp.SponsorId = c.AgentId " +
                ")" +
                "" + "SELECT * FROM category_path cp ";

        List allAgentList = masterDAO.getAllData(selectChainAgents);
        queries = new ArrayList<String>();
        for (Object agentObject : allAgentList) {
            Map agentMap = (Map) agentObject;
            Double commissionAmount = 0.00;

            if (Integer.parseInt(agentMap.get("agentId").toString()) == agentId) {
                Double commissionPerc = Double.parseDouble(agentMap.get("Commission").toString());
                Double existingSelfBusiness = 0.00;
                if(agentMap.get("SelfBusiness")!=null)
                    existingSelfBusiness = Double.parseDouble(agentMap.get("SelfBusiness").toString());
                Double totalSelfBusiness = existingSelfBusiness + businessValue;
                Double maxTarget = Double.parseDouble(agentMap.get("MaxTarget").toString());
                sellerAgentLevel = Integer.parseInt(agentMap.get("Designation").toString());
                sponsorId = agentMap.get("SponsorId")!=null ? agentMap.get("SponsorId").toString():null;
                sellerAgentCommission = commissionPerc;
                //get rank from Matrix plan
                if (totalSelfBusiness > maxTarget) {
                    String selectNewLevel = "SELECT Level,Commission,MinTarget,MaxTarget from MatrixPlan WHERE " + totalSelfBusiness + " BETWEEN MinTarget and MaxTarget";
                    List newLevelList = masterDAO.getAllData(selectNewLevel);

                    int newLevel = 0;
                    Double newMinTarget = 0.00;
                    Double newMaxTarget = 0.00;
                    Double newCommissionPerc = 0.00;
                    if (newLevelList.size() > 0) {
                        newLevel = Integer.parseInt(((Map) newLevelList.get(0)).get("Level").toString());
                        newMinTarget = Double.parseDouble(((Map) newLevelList.get(0)).get("MinTarget").toString());
                        newCommissionPerc = Double.parseDouble(((Map) newLevelList.get(0)).get("Commission").toString());
                        //update agent details with new agent level;
                        String agentNewLevel = "UPDATE AgentMaster SET Designation = '"+newLevel+"' WHERE AgentId = " + agentMap.get("agentId");
                       queries.add(agentNewLevel);
                        count++;
                    }

                    if (newLevel > sellerAgentLevel + 1) {
                        commissionAmount = calculateCommision(newLevel, sellerAgentLevel, sellerAgentCommission, existingSelfBusiness, totalSelfBusiness);
                    } else {
                        commissionAmount = ((newMinTarget - existingSelfBusiness) * commissionPerc / 100) + ((totalSelfBusiness - newMinTarget) * newCommissionPerc / 100);
                    }
                    sellerAgentLevel = newLevel;
                    sellerAgentCommission = newCommissionPerc;

                } else {
                    commissionAmount = businessValue * commissionPerc / 100;
                    //update in agent business details
                }

                // update total amount in Agent master
                String agentTotalAmount =  "UPDATE AgentMaster SET TotalAmount = CASE WHEN TotalAmount = 0.00 || TotalAmount is null  THEN " + businessValue + "  ELSE TotalAmount + " + businessValue + " END WHERE AgentId = " + agentMap.get("agentId");
                //update chain business value
                queries.add(agentTotalAmount);

                String updateABQuery = "UPDATE AgentBusinessDetails SET  TotalBusiness = CASE WHEN TotalBusiness = 0.00 || TotalBusiness is null  THEN " + businessValue + "  ELSE TotalBusiness + " + businessValue + " END, SelfBusiness = CASE WHEN SelfBusiness = 0.00 || SelfBusiness is null THEN " + businessValue + "  ELSE SelfBusiness + " + businessValue + " END,  TotalCommission = CASE WHEN TotalCommission = 0.00 || TotalCommission is null THEN " + commissionAmount + "  ELSE TotalCommission + " + commissionAmount + " END WHERE AgentId = " + agentMap.get("agentId");
                queries.add(updateABQuery);
            } else {
                Double chainAgentCommission = Double.parseDouble(agentMap.get("Commission").toString());
                int chainAgentLevel = Integer.parseInt(agentMap.get("Designation").toString());

                if (chainAgentLevel > sellerAgentLevel) {
                    commissionAmount = businessValue * (chainAgentCommission - sellerAgentCommission) / 100;

                }
                sellerAgentCommission = chainAgentCommission;
                String agentTotalAmount =  "UPDATE AgentMaster SET TotalAmount = CASE WHEN TotalAmount = 0.00 || TotalAmount is null  THEN " + businessValue + "  ELSE TotalAmount + " + businessValue + " END WHERE AgentId = " + agentMap.get("agentId");
                //update chain business value
                queries.add( agentTotalAmount);

                //update chain business value
                String updateABQuery = null;
                updateABQuery = "UPDATE AgentBusinessDetails SET TotalCommission = CASE WHEN TotalCommission = 0.00  || TotalCommission is null THEN  " + commissionAmount + "  ELSE TotalCommission + " + commissionAmount + " END , ChainBusiness = CASE WHEN ChainBusiness = 0.00  || ChainBusiness is null THEN " + businessValue + "  ELSE ChainBusiness + " + businessValue + " END WHERE AgentId = " + agentMap.get("agentId");

                queries.add( updateABQuery);
            }
            count++;
        }
        //update commissionAmount and chain business in agent business details
        String stringArray[] = Arrays.stream(queries.toArray()).toArray(String[]::new);
        transactionDAO.insertDataBatch(stringArray);
        calculateReward(sponsorId);
    }

    private Double calculateCommision(int newLevel, int oldLevel, Double oldCommissionPerc, Double existingSelfBusiness, Double totalSelfBusiness) throws Exception {
        String selectNewLevel = "SELECT Level,Commission,MinTarget,MaxTarget from MatrixPlan";
        List newLevelList = masterDAO.getAllData(selectNewLevel);
        // int levelDiff = newLevel - oldLevel;
        Double totalCommission = 0.00;
        // for (int i = 0; i < levelDiff; i++) {
        for (int j = 0; j < newLevelList.size(); j++) {
            int level = Integer.parseInt(((Map) newLevelList.get(j)).get("Level").toString());
            Double minTarget = Double.parseDouble(((Map) newLevelList.get(j)).get("MinTarget").toString());
            Double maxTarget = Double.parseDouble(((Map) newLevelList.get(j)).get("MaxTarget").toString());
            Double commissionPerc = Double.parseDouble(((Map) newLevelList.get(j)).get("Commission").toString());
            Double commissionAmount = 0.00;
            if (oldLevel > level)
                continue;
            if (newLevel < level)
                break;
            if (oldLevel >= level || level <= newLevel) {
                // Double commissionAmount = ((newMinTarget - existingSelfBusiness) * oldCommissionPerc / 100);
                if (level == oldLevel) {
                    commissionAmount = (maxTarget + 1 - existingSelfBusiness) * oldCommissionPerc / 100;
                    totalSelfBusiness = totalSelfBusiness - existingSelfBusiness;
                } else if (level == newLevel) {
                    commissionAmount = (totalSelfBusiness) * commissionPerc / 100;
                } else {
                    commissionAmount = (maxTarget - minTarget) * commissionPerc / 100;
                    totalSelfBusiness = totalSelfBusiness - (maxTarget - minTarget);
                }

                totalCommission += commissionAmount;
            }
        }
        System.out.println(totalCommission);
        return Math.ceil(totalCommission);
    }


    public void createUserAndRole(String userid, String password, String role) {
        String encodedPassword = new BCryptPasswordEncoder().encode(password);
        String insertquery = "INSERT INTO user (username,password) VALUES ('" + userid + "','" + encodedPassword + "')";
        String insertRole = "INSERT INTO user_roles(username,role) VALUES ('" + userid + "','" + role + "')";
        transactionDAO.insertDataBatch(new String[]{insertquery, insertRole});
    }

    public RestResponse generateRecieptNumber() {
        String statusCode = "";
        String statusMessage = "";
        RestResponse response = null;
        Object receiptNumber = null;
        try {
            String updateReceiptQuery = "UPDATE  ReceiptNumber SET ReceiptNo = LAST_INSERT_ID (ReceiptNo+1)";
            receiptNumber = transactionDAO.getReceiptNumber(updateReceiptQuery);
            statusCode = "1";
            statusMessage = "Success";
        } catch (Exception e) {
            statusCode = "2";
            statusMessage = "Failed";

        }
        return  response = ServiceUtils.convertObjToResponse(statusCode, statusMessage, receiptNumber);
    }

    public RestResponse generatePrintReceipt(Integer bookingId, Integer paymnentId){
        String statusCode = "";
        String statusMessage = "";
        RestResponse response = null;
        String paymentDetailsQuery = "";
        String firmId = "";
        if(paymnentId == null) {
            paymentDetailsQuery = "select a.Amount as paidAmount, a.PaymentType as paymentType , a.PaymentMode as paymentMode, a.TransactionId as transactionId,a.ReceiptNo as receiptNo, a.BankName as bank,b.ProjectName as projectName, d.Block as block, b.PlotNumber as plotNumber,b.FirmName as firmName,DATE_FORMAT(a.PaymentDate,'%d/%m/%Y') as paymentDate,\n" +
                    "b.FirmId as firmId, DATE_FORMAT(a.TransactionDate,'%d/%m/%Y') as transactionDate,c.CustomerId as customerId, c.CustomerName as customerName from PaymentDetails a INNER JOIN  BookingDetails b ON b.BookingId = a.BookingId INNER JOIN CustomerDetails c ON  b.BookingId = c.BookingId  INNER JOIN BlockMaster d ON d.Id = b.BlockId WHERE b.BookingId = " + bookingId + " Order by PaymentDate asc";
        }
        else {
            paymentDetailsQuery = "select a.Amount as paidAmount, a.PaymentType as paymentType , a.PaymentMode as paymentMode, a.TransactionId as transactionId,a.ReceiptNo as receiptNo, a.BankName as bank,b.ProjectName as projectName, d.Block as block, b.PlotNumber as plotNumber,b.FirmName as firmName,DATE_FORMAT(a.PaymentDate,'%d/%m/%Y') as paymentDate,\n" +
                    "b.FirmId as firmId, DATE_FORMAT(a.TransactionDate,'%d/%m/%Y') as transactionDate,c.CustomerId as customerId, c.CustomerName as customerName from PaymentDetails a INNER JOIN  BookingDetails b ON b.BookingId = a.BookingId INNER JOIN CustomerDetails c ON  b.BookingId = c.BookingId INNER JOIN BlockMaster d ON d.Id = b.blockId WHERE b.BookingId = " + bookingId + "  AND PaymentId = "+paymnentId+" Order by PaymentDate asc";
        }

        String receiptTemplate ="";
        try {
            receiptTemplate =  PrintReceiptTemplate.getReceiptTemplate();
            List paymentDeatils = null;
            paymentDeatils = masterDAO.getAllData(paymentDetailsQuery);
            if(paymentDeatils!=null && paymentDeatils.size()>0)
            {
                Map paymentDetail  = (Map) paymentDeatils.get(0);
                // receiptTemplate =  receiptTemplate.replaceAll("","");
//                  / receiptTemplate =  receiptTemplate.replace("@irmName","");
                String replaceString[] = {"firmName","paidAmount","receiptNo","paymentDate","paymentMode","transactionId","transactionDate","customerId","customerName","plotNumber","block","projectName"};
                for(int i = 0 ; i < replaceString.length;i++)
                {
                    System.out.println(replaceString[i]+" -- "+paymentDetail.get(replaceString[i]));
                    String replaceValue = paymentDetail.get(replaceString[i])!=null?paymentDetail.get(replaceString[i]).toString():"";
                    String name = "@"+replaceString[i];
                    System.out.println(name +" --- "+replaceValue);
                    if(replaceString[i].equalsIgnoreCase("customerId")){
                        replaceValue = replaceValue.substring(0,replaceValue.indexOf("_"));
                    }
                    receiptTemplate =   receiptTemplate.replaceAll(name,replaceValue);
                }
                firmId = paymentDetail.get("firmId").toString();
            }
            Blob blob = masterDAO.getBlobData(firmId);
            int myblobLength = (int) blob.length();
            byte[] myblobAsBytes = blob.getBytes(1, myblobLength);
            blob.free();
            receiptTemplate =   receiptTemplate.replaceAll("@image", DatatypeConverter.printBase64Binary(myblobAsBytes));
            System.out.println(receiptTemplate);
            statusCode = "1";
            statusMessage = "Success";
        } catch (Exception e) {
            e.printStackTrace();
            statusCode = "0";
            statusMessage = "Failed";
        }
        return  response = ServiceUtils.convertObjToResponse(statusCode, statusMessage, receiptTemplate);
    }


    // for agent payment / farmer payment / booking/installment/ daily expense - make an entry in table

    private void dayBookEntry(Integer firmId, String firmName,String paymentMode, Double amount,String transactionType,String personName,String type )
    {
        String particulars = "";
        Double debitTransAmount = 0.00;
        Double creditTrasAmount = 0.00;
        if(transactionType.equalsIgnoreCase("Debit")) {
            debitTransAmount = amount;
            amount = -amount;
            particulars = "Paid To - "+personName +" | " + type   ;
        }
        else {
            creditTrasAmount = amount;
            particulars = "Customer Name - "+personName +" |  " + type ;
        }
        String insertQuery = "INSERT INTO DaybookEntry(TransactionDate,FirmId,FirmName,PaymentMode,Particulars,Debit,Credit)" +
                " VALUES (now(),'"+firmId+"','"+firmName+"','"+paymentMode+"','"+particulars+"',"+debitTransAmount+","+creditTrasAmount+")";
        Object id = transactionDAO.updateData(insertQuery);
        String updateBalance = "UPDATE DaybookEntry SET Balance = CASE WHEN Balance = 0.00 || Balance is null THEN " + amount + "  ELSE Balance + " + amount + " END WHERE Id = "+id ;
        transactionDAO.updateData(updateBalance);
    }

    private void makeChequeEntry(String chequeNumber,String chequeDate,Integer bookingId)
    {
        try {
            Instant instant = Instant.parse(chequeDate);
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Kolkata"));

            String chequeEntryQuery = "INSERT INTO  ChequeEntry (ChequeNumber, ChequeDate,  BookingId, Status) VALUES ('" + chequeNumber + "',"+ new java.sql.Date(Date.from(zonedDateTime.toInstant()).getTime()) + "," + bookingId + ", 'Pending')";
            transactionDAO.updateData(chequeEntryQuery);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public RestResponse udpateChequeEntry(String id)
    {
        RestResponse restResponse = null;
        String statusMessage = "";
        String statusCode = "";
        try {
            String chequeUpdateQuery = "Update ChequeEntry set Status = 'Completed', ClearanceDate = now() WHERE Id = "+id;
            transactionDAO.updateData(chequeUpdateQuery);
            statusCode = "1";
            statusMessage = "Success";
        }
        catch(Exception e)
        {
            e.printStackTrace();
            statusCode = "0";
            statusMessage = "Failed";
        }
        return  restResponse = ServiceUtils.convertObjToResponse(statusCode, statusMessage, null);
    }

    @Override
    public RestResponse getAllChequeEntries()
    {
        List chequeList = new ArrayList();
        RestResponse restResponse = null;
        String statusMessage = "";
        String statusCode = "";
        String fetchChequeQuery = "SELECT Id as id, BookingId as bookingId, ChequeNumber as chequeNumber, ChequeDate as chequeDate FROM ChequeEntry where Status = 'Pending'";
        try {
             chequeList = masterDAO.getAllData(fetchChequeQuery);
            statusCode = "1";
            statusMessage = "Success";
        }
        catch(Exception e) {
            e.printStackTrace();
            statusCode = "0";
            statusMessage = "Failed";
        }
        return  restResponse = ServiceUtils.convertObjToResponse(statusCode, statusMessage, chequeList);
    }


    public RestResponse createDailyExpense(Map paymentMap, String type, String createdBy) {
        RestResponse restResponse = null;
        String statusMessage = "";
        String statusCode = "";
        List expenseList = null;
        Map commonMap = null;

        try {
            String firmId = paymentMap.get("firmId")!=null?paymentMap.get("firmId").toString():"-1";
            expenseList = new ArrayList();
            if(firmId.equalsIgnoreCase("-1"))
            {
                commonMap = null;
                // get firm count.
                List firmList = masterDAO.getAllData("SELECT Id, FirmName from FirmMaster");
                Double amount = Double.parseDouble(paymentMap.get("amount").toString());
                Double sharedAmount = amount/firmList.size();
                for(int i = 0 ; i < firmList.size();i++)
                {
                    commonMap = new HashMap();
                    commonMap.putAll(paymentMap);
                    commonMap.put("amount",sharedAmount);
                    commonMap.put("firmId",((Map)firmList.get(i)).get("Id"));
                    commonMap.put("firmName",((Map)firmList.get(i)).get("FirmName"));
                    commonMap.put("transactionDetail", "Common - "+paymentMap.get("transactionDetail"));
                    expenseList.add(commonMap);
                }
            }
            else{
                expenseList.add(paymentMap);
            }

            ConfigBO configBO = ConfigReader.getConfig();
            Transactions transactions = configBO.getTransactions();
            ServiceUtils serviceUtils = new ServiceUtils();
            for (int i = 0; i < transactions.getTransaction().length; i++) {
                Transaction transaction = transactions.getTransaction()[i];
                if (transaction.getId().equalsIgnoreCase(type)) {
                    for(int count = 0 ;count < expenseList.size();count++) {
                        Map expenseMap = (Map) expenseList.get(count);
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
                            Object colValue = expenseMap.get(jsonColTypeList.get(0));
                            arguments[j] = colValue;
                            argumentTypes[j] = Integer.parseInt((String) jsonColTypeList.get(1));
                            if (argumentTypes[j] == 93 && arguments[j] != null) {
                                Instant instant = Instant.parse((String) arguments[j]);
                                ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Kolkata"));
                                arguments[j] = new java.sql.Date(Date.from(zonedDateTime.toInstant()).getTime());
                            }
                            if (paramsArr[j].trim().equalsIgnoreCase("CreatedBy")) {
                                arguments[j] = createdBy;
                            } else if (paramsArr[j].trim().equalsIgnoreCase("CreatedOn")) {
                                java.sql.Date currentDate = new java.sql.Date(new Date().getTime());
                                arguments[j] = currentDate;
                            }
                        }
                        transactionDAO.addData(sql, arguments, argumentTypes); // add entry in table
                    }
                }

            }
            statusCode = "1";
            statusMessage = "Success";
        }
        catch(Exception e)
        {
            e.printStackTrace();
            statusCode = "0";
            statusMessage = "Failed";
        }
        restResponse = ServiceUtils.convertObjToResponse(statusCode, statusMessage, null);
        return restResponse;

    }

    public void calculateReward(String parentId) throws Exception {
        Double parentChainBusiness = 0.00;
        String giftItem = "";
        Double giftAmount = 0.00;
        Double rewardCategoryAmount = 0.00;
        Integer rewardId = 0;
        // if parentChainBusiness >= defined reward limit
        // fetch reward category by parentchainbusiness value.
        //String selectChild = "Select b.ChainBusiness as ChainBusiness,a.AgentId as AgentId,a.AgentName as AgentName FROM AgentBusinessDetails a INNER JOIN AgentMaster b ON  a.Id = b.AgentId where b.SponsorId = parentId";
        String parentNode = "SELECT am.AgentId as AgentId, am.AgentName as AgentName,am.SponsorId as SponsorId,ab.ChainBusiness as ChainBusiness from hyperland.AgentMaster am INNER JOIN hyperland.AgentBusinessDetails ab ON ab.AgentId = am.AgentId  where am.AgentId =" + parentId + "\n";
        List resultDataList = masterDAO.getAllData(parentNode);
        if (resultDataList != null && resultDataList.size() > 0) {
            parentChainBusiness = ((BigDecimal) ((Map) resultDataList.get(0)).get("ChainBusiness")).doubleValue();
            String parentName = (String) ((Map) resultDataList.get(0)).get("AgentName");
            Map parentResult = (Map) resultDataList.get(0);
            String selectParentReward = "SELECT Id,MinRewardBaseAmount,GiftAmount, GiftItem from hyperland.MatrixReward WHERE " + parentChainBusiness + " BETWEEN MinRewardBaseAmount AND MaxRewardBaseAmount";
            List rewardList = masterDAO.getAllData(selectParentReward);
            if (rewardList != null && rewardList.size() > 0) {
                Map rewardMap = (Map) rewardList.get(0);
                giftAmount = (Double) rewardMap.get("GiftAmount");
                giftItem = (String) rewardMap.get("GiftItem");
                rewardCategoryAmount = ((Double) rewardMap.get("MinRewardBaseAmount"));
                rewardId = (Integer) rewardMap.get("Id");

                String childParentNode = "SELECT am.AgentId as AgentId, am.AgentName as AgentName,am.SponsorId as SponsorId,ab.ChainBusiness as ChainBusiness from hyperland.AgentMaster am INNER JOIN hyperland.AgentBusinessDetails ab ON ab.AgentId = am.AgentId  where am.SponsorId =" + parentId + "\n";
                List childDataList = masterDAO.getAllData(childParentNode);
                if (childDataList != null && childDataList.size() > 1) {
                    if (childDataList.size() == 2) {
                        Map result1 = (Map) childDataList.get(0);
                        Map result2 = (Map) childDataList.get(1);
                        Double chainBusiness1 = ((BigDecimal) result1.get("ChainBusiness")).doubleValue();
                        Double chainBusiness2 = ((BigDecimal) result2.get("ChainBusiness")).doubleValue();
                        boolean updateReward = false;
                        if (chainBusiness1 >= chainBusiness2) {
                            if (chainBusiness1 >= rewardCategoryAmount * .6 && chainBusiness2 >= rewardCategoryAmount * .4) {
                                //update entry in rewards
                                updateReward = true;
                            }
                        } else if (chainBusiness2 > chainBusiness1) {
                            if (chainBusiness2 >= rewardCategoryAmount * .6 && chainBusiness1 >= rewardCategoryAmount * .4) {
                                //update entry in rewards
                                updateReward = true;
                            }
                        }
                        if (updateReward) {
                            String checkRewardQuery = "Select AgentId from UserReward Where AgentId = " + parentId + " AND RewardId = " + rewardId;
                            List rewardDataList = masterDAO.getAllData(checkRewardQuery);
                            if (rewardDataList == null || rewardDataList.size() == 0) {
                                //insert entry in user reward table;
                                String rewardCategory = giftItem + " / " + giftAmount;
                                String insertReward = "INSERT INTO UserReward (RewardId,AgentId,AgentName,Status,RewardCategory) VALUES (" + rewardId + "," + parentId + ",'" + parentName + "', 'Pending', '" + rewardCategory + "')";
                                transactionDAO.insertDataBatch(new String[]{insertReward});
                            }
                        }

                    } else if (childDataList.size() == 3) {
                        Map result1 = (Map) childDataList.get(0);
                        Map result2 = (Map) childDataList.get(1);
                        Map result3 = (Map) childDataList.get(2);
                        Double chainBusiness1 = ((BigDecimal) result1.get("ChainBusiness")).doubleValue();
                        Double chainBusiness2 = ((BigDecimal) result2.get("ChainBusiness")).doubleValue();
                        Double chainBusiness3 = ((BigDecimal) result3.get("ChainBusiness")).doubleValue();
                        List<Double> chainBusinessList = new ArrayList<>();

                        chainBusinessList.add(chainBusiness1);
                        chainBusinessList.add(chainBusiness2);
                        chainBusinessList.add(chainBusiness3);
                        Collections.sort(chainBusinessList);
                        if (chainBusinessList.get(0) >= rewardCategoryAmount * .6 && (chainBusinessList.get(1) + chainBusinessList.get(2)) >= rewardCategoryAmount * .4) {
                            // before making and entry , first check the existense of same reward in
                            String checkRewardQuery = "Select count(*) from UserReward Where AgentId = " + parentId + " AND RewardId = " + rewardId;
                            List rewardDataList = masterDAO.getAllData(checkRewardQuery);
                            if (rewardDataList == null || rewardDataList.size() == 0) {
                                //insert entry in user reward table;
                                String rewardCategory = giftItem + " / " + giftAmount;
                                String insertReward = "INSERT INTO UserReward (RewardId,AgentId,AgentName,Status,RewardCategory) VALUES (" + rewardId + "," + parentId + ",'" + parentName + "', 'Pending', '" + rewardCategory + "')";
                                transactionDAO.insertDataBatch(new String[]{insertReward});
                            }
                        }
                    }
                }
            }
        }

        if(resultDataList!=null && resultDataList.size()>0){
            Map result1 = (Map) resultDataList.get(0);
            String sponsorId = (String)result1.get("SponsorId");
            String agentName = (String)result1.get("AgentName");
            calculateReward(sponsorId);
        }
    }

    public RestResponse getRewards(Integer agentId){
        String rewardQuery = "SELECT AgentId,AgentName,RewardId,RewardCategory,Status from UserReward";
        String whereClause = "";
        String statusCode = "";
        String statusMessage = "";
        List result = null;
        RestResponse restResponse = null;
        if(agentId!=null){
            whereClause = "AgentId = "+agentId;
            rewardQuery += " WHERE "+whereClause;
        }
        try{
            result = masterDAO.getAllData(rewardQuery);
            statusCode = "1";
            statusMessage = "Success";
        }
        catch(Exception e)
        {
            e.printStackTrace();
            statusCode = "0";
            statusMessage = "Failed";
        }
        restResponse = ServiceUtils.convertObjToResponse(statusCode, statusMessage, result);
        return restResponse;
    }

    public RestResponse updateRewards(String agentId, String rewardId, String issuedBy,String rewardOpted){
        String statusCode = "";
        String statusMessage = "";
        RestResponse restResponse = null;
        String updateReward = "UPDATE UserReward SET RewardOpted = '"+rewardOpted+"', STATUS = 'Done',IssuedBy = '"+issuedBy+"', IssuedOn = Now() WHERE RewardId = "+rewardId+" AND AgentId ="+agentId;
        try {
            transactionDAO.updateData(updateReward);
            statusCode = "1";
            statusMessage = "Success";
        }
        catch(Exception e){
            e.printStackTrace();
            statusCode = "0";
            statusMessage = "Failed";
        }
        restResponse = ServiceUtils.convertObjToResponse(statusCode, statusMessage, null);
        return restResponse;
    }

    @Override
    public RestResponse holdProperty(List plotIds,String userId) {
        String query[] = new String[plotIds.size()];
        int count = 0;
        for (Object plotId:
              plotIds) {
            String updatePlotDetails = "UPDATE PlotDetails set Status = 'Hold', HoldBy = '"+userId+"' , HoldOn = Now() WHERE Id = "+plotId  ;
            query[count] = updatePlotDetails;
            count++;
        }

        String statusCode = "";
        String statusMessage = "";
        RestResponse restResponse = null;
        try {
            transactionDAO.insertDataBatch(query);
            statusCode = "1";
            statusMessage = "Success";

        }
        catch(Exception e){
            statusCode = "0";
            statusMessage = "Failed";
        }
        restResponse = ServiceUtils.convertObjToResponse(statusCode, statusMessage, null);
        return restResponse;
    }

    @Override
    public RestResponse unHoldProperty(List plotIds) {
        String statusCode = "";
        String statusMessage = "";
        RestResponse restResponse = null;
        String query[] = new String[plotIds.size()];
        try {
            int count = 0;
            for (Object plotId:
                    plotIds) {
                String updatePlotDetails = "UPDATE PlotDetails set Status = 'Available', HoldBy = null , HoldOn = null WHERE Id = "+plotId  ;
                query[count] = updatePlotDetails;
                count++;
            }
            transactionDAO.insertDataBatch(query);
            statusCode = "1";
            statusMessage = "Success";

        }
        catch(Exception e){
            statusCode = "0";
            statusMessage = "Failed";
        }
        restResponse = ServiceUtils.convertObjToResponse(statusCode, statusMessage, null);
        return restResponse;

    }


}

