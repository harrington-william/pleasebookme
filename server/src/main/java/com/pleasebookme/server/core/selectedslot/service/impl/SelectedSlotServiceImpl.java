package com.pleasebookme.server.core.selectedslot.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.core.selectedslot.dto.SelectedSlotRequest;
import com.pleasebookme.server.core.selectedslot.entity.SelectedSlotEntity;
import com.pleasebookme.server.core.selectedslot.exception.DuplicateSelectedSlotException;
import com.pleasebookme.server.core.selectedslot.exception.SelectedSlotNotFoundException;
import com.pleasebookme.server.core.selectedslot.repository.SelectedSlotRepository;
import com.pleasebookme.server.core.selectedslot.service.SelectedSlotService;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.exception.ServiceNotFoundException;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SelectedSlotServiceImpl implements SelectedSlotService {
    private final SelectedSlotRepository selectedSlotRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    @Override
    public SelectedSlotEntity createSelectedSlot(SelectedSlotRequest request) {
        if (selectedSlotRepository.existsByServiceServiceIdAndUserUserIdAndSlotStartAndSlotEnd(
            request.serviceId(), request.userId(), request.slotStart(), request.slotEnd()
        )) {
            throw new DuplicateSelectedSlotException(
                "Selected slot already exists for service " + request.serviceId() + " and user " + request.userId()
            );
        }

        ServiceEntity service = serviceRepository.findById(request.serviceId())
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + request.serviceId()));

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        SelectedSlotEntity.SelectedSlotEntityBuilder selectedSlot = SelectedSlotEntity.builder()
            .service(service)
            .user(user)
            .slotStart(request.slotStart())
            .slotEnd(request.slotEnd())
            .releaseAt(request.releaseAt());

        if (request.isSeat() != null) selectedSlot.isSeat(request.isSeat());

        return selectedSlotRepository.save(selectedSlot.build());
    }

    @Override
    public SelectedSlotEntity getSelectedSlotById(BigInteger selectedSlotId) {
        return selectedSlotRepository.findById(selectedSlotId)
            .orElseThrow(() -> new SelectedSlotNotFoundException(
                "Selected slot not found: " + selectedSlotId
            ));
    }

    @Override
    public List<SelectedSlotEntity> getAllSelectedSlots() {
        return selectedSlotRepository.findAll();
    }

    @Override
    public SelectedSlotEntity updateSelectedSlot(
        BigInteger selectedSlotId,
        SelectedSlotRequest request
    ) {
        SelectedSlotEntity selectedSlot = getSelectedSlotById(selectedSlotId);

        ServiceEntity service = serviceRepository.findById(request.serviceId())
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + request.serviceId()));

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        selectedSlot.setService(service);
        selectedSlot.setUser(user);
        selectedSlot.setSlotStart(request.slotStart());
        selectedSlot.setSlotEnd(request.slotEnd());
        selectedSlot.setReleaseAt(request.releaseAt());

        if (request.isSeat() != null) selectedSlot.setIsSeat(request.isSeat());

        return selectedSlotRepository.save(selectedSlot);
    }

    @Override
    public void deleteSelectedSlot(BigInteger selectedSlotId) {
        selectedSlotRepository.delete(getSelectedSlotById(selectedSlotId));
    }
}
