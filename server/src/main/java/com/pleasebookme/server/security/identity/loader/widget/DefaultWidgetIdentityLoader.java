package com.pleasebookme.server.security.identity.loader.widget;

import com.pleasebookme.server.security.identity.enums.AuthenticatedActorType;
import com.pleasebookme.server.security.identity.principal.WidgetPrincipal;
import com.pleasebookme.server.security.token.jwt.exception.WidgetOriginMismatchException;
import com.pleasebookme.server.widget.widgetorigin.repository.WidgetOriginRepository;
import com.pleasebookme.server.widget.widgets.entity.WidgetEntity;
import com.pleasebookme.server.widget.widgets.exception.WidgetNotFoundException;
import com.pleasebookme.server.widget.widgets.repository.WidgetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DefaultWidgetIdentityLoader implements WidgetIdentityLoader {
    private final WidgetRepository widgetRepository;
    private final WidgetOriginRepository widgetOriginRepository;

    @Override
    public WidgetPrincipal loadByPublicKey(String publicKey, String origin) {
        WidgetEntity widget = widgetRepository.findByPublicKey(publicKey)
            .orElseThrow(() -> new WidgetNotFoundException(
                "Widget not found with public key: " + publicKey
            ));

        validateOrigin(widget, origin);

        return map(widget);
    }

    @Override
    public WidgetPrincipal loadByUid(UUID uid) {
        WidgetEntity widget = widgetRepository.findByWidgetUid(uid)
            .orElseThrow(() -> new WidgetNotFoundException(
                "Widget not found with uid " + uid
            ));

        return map(widget);
    }

    private WidgetPrincipal map(WidgetEntity widget) {
        return new WidgetPrincipal(
            AuthenticatedActorType.WIDGET,
            widget.getWidgetUid(),
            widget.getTenant().getTenantUid(),
            widget.getStatus()
        );
    }

    private void validateOrigin(WidgetEntity widget, String origin) {
        if (!widget.getOriginValidation()) {
            return;
        }

        boolean valid = widgetOriginRepository.existsByWidgetWidgetIdAndOrigin(
            widget.getWidgetId(),
            origin
        );

        if (!valid) {
            throw new WidgetOriginMismatchException(
                "Origin " + origin + " is not registered for widget " + widget.getWidgetUid()
            );
        }
    }
}
