package com.brainspace.hyperland.service;

import com.brainspace.hyperland.bo.*;
import com.brainspace.hyperland.dao.IMasterDAO;
import com.brainspace.hyperland.utils.ConfigReader;
import com.brainspace.hyperland.utils.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;

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
    public RestResponse addData(String type, Object object) {
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
                        List jsonColTypeList = jsonColumnMap.get(paramsArr[j]);
                        Object colValue = propertyMap.get(jsonColTypeList.get(0));
                        arguments[j] = colValue;
                        argumentTypes[j] = Integer.parseInt((String) jsonColTypeList.get(1));
                    }
                    masterDAO.addData(sql, arguments, argumentTypes);
                    statusCode = "1";
                    statusMessage = "Success";
                } catch (Exception e) {
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

    public RestResponse deleteData(String type,int id )
    {
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
}
