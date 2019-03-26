package com.brainspace.hyperland.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.brainspace.hyperland.bo.Firm;
import com.brainspace.hyperland.bo.FirmRowMapper;

@Transactional
@Repository
public class MasterDAO implements IMasterDAO {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public MasterDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List getAllData(String query) throws Exception {
        try {
            System.out.println("this.jdbcTemplate -- " + this.jdbcTemplate);
            return this.jdbcTemplate.queryForList(query);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Map<String, Object> getDataById(String query, int id) {
        try {
            return this.jdbcTemplate.queryForMap(query, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Object addData(String query, Object params[], int argTypes[]) {
        jdbcTemplate.update(query, params, argTypes);
        String selectQuery = "SELECT LAST_INSERT_ID() as Id";
        Map<String,Object> idMap = jdbcTemplate.queryForMap(selectQuery);
        return idMap.get("Id");
    }

	@Override
	public void updateData(String query,Object params[],int argTypes[]) {
		jdbcTemplate.update(query, params, argTypes);
	}

    @Override
    public void updateData(String query) {
        jdbcTemplate.update(query);
    }

	@Override
	public void deleteData(String query , int id) {
        jdbcTemplate.update(query, id);
	}
}
