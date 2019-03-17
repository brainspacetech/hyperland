package com.brainspace.hyperland.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.brainspace.hyperland.bo.SelectorBO;
import com.brainspace.hyperland.bo.SelectorRowMapper;

@Transactional
@Repository
public class SelectorDAO implements ISelectorDAO {

	private JdbcTemplate jdbcTemplate;
	@Autowired
	public SelectorDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	@Override
	public List<SelectorBO> getSelectorValue(String type,String value) {
		try {
			String sql = getSQL(type,value);
			RowMapper<SelectorBO> rowMapper = new SelectorRowMapper();
			System.out.println("this.jdbcTemplate -- "+this.jdbcTemplate);
			return this.jdbcTemplate.query(sql, rowMapper);
		}
		catch(Exception e)
		{
			throw e;
		}
	}
	private String getSQL(String type,String value)
	{
		String sql = "";
		switch(type)
		{
			case "Firm": 
						sql = "SELECT Id as Code, FirmName as Value FROM FirmMaster";
						break;
			default: sql = "";
					
		}
		return sql;
	}

}
