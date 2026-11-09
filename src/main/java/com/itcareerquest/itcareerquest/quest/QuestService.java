package com.itcareerquest.itcareerquest.quest;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class QuestService {
    private static final int MAX_DEPTH = 12;
    private static final int MAX_STEPS = 300;
    private final QuestRepository repository;

    public QuestService(QuestRepository repository) {
        this.repository = repository;
    }

    public List<QuestResponse> list() {
        return repository.findAll(Sort.by("title", "id")).stream().map(QuestResponse::from).toList();
    }

    public QuestResponse get(UUID id) {
        return QuestResponse.from(find(id));
    }

    @Transactional
    public QuestResponse create(CreateQuestRequest request) {
        validate(request);
        Quest quest = new Quest();
        quest.updateDefinition(request);
        return QuestResponse.from(repository.save(quest));
    }

    @Transactional
    public QuestResponse update(UUID id, CreateQuestRequest request) {
        validate(request);
        Quest quest = find(id);
        quest.updateDefinition(request);
        // El flush asigna también los UUID de los pasos nuevos antes de responder.
        repository.flush();
        return QuestResponse.from(quest);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(find(id));
    }

    private Quest find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Misión no encontrada"));
    }

    private void validate(CreateQuestRequest request) {
        validateTitle(request.title(), "El título de la misión");
        if (request.difficulty() == null) {
            throw badRequest("La dificultad es obligatoria");
        }
        if (request.xp() == null || request.xp() < 0) {
            throw badRequest("La XP debe ser un entero mayor o igual a cero");
        }
        validateSteps(request.steps(), 1, 0);
    }

    private void validateTitle(String title, String field) {
        if (title == null || title.isBlank() || title.strip().length() > 200) {
            throw badRequest(field + " debe tener entre 1 y 200 caracteres");
        }
    }

    private int validateSteps(List<CreateQuestRequest.StepRequest> steps, int depth, int count) {
        if (steps == null || steps.isEmpty()) {
            return count;
        }
        if (depth > MAX_DEPTH) {
            throw badRequest("Se permiten hasta " + MAX_DEPTH + " niveles de pasos");
        }
        for (var step : steps) {
            if (step == null || ++count > MAX_STEPS) {
                throw badRequest("Los pasos deben ser válidos y no superar " + MAX_STEPS + " elementos");
            }
            validateTitle(step.title(), "El título de cada paso");
            count = validateSteps(step.steps(), depth + 1, count);
        }
        return count;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
