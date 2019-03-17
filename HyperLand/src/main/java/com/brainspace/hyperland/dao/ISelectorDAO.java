package com.brainspace.hyperland.dao;

import java.util.List;

import com.brainspace.hyperland.bo.SelectorBO;

public interface ISelectorDAO {

	List<SelectorBO> getSelectorValue(String type);
}
