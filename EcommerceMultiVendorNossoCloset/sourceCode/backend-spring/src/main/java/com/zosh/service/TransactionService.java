package com.nossocloset.service;

import com.nossocloset.model.Order;
import com.nossocloset.model.Seller;
import com.nossocloset.model.Transaction;

import java.util.List;

public interface TransactionService {

    Transaction createTransaction(Order order);
    List<Transaction> getTransactionBySeller(Seller seller);
    List<Transaction>getAllTransactions();
}
