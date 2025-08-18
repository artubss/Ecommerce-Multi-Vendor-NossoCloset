package com.nossocloset.response;

import com.nossocloset.dto.OrderHistory;
import com.nossocloset.model.Cart;
import com.nossocloset.model.Product;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionResponse {
    private String functionName;
    private Cart userCart;
    private OrderHistory orderHistory;
    private Product product;
}
