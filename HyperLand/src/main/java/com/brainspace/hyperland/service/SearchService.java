package com.brainspace.hyperland.service;

import com.brainspace.hyperland.bo.ConfigBO;
import com.brainspace.hyperland.bo.Property;
import com.brainspace.hyperland.bo.RestResponse;
import com.brainspace.hyperland.bo.Search;
import com.brainspace.hyperland.dao.IMasterDAO;
import com.brainspace.hyperland.utils.ConfigReader;
import com.brainspace.hyperland.utils.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchService implements ISearchService {

    @Autowired
    private IMasterDAO masterDAO;

    public RestResponse searchObject(Map searchCriteria, String type) {
        RestResponse restResponse = null;
        ConfigBO configBO = ConfigReader.getConfig();
        ServiceUtils serviceUtils = new ServiceUtils();
        for (int i = 0; i < configBO.getSearches().getSearch().length; i++) {
            Search search = configBO.getSearches().getSearch()[i];
            if (search.getId().equalsIgnoreCase(type)) {
                Property property[] = search.getPropertyMapping().getProperty();
                Map jsonColumnMap = serviceUtils.jsonColumnNameMapper(property);
                Set<String> keys = searchCriteria.keySet();
                String whereClause = "";
                for (String key : keys) {
                    if (searchCriteria.get(key) != null) {
                        List<String> colDataType = (List<String>) jsonColumnMap.get(key);
                        if (colDataType.get(1).equalsIgnoreCase("-101")) {
                            whereClause += colDataType.get(0) + " >= CAST('" + searchCriteria.get(key) + "' AS DATE) AND ";
                        } else if (colDataType.get(1).equalsIgnoreCase("-102")) {
                            whereClause += colDataType.get(0) + " <= CAST('" + searchCriteria.get(key) + "' AS DATE) AND ";
                        } else if (colDataType.get(1).equalsIgnoreCase("12")) {
                            whereClause += colDataType.get(0) + " = '" + searchCriteria.get(key) + "' AND ";
                        } else {
                            whereClause += colDataType.get(0) + " = " + searchCriteria.get(key) + " AND ";
                        }
                    }
                }
                whereClause = whereClause.substring(0, whereClause.lastIndexOf(" AND"));
                String sql = search.getSearchQuery().replace("{MACRO}", whereClause);
                String statusCode = "";
                String statusMessage = "";
                List result = null;
                try {

                    result = masterDAO.getAllData(sql);
                    statusCode = "1";
                    if (result.size() == 0)
                        statusCode = "2";
                    statusMessage = "Success";

                    System.out.println(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    statusCode = "0";
                    statusMessage = "Failed";
                }
                restResponse = ServiceUtils.convertObjToResponse(statusCode, statusMessage, result);

            }
        }
        return restResponse;
    }
}
