package com.hydrogest.consulta.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "tb_tarifas")
@Immutable
public class Tarifa {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "monto_por_unidad", nullable = false)
    private Double montoPorUnidad;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDateTime vigenteDesde;

    @Column(name = "vigente_hasta")
    private LocalDateTime vigenteHasta;

    @ManyToOne
    @JoinColumn(name = "tipo_tarifa_id")
    private TipoTarifa tipoTarifa;

    @OneToMany(mappedBy = "tarifa", fetch = FetchType.LAZY)
    private Set<Lectura> lecturas;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getMontoPorUnidad() { return montoPorUnidad; }
    public void setMontoPorUnidad(Double montoPorUnidad) { this.montoPorUnidad = montoPorUnidad; }
    public LocalDateTime getVigenteDesde() { return vigenteDesde; }
    public void setVigenteDesde(LocalDateTime vigenteDesde) { this.vigenteDesde = vigenteDesde; }
    public LocalDateTime getVigenteHasta() { return vigenteHasta; }
    public void setVigenteHasta(LocalDateTime vigenteHasta) { this.vigenteHasta = vigenteHasta; }
    public TipoTarifa getTipoTarifa() { return tipoTarifa; }
    public void setTipoTarifa(TipoTarifa tipoTarifa) { this.tipoTarifa = tipoTarifa; }
    public Set<Lectura> getLecturas() { return lecturas; }
    public void setLecturas(Set<Lectura> lecturas) { this.lecturas = lecturas; }
}