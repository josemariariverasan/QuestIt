const LOGROS_SISTEMA = [
    {
        id: "primera-mision",
        tipo: "SISTEMA",
        nombre: "Primera misión",
        descripcion: "Completa tu primera misión.",
        xp: 50
    },
    {
        id: "cinco-misiones",
        tipo: "SISTEMA",
        nombre: "En marcha",
        descripcion: "Completa 5 misiones.",
        xp: 100
    },
    {
        id: "subir-de-nivel",
        tipo: "SISTEMA",
        nombre: "Subiendo de nivel",
        descripcion: "Alcanza el nivel 2.",
        xp: 100
    }
];

function obtenerLogros() {
    return obtenerJugador().logros;
}

function inicializarLogrosSistema(jugador) {
    for (const plantilla of LOGROS_SISTEMA) {
        const existe = jugador.logros.some(function (logro) {
            return logro.id === plantilla.id;
        });

        if (!existe) {
            jugador.logros.push({
                ...plantilla,
                tipo: "SISTEMA",
                misiones: [],
                completado: false,
                completadoAt: null
            });
        }
    }
}

function comprobarLogrosIniciales() {
    comprobarLogros();
}

function comprobarLogros() {
    const jugador = obtenerJugador();

    const misiones = JSON.parse(
        localStorage.getItem("missions") || "[]"
    );

    const completadas = misiones.filter(function (mision) {
        return mision.completed === true;
    }).length;

    const desbloqueados = [];

    for (const logro of jugador.logros) {
        if (logro.completado) {
            continue;
        }

        let cumple = false;

        switch (logro.id) {
            case "primera-mision":
                cumple = completadas >= 1;
                break;

            case "cinco-misiones":
                cumple = completadas >= 5;
                break;

            case "subir-de-nivel":
                cumple = jugador.nivel >= 2;
                break;
        }

        if (cumple) {
            logro.completado = true;
            logro.completadoAt = new Date().toLocaleString();

            jugador.historial.push({
                tipo: "LOGRO_CONSEGUIDO",
                descripcion: logro.nombre,
                fecha: logro.completadoAt
            });

            desbloqueados.push({
                nombre: logro.nombre,
                xp: logro.xp
            });
        }
    }

    // Guardamos el estado antes de entregar las recompensas.
    localStorage.setItem("jugador", JSON.stringify(jugador));

    for (const logro of desbloqueados) {
        alert("¡Logro desbloqueado!\n\n" + logro.nombre);

        if (
            logro.xp > 0 &&
            typeof ganarXP === "function"
        ) {
            ganarXP(
                logro.xp,
                "Logro desbloqueado: " + logro.nombre
            );
        }
    }
}