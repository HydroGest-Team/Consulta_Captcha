package com.hydrogest.consulta.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "tb_contadores")
@Immutable
public class Contador {

    @Id
    @Column(name = "codigo_contador", length = 20, nullable = false)
    private String codigoContador;

    @Column(name = "sector_contador", length = 50)
    private String sectorContador;

    @Column(name = "activo_contador")
    private String activoContador;

    @Column(name = "fecha_instalacion")
    private LocalDateTime fechaInstalacion;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "contador", fetch = FetchType.LAZY)
    private Set<Lectura> lecturas;

    public String getCodigoContador() { return codigoContador; }
    public void setCodigoContador(String codigoContador) { this.codigoContador = codigoContador; }
    public String getSectorContador() { return sectorContador; }
    public void setSectorContador(String sectorContador) { this.sectorContador = sectorContador; }
    public String getActivoContador() { return activoContador; }
    public void setActivoContador(String activoContador) { this.activoContador = activoContador; }
    public LocalDateTime getFechaInstalacion() { return fechaInstalacion; }
    public void setFechaInstalacion(LocalDateTime fechaInstalacion) { this.fechaInstalacion = fechaInstalacion; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Set<Lectura> getLecturas() { return lecturas; }
    public void setLecturas(Set<Lectura> lecturas) { this.lecturas = lecturas; }
}