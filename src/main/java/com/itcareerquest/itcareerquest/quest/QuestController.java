package com.itcareerquest.itcareerquest.quest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quests")
public class QuestController {
    private final QuestService service;

    public QuestController(QuestService service) {
        this.service = service;
    }

    @GetMapping
    public List<QuestResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public QuestResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<QuestResponse> create(@RequestBody CreateQuestRequest request) {
        QuestResponse quest = service.create(request);
        return ResponseEntity.created(URI.create("/api/quests/" + quest.id())).body(quest);
    }

    @PutMapping("/{id}")
    public QuestResponse update(@PathVariable UUID id, @RequestBody CreateQuestRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}