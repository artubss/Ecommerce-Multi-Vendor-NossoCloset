package com.nossocloset.service;

import com.nossocloset.model.Home;
import com.nossocloset.model.HomeCategory;

import java.util.List;

public interface HomeService {

    Home creatHomePageData(List<HomeCategory> categories);

}
