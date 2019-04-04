package com.brainspace.hyperland.utils;

import com.brainspace.hyperland.bo.AgentNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentChart {
    public static void generateAgentChart()
    {


        List<Map<String,Object>> obj = new ArrayList<>();
        Map a = new HashMap();
        AgentNode node1 = new AgentNode("Pankaj", "12", null,"SelfBusiness - 3232099    chainbusiness - 3000.00");
        AgentNode node2 = new AgentNode("Arati", "13", "12","SelfBusiness - 3232099    chainbusiness - 3000.00");
        AgentNode node3 = new AgentNode("Niraj",  "14", "13","SelfBusiness - 3232099    chainbusiness - 3000.00");
        AgentNode node4 = new AgentNode("Divit", "16", "13","SelfBusiness - 3232099    chainbusiness - 3000.00");
        AgentNode node5 = new AgentNode("Divyansh",   "15", "14","SelfBusiness -  3232099    chainbusiness - 3000.00");

        List<AgentNode> nodes = new ArrayList<>();
        nodes.add(node1);
        nodes.add(node2);
        nodes.add(node3);
        nodes.add(node4);
        nodes.add(node5);

        createTree(nodes);

    System.out.println(obj);

    }

    private static void createTree(List<AgentNode> nodes) {

        Map<String, AgentNode> mapTmp = new HashMap<>();

        //Save all nodes to a map
        for (AgentNode current : nodes) {
            mapTmp.put(current.getAgentId(), current);
        }

        //loop and assign parent/child relationships
        for (AgentNode current : nodes) {
            String parentId = current.getSponsorId();

            if (parentId != null) {
                AgentNode parent = mapTmp.get(parentId);
                if (parent != null) {
                    current.setParent(parent);
                    parent.addChild(current);
                    mapTmp.put(parentId, parent);
                    mapTmp.put(current.getAgentId(), current);
                }
            }

        }
        AgentNode root = null;
        for (AgentNode node : mapTmp.values()) {
            if(node.getParent() == null) {
                root = node;
                break;
            }
        }
String str = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonInString = mapper.writeValueAsString(root);
            System.out.println(jsonInString);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public static void main(String s[])
    {
        generateAgentChart();
    }
}
