package com.pleasebookme.server.widget.widgetorigin.repository;

import com.pleasebookme.server.widget.widgetorigin.entity.WidgetOriginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

@Repository
public interface WidgetOriginRepository extends JpaRepository<WidgetOriginEntity, BigInteger> {
    boolean existsByWidgetWidgetIdAndOrigin(
        BigInteger widgetId,
        String origin
    );

    List<WidgetOriginEntity> findAllByWidgetWidgetIdIn(Collection<BigInteger> widgetIds);
}
