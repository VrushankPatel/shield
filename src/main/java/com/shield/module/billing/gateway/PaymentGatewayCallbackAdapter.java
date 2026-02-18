package com.shield.module.billing.gateway;

import com.shield.module.billing.dto.PaymentCallbackRequest;
import com.shield.module.billing.entity.PaymentGatewayTransactionEntity;

public interface PaymentGatewayCallbackAdapter {

    String provider();

    String buildSignaturePayload(PaymentCallbackRequest request, PaymentGatewayTransactionEntity transaction);

    boolean isSuccessStatus(String status);
}
