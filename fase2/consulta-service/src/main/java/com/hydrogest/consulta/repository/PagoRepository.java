package com.hydrogest.consulta.repository;

import com.hydrogest.consulta.entity.Pago;
import org.springframework.data.repository.Repository;
import java.util.List;

public interface PagoRepository extends Repository<Pago, Long> {
    List<Pago> findTop5ByLecturaContadorIdOrderByFechaPagoDesc(Long contadorId);
}