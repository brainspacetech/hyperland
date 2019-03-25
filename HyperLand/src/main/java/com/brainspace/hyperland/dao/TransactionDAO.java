package com.brainspace.hyperland.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
                    } else if (dataList.get(j).get(paramsArr[i].trim()) instanceof Date) {
                        preparedStatement.setDate((i + 1), (Date) dataList.get(j).get(paramsArr[i].trim()));
                    }
                    else if (dataList.get(j).get(paramsArr[i].trim()) instanceof Double) {
                        preparedStatement.setDouble((i + 1), (Double) dataList.get(j).get(paramsArr[i].trim()));
                    }
                    else{
                        preparedStatement.setDate((i + 1), null);
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
        Map<String,Object> idMap = jdbcTemplate.queryForMap(selectQuery);
        return idMap.get("bookingId");
    }

    public void updateData(final String sql, Map dataMap,String idColName)
    {
        jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement) throws SQLException {
                String params = sql.substring(sql.indexOf(" SET") + 4, sql.indexOf("WHERE"));
                String paramsArr[] = params.split(",");
                int count = 0;
                for (int i = 0; i < paramsArr.length; i++) {
                    String paramName = paramsArr[i].substring(0,paramsArr[i].indexOf("=")).trim();
                    System.out.println(paramName + " --- "+paramName);
                    System.out.println(paramName + "-- " + dataMap.get(paramName));
                    if (dataMap.get(paramName) instanceof String) {
                        preparedStatement.setString((i + 1), (String) dataMap.get(paramName));
                    } else if (dataMap.get(paramName) instanceof Integer) {
                        preparedStatement.setInt((i + 1), (Integer) dataMap.get(paramName));
                    } else if (dataMap.get(paramName) instanceof Date) {
                        preparedStatement.setDate((i + 1), (Date) dataMap.get(paramName));
                    }
                    else if (dataMap.get(paramName) instanceof Double) {
                        preparedStatement.setDouble((i + 1), (Double) dataMap.get(paramName));
                    }
                    else{
                        preparedStatement.setDate((i + 1), null);
                    }
                    count++;
                }

                preparedStatement.setInt(count+1,((Integer)dataMap.get(idColName)).intValue());
                System.out.println("preparedStatement  -- " + preparedStatement);
            }
        });
    }
}
