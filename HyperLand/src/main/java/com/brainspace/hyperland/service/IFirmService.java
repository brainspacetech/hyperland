package com.brainspace.hyperland.service;

import com.brainspace.hyperland.bo.Firm;
import com.brainspace.hyperland.bo.RestResponse;

public interface IFirmService {

	RestResponse getAllFirms();
	RestResponse getFirmById(int firmId);
	RestResponse addFirm(Firm firm);
	/*RestResponse updateFirm(Firm firm);
	RestResponse deleteFirm(int firmId);*/
}
