package com.hydrogest.consulta.repository;

import com.hydrogest.consulta.entity.Contador;
import org.springframework.data.repository.Repository;

public interface ContadorRepository extends Repository<Contador, String> {
    Contador findByCodigoContador(String codigoContador);
}