package com.hydrogest.consulta.repository;

import com.hydrogest.consulta.entity.Contador;
import org.springframework.data.repository.Repository;
import java.util.Optional;

public interface ContadorRepository extends Repository<Contador, Long> {
    Optional<Contador> findFirstByCodigoContador(String codigoContador);
}