package com.pleasebookme.server.core.selectedslot.service;

import com.pleasebookme.server.core.selectedslot.dto.SelectedSlotRequest;
import com.pleasebookme.server.core.selectedslot.entity.SelectedSlotEntity;

import java.math.BigInteger;
import java.util.List;

public interface SelectedSlotService {
    SelectedSlotEntity createSelectedSlot(SelectedSlotRequest request);

    SelectedSlotEntity getSelectedSlotById(BigInteger selectedSlotId);

    List<SelectedSlotEntity> getAllSelectedSlots();

    SelectedSlotEntity updateSelectedSlot(
        BigInteger selectedSlotId,
        SelectedSlotRequest request
    );

    void deleteSelectedSlot(BigInteger selectedSlotId);
}
