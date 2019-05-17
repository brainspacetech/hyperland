package com.brainspace.hyperland.service;

import com.brainspace.hyperland.bo.*;
import com.brainspace.hyperland.dao.IMasterDAO;
import com.brainspace.hyperland.dao.MasterDAO;
import com.brainspace.hyperland.utils.AgentChart;
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
                        } else if (colDataType.get(1).equalsIgnoreCase("12") || colDataType.get(1).equalsIgnoreCase("1")) {
                            whereClause += colDataType.get(0) + " = '" + searchCriteria.get(key) + "' AND ";
                        }
                        else {
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

    public RestResponse getChainAgent(String agentId)
    {
        String statusCode = "'";
        String statusMessage ="";
        String resultString = null;
        RestResponse restResponse = null;
        try {
            String selectChainAgents = "select am.AgentId as AgentId, am.AgentName as AgentName, am.SponsorId as SponsorId,am.Designation as Designation,ab.SelfBusiness as SelfBusiness,ab.ChainBusiness as ChainBusiness from  AgentMaster am INNER JOIN  AgentBusinessDetails ab ON am.agentId = ab.agentId\n" +
                    "         where am.agentId = "+agentId+"\n" +
                    "UNION ALL\n" +
                    "(select  AgentId, AgentName, SponsorId,Designation,SelfBusiness,ChainBusiness\n" +
                    "from    (\n" +
                    "         select am.AgentId, am.AgentName, am.SponsorId,am.Designation,ab.SelfBusiness,ab.ChainBusiness as ChainBusiness from  AgentMaster am INNER JOIN  AgentBusinessDetails ab ON am.agentId = ab.agentId\n" +
                    "             order by sponsorId, agentId    \n" +
                    "         ) AgentMaster,\n" +
                    "        (select @pv := "+agentId+") initialisation\n" +
                    "where   find_in_set(sponsorId, @pv) > 0\n" +
                    "and     @pv := concat(@pv, ',', agentId)  )";
            List result = masterDAO.getAllData(selectChainAgents);
            List agentList = new ArrayList();
            for(Object resultObj : result)
            {
                Map map = (Map) resultObj;
                System.out.println("agentName"+map.get("AgentName")+" - "+ map.get("Designation"));
                String designation = "SB - "+map.get("SelfBusiness")+" / CB "+ map.get("ChainBusiness");
                agentList.add(new AgentNode(map.get("AgentName")+" - "+ map.get("Designation"),map.get("AgentId").toString(), map.get("SponsorId")!=null? map.get("SponsorId").toString():null, designation));
            }
            AgentChart agentChart = new AgentChart();
            resultString = agentChart.createTree(agentList);
            statusCode = "1";
            if (result.size() == 0)
                statusCode = "2";
            statusMessage = "Success";

            System.out.println(result);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            statusCode = "0";
            statusMessage = "Failed";

        }
        restResponse = ServiceUtils.convertObjToResponse(statusCode, statusMessage, resultString);
        return restResponse;
    }
}
