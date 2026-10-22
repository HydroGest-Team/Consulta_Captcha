package com.hydrogest.consulta.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "tb_pagos")
public class Pago {

    @Id
    private Long id;

    @Column(name = "monto_pago")
    private BigDecimal montoPago;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturas_id")
    private Lectura lectura;

    public Long getId() { return id; }
    public BigDecimal getMontoPago() { return montoPago; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public Lectura getLectura() { return lectura; }
}