package com.pleasebookme.server.widget.widgets.repository;

import com.pleasebookme.server.widget.widgets.entity.WidgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface WidgetRepository extends JpaRepository<WidgetEntity, BigInteger> {
    boolean existsByPublicKey(String publicKey);

    Optional<WidgetEntity> findByPublicKey(String publicKey);
}
