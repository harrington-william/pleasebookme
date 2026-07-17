package com.pleasebookme.server.widget.widgetorigin.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.widget.widgetorigin.dto.WidgetOriginRequest;
import com.pleasebookme.server.widget.widgetorigin.entity.WidgetOriginEntity;
import com.pleasebookme.server.widget.widgetorigin.exception.DuplicateWidgetOriginException;
import com.pleasebookme.server.widget.widgetorigin.exception.WidgetOriginNotFoundException;
import com.pleasebookme.server.widget.widgetorigin.repository.WidgetOriginRepository;
import com.pleasebookme.server.widget.widgetorigin.service.WidgetOriginService;
import com.pleasebookme.server.widget.widgets.entity.WidgetEntity;
import com.pleasebookme.server.widget.widgets.exception.WidgetNotFoundException;
import com.pleasebookme.server.widget.widgets.repository.WidgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WidgetOriginServiceImpl implements WidgetOriginService {
    private final WidgetOriginRepository widgetOriginRepository;
    private final WidgetRepository widgetRepository;
    private final UserRepository userRepository;

    @Override
    public WidgetOriginEntity createWidgetOrigin(WidgetOriginRequest request) {
        if (widgetOriginRepository.existsByWidgetWidgetIdAndOrigin(request.widgetId(), request.origin())) {
            throw new DuplicateWidgetOriginException(
                "Origin already exists for widget: " + request.widgetId() + ", " + request.origin()
            );
        }

        WidgetEntity widget = widgetRepository.findById(request.widgetId())
            .orElseThrow(() -> new WidgetNotFoundException(
                "Widget not found: " + request.widgetId()
            ));

        WidgetOriginEntity.WidgetOriginEntityBuilder widgetOrigin = WidgetOriginEntity.builder()
            .widget(widget)
            .origin(request.origin())
            .createdBy(resolveUser(request.createdById()));

        if (request.verified() != null) widgetOrigin.verified(request.verified());

        return widgetOriginRepository.save(widgetOrigin.build());
    }

    @Override
    public WidgetOriginEntity getWidgetOriginById(BigInteger widgetOriginId) {
        return widgetOriginRepository.findById(widgetOriginId)
            .orElseThrow(() -> new WidgetOriginNotFoundException(
                "Widget origin not found: " + widgetOriginId
            ));
    }

    @Override
    public List<WidgetOriginEntity> getAllWidgetOrigins() {
        return widgetOriginRepository.findAll();
    }

    @Override
    public WidgetOriginEntity updateWidgetOrigin(
        BigInteger widgetOriginId,
        WidgetOriginRequest request
    ) {
        WidgetOriginEntity widgetOrigin = getWidgetOriginById(widgetOriginId);

        WidgetEntity widget = widgetRepository.findById(request.widgetId())
            .orElseThrow(() -> new WidgetNotFoundException(
                "Widget not found: " + request.widgetId()
            ));

        widgetOrigin.setWidget(widget);
        widgetOrigin.setOrigin(request.origin());
        widgetOrigin.setCreatedBy(resolveUser(request.createdById()));

        if (request.verified() != null) widgetOrigin.setVerified(request.verified());

        return widgetOriginRepository.save(widgetOrigin);
    }

    @Override
    public void deleteWidgetOrigin(BigInteger widgetOriginId) {
        widgetOriginRepository.delete(getWidgetOriginById(widgetOriginId));
    }

    private UserEntity resolveUser(BigInteger userId) {
        if (userId == null) return null;

        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }
}
