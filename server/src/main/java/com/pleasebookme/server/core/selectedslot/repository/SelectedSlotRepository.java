package com.pleasebookme.server.core.selectedslot.repository;

import com.pleasebookme.server.core.selectedslot.entity.SelectedSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

@Repository
public interface SelectedSlotRepository extends JpaRepository<SelectedSlotEntity, BigInteger> {
    boolean existsByServiceServiceIdAndUserUserIdAndSlotStartAndSlotEnd(
        BigInteger serviceId,
        BigInteger userId,
        Instant slotStart,
        Instant slotEnd
    );

    List<SelectedSlotEntity> findByServiceUserUserIdAndReleaseAtAfterAndSlotStartBeforeAndSlotEndAfter(
        BigInteger hostUserId,
        Instant now,
        Instant slotStartBefore,
        Instant slotEndAfter
    );
}
