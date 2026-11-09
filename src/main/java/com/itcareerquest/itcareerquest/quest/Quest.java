package com.itcareerquest.itcareerquest.quest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.time.LocalDate;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quests")
public class Quest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty;

    @Column(nullable = false)
    private int xp;

    private LocalDate deadline;

    @OneToMany(mappedBy = "quest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<QuestStep> steps = new ArrayList<>();

    protected Quest() {
    }

    public Quest(String title, String description, Difficulty difficulty, int xp, LocalDate deadline) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.xp = xp;
        this.deadline = deadline;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getXp() { return xp; }
    public LocalDate getDeadline() { return deadline; }
    public List<QuestStep> getSteps() { return steps; }

    public void updateDefinition(CreateQuestRequest request) {
        title = request.title().strip();
        description = request.description() == null ? "" : request.description();
        difficulty = request.difficulty();
        xp = request.xp();
        deadline = request.deadline();
        // Solo reemplazamos la estructura de la definición, nunca un progreso de ejecución.
        steps.clear();
        if (request.steps() != null) {
            for (int position = 0; position < request.steps().size(); position++) {
                steps.add(QuestStep.root(this, request.steps().get(position), position));
            }
        }
    }
}
