package com.brainspace.hyperland.utils;

import com.brainspace.hyperland.dao.ITransactionDAO;

import java.util.HashMap;
import java.util.Map;

public class TransactionUtils{
    public void addBalanceEntry(ITransactionDAO transactionDAO,String paymentMode, Double depositAmount, String particulars, Double withdrawlAmount,String transactionDoneBy)
    {
        Map balanceEntryData = new HashMap();
        balanceEntryData.put("TransactionDate","");
        balanceEntryData.put("PaymentMode",paymentMode);
        balanceEntryData.put("Particulars",particulars);
        if(depositAmount!=null) {
            balanceEntryData.put("Deposits", depositAmount);
        }
        else if(withdrawlAmount!=null) {
            balanceEntryData.put("Withdrawls", withdrawlAmount);
        }

    }
}