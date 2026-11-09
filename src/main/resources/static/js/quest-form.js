import {questApi} from "/js/quest-api.js";

const path = window.location.pathname.split("/");
const editing = path[1] === "quests" && path[3] === "edit";
const questId = editing ? path[2] : null;
const form = document.getElementById("quest-form");
const fields = document.getElementById("quest-fields");
const error = document.getElementById("form-error");
const submitButton = document.getElementById("submit-button");

function createStep(existing = {title: "", steps: []}, depth = 1) {
    const element = document.createElement("div");
    element.className = "step";
    const input = document.createElement("input");
    input.className = "step-title";
    input.placeholder = "Paso...";
    input.setAttribute("aria-label", "Título del paso");
    input.required = true;
    input.maxLength = 200;
    input.value = existing.title;
    element.append(input);

    const children = document.createElement("div");
    children.className = "substeps";
    children.style.marginLeft = "1.5rem";

    const addButton = document.createElement("button");
    addButton.type = "button";
    addButton.textContent = "Añadir subpaso";
    addButton.disabled = depth >= 12;
    addButton.addEventListener("click", () => children.append(createStep(undefined, depth + 1)));

    const deleteButton = document.createElement("button");
    deleteButton.type = "button";
    deleteButton.textContent = "Eliminar paso";
    deleteButton.addEventListener("click", () => {
        if (confirm("¿Quieres eliminar este paso y sus subpasos?")) element.remove();
    });
    element.append(addButton, deleteButton, children);
    for (const child of existing.steps) children.append(createStep(child, depth + 1));
    return element;
}

function readSteps(container) {
    return Array.from(container.children, element => ({
        title: element.querySelector(":scope > .step-title").value.trim(),
        steps: readSteps(element.querySelector(":scope > .substeps"))
    }));
}

document.getElementById("add-step").addEventListener("click", () => {
    document.getElementById("steps").append(createStep());
});

form.addEventListener("submit", async event => {
    event.preventDefault();
    error.textContent = "";
    const quest = {
        title: document.getElementById("title").value.trim(),
        description: document.getElementById("description").value,
        difficulty: document.getElementById("difficulty").value,
        xp: Number(document.getElementById("xp").value),
        deadline: document.getElementById("deadline").value || null,
        steps: readSteps(document.getElementById("steps"))
    };
    fields.disabled = true;
    try {
        const saved = editing ? await questApi.update(questId, quest) : await questApi.create(quest);
        window.location.assign(`/quests/${saved.id}`);
    } catch (failure) {
        error.textContent = failure.message;
        fields.disabled = false;
    }
});

async function initialize() {
    if (!editing) return;
    document.getElementById("form-title").textContent = "Editar misión";
    submitButton.textContent = "Guardar cambios";
    fields.disabled = true;
    try {
        const quest = await questApi.get(questId);
        for (const name of ["title", "description", "difficulty", "xp", "deadline"]) {
            document.getElementById(name).value = quest[name] ?? "";
        }
        for (const step of quest.steps) document.getElementById("steps").append(createStep(step));
        fields.disabled = false;
    } catch (failure) {
        error.textContent = failure.message;
    }
}

initialize();
