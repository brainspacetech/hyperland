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
			case "firm":
						sql = "SELECT Id as Code, FirmName as Value FROM FirmMaster";
						break;
			case "project":
						sql = "SELECT ProjectId as Code, ProjectName as Value FROM ProjectMaster where FirmId = ?";
						break;
			case "property":
						sql = "SELECT ProjectId as Code, ProjectName as Value FROM ProjectMaster where FirmId = ?";
						break;
			case "city":
						sql = "SELECT Id as Code, City as Value FROM CityMaster";
						break;
			case "location":
						sql = "SELECT Id as Code, Location as Value FROM LocationMaster where CityId = ?";
						break;
			case "propertyType":
						sql = "SELECT Id as Code, PropertyType as Value FROM PropertyTypeMaster";
						break;
			case "block":
						sql = "SELECT Id as Code, Block as Value FROM BlockMaster where PropertyId = ?";
						break;
			case "plot":
						sql = "SELECT Id as Code, PlotNo as Value FROM PlotDetails where BlockId = ?";
						break;
			case "farmer":
				sql = "SELECT Id as Code, FarmerName as Value FROM FarmerMaster where LandId = ?";
				break;
			case "bank":
				sql = "SELECT Id as Code, BankName as Value FROM BankMaster";
				break;
			case "agent":
				sql = "SELECT AgentId as Code, AgentName as Value FROM AgentMaster";
				break;
			case "investor":
				sql = "SELECT Id as Code, InvestorName as Value FROM InvestorMaster";
				break;
			case "availablePlot":
				sql = "SELECT Id as Code, PlotNo as Value FROM PlotDetails where BlockId = ? AND Status = 'Available'";
				break;
			case "bookedPlot":
				sql = "SELECT Id as Code, PlotNo as Value FROM PlotDetails where BlockId = ? AND Status = 'Booked'";
				break;
			case "holdPlot":
				sql = "SELECT Id as Code, PlotNo as Value FROM PlotDetails where BlockId = ? AND Status = 'Hold'";
				break;
			case "expenseCategory":
				sql = "SELECT Id as Code, ExpenseCategory as Value FROM ExpenseCategoryMaster";
				break;
			default: sql = "";

		}
		return sql;
	}

}
