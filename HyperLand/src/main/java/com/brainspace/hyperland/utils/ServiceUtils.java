package com.brainspace.hyperland.utils;

import com.brainspace.hyperland.bo.RestResponse;

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
}
