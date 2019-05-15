package com.brainspace.hyperland.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;


@Transactional
@Repository
public class TransactionDAO implements ITransactionDAO {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public TransactionDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertDataBatch(final String sql, List<Map> dataList) {

        this.jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int j) throws SQLException {
                //   preparedStatement.setString(1,"");
                String params = sql.substring(sql.indexOf("(") + 1, sql.indexOf(")"));
                String paramsArr[] = params.split(",");
                for (int i = 0; i < paramsArr.length; i++) {
                    System.out.println(paramsArr[i].trim() + "-- " + dataList.get(j).get(paramsArr[i].trim()));
                    if (dataList.get(j).get(paramsArr[i].trim()) instanceof String) {
                        preparedStatement.setString((i + 1), (String) dataList.get(j).get(paramsArr[i].trim()));
                    } else if (dataList.get(j).get(paramsArr[i].trim()) instanceof Integer) {
                        preparedStatement.setInt((i + 1), (Integer) dataList.get(j).get(paramsArr[i].trim()));
                    } else if (dataList.get(j).get(paramsArr[i].trim()) instanceof Timestamp) {
                        preparedStatement.setObject((i + 1), (Timestamp) dataList.get(j).get(paramsArr[i].trim()));
                    } else if (dataList.get(j).get(paramsArr[i].trim()) instanceof Double) {
                        preparedStatement.setDouble((i + 1), (Double) dataList.get(j).get(paramsArr[i].trim()));
                    } else {
                        preparedStatement.setObject((i + 1), null);
                    }

                }
                System.out.println("preparedStatement  -- " + preparedStatement);
            }

            @Override
            public int getBatchSize() {
                return dataList.size();
            }
        });
    }

    public Object getBookingId(String query, Object params[], int argTypes[]) {
        jdbcTemplate.update(query, params, argTypes);
        String selectQuery = "SELECT LAST_INSERT_ID() as bookingId";
        Map<String, Object> idMap = jdbcTemplate.queryForMap(selectQuery);
        return idMap.get("bookingId");
    }

    @Override
    public Object addData(String query, Object params[], int argTypes[]) {
        jdbcTemplate.update(query, params, argTypes);
        String selectQuery = "SELECT LAST_INSERT_ID() as bookingId";
        Map<String, Object> idMap = jdbcTemplate.queryForMap(selectQuery);
        return idMap.get("id");
    }

    @Override
    public Object updateData(String query) {
        jdbcTemplate.update(query);
        String selectQuery = "SELECT LAST_INSERT_ID() as bookingId";
        Map<String, Object> idMap = jdbcTemplate.queryForMap(selectQuery);
        return idMap.get("id");
    }
    public void updateData(final String sql, Map dataMap, String idColName) {
        jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement) throws SQLException {
                String params = sql.substring(sql.indexOf(" SET") + 4, sql.indexOf("WHERE"));
                String paramsArr[] = params.split(",");
                int count = 0;
                for (int i = 0; i < paramsArr.length; i++) {
                    String paramName = paramsArr[i].substring(0, paramsArr[i].indexOf("=")).trim();
                    System.out.println(paramName + " --- " + paramName);
                    System.out.println(paramName + "-- " + dataMap.get(paramName));
                    if (dataMap.get(paramName) instanceof String) {
                        preparedStatement.setString((i + 1), (String) dataMap.get(paramName));
                    } else if (dataMap.get(paramName) instanceof Integer) {
                        preparedStatement.setInt((i + 1), (Integer) dataMap.get(paramName));
                    } else if (dataMap.get(paramName) instanceof Timestamp) {
                        preparedStatement.setTimestamp((i + 1), (Timestamp) dataMap.get(paramName));
                    } else if (dataMap.get(paramName) instanceof Double) {
                        preparedStatement.setDouble((i + 1), (Double) dataMap.get(paramName));
                    } else {
                        preparedStatement.setDate((i + 1), null);
                    }
                    count++;
                }

                preparedStatement.setInt(count + 1, ((Integer) dataMap.get(idColName)).intValue());
                System.out.println("preparedStatement  -- " + preparedStatement);
            }
        });
    }
    public void insertDataBatch(final String sql[]) {
        this.jdbcTemplate.batchUpdate(sql);
    }
    public void updateDataBatch(final String sql, List<Map> dataList,int id) {

        this.jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int j) throws SQLException {
                String params = sql.substring(sql.indexOf(" SET") + 4, sql.indexOf("WHERE"));
                String paramsArr[] = params.split(",");
                int count = 0 ;
                for (int i = 0; i < paramsArr.length; i++) {
                    String paramKey = paramsArr[i].replace("=","");
                    paramKey = paramKey.replace("?","").trim();
                    System.out.println(paramKey + "-- " + dataList.get(j).get(paramKey));
                    if (dataList.get(j).get(paramKey) instanceof String) {
                        preparedStatement.setString((i + 1), (String) dataList.get(j).get(paramKey));
                    } else if (dataList.get(j).get(paramKey) instanceof Integer) {
                        preparedStatement.setInt((i + 1), (Integer) dataList.get(j).get(paramKey));
                    } else if (dataList.get(j).get(paramKey) instanceof Timestamp) {
                        preparedStatement.setObject((i + 1), (Timestamp) dataList.get(j).get(paramKey));
                    } else if (dataList.get(j).get(paramKey) instanceof Double) {
                        preparedStatement.setDouble((i + 1), (Double) dataList.get(j).get(paramKey));
                    } else {
                        preparedStatement.setObject((i + 1), null);
                    }
                    count++;
                }
                preparedStatement.setObject(count+1, dataList.get(j).get("Id"));
                System.out.println("preparedStatement  -- " + preparedStatement);
            }

            @Override
            public int getBatchSize() {
                System.out.println("dataList.size() -- "+dataList.size());
                return dataList.size();
            }
        });
    }

    @Override
    public Object getReceiptNumber(String query) {
        jdbcTemplate.update(query);
        Map<String, Object> idMap = jdbcTemplate.queryForMap("SELECT LAST_INSERT_ID() as receiptNumber from ReceiptNumber");
        return idMap.get("receiptNumber");
    }

}
