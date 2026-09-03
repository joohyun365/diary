package com.myDiary.doospatch.dto;

import jakarta.validation.constraints.NotBlank;

public record AiResponseDto(
        @NotBlank
        String analyzedMood
) {}
