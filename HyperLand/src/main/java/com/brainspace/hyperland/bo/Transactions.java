package com.brainspace.hyperland.bo;

import javax.xml.bind.annotation.XmlElement;

public class Transactions {
    private Transaction transaction[];

    @XmlElement(name = "Transaction")
    public Transaction[] getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction[] transaction) {
        this.transaction = transaction;
    }
}
