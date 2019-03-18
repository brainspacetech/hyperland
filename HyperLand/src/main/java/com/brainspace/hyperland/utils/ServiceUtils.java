package com.brainspace.hyperland.utils;

import com.brainspace.hyperland.bo.Property;
import com.brainspace.hyperland.bo.RestResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceUtils {

	public static RestResponse convertObjToResponse(String statusCode,String statusMessage, Object result)
	{
		RestResponse response = new RestResponse();
		response.setStatusCode(statusCode);
		response.setStatusMessage(statusMessage);
		if(result != null)
			response.setResult(result);
		return response;
	}
	public Map<String,List> propertyMapper(Property property[])
	{
		Map<String, List> propertyMap = new HashMap<>();
 		for(int i = 0 ; i < property.length;i++)
		{
			List arr = new ArrayList();
			arr.add(property[i].getName());
			arr.add(property[i].getDataType());
			propertyMap.put(property[i].getColumnName(),arr);
		}
 		return propertyMap;
	}
}
