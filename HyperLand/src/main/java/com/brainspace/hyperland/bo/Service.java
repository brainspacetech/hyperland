package com.brainspace.hyperland.bo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Service {
    private String selectQuery;
    private String selectQueryById;
    private String insertQuery;
    private String updateQuery;
    private String deleteQuery;
    private String ClassName;
    private PropertyMapping PropertyMapping;
    private String id;
    private String dependentMaster;

    @XmlElement(name="SelectQuery")
    public String getSelectQuery() {
        return selectQuery;
    }

    public void setSelectQuery(String selectQuery) {
        this.selectQuery = selectQuery;
    }

    @XmlElement(name="SelectQueryById")
    public String getSelectQueryById() {
        return selectQueryById;
    }

    public void setSelectQueryById(String selectQueryById) {
        this.selectQueryById = selectQueryById;
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
    @XmlElement(name="DeleteQuery")
    public String getDeleteQuery() {
        return deleteQuery;
    }

    public void setDeleteQuery(String deleteQuery) {
        this.deleteQuery = deleteQuery;
    }

    @XmlElement(name="ClassName")
    public String getClassName() {
        return ClassName;
    }

    public void setClassName(String ClassName) {
        this.ClassName = ClassName;
    }
    @XmlElement(name="PropertyMapping")
    public PropertyMapping getPropertyMapping() {
        return PropertyMapping;
    }

    public void setPropertyMapping(PropertyMapping PropertyMapping) {
        this.PropertyMapping = PropertyMapping;
    }

    @XmlAttribute(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name="DependentMaster")
    public String getDependentMaster() {
        return dependentMaster;
    }

    public void setDependentMaster(String dependentMaster) {
        this.dependentMaster = dependentMaster;
    }
}