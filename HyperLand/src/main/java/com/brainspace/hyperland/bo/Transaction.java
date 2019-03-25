package com.brainspace.hyperland.bo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Transaction {
    private PropertyMapping PropertyMapping;
    private String id;
    private String insertQuery;
    private String updateQuery;

    @XmlElement(name="PropertyMapping")
    public com.brainspace.hyperland.bo.PropertyMapping getPropertyMapping() {
        return PropertyMapping;
    }

    public void setPropertyMapping(com.brainspace.hyperland.bo.PropertyMapping propertyMapping) {
        PropertyMapping = propertyMapping;
    }
    @XmlAttribute(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name="InsertQuery")
    public String getInsertQuery() {
        return insertQuery;
    }

    public void setInsertQuery(String insertQuery) {
        this.insertQuery = insertQuery;
    }

    @XmlElement(name="UpdateQuery")
    public String getUpdateQuery() {
        return updateQuery;
    }

    public void setUpdateQuery(String updateQuery) {
        this.updateQuery = updateQuery;
    }
}
