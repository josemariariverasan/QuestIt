-- Ampliación aditiva: no modifica las misiones existentes.
CREATE TABLE quest_steps (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL CHECK (char_length(trim(title)) > 0),
    step_position INTEGER NOT NULL CHECK (step_position >= 0),
    quest_id UUID REFERENCES quests(id) ON DELETE CASCADE,
    parent_id UUID REFERENCES quest_steps(id) ON DELETE CASCADE,
    -- Un paso raíz pertenece a la misión; un subpaso pertenece a otro paso.
    CHECK ((quest_id IS NOT NULL) <> (parent_id IS NOT NULL))
);

CREATE INDEX quest_steps_quest_idx ON quest_steps(quest_id);
CREATE INDEX quest_steps_parent_idx ON quest_steps(parent_id);
