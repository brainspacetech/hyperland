package com.brainspace.hyperland.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.brainspace.hyperland.bo.RestResponse;
import com.brainspace.hyperland.bo.SelectorBO;
import com.brainspace.hyperland.dao.ISelectorDAO;
import com.brainspace.hyperland.utils.ServiceUtils;

@Service
public class SelectorService implements ISelectorService{

	@Autowired
	private ISelectorDAO selectorDao;
	
	@Override
	public RestResponse getSelector(String type,String value) {
		String statusCode = "";
		String statusMessage = "";
		List<SelectorBO> selectorList = null;
		try {
			selectorList = selectorDao.getSelectorValue(type,value);
			statusCode = "1";
			if(selectorList.size() == 0)
				statusCode = "2";
			statusMessage = "Success";
		}
		catch(Exception e)
		{
			statusCode = "0";
			statusMessage = "Failed";
		}
		return ServiceUtils.convertObjToResponse(statusCode, statusMessage, selectorList);
	}

}
