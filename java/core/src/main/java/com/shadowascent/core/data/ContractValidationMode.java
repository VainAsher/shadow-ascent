package com.shadowascent.core.data;

import java.util.Locale;

/**
 * Policy for handling contract validation failures during runtime startup.
 */
public enum ContractValidationMode {
    WARN,
    FAIL_FAST;

    public static ContractValidationMode resolve() {
        String raw = readConfiguredValue();
        if (raw == null || raw.isBlank()) {
            return WARN;
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "fail", "failfast", "fail_fast", "strict", "error" -> FAIL_FAST;
            case "warn", "warning", "lenient" -> WARN;
            default -> WARN;
        };
    }

    public static String configuredValue() {
        String raw = readConfiguredValue();
        return raw == null ? "" : raw;
    }

    private static String readConfiguredValue() {
        String property = System.getProperty("shadowascent.contracts.validation.mode", "").trim();
        if (!property.isEmpty()) {
            return property;
        }
        String env = System.getenv("SHADOWASCENT_CONTRACTS_VALIDATION_MODE");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return "";
    }
}
