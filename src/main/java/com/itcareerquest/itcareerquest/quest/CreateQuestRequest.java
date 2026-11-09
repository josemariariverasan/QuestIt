package com.itcareerquest.itcareerquest.quest;

import java.time.LocalDate;
import java.util.List;

public record CreateQuestRequest(
        String title,
        String description,
        Difficulty difficulty,
        Integer xp,
        LocalDate deadline,
        List<StepRequest> steps
) {
    public record StepRequest(String title, List<StepRequest> steps) {
    }
}
