package com.hydrogest.consulta.repository;

import com.hydrogest.consulta.entity.Pago;
import org.springframework.data.repository.Repository;

public interface PagoRepository extends Repository<Pago, Long> {
    Pago findByLecturasId(Long lecturaId);
}