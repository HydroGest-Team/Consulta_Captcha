package com.hydrogest.consulta.controller;

import com.hydrogest.consulta.api.ConsultaApi;
import com.hydrogest.consulta.api.model.EstadoCuenta;
import com.hydrogest.consulta.service.ConsultaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConsultaController implements ConsultaApi {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @Override
    public ResponseEntity<EstadoCuenta> consultarEstadoCuenta(String codigoContador) {
        return ResponseEntity.ok(consultaService.consultar(codigoContador));
    }
}