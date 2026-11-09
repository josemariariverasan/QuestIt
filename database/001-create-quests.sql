-- Ejecutar una vez sobre questit antes de utilizar /api/quests.
CREATE TABLE quests (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL CHECK (char_length(trim(title)) > 0),
    description TEXT NOT NULL,
    difficulty VARCHAR(10) NOT NULL CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD', 'EPIC')),
    xp INTEGER NOT NULL CHECK (xp >= 0),
    deadline DATE
);
