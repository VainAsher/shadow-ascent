package com.shadowascent.core.world.streaming;

/** Thrown by RegionLoader when a blocking constraint issue prevents region load. */
public final class RegionLoadException extends RuntimeException {

    private final RegionalStreamingConstraintValidator.ValidationResult validationResult;

    public RegionLoadException(
            String message,
            RegionalStreamingConstraintValidator.ValidationResult validationResult) {
        super(message);
        this.validationResult = validationResult;
    }

    public RegionalStreamingConstraintValidator.ValidationResult validationResult() {
        return validationResult;
    }
}
