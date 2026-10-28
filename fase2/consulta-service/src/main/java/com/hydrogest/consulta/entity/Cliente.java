package com.hydrogest.consulta.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "tb_clientes")
public class Cliente {

    @Id
    private Long id;

    @Column(name = "nombre1_cliente")
    private String nombre1Cliente;

    @Column(name = "apellido1_cliente")
    private String apellido1Cliente;

    public Long getId() { return id; }
    public String getNombre1Cliente() { return nombre1Cliente; }
    public String getApellido1Cliente() { return apellido1Cliente; }
}