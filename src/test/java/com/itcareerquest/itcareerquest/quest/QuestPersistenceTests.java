package com.itcareerquest.itcareerquest.quest;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class QuestPersistenceTests {
    @Autowired private QuestService service;
    @Autowired private EntityManager entityManager;

    @Test
    void persistsNestedStepsAndTheirOrder() {
        QuestResponse created = service.create(definition("Integración", List.of(
                new CreateQuestRequest.StepRequest("Primero", List.of(
                        new CreateQuestRequest.StepRequest("Hijo A", List.of(
                                new CreateQuestRequest.StepRequest("Nieto", null))),
                        new CreateQuestRequest.StepRequest("Hijo B", null))),
                new CreateQuestRequest.StepRequest("Segundo", null))));
        reload();

        QuestResponse stored = service.get(created.id());
        assertEquals(List.of("Primero", "Segundo"), stored.steps().stream().map(QuestResponse.StepResponse::title).toList());
        assertEquals("Nieto", stored.steps().getFirst().steps().getFirst().steps().getFirst().title());
        assertNotNull(stored.steps().getFirst().steps().getFirst().id());
        assertEquals(5L, stepCount(created.id()));
    }

    @Test
    void replacingStepsRemovesOldDescendants() {
        QuestResponse created = service.create(definition("Antes", List.of(
                new CreateQuestRequest.StepRequest("Raíz", List.of(
                        new CreateQuestRequest.StepRequest("Hijo", null))))));
        reload();
        service.update(created.id(), definition("Después", List.of(
                new CreateQuestRequest.StepRequest("Sustituto", null))));
        reload();

        assertEquals("Después", service.get(created.id()).title());
        assertEquals("Sustituto", service.get(created.id()).steps().getFirst().title());
        assertEquals(1L, stepCount(created.id()));
        assertNull(entityManager.find(QuestStep.class, created.steps().getFirst().steps().getFirst().id()));
    }

    @Test
    void deletingTestDefinitionRemovesItsSteps() {
        QuestResponse created = service.create(definition("Borrar prueba", List.of(
                new CreateQuestRequest.StepRequest("Raíz", List.of(
                        new CreateQuestRequest.StepRequest("Hijo", null))))));
        reload();
        UUID childId = service.get(created.id()).steps().getFirst().steps().getFirst().id();
        service.delete(created.id());
        reload();

        assertNull(entityManager.find(Quest.class, created.id()));
        assertNull(entityManager.find(QuestStep.class, childId));
    }

    private CreateQuestRequest definition(String title, List<CreateQuestRequest.StepRequest> steps) {
        return new CreateQuestRequest(title, "Prueba con rollback", Difficulty.EASY, 50, null, steps);
    }

    private void reload() {
        entityManager.flush();
        entityManager.clear();
    }

    private long stepCount(UUID questId) {
        // Incluye todos los descendientes para detectar pasos huérfanos tras una edición.
        return ((Number) entityManager.createNativeQuery("""
                WITH RECURSIVE tree AS (
                    SELECT id FROM quest_steps WHERE quest_id = :questId
                    UNION ALL
                    SELECT child.id FROM quest_steps child JOIN tree parent ON child.parent_id = parent.id
                ) SELECT count(*) FROM tree
                """).setParameter("questId", questId).getSingleResult()).longValue();
    }
}
