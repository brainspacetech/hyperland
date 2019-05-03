package com.brainspace.hyperland.dao;

import java.util.List;
import java.util.Map;

public interface ITransactionDAO {
    public void insertDataBatch(String sql, List<Map> dataList);
    public void updateDataBatch(String sql, List<Map> dataList,int id);
    public Object getBookingId(String query, Object params[], int argTypes[]);
    public void updateData(final String sql, Map dataMap,String idColName);
    public void addData(String query,Object params[], int argTypes[]);
    public void updateData(String query);
    public void insertDataBatch(final String sql[]);
    public Object getReceiptNumber(String query);

}
