package com.hydrogest.consulta.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_tipo_tarifa")
@Immutable
public class TipoTarifa {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "descripcion", length = 50)
    private String descripcion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}