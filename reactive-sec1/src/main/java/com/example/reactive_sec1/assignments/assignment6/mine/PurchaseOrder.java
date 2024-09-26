package com.example.reactive_sec1.assignments.assignment6.mine;

import com.example.reactive_sec1.common.Util;

public record PurchaseOrder(
        String item,
        String category,
        Integer price
) {
    public static PurchaseOrder create() {
        var commerce = Util.faker().commerce();
        return new PurchaseOrder(
                commerce.productName(),
                commerce.department(),
                Util.faker().random().nextInt(10,100)
        );
    }
}
