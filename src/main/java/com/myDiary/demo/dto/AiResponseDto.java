package com.myDiary.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record AiResponseDto(
        @NotBlank
        String analyzedMood
) {}
