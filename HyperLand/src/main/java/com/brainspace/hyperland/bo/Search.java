package com.brainspace.hyperland.bo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Search {
    private PropertyMapping PropertyMapping;
    private String id;
    private String searchQuery;

    @XmlElement(name="PropertyMapping")
    public com.brainspace.hyperland.bo.PropertyMapping getPropertyMapping() {
        return PropertyMapping;
    }

    public void setPropertyMapping(com.brainspace.hyperland.bo.PropertyMapping propertyMapping) {
        PropertyMapping = propertyMapping;
    }
    @XmlAttribute(name="id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name="SearchQuery")
    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}
