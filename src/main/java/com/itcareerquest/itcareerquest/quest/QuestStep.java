package com.itcareerquest.itcareerquest.quest;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "quest_steps")
public class QuestStep {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "step_position", nullable = false)
    private int position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id")
    private Quest quest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private QuestStep parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<QuestStep> steps = new ArrayList<>();

    protected QuestStep() {
    }

    private QuestStep(CreateQuestRequest.StepRequest request, int position) {
        this.title = request.title().strip();
        this.position = position;
        if (request.steps() != null) {
            for (int childPosition = 0; childPosition < request.steps().size(); childPosition++) {
                QuestStep child = new QuestStep(request.steps().get(childPosition), childPosition);
                child.parent = this;
                steps.add(child);
            }
        }
    }

    public static QuestStep root(Quest quest, CreateQuestRequest.StepRequest request, int position) {
        QuestStep step = new QuestStep(request, position);
        step.quest = quest;
        return step;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public List<QuestStep> getSteps() { return steps; }
}
