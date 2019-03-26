package com.brainspace.hyperland.dao;

import java.util.List;
import java.util.Map;

public interface ITransactionDAO {
    public void insertDataBatch(String sql, List<Map> dataList);
    public Object getBookingId(String query, Object params[], int argTypes[]);
    public void updateData(final String sql, Map dataMap,String idColName);
}