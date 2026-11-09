# Ejecutar QuestIt desde VS Code

La configuracion de IntelliJ no se comparte automaticamente con VS Code.
Spring ya utiliza `${DB_PASSWORD}`; no hace falta modificar `application.yaml`.

Abre una terminal **PowerShell** en VS Code, en la raiz del proyecto.
Para arrancar QuestIt:

```powershell
powershell -NoProfile -File .\scripts\questit.ps1 run
```

Para ejecutar todas las pruebas:

```powershell
powershell -NoProfile -File .\scripts\questit.ps1 test
```

Introduce tu contrasena de PostgreSQL cuando aparezca la solicitud de entrada
oculta. Escribe solo en esa solicitud, no como un comando ni en el chat. Usa la
misma contrasena que tienes configurada en IntelliJ para el usuario `postgres`.
No aparece en pantalla ni queda como comando en el historial.

Cada comando abre un proceso separado. La variable se establece solo en ese
proceso y se hereda por Maven y Java; no se guarda en Windows, `.env`, archivos de
VS Code ni Git. El script la restaura al terminar y el proceso separado se cierra.
Si `DB_PASSWORD` ya existe en el entorno heredado, se utiliza sin volver a pedirla.
Como cualquier variable de entorno usada por Java, existe en texto en la memoria
de los procesos mientras se ejecutan; la entrada oculta no cifra ese entorno.

Para detener la aplicacion, pulsa `Ctrl+C`. Repite el comando para volver a
arrancar. Los botones Run/Debug y Test Explorer de VS Code no heredan la variable
de estos procesos; este procedimiento ejecuta la aplicacion y las pruebas desde
la terminal.

Si PowerShell indica que la ejecucion de scripts esta deshabilitada, utiliza
uno de estos comandos, que aplica la excepcion solo al proceso que se abre:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\questit.ps1 run
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\questit.ps1 test
```

No cambies la politica de ejecucion global. Las politicas de tu organizacion,
si existen, pueden impedir esta excepcion.

El script utiliza Maven instalado o la distribucion del wrapper ya descargada.
Si no encuentra ninguno, intenta utilizar `mvnw.cmd`. No modifica el wrapper.
La URL y el usuario siguen siendo `localhost:5432/questit` y `postgres`, salvo
que configures `DB_URL` o `DB_USERNAME` en el entorno.

Al ejecutar `test`, Maven debe informar del resultado de las pruebas, incluida
`contextLoads`, que inicia Spring y utiliza PostgreSQL. Las pruebas HTTP del
controlador usan un repositorio simulado y no comprueban el guardado real.
Para comprobar la tabla y los datos, sigue
[persistencia-misiones.md](persistencia-misiones.md).

La suite incluye ahora pruebas de persistencia real de pasos y subpasos.
Necesita las tablas de los scripts `001-create-quests.sql` y
`002-create-quest-steps.sql`. Estas pruebas revierten sus cambios al terminar.

No uses comandos que impriman el entorno completo, ni opciones de diagnostico
que vuelquen variables o configuraciones sensibles para comprobar la contrasena.
