package com.hydrogest.consulta.interceptor;

import com.hydrogest.consulta.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Value("${consulta.rate-limit.max-por-minuto:10}")
    private int maxPorMinuto;

    private final Map<String, int[]> mapaRateLimit = new ConcurrentHashMap<>();

    private static final int VENTANA_SEGUNDOS = 60;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String path = request.getRequestURI();

        // Aplicar rate limit solo a los endpoints de consulta API
        boolean esEndpointConsulta = path.startsWith("/api/v1/consulta")
                || path.startsWith("/api/consulta");

        if (!esEndpointConsulta) {
            return true; // Dejar pasar Swagger, saludos, etc.
        }

        String ip = obtenerIpCliente(request);
        Instant ahora = Instant.now();

        int[] entrada = mapaRateLimit.get(ip);

        if (entrada == null) {
            // Primera petición desde esta IP
            mapaRateLimit.put(ip, new int[]{1, (int) ahora.getEpochSecond()});
            return true;
        }

        int contador = entrada[0];
        int tiempoReseteo = entrada[1];

        // Verificar si la ventana se reinició (más de 60 segundos después)
        long elapsed = ahora.getEpochSecond() - tiempoReseteo;
        if (elapsed >= VENTANA_SEGUNDOS) {
            // Reiniciar ventana
            mapaRateLimit.put(ip, new int[]{1, (int) ahora.getEpochSecond()});
            return true;
        }

        // Verificar si se superó el límite
        if (contador >= maxPorMinuto) {
            // Responder 429 con cabecera Retry-After
            int retryAfter = VENTANA_SEGUNDOS - (int) elapsed;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(Math.max(1, retryAfter)));
            response.setContentType("application/json");
            ErrorResponseError error = new ErrorResponseError(
                    "Demasiadas peticiones desde esta IP. Inténtalo de nuevo más tarde.",
                    429);
            response.getWriter().write(error.toJson());
            response.getWriter().flush();
            return false;
        }

        // Incrementar contador
        mapaRateLimit.put(ip, new int[]{contador + 1, tiempoReseteo});
        return true;
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            String ip = xForwardedFor.split(",")[0].trim();
            if (!ip.isEmpty()) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    // Clase interna para la respuesta de error 429
    private static class ErrorResponseError {
        private final String mensaje;
        private final int codigo;

        public ErrorResponseError(String mensaje, int codigo) {
            this.mensaje = mensaje;
            this.codigo = codigo;
        }

        public String toJson() {
            return "{\"codigo\":" + codigo +
                    ",\"titulo\":\"Demasiadas peticiones\"" +
                ",\"mensaje\":\"" + mensaje + "\"}";
        }
    }
}