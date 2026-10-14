package com.hydrogest.consulta.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "tb_periodos")
@Immutable
public class Periodo {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "estado_periodo", nullable = false)
    private String estadoPeriodo;

    @OneToMany(mappedBy = "periodo", fetch = FetchType.LAZY)
    private Set<Lectura> lecturas;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    public String getEstadoPeriodo() { return estadoPeriodo; }
    public void setEstadoPeriodo(String estadoPeriodo) { this.estadoPeriodo = estadoPeriodo; }
    public Set<Lectura> getLecturas() { return lecturas; }
    public void setLecturas(Set<Lectura> lecturas) { this.lecturas = lecturas; }
}