# Esquema usado por la consulta pública

El microservicio **solo lee** estas tablas del monolito Laravel (base `hidrogest`). No crea ni altera nada: `ddl-auto=none`, usuario `hydro_ro` con solo `SELECT` y entidades `@Immutable`.

## Tablas y columnas mapeadas

| Tabla | Columnas que se leen |
|---|---|
| `tb_clientes` | `id` (PK), `nombre1_cliente`, `apellido1_cliente` |
| `tb_contadores` | `id` (PK), `codigo_contador`, `cliente_id` → `tb_clientes.id` |
| `tb_lecturas` | `id` (PK), `lectura_actual`, `consumo` (columna generada: `lectura_actual - lectura_anterior`), `monto`, `fecha_lectura`, `contador_id` → `tb_contadores.id` |
| `tb_pagos` | `id` (PK), `monto_pago`, `fecha_pago`, `lecturas_id` → `tb_lecturas.id` (UNIQUE) |

Nunca se devuelven DPI, teléfono, dirección, número de cuenta ni IDs internos.

## Reglas de negocio (iguales al `DashboardController` de Laravel)

- Una lectura está **pendiente** si **no tiene un pago** asociado en `tb_pagos`.
- `saldoPendiente` = suma de `tb_lecturas.monto` de las lecturas pendientes del contador.
- `estado` = `PENDIENTE` si `saldoPendiente > 0`; si no, `AL_DIA`.
- `ultimaLectura` = la lectura con `fecha_lectura` más reciente.
- `ultimosPagos` = los 5 pagos más recientes del contador.
- `titular` = primer nombre + inicial del primer apellido + `***` (ej. `Carlos G***`).

## Consultas (JPQL / Spring Data)

```sql
-- Saldo pendiente
SELECT SUM(l.monto) FROM Lectura l
WHERE l.contador.id = :contadorId
  AND NOT EXISTS (SELECT p.id FROM Pago p WHERE p.lectura = l)

-- Última lectura
findFirstByContadorIdOrderByFechaLecturaDesc(contadorId)

-- Últimos 5 pagos
findTop5ByLecturaContadorIdOrderByFechaPagoDesc(contadorId)
```