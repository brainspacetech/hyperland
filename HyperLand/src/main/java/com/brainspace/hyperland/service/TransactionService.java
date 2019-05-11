package com.brainspace.hyperland.service;

import com.brainspace.hyperland.bo.*;
import com.brainspace.hyperland.dao.IMasterDAO;
import com.brainspace.hyperland.dao.ITransactionDAO;

import com.brainspace.hyperland.utils.ConfigReader;
import com.brainspace.hyperland.utils.ServiceUtils;
import com.brainspace.hyperland.utils.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import sun.java2d.pipe.SpanShapeRenderer;

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
    @Autowired
    private IMasterDAO masterDAO;

    @Override
    public RestResponse createBooking(Object requestObject, String createdBy) {
        List<Map> installmentList = new ArrayList<>();
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
                bookingDetails.put("totalPaidAmount", mainObject.get("totalAmount"));
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
                if(((String)mainObject.get("amountType")).equalsIgnoreCase("Token"))
                {
                    Double bookingAmount = Double.parseDouble(mainObject.get("bookingAmount").toString());
                   Double remainingBookingAmount  = Double.parseDouble(mainObject.get("toakenAmount").toString()) - bookingAmount;
                   if(remainingBookingAmount>0)
                   {
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
                String password = "";
                if(customerDetails.get("panNumber")!=null)
                {
                    password += customerDetails.get("panNumber");
                }
                if(customerDetails.get("dateOfBirth")!=null)
                {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
                    Instant instant = Instant.parse((String) customerDetails.get("dateOfBirth"));
                    ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Kolkata"));
                    password+=zonedDateTime.getMonthValue()+""+zonedDateTime.getMonthValue()+""+zonedDateTime.getYear();
                }
                System.out.println("C"+bookingId.toString()+" === "+password);
                    createUserAndRole("C"+bookingId.toString(), password, "ROLE_CUSTOMER");
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
                paymentMap.put("amount", mainObject.get("bookingAmount"));
                paymentMap.put("transactionDate", mainObject.get("transactionDate"));
                //   installmentList.add(serviceUtils.customerMap(paymentMap, jsonColumnMap));

                Map convertedPaymentMap = serviceUtils.customerMap(paymentMap, jsonColumnMap);
                ArrayList paymentMapList = new ArrayList();
                paymentMapList.add(convertedPaymentMap);
                transactionDAO.insertDataBatch(insertQuery, paymentMapList);
            }
            if (((String) bookingDetails.get("paymentType")).equalsIgnoreCase("installment") && transactions.getTransaction()[i].getId().equalsIgnoreCase("installment")) {
                Map<String, List> jsonColumnMap = serviceUtils.jsonColumnNameMapper(property);
                insertQuery = transactions.getTransaction()[i].getInsertQuery();
                String installlmentDueDate = (String) mainObject.get("installmentStartDate");
                if(installmentList.size() > 0)
                {
                    installmentList.remove(0);
                    installmentList.add(serviceUtils.customerMap(installmentList.get(0), jsonColumnMap));
                }
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
                TransactionUtils transactionUtils = new TransactionUtils();
                Double bookingAmount = Double.valueOf(bookingDetails.get("bookingAmount").toString());
                String particulars = bookingDetails.get("customerName") + " - " + bookingDetails.get("bookingAmount") + " - ";
                transactionUtils.addBalanceEntry(transactionDAO, (String) bookingDetails.get("paymentMode"), bookingAmount, particulars, null, createdBy);
                transactionDAO.insertDataBatch(insertQuery, installmentList);

            }
        }
        if (bookingDetails.get("bookingType").toString().equalsIgnoreCase("MLM")) {
            try {
                agentCommissionCalculation(Integer.parseInt(bookingDetails.get("firmId").toString()), Integer.parseInt(bookingDetails.get("projectId").toString()), Double.parseDouble(bookingDetails.get("totalPaidAmount").toString()), Integer.parseInt(bookingDetails.get("agentId").toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //used for Farmer and Agent Payment Entry
    public void createPayment(Map paymentMap, String type, String createdBy) {
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
                    String updateQuery = "UPDATE LandMaster SET PaidAmount = CASE WHEN PaidAmount = 0.00  THEN " + paidAmount + "  ELSE PaidAmount + " + paidAmount + " END WHERE Id = " + paymentMap.get("landId");
                    String updateFarmerMasterQuery = "UPDATE FarmerMaster SET PaidAmount = CASE WHEN PaidAmount = 0.00  THEN " + paidAmount + "  ELSE PaidAmount + " + paidAmount + " END WHERE Id = " + paymentMap.get("farmerId");
                    transactionDAO.insertDataBatch(new String[]{updateQuery, updateFarmerMasterQuery});
                } else if (type.equalsIgnoreCase("agentPayment")) {
                    Double paidAmount = Double.valueOf(paymentMap.get("paymentAmount").toString());
                    String updateQuery = " UPDATE AgentBusinessDetails SET AmountPaidTillNow = CASE WHEN AmountPaidTillNow = 0.00 THEN " + paidAmount + "  ELSE AmountPaidTillNow " + paidAmount + " END WHERE Id = " + paymentMap.get("agentId");
                    transactionDAO.updateData(updateQuery);
                }

            }

        }

    }


    //used for Daily Expense / Farmer Payment / Agent Payment Approval / Property Cancellation Payment - > make and entry in Day book Entry table
    public void approvePayment(String id, String type, String approvedBy) {
        ConfigBO configBO = ConfigReader.getConfig();
        Transactions transactions = configBO.getTransactions();
        String updateQuery;
        for (Transaction transaction : transactions.getTransaction()) {
            if (transaction.getId().equalsIgnoreCase(type)) {
                updateQuery = transaction.getUpdateQuery();
                updateQuery = updateQuery.replace("{1}", "'" + approvedBy + "'");
                updateQuery = updateQuery.replace("{2}", id);
                transactionDAO.updateData(updateQuery);
                // after payment make an entry in DaybookEntry table

            }
        }
    }

    // cancel property, update IsCancelled = 'Y' in BookingDetails table
    public void cancelProperty(String bookingId) {

    }

    public void updateTransaction(Map restRequest, String type, String id, String createdBy) {
        // in case of installment update, update booking details table with total amount.
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
                transactionDAO.addData(sql, arguments, argumentTypes); // add entry in table
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
            String statusCode = "";
            String statusMessage = "";
            RestResponse response = null;
            restRequest.remove("dueDate");
            for (int i = 0; i < configBO.getTransactions().getTransaction().length; i++) {
                if (configBO.getTransactions().getTransaction()[i].getId().equalsIgnoreCase(type)) {
                    try {
                        String sql = configBO.getTransactions().getTransaction()[i].getUpdateQuery();
                        PropertyMapping propertyMapping = configBO.getTransactions().getTransaction()[i].getPropertyMapping();
                        Property property[] = propertyMapping.getProperty();
                        Map<String, List> jsonColumnMap = new ServiceUtils().jsonColumnNameMapper(property);
                        restRequest.put("paymentDate", restRequest.get("paymentDate"));
                        if (restRequest.get("pendingAmount") != null) {
                            Double pendingAmount = Double.parseDouble(restRequest.get("pendingAmount").toString());
                            if (pendingAmount < 0.00) {
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
                            try {
                                agentCommissionCalculation(Integer.parseInt(bookingData.get("FirmId").toString()), Integer.parseInt(bookingData.get("projectId").toString()), totalAmountPaid, Integer.parseInt(bookingData.get("agentId").toString()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        statusCode = "1";
                        statusMessage = "Success";
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        statusCode = "0";
                        statusMessage = "Failed";
                    }
                    response = ServiceUtils.convertObjToResponse(statusCode, statusMessage, null);
                    break;
                }
            }
        }

    }

    private void agentCommissionCalculation(int firmId, int projectId, Double amount, int agentId) throws Exception {
        // deduct percentage from amount. If property has 80% business value then calculate commission amount from (amount*80)

        String selectProjectMaster = "SELECT BusinessValue as businessValue from ProjectMaster WHERE FirmId = " + firmId + " and ProjectId =" + projectId;
        List projectList = masterDAO.getAllData(selectProjectMaster);
        Integer busnessValuePercentage = Integer.parseInt(((Map) projectList.get(0)).get("businessValue").toString());
        Double businessValue = amount * busnessValuePercentage / 100;
        String queries[] = null;
        int count = 0;
        Double chainBusiness = 0.00;
        int sellerAgentLevel = 0;
        Double sellerAgentCommission = 0.00;
        // fetch all parent agents.
        String selectChainAgents = "WITH RECURSIVE category_path (AgentId, AgentName, SponsorId,Designation,SelfBusiness,TotalCommission,Commission,MaxTarget) AS " +
                "(" +
                "   SELECT am.AgentId, am.AgentName, am.SponsorId,am.Designation,ab.SelfBusiness,ab.TotalCommission,mp.Commission,mp.MaxTarget " +
                "    FROM  hyperland.AgentMaster am INNER JOIN hyperland.AgentBusinessDetails ab INNER JOIN hyperland.MatrixPlan mp ON mp.Level =am.Designation and am.AgentId = ab.AgentId " +
                "    WHERE am.AgentId = " + agentId +
                "  UNION ALL" +
                "  SELECT c.AgentId, c.AgentName, c.SponsorId,c.Designation, ab.SelfBusiness,ab.TotalCommission,mp.Commission,mp.MaxTarget " +
                "    FROM category_path AS cp JOIN  hyperland.AgentMaster AS c INNER JOIN hyperland.AgentBusinessDetails  ab   INNER JOIN hyperland.MatrixPlan mp ON mp.Level = c.Designation and ab.AgentId = cp.SponsorId and cp.SponsorId = c.AgentId " +
                ")" +
                "" + "SELECT * FROM category_path cp ";

        List allAgentList = masterDAO.getAllData(selectChainAgents);
        queries = new String[allAgentList.size()];
        for (Object agentObject : allAgentList) {
            Map agentMap = (Map) agentObject;
            Double commissionAmount = 0.00;
            if (Integer.parseInt(agentMap.get("agentId").toString()) == agentId) {
                Double commissionPerc = Double.parseDouble(agentMap.get("Commission").toString());
                Double existingSelfBusiness = Double.parseDouble(agentMap.get("SelfBusiness").toString());
                Double totalSelfBusiness = businessValue;
                Double maxTarget = Double.parseDouble(agentMap.get("MaxTarget").toString());
                sellerAgentLevel = Integer.parseInt(agentMap.get("Designation").toString());

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

                String updateABQuery = "UPDATE AgentBusinessDetails SET  TotalBusiness = CASE WHEN TotalBusiness = 0.00  THEN " + businessValue + "  ELSE TotalBusiness + " + businessValue + " END, SelfBusiness = CASE WHEN SelfBusiness = 0.00  THEN " + businessValue + "  ELSE SelfBusiness + " + businessValue + " END,  TotalCommission = CASE WHEN TotalCommission = 0.00  THEN " + commissionAmount + "  ELSE TotalCommission + " + commissionAmount + " END WHERE AgentId = " + agentMap.get("agentId");
                queries[count] = updateABQuery;
            } else {
                Double chainAgentCommission = Double.parseDouble(agentMap.get("Commission").toString());
                int chainAgentLevel = Integer.parseInt(agentMap.get("Designation").toString());

                if (chainAgentLevel > sellerAgentLevel) {
                    commissionAmount = businessValue * (chainAgentCommission - sellerAgentCommission) / 100;

                }
                sellerAgentCommission = chainAgentCommission;
                String updateABQuery = "UPDATE AgentBusinessDetails SET TotalCommission = CASE WHEN TotalCommission = 0.00  THEN " + commissionAmount + "  ELSE TotalCommission + " + commissionAmount + " END WHERE AgentId = " + agentMap.get("agentId");
                queries[count] = updateABQuery;
            }
            count++;
        }
        //update commissionAmount and chain business in agent business details
        transactionDAO.insertDataBatch(queries);
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
        String insertquery = "INSERT INTO user (username,password) VALUES (" + userid + ",'" + encodedPassword + "')";
        String insertRole = "INSERT INTO user_roles(username,role) VALUES (" + userid + ",'" + role + "')";
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
}
