# Consulta Pública con Captcha — HydroGest (Fase 2, Feature 1)

Página pública, sin login, donde un cliente escribe el **código de su contador**, resuelve un **reCAPTCHA** y ve su **estado de cuenta**. El monolito Laravel no se modifica.

## Arquitectura

```
[Navegador] --POST /api/consulta {codigo, token}--> [web-publica · Next.js BFF :3000]
                                                       1. valida formato del código
                                                       2. verifica captcha con Google (lado servidor)
                                                       3. GET /api/v1/consulta/{codigo}
                                                              |
                                                              v
                                              [consulta-service · Spring Boot :8081]
                                                       1. rate limit por IP (10/min)
                                                       2. SELECT con usuario hydro_ro
                                                              |
                                                              v
                                      [MariaDB XAMPP · base hidrogest] <--- Laravel (sin cambios)
```

El navegador **nunca** llama al puerto 8081: solo ve `/api/consulta`.

## Requisitos

- JDK 21, Node.js 20+, XAMPP con la base `hidrogest` corriendo.
- Docker Desktop (solo para levantar con `docker compose`).

## 1. Usuario de solo lectura (una vez)

Como `root`. Se crea para `localhost` (ejecución local) y para `%` (Docker):

```powershell
& "C:\xampp\mysql\bin\mysql.exe" -u root -p -e "CREATE USER IF NOT EXISTS 'hydro_ro'@'localhost' IDENTIFIED BY 'HydroRO2026'; CREATE USER IF NOT EXISTS 'hydro_ro'@'%' IDENTIFIED BY 'HydroRO2026'; GRANT SELECT ON hidrogest.* TO 'hydro_ro'@'localhost'; GRANT SELECT ON hidrogest.* TO 'hydro_ro'@'%'; FLUSH PRIVILEGES;"
```

Verificación: `SELECT` funciona y `UPDATE` responde `ERROR 1142 ... command denied`.

## 2. Levantar sin Docker (Windows / PowerShell)

**Terminal 1: microservicio**
```powershell
cd fase2\consulta-service
$env:DB_PASSWORD = "HydroRO2026"
$env:DB_URL = "jdbc:mariadb://localhost:3306/hidrogest"
.\mvnw.cmd spring-boot:run
```

**Terminal 2: frontend**. Crear `fase2\web-publica\.env.local` con las dos llaves de `fase2\.env.example` más `CONSULTA_SERVICE_URL=http://localhost:8081`, luego:
```powershell
cd fase2\web-publica
npm install
npm run dev
```

Si PowerShell bloquea `npm`: `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned`, o usar `npm.cmd`.

- Página: http://localhost:3000/consulta
- Swagger: http://localhost:8081/swagger-ui.html

## 3. Levantar con Docker

```powershell
cd fase2
Copy-Item .env.example .env
docker compose up --build
```

## Contrato (contract-first)

`consulta-service/src/main/resources/static/openapi.yaml` es la fuente de verdad. En cada build, `openapi-generator-maven-plugin` genera la interfaz `ConsultaApi` y los modelos (`EstadoCuenta`, `UltimaLectura`, `PagoResumen`, `ErrorRespuesta`). El `ConsultaController` implementa esa interfaz.

## Seguridad

1. **reCAPTCHA v2** verificado en el servidor de Next.js (la llave secreta nunca llega al navegador).
2. **Rate limit** de 10 peticiones por minuto por IP en el microservicio, con respuesta `429` y `Retry-After`.
3. **Solo lectura** en tres capas: usuario `hydro_ro` con solo `SELECT`, `ddl-auto=none` y entidades `@Immutable`.
4. **Titular enmascarado**: `Carlos G***`.

Limitación conocida: en local el puerto 8081 está publicado para Swagger. En producción no debe exponerse; solo el BFF debería alcanzarlo.

## Datos de prueba

- `CTR-001`: al día.
- `CTR-002`: pendiente, tras ejecutar `sql/02-dato-prueba-deuda.sql` como root.

## Pruebas de aceptación

| # | Prueba | Resultado esperado |
|---|---|---|
| 1 | `CTR-001` + captcha | Estado de cuenta igual al dashboard de Laravel |
| 2 | `CTR-999` | 404 "No existe un contador con ese codigo" |
| 3 | `ctr@001` directo al 8081 | 400 formato inválido |
| 4 | 15 peticiones seguidas al 8081 | 10 × 200 y 5 × 429 |
| 5 | `UPDATE` como `hydro_ro` | `ERROR 1142 command denied` |
| 6 | Swagger UI | Muestra el contrato |
| 7 | F12 → Red | Solo `/api/consulta`, nunca 8081 |
| 8 | `docker compose up --build` | Ambos servicios arriba y la consulta funciona |
| 9 | `git status` del monolito | Ningún archivo de Laravel modificado |

Prueba de rate limit:
```powershell
1..15 | ForEach-Object { curl.exe -s -o NUL -w "%{http_code}`n" http://localhost:8081/api/v1/consulta/CTR-001 }
```

Tests unitarios (enmascarado, sin base de datos):
```powershell
cd fase2\consulta-service
.\mvnw.cmd test
```