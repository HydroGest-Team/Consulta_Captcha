package com.hydrogest.consulta.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "tb_contadores")
public class Contador {

    @Id
    private Long id;

    @Column(name = "codigo_contador")
    private String codigoContador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    public Long getId() { return id; }
    public String getCodigoContador() { return codigoContador; }
    public Cliente getCliente() { return cliente; }
}