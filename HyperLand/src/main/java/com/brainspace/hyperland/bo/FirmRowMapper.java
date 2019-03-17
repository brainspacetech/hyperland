package com.brainspace.hyperland.bo;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class FirmRowMapper implements RowMapper<Firm> {

	@Override
	public Firm mapRow(ResultSet rs, int rowNum) throws SQLException {
		Firm firm = new Firm();
		firm.setId(rs.getInt("Id"));
		firm.setFirmName(rs.getString("FirmName"));
		return firm;
	}

}
