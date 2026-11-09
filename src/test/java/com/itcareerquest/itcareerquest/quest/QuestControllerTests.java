package com.itcareerquest.itcareerquest.quest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class QuestControllerTests {
    private QuestRepository repository;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        repository = mock(QuestRepository.class);
        mvc = MockMvcBuilders.standaloneSetup(new QuestController(new QuestService(repository))).build();
    }

    @Test
    void createsQuestWithNormalizedTitleAndOptionalFields() throws Exception {
        UUID id = UUID.randomUUID();
        when(repository.save(any(Quest.class))).thenAnswer(invocation -> {
            Quest quest = invocation.getArgument(0);
            var field = Quest.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(quest, id);
            return quest;
        });

        mvc.perform(post("/api/quests").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"  Aprender JPA  ","difficulty":"EASY","xp":50}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/quests/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Aprender JPA"))
                .andExpect(jsonPath("$.description").value(""))
                .andExpect(jsonPath("$.xp").value(50));
    }

    @Test
    void listsQuests() throws Exception {
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(
                new Quest("Estudiar", "JPA", Difficulty.MEDIUM, 100, null)));
        mvc.perform(get("/api/quests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Estudiar"));
    }

    @Test
    void findsQuestById() throws Exception {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(
                new Quest("Estudiar", "JPA", Difficulty.MEDIUM, 100, null)));
        mvc.perform(get("/api/quests/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Estudiar"));
    }

    @Test
    void returnsNotFoundForUnknownQuest() throws Exception {
        mvc.perform(get("/api/quests/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createsNestedDefinitionWithoutCompletionState() throws Exception {
        when(repository.save(any(Quest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        mvc.perform(post("/api/quests").contentType(MediaType.APPLICATION_JSON).content("""
                {"title":"Portfolio","difficulty":"MEDIUM","xp":100,
                 "steps":[{"title":" Backend ","steps":[{"title":"API"},{"title":"SQL"}]},
                          {"title":"Frontend","steps":[]}]}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.steps[0].title").value("Backend"))
                .andExpect(jsonPath("$.steps[0].steps[0].title").value("API"))
                .andExpect(jsonPath("$.steps[0].steps[1].title").value("SQL"))
                .andExpect(jsonPath("$.steps[1].title").value("Frontend"))
                .andExpect(jsonPath("$.steps[0].completed").doesNotExist());
    }

    @Test
    void rejectsInvalidNestedStepsBeforeSaving() throws Exception {
        for (String steps : List.of("[null]", "[{\"title\":\"Root\",\"steps\":[{\"title\":\" \"}]}]")) {
            mvc.perform(post("/api/quests").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Test\",\"difficulty\":\"EASY\",\"xp\":0,\"steps\":" + steps + "}"))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(repository);
    }

    @Test
    void updatesAndDeletesExistingDefinition() throws Exception {
        UUID id = UUID.randomUUID();
        Quest quest = new Quest("Antes", "", Difficulty.EASY, 0, null);
        when(repository.findById(id)).thenReturn(Optional.of(quest));
        mvc.perform(put("/api/quests/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                {"title":"Después","difficulty":"HARD","xp":200,
                 "steps":[{"title":"Nuevo paso"}]}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Después"))
                .andExpect(jsonPath("$.steps[0].title").value("Nuevo paso"));
        mvc.perform(delete("/api/quests/{id}", id)).andExpect(status().isNoContent());
        verify(repository).delete(quest);
    }

    @Test
    void rejectsInvalidRequestsWithoutWriting() throws Exception {
        for (String body : List.of(
                "{\"title\":\" \",\"difficulty\":\"EASY\",\"xp\":50}",
                "{\"title\":\"Test\",\"difficulty\":\"EASY\",\"xp\":-1}",
                "{\"title\":\"Test\",\"xp\":50}",
                "{\"title\":\"Test\",\"difficulty\":\"EASY\"}",
                "{\"title\":\"Test\",\"difficulty\":\"UNKNOWN\",\"xp\":50}",
                "{\"title\":\"Test\",\"difficulty\":\"EASY\",\"xp\":50,\"deadline\":\"invalid\"}",
                "{\"title\":\"" + "a".repeat(201) + "\",\"difficulty\":\"EASY\",\"xp\":0}")) {
            mvc.perform(post("/api/quests").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(repository);
    }
}
