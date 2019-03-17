package com.brainspace.hyperland.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.brainspace.hyperland.bo.Firm;
import com.brainspace.hyperland.bo.RestResponse;
import com.brainspace.hyperland.dao.IFirmDAO;
import com.brainspace.hyperland.utils.ServiceUtils;

@Service
public class FirmService implements IFirmService{

	@Autowired
	private IFirmDAO firmDao;

	@Override
	public RestResponse getAllFirms() {
		String statusCode = "";
		String statusMessage = "";
		List<Firm> list = null;
		try {
			list = firmDao.getAllFirm();
			statusCode = "1";
			if(list.size() == 0)
				statusCode = "2";
			statusMessage = "Success";
		}
		catch(Exception e)
		{
			statusCode = "0";
			statusMessage = "Failed";
		}
		RestResponse response = ServiceUtils.convertObjToResponse(statusCode, statusMessage, list);
		return  response;
	}

	@Override
	public RestResponse getFirmById(int firmId) {
		String statusCode = "";
		String statusMessage = "";
		Firm firm = null;
		try {
			firm = firmDao.getFirmById(firmId);
			statusCode = "1";
			if(firm == null)
				statusCode = "2";
			statusMessage = "Success";
		}
		catch(Exception e)
		{
			statusCode = "0";
			statusMessage = "Failed";
		}
		RestResponse response = ServiceUtils.convertObjToResponse(statusCode, statusMessage, firm);
		return  response;
	}

	@Override
	public RestResponse addFirm(Firm firm) {
		String statusCode = "";
		String statusMessage = "";
		try {
			firmDao.addFirm(firm);
			statusCode = "1";
			statusMessage = "Success";
		}
		catch(Exception e)
		{
			statusCode = "0";
			statusMessage = "Failed";
		}
		RestResponse response = ServiceUtils.convertObjToResponse(statusCode, statusMessage, null);
		return  response;
	}

	/*@Override
	public void updateFirm(Firm firm) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteFirm(int firmId) {
		// TODO Auto-generated method stub

	}
	 */
}
