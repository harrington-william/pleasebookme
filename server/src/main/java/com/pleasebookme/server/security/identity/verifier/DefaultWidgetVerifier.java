package com.pleasebookme.server.security.identity.verifier;

import com.pleasebookme.server.widget.widgets.entity.WidgetEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultWidgetVerifier implements WidgetVerifier {
    private final PasswordEncoder passwordEncoder;
    @Override
    public boolean verify(String secretKey, WidgetEntity widget) {
        String hash = widget.getSecretKey();

        return passwordEncoder.matches(secretKey, hash);
    }
}
