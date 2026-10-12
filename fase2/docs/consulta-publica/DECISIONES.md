DECISIONES — Feature 1: Consulta pública con captcha

1. Usar Next.js como BFF (Backend-for-Frontend)
   - Razón: El monolito Laravel está fuera de Docker; Next.js corre en el mismo entorno que la página pública y puede llamar al microservicio con la IP real del cliente.

2. Microservicio Spring Boot con solo lectura
   - Razón: El documento lo exige. Se usará usuario `hydro_ro` con permiso SELECT sobre hydrogest.*.
   - ddl-auto=none para que Hibernate nunca altere tablas del monolito.

3. Contrato OpenAPI contract-first
   - Razón: Requisito del docente. Se escribe YAML primero y las interfaces/DTOs de Java se generan con openapi-generator-maven-plugin.
   - El contrato queda visible en Swagger UI en http://localhost:8081/swagger-ui.html.

4. reCAPTCHA v2 en el BFF (Next.js)
   - Razón: Primera barrera contra scraping. Se verifica en el lado servidor del Next.js, nunca en el cliente directo.
   - Las llaves públicas van en variables de entorno NEXT_PUBLIC_; la llave privada va solo en el servidor.

5. Rate limit por IP en el microservicio
   - Razón: Segunda barrera, independiente del captcha. Límite configurable de 10 peticiones/minuto por IP.
   - Se toma la cabecera X-Forwarded-For primero, si no viene usa request.getRemoteAddr().

6. Enmascarado del titular
   - Razón: La página es pública, nunca se debe devolver el nombre completo.
   - Regla: primera palabra completa + inicial de la segunda + "***".
   - Ejemplo: "Juan Pérez López" → "Juan P***". Si hay una sola palabra: inicial + "***".

7. No modificar el monolito Laravel
   - Razón: Regla de oro. Todo lo nuevo se construye al lado (fase2/).
   - No se tocan migrations, modelos, controladores ni vistas Blade del Laravel.

8. Docker Compose: monolito fuera, solo consulta-service y web-publica adentro
   - Razón: El monolito y su base de datos viven en XAMPP fuera del Compose.
   - consulta-service se conecta a host.docker.internal:3306 para acceder a la BD del monolito.
   - web-publica llama al BFF (/api/consulta) y nunca al puerto 8081 directamente.

9. rate-limit en memoria con ConcurrentHashMap
   - Razón: Requisito "sin librerías externas". Simple y efectivo para un entorno Docker con un solo contenedor.
   - Ventana de 1 minuto, limpieza periódica de entradas vencidas.

10. Usar host.docker.internal para que el consulta-service alcance la BD del monolito
    - Razón: En Docker Desktop, host.docker.internal resolución a la IP de la máquina host.
    - Esto permite que consulta-service se conecte a la MariaDB del XAMPP en localhost:3306.

11. .env.example sube las variables no secretas; .env.local NO se sube
    - Razón: Seguridad. Las llaves secretas del captcha no deben comprometerse.
    - NEXT_PUBLIC_RECAPTCHA_SITE_KEY y RECAPTCHA_SECRET_KEY van en .env.example (solo llaves de prueba).
    - .env.local se ignora con .gitignore.

12. Tests unitarios sin base de datos
    - Razón: Ningún test debe requerir la base de datos real. Usar Mockito/estubs para repositorios.
    - Tests de enmascarado: varias palabras, una palabra, espacios extra, nombre vacío.