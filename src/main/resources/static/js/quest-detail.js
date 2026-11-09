import {questApi} from "/js/quest-api.js";

const id = window.location.pathname.split("/").pop();

function showSteps(steps, container) {
    for (const step of steps) {
        const item = document.createElement("li");
        const title = document.createElement("span");
        title.textContent = step.title;
        item.append(title);
        if (step.steps.length) {
            const children = document.createElement("ol");
            showSteps(step.steps, children);
            item.append(children);
        }
        container.append(item);
    }
}

async function loadQuest() {
    try {
        const quest = await questApi.get(id);
        for (const field of ["title", "description", "difficulty", "xp"]) {
            document.getElementById(field).textContent = quest[field];
        }
        document.getElementById("deadline").textContent = quest.deadline ?? "Sin fecha";
        document.getElementById("edit-link").href = `/quests/${quest.id}/edit`;
        document.getElementById("edit-link").hidden = false;
        showSteps(quest.steps, document.getElementById("steps"));
        if (!quest.steps.length) document.getElementById("steps").textContent = "Sin pasos.";
    } catch (failure) {
        document.getElementById("quest-error").textContent = failure.message;
    }
}

loadQuest();
