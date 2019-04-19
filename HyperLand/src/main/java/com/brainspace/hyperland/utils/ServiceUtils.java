package com.brainspace.hyperland.utils;

import com.brainspace.hyperland.bo.Property;
import com.brainspace.hyperland.bo.RestResponse;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ServiceUtils {

    public static RestResponse convertObjToResponse(String statusCode, String statusMessage, Object result) {
        RestResponse response = new RestResponse();
        response.setStatusCode(statusCode);
        response.setStatusMessage(statusMessage);
        if (result != null)
            response.setResult(result);
        return response;
    }

    public Map<String, List> propertyMapper(Property property[]) {
        Map<String, List> propertyMap = new HashMap<>();
        for (int i = 0; i < property.length; i++) {
            List arr = new ArrayList();
            arr.add(property[i].getName());
            arr.add(property[i].getDataType());
            propertyMap.put(property[i].getColumnName(), arr);
        }
        return propertyMap;
    }

    public Map<String, List> jsonColumnNameMapper(Property property[]) {
        Map<String, List> propertyMap = new HashMap<>();
        for (int i = 0; i < property.length; i++) {
            List arr = new ArrayList();
            arr.add(property[i].getColumnName());
            arr.add(property[i].getDataType());
            propertyMap.put(property[i].getName(), arr);
        }
        return propertyMap;
    }

    public Map customerMap(Map customerDetails, Map<String, List> jsonColumnMap) {
        Map<String, Object> customerMap = new HashMap<>();
        if (customerDetails != null) {
            Set<String> keys = customerDetails.keySet();
            for (String key : keys) {
                List columnNameList = jsonColumnMap.get(key);
                System.out.println(key + " -- " + customerDetails.get(key));
                Object value = "";

                if (Integer.parseInt((String) columnNameList.get(1)) == 4) {
                    value = customerDetails.get(key) != null ? Integer.valueOf(customerDetails.get(key).toString()) : null;
                } else if (Integer.parseInt((String) columnNameList.get(1)) == 91) {
                  //  DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    String date1 = "";
                    if (customerDetails.get(key) != null) {
                        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        DateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

                        try {
                            java.util.Date date = sdf.parse("2019-04-09T00:00:26Z");
                            sdf.setTimeZone(TimeZone.getTimeZone("IST"));
                            date1 = sdf1.format(date);
                            System.out.println(date1);
                            System.out.println(Date.valueOf(date1));
                            value = Date.valueOf(date1);
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }

                    } else {
                        value = null;
                    }
                } else if (Integer.parseInt((String) columnNameList.get(1)) == 3) {
                    value = customerDetails.get(key) != null ? Double.valueOf(customerDetails.get(key).toString()) : null;
                } else {
                    value = customerDetails.get(key);
                }
                customerMap.put((String) columnNameList.get(0), value);
            }
        }
        return customerMap;
    }
}
