package com.brainspace.hyperland.dao;

import java.util.List;

import com.brainspace.hyperland.bo.Firm;

public interface IFirmDAO {
	List<Firm> getAllFirm()  throws Exception ;
    Firm getFirmById(int firmId);
    void addFirm(Firm firm);
    void updateFirm(Firm firm);
    void deleteFirm(Firm firmId);
}
