package com.pleasebookme.server.security.identity.verifier;

import com.pleasebookme.server.widget.widgets.entity.WidgetEntity;

public interface WidgetVerifier {
    boolean verify(String secretKey, WidgetEntity widget);
}
