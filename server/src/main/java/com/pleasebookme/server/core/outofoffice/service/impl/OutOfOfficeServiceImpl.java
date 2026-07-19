package com.pleasebookme.server.core.outofoffice.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.core.outofoffice.dto.OutOfOfficeRequest;
import com.pleasebookme.server.core.outofoffice.entity.OutOfOfficeEntity;
import com.pleasebookme.server.core.outofoffice.exception.OutOfOfficeNotFoundException;
import com.pleasebookme.server.core.outofoffice.repository.OutOfOfficeRepository;
import com.pleasebookme.server.core.outofoffice.service.OutOfOfficeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutOfOfficeServiceImpl implements OutOfOfficeService {
    private final OutOfOfficeRepository outOfOfficeRepository;
    private final UserRepository userRepository;

    @Override
    public OutOfOfficeEntity createOutOfOffice(OutOfOfficeRequest request) {
        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        UserEntity toUser = userRepository.findById(request.toUserId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.toUserId()));

        OutOfOfficeEntity.OutOfOfficeEntityBuilder outOfOffice = OutOfOfficeEntity.builder()
            .startTime(request.startTime())
            .endTime(request.endTime())
            .notes(request.notes())
            .user(user)
            .toUser(toUser)
            .reason(request.reason());

        if (request.showNotePublicly() != null) outOfOffice.showNotePublicly(request.showNotePublicly());

        return outOfOfficeRepository.save(outOfOffice.build());
    }

    @Override
    public OutOfOfficeEntity getOutOfOfficeById(BigInteger outOfOfficeId) {
        return outOfOfficeRepository.findById(outOfOfficeId)
            .orElseThrow(() -> new OutOfOfficeNotFoundException(
                "Out of office not found: " + outOfOfficeId
            ));
    }

    @Override
    public List<OutOfOfficeEntity> getAllOutOfOffices() {
        return outOfOfficeRepository.findAll();
    }

    @Override
    public OutOfOfficeEntity updateOutOfOffice(
        BigInteger outOfOfficeId,
        OutOfOfficeRequest request
    ) {
        OutOfOfficeEntity outOfOffice = getOutOfOfficeById(outOfOfficeId);

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        UserEntity toUser = userRepository.findById(request.toUserId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.toUserId()));

        outOfOffice.setStartTime(request.startTime());
        outOfOffice.setEndTime(request.endTime());
        outOfOffice.setNotes(request.notes());
        outOfOffice.setUser(user);
        outOfOffice.setToUser(toUser);
        outOfOffice.setReason(request.reason());

        if (request.showNotePublicly() != null) outOfOffice.setShowNotePublicly(request.showNotePublicly());

        return outOfOfficeRepository.save(outOfOffice);
    }

    @Override
    public void deleteOutOfOffice(BigInteger outOfOfficeId) {
        outOfOfficeRepository.delete(getOutOfOfficeById(outOfOfficeId));
    }
}
