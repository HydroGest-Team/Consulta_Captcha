package com.hydrogest.consulta.service;

import com.hydrogest.consulta.api.model.EstadoCuenta;
import com.hydrogest.consulta.api.model.PagoResumen;
import com.hydrogest.consulta.api.model.UltimaLectura;
import com.hydrogest.consulta.entity.Contador;
import com.hydrogest.consulta.exception.InvalidCodeException;
import com.hydrogest.consulta.exception.NotFoundException;
import com.hydrogest.consulta.repository.ContadorRepository;
import com.hydrogest.consulta.repository.LecturaRepository;
import com.hydrogest.consulta.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class ConsultaService {

    private static final Pattern FORMATO = Pattern.compile("^[A-Z0-9_-]{1,20}$");

    private final ContadorRepository contadorRepo;
    private final LecturaRepository lecturaRepo;
    private final PagoRepository pagoRepo;

    public ConsultaService(ContadorRepository contadorRepo, LecturaRepository lecturaRepo, PagoRepository pagoRepo) {
        this.contadorRepo = contadorRepo;
        this.lecturaRepo = lecturaRepo;
        this.pagoRepo = pagoRepo;
    }

    public EstadoCuenta consultar(String codigoContador) {
        if (codigoContador == null || !FORMATO.matcher(codigoContador).matches()) {
            throw new InvalidCodeException("El codigo del contador tiene formato invalido");
        }

        Contador contador = contadorRepo.findFirstByCodigoContador(codigoContador)
                .orElseThrow(() -> new NotFoundException("No existe un contador con ese codigo"));

        BigDecimal saldo = lecturaRepo.saldoPendiente(contador.getId());
        if (saldo == null) {
            saldo = BigDecimal.ZERO;
        }

        EstadoCuenta respuesta = new EstadoCuenta();
        respuesta.setCodigoContador(contador.getCodigoContador());
        respuesta.setTitular(Enmascarador.enmascarar(
                contador.getCliente().getNombre1Cliente(),
                contador.getCliente().getApellido1Cliente()));
        respuesta.setEstado(saldo.signum() > 0 ? EstadoCuenta.EstadoEnum.PENDIENTE : EstadoCuenta.EstadoEnum.AL_DIA);
        respuesta.setSaldoPendiente(saldo);

        lecturaRepo.findFirstByContadorIdOrderByFechaLecturaDesc(contador.getId()).ifPresent(l -> {
            UltimaLectura ultima = new UltimaLectura();
            ultima.setFecha(l.getFechaLectura().toLocalDate());
            ultima.setLectura(l.getLecturaActual());
            ultima.setConsumoM3(l.getConsumo());
            respuesta.setUltimaLectura(ultima);
        });

        List<PagoResumen> pagos = pagoRepo.findTop5ByLecturaContadorIdOrderByFechaPagoDesc(contador.getId())
                .stream()
                .map(p -> {
                    PagoResumen resumen = new PagoResumen();
                    resumen.setFecha(p.getFechaPago().toLocalDate());
                    resumen.setMonto(p.getMontoPago());
                    return resumen;
                })
                .toList();
        respuesta.setUltimosPagos(pagos);

        return respuesta;
    }
}