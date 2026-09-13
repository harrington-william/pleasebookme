package com.pleasebookme.server.widget.widgets.dto;

/** The only widget-domain response that exposes a plaintext secret, returned once before persistence. */
public record WidgetCredentialResponse(
    String publicKey,
    String secretKey
) {
}
