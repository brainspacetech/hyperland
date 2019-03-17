package com.brainspace.hyperland.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.brainspace.hyperland.bo.Firm;
import com.brainspace.hyperland.bo.FirmRowMapper;

@Transactional
@Repository
public class FirmDAO implements IFirmDAO{

	private JdbcTemplate jdbcTemplate;
	@Autowired
	public FirmDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public List<Firm> getAllFirm() throws Exception {
		try {
			String sql = "SELECT Id, FirmName FROM FirmMaster";
			RowMapper<Firm> rowMapper = new FirmRowMapper();
			System.out.println("this.jdbcTemplate -- "+this.jdbcTemplate);
			return this.jdbcTemplate.query(sql, rowMapper);
		}
		catch(Exception e)
		{
			throw e;
		}
	}

	@Override
	public Firm getFirmById(int firmId) {
		String sql = "SELECT Id, FirmName FROM FirmMaster where Id = ?";
		RowMapper<Firm> rowMapper = new FirmRowMapper();
		System.out.println("this.jdbcTemplate -- "+this.jdbcTemplate);
		try {
			return this.jdbcTemplate.queryForObject(sql, rowMapper,firmId);
		}
		catch(EmptyResultDataAccessException e)
		{
			return null;
		}
	}

	@Override
	public void addFirm(Firm firm) {
		String sql = "INSERT INTO FirmMaster (FirmName) values (?)";
		jdbcTemplate.update(sql, firm.getFirmName());
	}

	@Override
	public void updateFirm(Firm firm) {
		String sql = "UPDATE FirmMaster SET FirmName=? WHERE Id=?";
		jdbcTemplate.update(sql, firm.getFirmName(), firm.getId());
	}

	@Override
	public void deleteFirm(Firm firmId) {
		// TODO Auto-generated method stub

	}
}
