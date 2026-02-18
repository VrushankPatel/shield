package com.shield.module.billing.gateway;

import com.shield.common.exception.BadRequestException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentWebhookSignatureVerifier {

    private static final String HMAC_SHA_256 = "HmacSHA256";

    private final Map<String, String> providerSecrets;

    public PaymentWebhookSignatureVerifier(
            @Value("${shield.payment.webhook.provider-secrets:}") String providerSecretsRaw) {
        this.providerSecrets = parse(providerSecretsRaw);
    }

    public void assertValidSignature(String provider, String payload, String signature) {
        String normalizedProvider = normalize(provider);
        String secret = providerSecrets.get(normalizedProvider);
        if (secret == null || secret.isBlank()) {
            return;
        }

        if (signature == null || signature.isBlank()) {
            throw new BadRequestException("Missing webhook signature for provider: " + normalizedProvider);
        }

        String expected = sign(secret, payload == null ? "" : payload);
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = signature.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expectedBytes, actualBytes)) {
            throw new BadRequestException("Invalid webhook signature");
        }
    }

    String sign(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to calculate webhook signature", ex);
        }
    }

    private Map<String, String> parse(String providerSecretsRaw) {
        Map<String, String> parsed = new HashMap<>();
        if (providerSecretsRaw == null || providerSecretsRaw.isBlank()) {
            return parsed;
        }

        String[] entries = providerSecretsRaw.split(",");
        for (String entry : entries) {
            String[] pair = entry.split("=", 2);
            if (pair.length != 2) {
                continue;
            }
            String provider = normalize(pair[0]);
            String secret = pair[1].trim();
            if (!provider.isBlank() && !secret.isBlank()) {
                parsed.put(provider, secret);
            }
        }
        return Map.copyOf(parsed);
    }

    private String normalize(String provider) {
        return provider == null ? "" : provider.trim().toUpperCase(Locale.ROOT);
    }
}
