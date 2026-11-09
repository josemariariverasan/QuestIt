package com.itcareerquest.itcareerquest.quest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record QuestResponse(UUID id, String title, String description, Difficulty difficulty,
                            int xp, LocalDate deadline, List<StepResponse> steps) {
    public static QuestResponse from(Quest quest) {
        return new QuestResponse(quest.getId(), quest.getTitle(), quest.getDescription(),
                quest.getDifficulty(), quest.getXp(), quest.getDeadline(),
                quest.getSteps().stream().map(StepResponse::from).toList());
    }

    public record StepResponse(UUID id, String title, List<StepResponse> steps) {
        static StepResponse from(QuestStep step) {
            return new StepResponse(step.getId(), step.getTitle(),
                    step.getSteps().stream().map(StepResponse::from).toList());
        }
    }
}
