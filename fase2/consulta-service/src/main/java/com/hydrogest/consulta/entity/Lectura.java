package com.hydrogest.consulta.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "tb_lecturas")
public class Lectura {

    @Id
    private Long id;

    @Column(name = "lectura_actual")
    private BigDecimal lecturaActual;

    @Column(name = "consumo", insertable = false, updatable = false)
    private BigDecimal consumo;

    @Column(name = "monto")
    private BigDecimal monto;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contador_id")
    private Contador contador;

    public Long getId() { return id; }
    public BigDecimal getLecturaActual() { return lecturaActual; }
    public BigDecimal getConsumo() { return consumo; }
    public BigDecimal getMonto() { return monto; }
    public LocalDateTime getFechaLectura() { return fechaLectura; }
    public Contador getContador() { return contador; }
}