package com.zosh.service;

import com.zosh.model.OrderItem;

public interface OrderItemService {

    OrderItem getOrderItemById(Long id) throws Exception;

}
