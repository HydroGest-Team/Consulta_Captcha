package com.hydrogest.consulta.repository;

import com.hydrogest.consulta.entity.Lectura;
import org.springframework.data.repository.Repository;

public interface LecturaRepository extends Repository<Lectura, Long> {
    Lectura findByContadorCodigoContadorAndPeriodoId(String codigoContador, Long periodoId);
}