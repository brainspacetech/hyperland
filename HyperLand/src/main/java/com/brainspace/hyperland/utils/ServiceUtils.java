package com.brainspace.hyperland.utils;

import com.brainspace.hyperland.bo.Property;
import com.brainspace.hyperland.bo.RestResponse;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

                if (Integer.parseInt((String) columnNameList.get(1)) == 4 && customerDetails.get(key)!=null) {
                    value = customerDetails.get(key) != null ? Integer.valueOf(customerDetails.get(key).toString()) : null;
                } else if (Integer.parseInt((String) columnNameList.get(1)) == 93 && customerDetails.get(key)!=null) {
                  //  DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    String date1 = "";
                    if (customerDetails.get(key) != null) {
                        Instant instant = Instant.parse((String) customerDetails.get(key));
                        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Kolkata"));
                        value = new java.sql.Timestamp(java.util.Date.from(zonedDateTime.toInstant()).getTime());

                    } else {
                        value = null;
                    }
                } else if (Integer.parseInt((String) columnNameList.get(1)) == 3 && customerDetails.get(key)!=null) {
                    value = customerDetails.get(key) != null ? Double.valueOf(customerDetails.get(key).toString()) : null;
                } else {
                    value = customerDetails.get(key);
                }
                customerMap.put((String) columnNameList.get(0), value);
            }
        }
        return customerMap;
    }
    public static java.sql.Date convertStrToSQLDate(String strDate)
    {
        System.out.println("strDate -- "+strDate);
        Instant instant = Instant.parse(strDate);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Kolkata"));
       return new java.sql.Date(java.util.Date.from(zonedDateTime.toInstant()).getTime());
    }
}
