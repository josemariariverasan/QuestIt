# Persistencia de definiciones de misión

La creación, listado, detalle, edición y borrado de misiones utilizan ahora
`/api/quests` y PostgreSQL. No se importan ni se leen las misiones de `localStorage`.
El perfil y los logros siguen utilizando su implementación provisional anterior.

`quests` representa la **definición**. `quest_steps` guarda títulos, orden y
jerarquía. Cada paso raíz referencia una misión; cada subpaso referencia su padre.
No contienen estados de completado, progreso ni XP concedida. La XP de `quests`
es la recompensa configurada. Las realizaciones y la entrega única de recompensas
quedan pendientes. Por eso esta vista todavía no permite marcar pasos ni completar
misiones, y la pestaña Terminadas está deshabilitada.

`deadline` se conserva por compatibilidad. Si representa un vencimiento de una
realización, se trasladará a esa realización cuando se incorpore ese modelo.
La edición actual sustituye el árbol de pasos y genera nuevos UUID para esos pasos;
antes de vincular ejecuciones habrá que definir cómo se conservan sus referencias.

## Esquema

En una instalación nueva, ejecutar una sola vez, en este orden:

1. `database/001-create-quests.sql`
2. `database/002-create-quest-steps.sql`

En una instalación que ya tiene `quests`, ejecutar solo el segundo script. Ambos
son explícitos: fallan si su tabla ya existe. No eliminan ni reemplazan tablas.
Usar una transacción (`psql --single-transaction -v ON_ERROR_STOP=1 -f ...`) para
que tabla e índices se creen juntos. La aplicación mantiene `ddl-auto: none`.
La base local de este proyecto ya tiene ambos scripts aplicados.

## Comprobar desde la interfaz

Arrancar con el procedimiento de [ejecucion-segura.md](ejecucion-segura.md).

1. Abrir `http://localhost:8080/create-quest`.
2. Introducir título, dificultad y XP. La descripción, fecha y los pasos son opcionales.
3. Añadir dos pasos, y varios subpasos a uno de ellos. Cada título debe tener contenido.
4. Guardar: se abre el detalle con el árbol completo en su orden original.
5. Volver a la lista: la misión aparece sin depender de datos del navegador.
6. Editar la definición, guardar y volver a consultar.
7. Reiniciar la aplicación o abrir otro navegador: la misión sigue disponible.

El borrado pide confirmación y elimina la definición seleccionada y sus pasos.
Los fallos de red/servidor se muestran en pantalla; un guardado fallido no vacía
el formulario. Mientras se guarda no se puede enviar de nuevo.

## API

- `GET /api/quests`: lista de definiciones con pasos.
- `GET /api/quests/{id}`: definición individual (`404` si no existe).
- `POST /api/quests`: crea y devuelve `201`, `Location` y la definición guardada.
- `PUT /api/quests/{id}`: reemplaza la definición completa y sus pasos.
- `DELETE /api/quests/{id}`: elimina y devuelve `204`.

Ejemplo de cuerpo POST/PUT:

```json
{
  "title": "Preparar portfolio",
  "description": "Primer proyecto completo",
  "difficulty": "MEDIUM",
  "xp": 100,
  "deadline": null,
  "steps": [
    {"title": "Backend", "steps": [
      {"title": "API", "steps": []},
      {"title": "Pruebas", "steps": []}
    ]},
    {"title": "Frontend", "steps": []}
  ]
}
```

Título (1–200 caracteres), dificultad y XP entera no negativa son obligatorios.
La fecha usa `AAAA-MM-DD` o `null`. Si se omite `steps`, se interpreta como lista
vacía; en PUT eso elimina los pasos anteriores. Se admiten hasta 12 niveles y
300 pasos por definición. Los datos inválidos se rechazan antes de escribir.

El servicio transaccional guarda la misión y su árbol en una sola operación.
Los objetos de respuesta contienen solo datos de la API, sin referencias inversas
al padre, evitando ciclos al convertir las entidades a JSON.

## Pruebas

Ejecutar `scripts/questit.ps1 test` como se indica en la guía de ejecución segura.
Las pruebas HTTP validan rutas y datos con un repositorio simulado. Las pruebas
`QuestPersistenceTests` utilizan PostgreSQL real, vacían la caché de JPA antes de
leer, comprueban jerarquía y orden, edición sin descendientes huérfanos y borrado.
Se ejecutan con rollback: no dejan datos de prueba ni alteran misiones existentes.
Es necesario haber aplicado ambos scripts antes de ejecutar la suite completa.