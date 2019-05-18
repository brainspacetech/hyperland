package com.brainspace.hyperland.service;

import com.brainspace.hyperland.bo.*;
import com.brainspace.hyperland.dao.IMasterDAO;
import com.brainspace.hyperland.utils.ConfigReader;
import com.brainspace.hyperland.utils.PlotCreation;
import com.brainspace.hyperland.utils.ServiceUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class MasterService implements IMasterService {
    @Autowired
    private IMasterDAO masterDAO;
    ConfigBO configBO = ConfigReader.getConfig();

    @Override
    public RestResponse getAllData(String type) throws Exception {
        RestResponse response = null;
        for (int i = 0; i < configBO.getServiceObj().length; i++) {
            if (configBO.getServiceObj()[i].getId().equalsIgnoreCase(type)) {
                Service serviceObj = configBO.getServiceObj()[i];
                String statusCode = "";
                String statusMessage = "";
                List list = null;
                try {
                    list = masterDAO.getAllData(serviceObj.getSelectQuery());
                    statusCode = "1";
                    if (list.size() == 0)
                        statusCode = "2";
                    statusMessage = "Success";
                } catch (Exception e) {
                    e.printStackTrace();
                    statusCode = "0";
                    statusMessage = "Failed";
                }
                response = ServiceUtils.convertObjToResponse(statusCode, statusMessage, list);
                break;
            }
        }

        return response;
    }

    @Override
    public RestResponse getDataById(String type, int id) {
        RestResponse response = null;
        for (int i = 0; i < configBO.getServiceObj().length; i++) {
            if (configBO.getServiceObj()[i].getId().equalsIgnoreCase(type)) {
                Service serviceObj = configBO.getServiceObj()[i];
                String statusCode = "";
                String statusMessage = "";
                Object result = null;
                try {
                    result = masterDAO.getDataById(serviceObj.getSelectQueryById(), id);
                    statusCode = "1";
                    if (result == null)
                        statusCode = "2";
                    statusMessage = "Success";
                } catch (Exception e) {
                    e.printStackTrace();
                    statusCode = "0";
                    statusMessage = "Failed";
                }
                response = ServiceUtils.convertObjToResponse(statusCode, statusMessage, result);
                break;
            }
        }

        return response;
    }

    @Override
    public RestResponse addData(String type, Object object,String createdBy) {
        RestResponse response = null;
        for (int i = 0; i < configBO.getServiceObj().length; i++) {
            if (configBO.getServiceObj()[i].getId().equalsIgnoreCase(type)) {
                String statusCode = "";
                String statusMessage = "";
                try {
                    String sql = configBO.getServiceObj()[i].getInsertQuery();
                    String params = sql.substring(sql.indexOf('(') + 1, sql.indexOf(')'));
                    String paramsArr[] = params.split(",");
                    Map<String, Object> propertyMap = (Map) object;
                    PropertyMapping propertyMapping = configBO.getServiceObj()[i].getPropertyMapping();
                    Property property[] = propertyMapping.getProperty();
                    Map<String, List> jsonColumnMap = new ServiceUtils().propertyMapper(property);
                    String value = "";
                    Object arguments[] = new Object[paramsArr.length];
                    int argumentTypes[] = new int[paramsArr.length];
                    for (int j = 0; j < paramsArr.length; j++) {
                        System.out.println("paramsArr[j].trim() -- "+paramsArr[j].trim());
                        List jsonColTypeList = jsonColumnMap.get(paramsArr[j].trim());
                        Object colValue = propertyMap.get(jsonColTypeList.get(0));
                        arguments[j] = colValue;
                        argumentTypes[j] = Integer.parseInt((String) jsonColTypeList.get(1));
                        if(argumentTypes[j] == 93 && arguments[j] !=null ){
                            Instant instant = Instant.parse((String) arguments[j]);
                            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Kolkata"));
                            arguments[j] = new java.sql.Date(Date.from(zonedDateTime.toInstant()).getTime());
                        }
                    }
                    Object  objId = masterDAO.addData(sql,arguments,argumentTypes);
                    //if type == agent create user and user roles ->AGENT
                    if(type.equalsIgnoreCase("agent") || type.equalsIgnoreCase("user"))
                    {
                        // make entry in agent business details table
                        String password = "";
                        if(propertyMap.get("panNumber")!=null)
                        {
                            password += propertyMap.get("panNumber").toString().toLowerCase();
                        }
                        if(propertyMap.get("dateOfBirth")!=null)
                        {
                            if(propertyMap.get("dateOfBirth")!=null)
                            {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
                                Instant instant = Instant.parse((String) propertyMap.get("dateOfBirth"));
                                ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Kolkata"));
                                password+=zonedDateTime.getDayOfMonth()+""+zonedDateTime.getMonthValue()+""+zonedDateTime.getYear();
                            }
                        }
                        String encodedPassword = new BCryptPasswordEncoder().encode(password);
                        String userid = "O"+objId;
                        System.out.println(Integer.parseInt(objId.toString()) + " === " + password);
                        if(type.equalsIgnoreCase("agent") ) {
                            userid = "A"+objId;
                            String insertBDQuery = "INSERT INTO AgentBusinessDetails(AgentId) VALUE (" + Integer.parseInt(objId.toString()) + ")";
                            masterDAO.updateData(insertBDQuery);
                        }

                        String insertUser = "INSERT INTO user (username,password) VALUES ('"+userid+"','" + encodedPassword + "')";
                        String insertUserRole = "INSERT INTO user_roles (username,role) VALUES ('" + userid + "','ROLE_AGENT')";

                        masterDAO.updateData(insertUser);
                        masterDAO.updateData(insertUserRole);
                    }
                    statusCode = "1";
                    statusMessage = "Success";
                } catch (Exception e) {
                    e.printStackTrace();
                    statusCode = "0";
                    statusMessage = "Failed";
                }
                response = ServiceUtils.convertObjToResponse(statusCode, statusMessage,null);
                break;
            }
        }

        return response;
    }

    @Override
    public RestResponse updateData(String type, int id, Object data) {
        String statusCode = "";
        String statusMessage = "";
        RestResponse response = null;
        for (int i = 0; i < configBO.getServiceObj().length; i++) {
            if (configBO.getServiceObj()[i].getId().equalsIgnoreCase(type)) {
                try {
                    String sql = configBO.getServiceObj()[i].getUpdateQuery();
                    String params = sql.substring(sql.indexOf(" SET") + 4, sql.indexOf("WHERE"));
                    String paramsArr[] = params.split(",");
                    Map<String, Object> propertyMap = (Map) data;
                    PropertyMapping propertyMapping = configBO.getServiceObj()[i].getPropertyMapping();
                    Property property[] = propertyMapping.getProperty();
                    Map<String, List> jsonColumnMap = new ServiceUtils().propertyMapper(property);
                    String value = "";
                    Object arguments[] = new Object[paramsArr.length + 1];
                    int argumentTypes[] = new int[paramsArr.length + 1];
                    int count = 0;
                    for (int j = 0; j < paramsArr.length; j++) {
                        count++;
                        String colName = paramsArr[j].trim();
                        colName = colName.substring(0, colName.indexOf(("=")) - 1);
                        List jsonColTypeList = jsonColumnMap.get(colName);
                        Object colValue = propertyMap.get(jsonColTypeList.get(0));
                        arguments[j] = colValue;
                        argumentTypes[j] = Integer.parseInt((String) jsonColTypeList.get(1));
                        if(argumentTypes[j] == 93 && arguments[j] !=null ){
                            Instant instant = Instant.parse((String) arguments[j]);
                            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Kolkata"));
                            arguments[j] = new java.sql.Date(Date.from(zonedDateTime.toInstant()).getTime());
                        }

                    }
                    arguments[count] = id;
                    argumentTypes[count] = 4;
                    masterDAO.updateData(sql, arguments, argumentTypes);
                    statusCode = "1";
                    statusMessage = "Success";
                } catch (Exception e) {
                    e.printStackTrace();
                    statusCode = "0";
                    statusMessage = "Failed";
                }
                response = ServiceUtils.convertObjToResponse(statusCode, statusMessage, null);
                break;
            }
        }
        return response;
    }

    public RestResponse deleteData(String type, int id) {
        String statusCode = "";
        String statusMessage = "";
        RestResponse response = null;
        for (int i = 0; i < configBO.getServiceObj().length; i++) {
            if (configBO.getServiceObj()[i].getId().equalsIgnoreCase(type)) {
                try {
                    String sql = configBO.getServiceObj()[i].getDeleteQuery();
                    masterDAO.deleteData(sql, id);
                    statusCode = "1";
                    statusMessage = "Success";
                } catch (Exception e) {
                    e.printStackTrace();
                    statusCode = "0";
                    statusMessage = "Failed";
                }
                response = ServiceUtils.convertObjToResponse(statusCode, statusMessage, null);
                break;
            }
        }
        return response;
    }

    public RestResponse addLandData(Object requestObject,String createdBy){
        String type = "land";
        RestResponse response = null;
        for (int i = 0; i < configBO.getServiceObj().length; i++) {
            if (configBO.getServiceObj()[i].getId().equalsIgnoreCase(type)) {
                String statusCode = "";
                String statusMessage = "";
                try {
                    String sql = configBO.getServiceObj()[i].getInsertQuery();
                    String params = sql.substring(sql.indexOf('(') + 1, sql.indexOf(')'));
                    String paramsArr[] = params.split(",");
                    Map<String, Object> propertyMap = (Map) requestObject;
                    PropertyMapping propertyMapping = configBO.getServiceObj()[i].getPropertyMapping();
                    Property property[] = propertyMapping.getProperty();
                    Map<String, List> jsonColumnMap = new ServiceUtils().propertyMapper(property);
                    String value = "";
                    Object arguments[] = new Object[paramsArr.length];
                    int argumentTypes[] = new int[paramsArr.length];
                    for (int j = 0; j < paramsArr.length; j++) {
                        System.out.println("paramsArr[j].trim() -- "+paramsArr[j].trim());
                        List jsonColTypeList = jsonColumnMap.get(paramsArr[j].trim());
                        Object colValue = propertyMap.get(jsonColTypeList.get(0));
                        arguments[j] = colValue;
                        argumentTypes[j] = Integer.parseInt((String) jsonColTypeList.get(1));
                    }
                    BigInteger landId = (BigInteger)masterDAO.addData(sql, arguments, argumentTypes);
                    //addFarmer
                    Map<String,Object> requestMap = (Map<String, Object>) requestObject;
                    List<Map> farmerList = (List<Map>) requestMap.get("farmers");
                    for(Map farmer:farmerList){
                        farmer.put("LandId",landId);
                        farmer.put("FarmerName",farmer.get("farmerName"));
                        farmer.put("PanNumber",farmer.get("panNumber"));
                    }
                    String farmerInsertQuery = "INSERT INTO FarmerMaster(LandId, FarmerName,AdhaarNo,PanNumber) VALUES (?,?,?,?) ";
                    masterDAO.insertDataBatch(farmerInsertQuery, farmerList);

                    statusCode = "1";
                    statusMessage = "Success";
                } catch (Exception e) {
                    e.printStackTrace();
                    statusCode = "0";
                    statusMessage = "Failed";
                }
                response = ServiceUtils.convertObjToResponse(statusCode, statusMessage, null);
                break;
            }
        }

        return response;
    }
    @Override
    public RestResponse getLandDataById(String type,int id){
        String statusCode = "";
        String statusMessage = "";
        RestResponse restResponse = null;
        Map landData = null;
        try {
            String landQuery = "SELECT Id as id, KhasraNumber as khasraNumber, LandAmount as landAmount FROM LandMaster Where Id = ?";
            landData = masterDAO.getDataById(landQuery, id);
            String farmerQuery = "SELECT FarmerName as farmerName, panNumber as panNumber FROM FarmerMaster Where LandId = " + id;
            List<Map> farmerData = masterDAO.getAllData(farmerQuery);
            landData.put("farmers",farmerData);
            statusCode = "1";
            statusMessage = "Success";
        }
        catch(Exception e)
        {
            statusCode = "1";
            statusMessage = "Success";
        }
        restResponse = ServiceUtils.convertObjToResponse(statusCode,statusMessage,landData);
        return restResponse;
    }

    @Override
    public RestResponse addMenuConfig(String menuConfig) {

        RestResponse response = null;
        String deleteQuery = "DELETE FROM MenuConfig";
        masterDAO.updateData(deleteQuery);
        String query = "INSERT INTO MenuConfig (MenuConfig) VALUES ('"+menuConfig+"')";
        masterDAO.updateData(query);
        return response;
    }
    @Override
    public RestResponse addRoleMenuConfig(String menuConfig,String role) {
        RestResponse response = null;
        String deleteQuery = "DELETE FROM RoleMenuConfig WHERE Role = 'ROLE_"+role+"'";
        System.out.println("deleteQuery -- "+deleteQuery);
        masterDAO.updateData(deleteQuery);
        String query = "INSERT INTO RoleMenuConfig (Role,MenuConfig) VALUES ('ROLE_"+role+"','"+menuConfig+"')";
        masterDAO.updateData(query);
        return response;
    }

    @Override
    public RestResponse getMenuConfig(){
        String statusCode = "";
        String statusMessage = "";
        RestResponse restResponse = null;
        Map landData = null;
        String menuConfig = "";
        try {
            String userName = new ServiceUtils().getUserName();
            String fetchMenuConfig = "select rm.MenuConfig as MenuConfig from  MenuConfig rm";
            List<Map> menuConfigList = masterDAO.getAllData(fetchMenuConfig);
            if(menuConfigList.size()>0) {
                menuConfig = (String)((Map)menuConfigList.get(0)).get("MenuConfig");
            }
            statusCode = "1";
            statusMessage = "Success";
        }
        catch(Exception e)
        {
            statusCode = "1";
            statusMessage = "Success";
        }
        restResponse = ServiceUtils.convertObjToResponse(statusCode,statusMessage,menuConfig);
        return restResponse;
    }

    @Override
    public RestResponse getRoleMenuConfig(){
        String statusCode = "";
        String statusMessage = "";
        RestResponse restResponse = null;
        Map menuConfig = null;
        try
        {
            String userName = new ServiceUtils().getUserName();
            String fetchMenuConfig = "select rm.MenuConfig as MenuConfig from  RoleMenuConfig rm INNER JOIN user_roles ur ON ur.role = rm.Role where username  = '"+userName+"'";
            List<Map> menuConfigList = masterDAO.getAllData(fetchMenuConfig);
            menuConfig = menuConfigList.get(0);
            statusCode = "1";
            statusMessage = "Success";
        }
        catch(Exception e)
        {
            statusCode = "1";
            statusMessage = "Success";
        }
        restResponse = ServiceUtils.convertObjToResponse(statusCode,statusMessage,menuConfig);
        return restResponse;
    }

    public RestResponse createPlots(InputStream inputStream)
    {

        String statusCode = "";
        String statusMessage ="";
        try {
            PlotCreation plotCreation = new PlotCreation();
            plotCreation.createPlot(masterDAO, inputStream);
            statusCode = "1";
            statusMessage = "Success";
        }
        catch(Exception e)
        {
            e.printStackTrace();
            statusCode = "0";
            statusMessage = "Failed";
        }
        return ServiceUtils.convertObjToResponse(statusCode,statusMessage,null);

    }


    public RestResponse createFirm(InputStream logoFile, String firmName){
        String statusCode = "";
        String statusMessage ="";
        try {
            String insertQuery = "INSERT INTO FirmMaster (FirmName,Logo) VALUES (?,?)";
            masterDAO.insertBlobData(insertQuery, logoFile, firmName);
            statusCode = "1";
            statusMessage = "Success";
        }
        catch(Exception e)
        {
            e.printStackTrace();
            statusCode = "0";
            statusMessage = "Failed";
        }
        return ServiceUtils.convertObjToResponse(statusCode,statusMessage,null);
    }
}
