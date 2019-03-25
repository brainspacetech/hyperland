package com.brainspace.hyperland.service;

import com.brainspace.hyperland.bo.RestResponse;

import java.util.List;
import java.util.Map;

public interface ITransactionService {
    RestResponse createBooking(Object requestObject);
}
