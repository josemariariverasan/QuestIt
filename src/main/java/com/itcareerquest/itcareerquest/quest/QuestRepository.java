package com.itcareerquest.itcareerquest.quest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuestRepository extends JpaRepository<Quest, UUID> {
}
