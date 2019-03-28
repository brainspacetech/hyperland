package com.brainspace.hyperland.bo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "Configuration")
public class ConfigBO {

    private Service serviceObj[];
    @XmlElement(name = "Service")
    public Service[] getServiceObj ()
    {
        return serviceObj;
    }

    public void setServiceObj (Service serviceObj[])
    {
        this.serviceObj = serviceObj;
    }

    private Transactions transactions;

    private Searches searches;

    @XmlElement(name = "Transactions")
    public Transactions getTransactions() {
        return transactions;
    }

    public void setTransactions(Transactions transactions) {
        this.transactions = transactions;
    }

    @XmlElement(name = "Searches")
    public Searches getSearches() {
        return searches;
    }

    public void setSearches(Searches searches) {
        this.searches = searches;
    }
}
