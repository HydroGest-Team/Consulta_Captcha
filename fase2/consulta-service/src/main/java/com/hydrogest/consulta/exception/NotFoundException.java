package com.hydrogest.consulta.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String mensaje) { super(mensaje); }
}