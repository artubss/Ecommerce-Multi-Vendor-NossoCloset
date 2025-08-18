package com.nossocloset.service;

import com.nossocloset.model.Seller;
import com.nossocloset.model.SellerReport;

public interface SellerReportService {
    SellerReport getSellerReport(Seller seller);
    SellerReport updateSellerReport( SellerReport sellerReport);

}
