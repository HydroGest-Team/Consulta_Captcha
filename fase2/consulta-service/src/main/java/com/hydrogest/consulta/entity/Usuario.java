package com.hydrogest.consulta.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_usuarios")
@Immutable
public class Usuario {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "contrasena", length = 255)
    private String contrasena;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}