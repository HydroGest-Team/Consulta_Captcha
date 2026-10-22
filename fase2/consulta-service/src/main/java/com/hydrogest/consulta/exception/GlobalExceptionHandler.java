package com.hydrogest.consulta.exception;

import com.hydrogest.consulta.api.model.ErrorRespuesta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(NotFoundException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidCodeException.class)
    public ResponseEntity<ErrorRespuesta> codigoInvalido(InvalidCodeException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorRespuesta> rutaInexistente(NoResourceFoundException ex) {
        return respuesta(HttpStatus.NOT_FOUND, "Recurso no encontrado");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> generico(Exception ex) {
        log.error("Error no controlado en la consulta", ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servicio");
    }

    private ResponseEntity<ErrorRespuesta> respuesta(HttpStatus status, String mensaje) {
        ErrorRespuesta error = new ErrorRespuesta();
        error.setMensaje(mensaje);
        return ResponseEntity.status(status).body(error);
    }
}