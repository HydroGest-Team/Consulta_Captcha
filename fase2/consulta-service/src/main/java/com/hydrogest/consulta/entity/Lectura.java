package com.hydrogest.consulta.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "tb_lecturas")
@Immutable
public class Lectura {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "lectura_anterior", nullable = false)
    private Double lecturaAnterior;

    @Column(name = "lectura_actual", nullable = false)
    private Double lecturaActual;

    @Column(name = "monto", nullable = false)
    private Double monto;

    @Column(name = "fecha_lectura", nullable = false)
    private LocalDateTime fechaLectura;

    @ManyToOne
    @JoinColumn(name = "contador_id", nullable = false)
    private Contador contador;

    @ManyToOne
    @JoinColumn(name = "periodo_id")
    private Periodo periodo;

    @ManyToOne
    @JoinColumn(name = "tarifa_id")
    private Tarifa tarifa;

    @OneToMany(mappedBy = "lectura", fetch = FetchType.LAZY)
    private Set<Pago> pagos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getLecturaAnterior() { return lecturaAnterior; }
    public void setLecturaAnterior(Double lecturaAnterior) { this.lecturaAnterior = lecturaAnterior; }
    public Double getLecturaActual() { return lecturaActual; }
    public void setLecturaActual(Double lecturaActual) { this.lecturaActual = lecturaActual; }
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
    public LocalDateTime getFechaLectura() { return fechaLectura; }
    public void setFechaLectura(LocalDateTime fechaLectura) { this.fechaLectura = fechaLectura; }
    public Contador getContador() { return contador; }
    public void setContador(Contador contador) { this.contador = contador; }
    public Periodo getPeriodo() { return periodo; }
    public void setPeriodo(Periodo periodo) { this.periodo = periodo; }
    public Tarifa getTarifa() { return tarifa; }
    public void setTarifa(Tarifa tarifa) { this.tarifa = tarifa; }
    public Set<Pago> getPagos() { return pagos; }
    public void setPagos(Set<Pago> pagos) { this.pagos = pagos; }
}