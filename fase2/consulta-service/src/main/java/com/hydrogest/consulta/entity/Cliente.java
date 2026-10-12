package com.hydrogest.consulta.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "tb_clientes")
@Immutable
public class Cliente {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nombre1_cliente", length = 20, nullable = false)
    private String nombre1Cliente;

    @Column(name = "nombre2_cliente", length = 20)
    private String nombre2Cliente;

    @Column(name = "apellido1_cliente", length = 20, nullable = false)
    private String apellido1Cliente;

    @Column(name = "apellido2_cliente", length = 20)
    private String apellido2Cliente;

    @Column(name = "telefono_cliente", length = 8)
    private String telefonoCliente;

    @Column(name = "direccion_cliente", length = 50)
    private String direccionCliente;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private Set<Contador> contadores;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre1Cliente() { return nombre1Cliente; }
    public void setNombre1Cliente(String nombre1Cliente) { this.nombre1Cliente = nombre1Cliente; }
    public String getNombre2Cliente() { return nombre2Cliente; }
    public void setNombre2Cliente(String nombre2Cliente) { this.nombre2Cliente = nombre2Cliente; }
    public String getApellido1Cliente() { return apellido1Cliente; }
    public void setApellido1Cliente(String apellido1Cliente) { this.apellido1Cliente = apellido1Cliente; }
    public String getApellido2Cliente() { return apellido2Cliente; }
    public void setApellido2Cliente(String apellido2Cliente) { this.apellido2Cliente = apellido2Cliente; }
    public String getTelefonoCliente() { return telefonoCliente; }
    public void setTelefonoCliente(String telefonoCliente) { this.telefonoCliente = telefonoCliente; }
    public String getDireccionCliente() { return direccionCliente; }
    public void setDireccionCliente(String direccionCliente) { this.direccionCliente = direccionCliente; }
    public Set<Contador> getContadores() { return contadores; }
    public void setContadores(Set<Contador> contadores) { this.contadores = contadores; }
}