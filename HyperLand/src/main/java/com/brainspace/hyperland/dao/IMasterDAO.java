package com.brainspace.hyperland.dao;

import java.util.List;
import java.util.Map;

import com.brainspace.hyperland.bo.Firm;

public interface IMasterDAO {
	List getAllData(String query)  throws Exception ;
    Map<String,Object> getDataById(String query, int firmId);
    Object addData(String query,Object params[],int argTypes[]);
    void updateData(String query,Object params[],int argTypes[]);
    void updateData(String query);
    void deleteData(String sql,int id);
    public void insertDataBatch(final String sql, List<Map> dataList);

}
