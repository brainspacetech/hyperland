package com.brainspace.hyperland.service;

import com.brainspace.hyperland.bo.RestResponse;

import java.util.Map;

public interface ISearchService {
    public RestResponse searchObject(Map searchCriteria, String type);
}
