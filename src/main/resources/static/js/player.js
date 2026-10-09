
function obtenerJugador() {
    let datos = localStorage.getItem("jugador");

    if (datos) {
        let jugador = JSON.parse(datos);

        if (jugador.xp === undefined) jugador.xp = 0;
        if (jugador.xpHistorica === undefined) jugador.xpHistorica = 0;
        if (jugador.nivel === undefined) {
            jugador.nivel = calcularNivel(jugador.xpHistorica);
        }
        if (jugador.logros === undefined) jugador.logros = [];
        if (jugador.historial === undefined) jugador.historial = [];

        inicializarLogrosSistema(jugador);
        localStorage.setItem("jugador", JSON.stringify(jugador));

        return jugador;
    }

    let nuevoJugador = {
        nombre: "Jugador",
        genero: "",
        avatar: null,
        xp: 0,
        xpHistorica: 0,
        nivel: 1,
        logros: [],
        historial: []
    };

    inicializarLogrosSistema(nuevoJugador);
    localStorage.setItem("jugador", JSON.stringify(nuevoJugador));

    return nuevoJugador;
}

function calcularNivel(xpHistorica) {
    return Math.floor(xpHistorica / 1000) + 1;
}

function ganarXP(cantidad, descripcion) {
    if (cantidad <= 0) return;

    let jugador = obtenerJugador();

    jugador.xp += cantidad;
    jugador.xpHistorica += cantidad;
    jugador.nivel = calcularNivel(jugador.xpHistorica);

    jugador.historial.push({
        tipo: "XP_GANADA",
        cantidad: cantidad,
        descripcion: descripcion,
        fecha: new Date().toLocaleString()
    });

    localStorage.setItem("jugador", JSON.stringify(jugador));

    if (typeof comprobarLogros === "function") {
        comprobarLogros();
    }
}

function obtenerLogros() {
    return obtenerJugador().logros;
}