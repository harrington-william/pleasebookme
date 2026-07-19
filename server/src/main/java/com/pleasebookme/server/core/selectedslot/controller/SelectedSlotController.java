package com.pleasebookme.server.core.selectedslot.controller;

import com.pleasebookme.server.core.selectedslot.dto.SelectedSlotRequest;
import com.pleasebookme.server.core.selectedslot.dto.SelectedSlotResponse;
import com.pleasebookme.server.core.selectedslot.service.SelectedSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/selected-slots")
@RequiredArgsConstructor
public class SelectedSlotController {
    private final SelectedSlotService selectedSlotService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SelectedSlotResponse createSelectedSlot(@Valid @RequestBody SelectedSlotRequest request) {
        return SelectedSlotResponse.from(selectedSlotService.createSelectedSlot(request));
    }

    @GetMapping("/{selectedSlotId}")
    public SelectedSlotResponse getSelectedSlot(@PathVariable BigInteger selectedSlotId) {
        return SelectedSlotResponse.from(selectedSlotService.getSelectedSlotById(selectedSlotId));
    }

    @GetMapping
    public List<SelectedSlotResponse> getSelectedSlots() {
        return selectedSlotService.getAllSelectedSlots().stream()
            .map(SelectedSlotResponse::from)
            .toList();
    }

    @PutMapping("/{selectedSlotId}")
    public SelectedSlotResponse updateSelectedSlot(
        @PathVariable BigInteger selectedSlotId,
        @Valid @RequestBody SelectedSlotRequest request
    ) {
        return SelectedSlotResponse.from(selectedSlotService.updateSelectedSlot(selectedSlotId, request));
    }

    @DeleteMapping("/{selectedSlotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSelectedSlot(@PathVariable BigInteger selectedSlotId) {
        selectedSlotService.deleteSelectedSlot(selectedSlotId);
    }
}
