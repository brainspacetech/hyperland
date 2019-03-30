package com.brainspace.hyperland.service;

import com.brainspace.hyperland.bo.Firm;
import com.brainspace.hyperland.bo.RestResponse;
import com.brainspace.hyperland.bo.Service;

public interface IMasterService {

	RestResponse getAllData(String type)  throws Exception;
	RestResponse getDataById(String type , int id);
	RestResponse addData(String type, Object requestObject,String createdBy);
	RestResponse updateData(String type,int id, Object data);
	RestResponse deleteData(String type,int id);
	/*RestResponse updateFirm(Firm firm);
	RestResponse deleteFirm(int firmId);*/
}
