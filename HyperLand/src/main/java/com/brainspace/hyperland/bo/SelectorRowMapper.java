package com.brainspace.hyperland.bo;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class SelectorRowMapper implements RowMapper<SelectorBO> {

	@Override
	public SelectorBO mapRow(ResultSet rs, int rowNum) throws SQLException {
		SelectorBO selectorBO = new SelectorBO();
		selectorBO.setCode(rs.getInt("Code"));
		selectorBO.setValue(rs.getString("Value"));
		return selectorBO;
	}

}
