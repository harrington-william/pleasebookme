package com.pleasebookme.server.auth.refreshtoken.controller;

import com.pleasebookme.server.auth.refreshtoken.dto.RefreshTokenRequest;
import com.pleasebookme.server.auth.refreshtoken.dto.RefreshTokenResponse;
import com.pleasebookme.server.auth.refreshtoken.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/refresh")
@RequiredArgsConstructor
public class RefreshTokenController {
    private final RefreshTokenService refreshTokenService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RefreshTokenResponse createRefreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return RefreshTokenResponse.from(refreshTokenService.createRefreshToken(request));
    }

    @GetMapping("/{refreshTokenId}")
    public RefreshTokenResponse getRefreshToken(@PathVariable BigInteger refreshTokenId) {
        return RefreshTokenResponse.from(refreshTokenService.getRefreshTokenById(refreshTokenId));
    }

    @GetMapping
    public List<RefreshTokenResponse> getRefreshTokens() {
        return refreshTokenService.getAllRefreshTokens().stream()
            .map(RefreshTokenResponse::from)
            .toList();
    }

    @PutMapping("/{refreshTokenId}")
    public RefreshTokenResponse updateRefreshToken(
        @PathVariable BigInteger refreshTokenId,
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        return RefreshTokenResponse.from(refreshTokenService.updateRefreshToken(refreshTokenId, request));
    }

    @DeleteMapping("/{refreshTokenId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRefreshToken(@PathVariable BigInteger refreshTokenId) {
        refreshTokenService.deleteRefreshToken(refreshTokenId);
    }
}
