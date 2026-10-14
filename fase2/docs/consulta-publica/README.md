# Consulta Pública con Captcha - HydroGest

## ¿Qué es la Feature 1?

Una página pública, sin login ni registro, donde un cliente final escribe el **código de su contador**, resuelve un **captcha** y ve su **estado de cuenta**.

## Diagrama de la Arquitectura

```
[Navegador del cliente]
   Página /consulta: formulario + captcha. Sin login.
        | POST /api/consulta   { codigo, token }
        v
+-------------------- Docker Compose (local) --------------------+
|                                                                |
|  [Next.js · BFF  :3000]                                        |
|    1. Valida el código                                         |
|    2. Verifica el captcha con Google -----> [Google reCAPTCHA] |
|    3. Llama al microservicio con la IP real del cliente        |
|        |                                                       |
|        |  GET /api/v1/consulta/{codigoContador}                |
|        |  Cabecera: X-Forwarded-For: <IP del cliente>          |
|        v                                                       |
|  [consulta-service · Spring Boot  :8081]                       |
|    1. Rate limit por IP (segunda barrera)                      |
|    2. Solo SELECT, con usuario de solo lectura                 |
|    3. Devuelve el estado de cuenta en JSON                     |
|                                                                |
+--------|-------------------------------------------------------+          |
         |  solo lectura (SELECT)                               |          |
         v                                                       v          |
[MariaDB de HydroGest] <---- lee y escribe ---- [Laravel · monolito]  |
  (fuera de Docker, en XAMPP)                      (no se modifica)          |
```

## Requisitos

- **JDK 21** — Para el microservicio Spring Boot
- **Node.js 20+** — Para la aplicación Next.js BFF
- **Docker & Docker Compose** — Para levantar los servicios en conjunto
- **XAMPP con HydroGest** — El monolito Laravel y su base de datos deben estar corriendo localmente (fuera de Docker)
- **LLaves de reCAPTCHA v2** — Llaves de prueba incluidas en `.env.example`

## Puesta en marcha paso a paso

### 1. Ejecutar el script del usuario de solo lectura

Ejecuta manualmente en phpMyAdmin o línea de comandos MySQL:

```sql
-- Desde la consolt: fase2/docs/consulta-publica/sql/01-usuario-solo-lectura.sql
CREATE USER 'hydro_ro'@'%' IDENTIFIED BY 'CAMBIAR_ESTA_CLAVE';
GRANT SELECT ON hydrogest.* TO 'hydro_ro'@'%';
FLUSH PRIVILEGES;
```

Verifica que `SELECT` funciona y `UPDATE` falla con "command denied".

### 2. Copiar las variables de entorno

```bash
cd fase2
cp .env.example .env
```

Edita el archivo `.env` y cambia:
- `HYDRO_RO_PASSWORD=CAMBIAR_ESTA_CLAVE` (tu contraseña para el usuario hydro_ro)

### 3. Levantar los servicios con Docker Compose

```bash
docker compose up --build
```

Esto levantará dos servicios:
- **web-publica** en `http://localhost:3000`
- **consulta-service** en `http://localhost:8081`

### 4. Acceder a la página pública

Abre tu navegador en:

- **Página de consulta:** http://localhost:3000/consulta
- **Swagger UI del microservicio:** http://localhost:8081/swagger-ui.html

### 5. Cómo correr cada servicio sin Docker

#### Microservicio Spring Boot:

```bash
cd fase2/consulta-service
./mvnw spring-boot:run
```

La aplicación correrá en `http://localhost:8081`

#### Aplicación Next.js BFF:

```bash
cd fase2/web-publica
npm install
npm run dev
```

La aplicación correrá en `http://localhost:3000`

## Variables de Entorno

| Nombre | Dónde se usa | Secreta |
|--------|--------------|---------|
| `NEXT_PUBLIC_RECAPTCHA_SITE_KEY` | Next.js (componente cliente) | No |
| `RECAPTCHA_SECRET_KEY` | Next.js (servidor, route handler) | Sí |
| `CONSULTA_SERVICE_URL` | Next.js (llama al microservicio) | No (puede ser http://localhost:8081) |
| `DB_URL` | consulta-service (jdbc:mariadb://host.docker.internal:3306/hydrogest) | No |
| `DB_USER` | consulta-service (hydro_ro) | No |
| `DB_PASSWORD` | consulta-service (contraseña de hydro_ro) | Sí |
| `HYDRO_RO_PASSWORD` | .env local (contraseña del usuario BD) | Sí |

## Dos barreras de seguridad

### 1. reCAPTCHA v2 (primera barrera)

- **Por qué:** Evita el scraping masivo del padrón de clientes.
- **Cómo resolver:** El usuario debe hacer clic en "No soy un robot".
- **Limitaciones:** ¿Quién llegue directo al microservicio saltándose el BFF (puerto 8081) se lo salta. Las llaves de prueba funcionan en `localhost` pero muestran aviso de prueba.

### 2. Rate limit por IP (segunda barrera)

- **Por qué:** Limita las peticiones aún si alguien resuelve el captcha.
- **Límite:** 10 peticiones por minuto por IP (configurable en `consulta.rate-limit.max-por-minuto`).
- **Cómo funciona:** Se toma la cabecera `X-Forwarded-For` primero; si no viene, usa `request.getRemoteAddr()`.
- **Respuesta 429:** Incluye cabecera `Retry-After` con segundos restantes.

**Importante:** El captcha no depende del rate limit y viceversa. Una persona puede tener captcha válido pero superar el límite, o tener captcha inválido pero no estar limitada.

## Problemas frecuentes

### 1. `Access denied` desde Docker

- **Causa:** El usuario `hydro_ro` no tiene permiso `'%'` o la BD no acepta conexiones.
- **Solución:** Verifica que el script SQL haya creado el usuario con `IDENTIFIED BY '...'` y `GRANT SELECT ON hydrogest.* TO 'hydro_ro'@'%'`. Revisa `bind-address` en `C:\xamp\my.ini` si XAMPP está en Windows.

### 2. `Connection refused`

- **Causa:** El microservicio no puede alcanzar la base de datos.
- **Solución:** Usar `host.docker.internal` en el `DB_URL` para que el contenedor Docker alcance la IP de la máquina host donde corre XAMPP. Verifica que la base de datos esté accesible en `localhost:3306` desde la máquina host.

### 3. Captcha siempre inválido

- **Causa:** Las llaves de reCAPTCHA no coinciden con el dominio.
- **Solución:** Las llaves de prueba (`6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI` y la secreta) están configuradas para dominio `localhost`. Si usas llaves reales, asegúrate de agregarlas en el panel de Google reCAPTCHA con `http://localhost` como dominio verificado.

### 4. Error de CORS

- **Causa:** La página pública llama al puerto 8081 directamente desde el navegador.
- **Solución:** Nunca debería ocurrir en la arquitectura correcta. La página llama a `/api/consulta` (Next.js BFF), que a su vez llama al microservicio 8081. El navegador solo ve la llamada a `/api/consulta`.

## Cambiar a llaves reales de reCAPTCHA para la demo

1. Ve a [Google reCAPTCHA Admin Console](https://www.google.com/recaptcha/admin/create)
2. Agrega un nuevo proyecto con dominio `localhost`
3. Copia la **llave del sitio** (publica) y la **llave secreta** (privada)
4. Actualiza el archivo `.env.example` (o `.env` local) con tus nuevas llaves

Las llaves de prueba siempre validan y muestran un aviso de prueba en el widget. Para una demo funcional, se recomienda usar llaves con dominio `localhost`.

## Pruebas de aceptación

| # | Prueba | Resultado esperado |
|---|--------|-------------------|
| 1 | Código real + captcha resuelto en `/consulta` | Se muestra el estado de cuenta, igual al del Laravel |
| 2 | Código inexistente | Mensaje "no encontrado" (404) |
| 3 | Envío sin captcha válido | Error 400; el microservicio no recibe la petición |
| 4 | 15 peticiones seguidas al microservicio | 10 responden 200 y 5 responden 429 |
| 5 | `UPDATE` conectado como `hydro_ro` | La base de datos responde "command denied" |
| 6 | `http://localhost:8081/swagger-ui.html` | Muestra el contrato y permite probarlo |
| 7 | Pestaña Red del navegador | Solo aparece la llamada a `/api/consulta`; nunca al 8081 |
| 8 | `docker compose up --build` | Levanta los dos servicios y la consulta funciona |
| 9 | `git status` sobre el monolito | Ningún archivo del Laravel modificado |

### Comandos de prueba de límite de peticiones

**PowerShell:**

```powershell
1..15 | ForEach-Object { curl.exe -s -o NUL -w "%{http_code}`n" http://localhost:8081/api/v1/consulta/CODIGO }
```

**Bash:**

```bash
for i in $(seq 1 15); do
  code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/v1/consulta/CODIGO)
  echo "Petición $i: $code"
done
```

## Decisiones tomadas

1. **Next.js como BFF:** Intermediario que evita que el navegador contacto directo con la base de datos o el microservicio.
2. **Microservicio solo lectura:** Usuario `hydro_ro` con `ddl-auto=none` para nunca alterar tablas del monolito.
3. **Contrato contract-first:** OpenAPI YAML escrito primero, código Java generado con `openapi-generator-maven-plugin`.
4. **Dos barreras independientes:** Captcha + rate limit son complementarias, no redundantes.
5. **Enmascarado de titular:** `Juan Pérez López` → `Juan P***`. Nunca se devuelven datos personales completos.
6. **Docker fuera del monolito:** La BD del Laravel vive en XAMPP fuera de Docker; el microservicio conecta via `host.docker.internal`.

## Verificación final

Ejecuta los siguientes comandos para confirmar que todo está en orden:

```bash
# Desde la raíz de fase2
cd fase2

# Verificar que no hay errores en el compose
docker compose config

# Verificar Spring Boot tests
./mvnw -q test

# Verificar Next.js lint y build
cd web-publica
npm run lint
npm run build
```

Confirma que `git status` (sobre el monolito HydroGest) no muestra ningún archivo modificado del proyecto original.

## Datos para el diagrama del equipo

| Servicio | Puerto | Solo lectura | Descripción |
|----------|--------|--------------|-------------|
| consulta-service | 8081 | ✅ Sí | Microservicio Spring Boot, acceso SELECT a BD HydroGest |
| web-publica | 3000 | ⌌ No | Next.js BFF, verifica captcha y llama al servicio de consulta |
| Laravel (monolito) | 3306 | ⌌ No | Base de datos MariaDB, fuera de Docker |
| XAMPP | - | ⌌ No | Entorno de desarrollo con la BD real |

--- 

*Desarrollado como proyecto universitario - HydroGest Fase 2*