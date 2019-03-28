package com.brainspace.hyperland.bo;

import javax.xml.bind.annotation.XmlElement;

public class Searches {

    private Search search[];
    @XmlElement(name = "Search")
    public Search[] getSearch() {
        return search;
    }

    public void setSearch(Search[] search) {
        this.search = search;
    }
}


