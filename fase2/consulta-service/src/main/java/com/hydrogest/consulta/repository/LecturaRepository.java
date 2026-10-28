package com.hydrogest.consulta.repository;

import com.hydrogest.consulta.entity.Lectura;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Optional;

public interface LecturaRepository extends Repository<Lectura, Long> {

    Optional<Lectura> findFirstByContadorIdOrderByFechaLecturaDesc(Long contadorId);

    // Misma regla que el DashboardController de Laravel: pendiente = lectura sin pago
    @Query("select sum(l.monto) from Lectura l where l.contador.id = :contadorId "
         + "and not exists (select p.id from Pago p where p.lectura = l)")
    BigDecimal saldoPendiente(@Param("contadorId") Long contadorId);
}