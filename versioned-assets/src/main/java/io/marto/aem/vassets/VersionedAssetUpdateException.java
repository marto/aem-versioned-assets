package io.marto.aem.vassets;

import lombok.Getter;

public class VersionedAssetUpdateException extends Exception {
    private static final long serialVersionUID = 1L;
    @Getter
    private final int response;

    public VersionedAssetUpdateException(String message, Throwable cause, int response) {
        super(message, cause);
        this.response = response;
    }
}
