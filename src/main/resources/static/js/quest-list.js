import {questApi} from "/js/quest-api.js";

const container = document.getElementById("missions");
const error = document.getElementById("list-error");
let viewVersion = 0;

async function showQuests() {
    const version = ++viewVersion;
    document.getElementById("section-title").textContent = "Misiones";
    container.textContent = "Cargando misiones…";
    error.textContent = "";
    try {
        const quests = await questApi.list();
        if (version !== viewVersion) return;
        container.replaceChildren();
        if (!quests.length) container.textContent = "Todavía no hay misiones. Crea la primera.";
        for (const quest of quests) {
            const row = document.createElement("div");
            const link = document.createElement("a");
            link.textContent = quest.title;
            link.href = `/quests/${quest.id}`;
            const remove = document.createElement("button");
            remove.type = "button";
            remove.textContent = "Borrar misión";
            remove.addEventListener("click", async () => {
                if (!confirm("¿Seguro que quieres borrar esta misión y sus pasos?")) return;
                remove.disabled = true;
                error.textContent = "";
                try {
                    await questApi.delete(quest.id);
                    if (version === viewVersion) await showQuests();
                } catch (failure) {
                    error.textContent = failure.message;
                    remove.disabled = false;
                }
            });
            row.append(link, " ", remove);
            container.append(row);
        }
    } catch (failure) {
        if (version !== viewVersion) return;
        container.replaceChildren();
        error.textContent = failure.message;
    }
}

function showAchievements() {
    ++viewVersion;
    error.textContent = "";
    document.getElementById("section-title").textContent = "Logros";
    container.replaceChildren();
    for (const achievement of obtenerJugador().logros) {
        const row = document.createElement("p");
        const state = achievement.completado ? "🏆 Logro desbloqueado" : "🔒 Bloqueado";
        row.textContent = `${state} — ${achievement.nombre}: ${achievement.descripcion}`;
        container.append(row);
    }
}

document.getElementById("show-quests").addEventListener("click", showQuests);
document.getElementById("show-achievements").addEventListener("click", showAchievements);
showQuests();
