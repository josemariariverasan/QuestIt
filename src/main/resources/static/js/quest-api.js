async function request(path = "", options = {}) {
    let response;
    try {
        response = await fetch(`/api/quests${path}`, {
            ...options,
            headers: {"Content-Type": "application/json", ...options.headers}
        });
    } catch {
        throw new Error("No se pudo conectar con el servidor. Comprueba que la aplicación esté arrancada.");
    }
    if (!response.ok) {
        if (response.status === 404) throw new Error("La misión ya no existe.");
        if (response.status === 400) throw new Error("Revisa los datos: títulos de 1 a 200 caracteres, XP no negativa y hasta 12 niveles y 300 pasos.");
        throw new Error("No se pudo completar la operación. Inténtalo de nuevo.");
    }
    return response.status === 204 ? null : response.json();
}

export const questApi = {
    list: () => request(),
    get: id => request(`/${encodeURIComponent(id)}`),
    create: quest => request("", {method: "POST", body: JSON.stringify(quest)}),
    update: (id, quest) => request(`/${encodeURIComponent(id)}`, {method: "PUT", body: JSON.stringify(quest)}),
    delete: id => request(`/${encodeURIComponent(id)}`, {method: "DELETE"})
};
