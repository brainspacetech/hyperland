package  com.brainspace.hyperland.bo;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;
import java.util.List;


public class AgentNode {

    private String agentId;
    private String sponsorId;
    private String name;
    private String designation;
    private String img = "";
    @JsonBackReference
    private AgentNode parent;
    @JsonManagedReference
    private List<AgentNode> subordinates;

    public AgentNode() {
        super();
        this.subordinates = new ArrayList<>();
    }

    public AgentNode(String name, String childId, String sponsorId,String designation) {
        this.name = name;
        this.agentId = childId;
        this.sponsorId = sponsorId;
        this.designation = designation;
        this.subordinates = new ArrayList<>();
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getSponsorId() {
        return sponsorId;
    }

    public void setSponsorId(String sponsorId) {
        this.sponsorId = sponsorId;
    }

    public AgentNode getParent() {
        return parent;
    }

    public void setParent(AgentNode parent) {
        this.parent = parent;
    }


    public List<AgentNode> getSubordinates() {
        return subordinates;
    }

    public void setSubordinates(List<AgentNode> subordinates) {
        this.subordinates = subordinates;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void addChild(AgentNode child) {
        if (!this.subordinates.contains(child) && child != null)
            this.subordinates.add(child);
    }



    @Override
    public String toString() {
        return "AgentNode [agentId=" + agentId + ", sponsorId=" + sponsorId + ", name=" + name + ", subordinates="
                + subordinates + "]";
    }
}