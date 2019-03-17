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
			if(sql.contains("?"))
				return this.jdbcTemplate.query(sql, rowMapper,value);
			else
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
			case "firms":
						sql = "SELECT Id as Code, FirmName as Value FROM FirmMaster";
						break;
			case "projects":
						sql = "SELECT Id as Code, ProjectName as Value FROM ProjectMaster where FirmId = ?";
						break;
			case "cities":
						sql = "SELECT Id as Code, City as Value FROM CityMaster";
						break;
			case "locations":
						sql = "SELECT Id as Code, Location as Value FROM LocationMaster where CityId = ?";
						break;
			case "propertyTypes":
						sql = "SELECT Id as Code, PropertyType as Value FROM PropertyTypeMaster";
						break;
			case "blocks":
						sql = "SELECT Id as Code, Block as Value FROM BlockMaster where PropertyId = ?";
						break;
			case "plots":
						sql = "SELECT Id as Code, Block as Value FROM BlockMaster where BlockId = ?";
						break;
			default: sql = "";
					
		}
		return sql;
	}

}
