package com.hydrogest.consulta.controller;

import com.hydrogest.consulta.exception.GlobalExceptionHandler;
import com.hydrogest.consulta.service.ConsultaService;
import com.hydrogest.consulta.service.ConsultaService.EstadoCuentaDTO;
import com.hydrogest.consulta.service.ConsultaService.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping("/api/v1/consulta/{codigoContador}")
    public ResponseEntity<EstadoCuentaDTO> consultar(
            @PathVariable("codigoContador") String codigoContador,
            HttpServletRequest request) {

        try {
            String ipCliente = request.getHeader("X-Forwarded-For");
            if (ipCliente == null || ipCliente.isEmpty()) {
                ipCliente = request.getRemoteAddr();
            }

            EstadoCuentaDTO resultado = consultaService.consultar(codigoContador, ipCliente);
            return new ResponseEntity<>(resultado, HttpStatus.OK);
        } catch (NotFoundException e) {
            ErrorRespuesta error = new ErrorRespuesta("No existe un contador con código: " + codigoContador);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMensaje());
        } catch (Exception e) {
            ErrorRespuesta error = new ErrorRespuesta("Error interno del servicio");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMensaje());
        }
    }

    private static class ErrorRespuesta {
        private final String mensaje;

        public ErrorRespuesta(String mensaje) { this.mensaje = mensaje; }

        public String getMensaje() { return mensaje; }
    }
}