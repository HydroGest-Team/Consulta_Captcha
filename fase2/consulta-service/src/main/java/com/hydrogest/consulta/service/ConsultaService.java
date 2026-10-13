package com.hydrogest.consulta.service;

import com.hydrogest.consulta.entity.Contador;
import com.hydrogest.consulta.entity.Lectura;
import com.hydrogest.consulta.entity.Pago;
import com.hydrogest.consulta.entity.Usuario;
import com.hydrogest.consulta.exception.InvalidCodeException;
import com.hydrogest.consulta.exception.NotFoundException;
import com.hydrogest.consulta.repository.ContadorRepository;
import com.hydrogest.consulta.repository.LecturaRepository;
import com.hydrogest.consulta.repository.PagoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ConsultaService {

    @Autowired private ContadorRepository contadorRepo;
    @Autowired private LecturaRepository lecturaRepo;
    @Autowired private PagoRepository pagoRepo;

    public EstadoCuentaDTO consultar(String codigoContador, String ipCliente) {
        // Validar formato del código (misma que OpenAPI pattern)
        if (!esFormatoCodigoValido(codigoContador)) {
            throw new InvalidCodeException("El código del contador tiene formato inválido");
        }

        Contador contador = contadorRepo.findByCodigoContador(codigoContador)
                .orElseThrow(() -> new NotFoundException("No existe un contador con código: " + codigoContador));

        // Obtener últimas lecturas y pagos
        List<Lectura> lecturas = lecturaRepo.findTop5ByContadorCodigoContadorOrderByFechaLecturaDesc(codigoContador);
        List<Pago> pagos = pagoRepo.findTop5ByLecturaContadorCodigoContadorOrderByFechaPagoDesc(codigoContador);

        // Calcular saldo pendiente y estado (misma lógica que Laravel)
        Double saldoPendiente = pagos.stream()
                .filter(p -> p.getEstadoPago() == EstadoPago.NO_PAGADO)
                .map(Pago::getMontoPago)
                .reduce(0.0, Double::add);

        String estado;
        if (saldoPendiente > 0) {
            estado = "PENDIENTE";
        } else {
            estado = "AL_DIA";
        }

        // Enmascarar titular
        String titular = enmascararTitular(contador.getCliente().getNombre1Cliente(),
                contador.getCliente().getNombre2Cliente());

        // Última lectura
        Lectura ultimaLectura = lecturas.isEmpty() ? null : lecturas.get(0);

        // Últimos pagos formateados
        List<Map<String, Object>> ultimosPagos = pagos.stream()
                .limit(5)
                .map(p -> Map.of(
                        "fecha", p.getFechaPago() != null ? p.getFechaPago().toLocalDate() : null,
                        "monto", p.getMontoPago()))
                .collect(Collectors.toList());

        return new EstadoCuentaDTO(
                contador.getCodigoContador(),
                titular,
                estado,
                saldoPendiente,
                ultimaLectura != null -> Map.of(
                        "fecha", ultimaLectura.getFechaLectura() != null ? ultimaLectura.getFechaLectura().toLocalDate() : null,
                        "lectura", ultimaLectura.getLecturaActual(),
                        "consumoM3", ultimaLectura.getMonto() != null ? ultimaLectura.getMonto() : 0.0)
                : null,
                ultimosPagos
        );
    }

    private boolean esFormatoCodigoValido(String codigo) {
        return codigo != null && codigo.matches("^[A-Z0-9_-]{1,20}$");
    }

    private String enmascararTitular(String nombre1, String nombre2) {
        if (nombre1 == null || nombre1.isEmpty()) {
            return "***";
        }
        String primerNombre = nombre1.trim();
        if (nombre2 == null || nombre2.isEmpty()) {
            return primerNombre + " ***";
        }
        String segundaInicial = nombre2.trim().substring(0, 1).toUpperCase();
        return primerNombre + " " + segundaInicial + "***";
    }

    // DTO para la respuesta
    public static class EstadoCuentaDTO {
        private final String codigoContador;
        private final String titular;
        private final String estado;
        private final Double saldoPendiente;
        private final Map<String, Object> ultimaLectura;
        private final List<Map<String, Object>> ultimosPagos;

        public EstadoCuentaDTO(String codigoContador, String titular, String estado, Double saldoPendiente,
                java.util.function.Supplier<Map<String, Object>> ultimaLectura, List<Map<String, Object>> ultimosPagos) {
            this.codigoContador = codigoContador;
            this.titular = titular;
            this.estado = estado;
            this.saldoPendiente = saldoPendiente;
            this.ultimaLectura = ultimaLectura.get();
            this.ultimosPagos = ultimosPagos;
        }

        public String getCodigoContador() { return codigoContador; }
        public String getTitular() { return titular; }
        public String getEstado() { return estado; }
        public Double getSaldoPendiente() { return saldoPendiente; }
        public Map<String, Object> getUltimaLectura() { return ultimaLectura; }
        public List<Map<String, Object>> getUltimosPagos() { return ultimosPagos; }
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String mensaje) { super(mensaje); }
    }
}