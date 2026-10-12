Esquema de base de datos para la consulta pública

Tablas y columnas utilizadas:

- tb_contadores
  - codigo_contador (VARCHAR(20), primary key) — Código único del contador
  - cliente_id (BIGINT, foreign key → tb_clientes) — Relación con el cliente
  - activo_contador (ENUM: 'ACTIVO', 'NO ACTIVO')
  - sector_contador (VARCHAR(50), nullable)
  - fecha_instalacion (DATETIME, nullable)

- tb_clientes
  - id (BIGINT, primary key)
  - nombre1_cliente (VARCHAR(20))
  - nombre2_cliente (VARCHAR(20), nullable)
  - nombre3_cliente (VARCHAR(20), nullable)
  - apellido1_cliente (VARCHAR(20))
  - apellido2_cliente (VARCHAR(20), nullable)
  - apellido3_cliente (VARCHAR(20), nullable)

- tb_lecturas
  - id (BIGINT, primary key)
  - contador_id (BIGINT, foreign key → tb_contadores)
  - periodo_id (BIGINT, foreign key → tb_periodos)
  - lectura_anterior (DECIMAL(10,2), default 0)
  - lectura_actual (DECIMAL(10,2))
  - monto (DECIMAL(10,2))
  - fecha_lectura (DATETIME)
  - usuario_id (BIGINT, foreign key → tb_usuarios)
  - tarifa_id (BIGINT, foreign key → tb_tarifas)

- tb_pagos
  - id (BIGINT, primary key)
  - lecturas_id (BIGINT, foreign key → tb_lecturas, unique)
  - monto_pago (DECIMAL(10,2))
  - fecha_pago (DATETIME)
  - metodo_pago (ENUM: 'Efectivo', 'Credito', 'Debito')
  - estado_pago (ENUM: 'PAGADO', 'NO PAGADO', default 'NO PAGADO')
  - usuario_id (BIGINT, foreign key → tb_usuarios)

- tb_tarifas
  - id (BIGINT, primary key)
  - monto_por_unidad (DECIMAL)
  - vigente_desde (DATETIME)
  - vigente_hasta (DATETIME, nullable)
  - tipo_tarifa_id (BIGINT, foreign key → tb_tipo_tarifa)

- tb_periodos
  - id (BIGINT, primary key)
  - fecha_apertura (DATETIME)
  - fecha_cierre (DATETIME, nullable)
  - estado_periodo (ENUM: 'ACTIVO', 'CERRADO')

Relaciones clave:
- tb_contadores.cliente_id → tb_clientes.id (CONSTRAINT restrict)
- tb_lecturas.contador_id → tb_contadores.id (CONSTRAINT restrict)
- tb_lecturas.periodo_id → tb_periodos.id (CONSTRAINT restrict)
- tb_lecturas.tarifa_id → tb_tarifas.id (CONSTRAINT restrict)
- tb_lecturas.usuario_id → tb_usuarios.id (CONSTRAINT restrict)
- tb_pagos.lecturas_id → tb_lecturas.id (CONSTRAINT restrict)
- tb_pagos.lecturas_id tiene restricción UNIQUE
- tb_pagos.usuario_id → tb_usuarios.id (CONSTRAINT restrict)
- tb_tarifas.tipo_tarifa_id → tb_tipo_tarifa.id (CONSTRAINT restrict)

Consulta SQL — Estado de cuenta dado un código de contador:

```sql
SELECT 
  c.codigo_contador,
  -- Enmascarado: primera palabra completa + inicial de segunda + ***
  CONCAT(
    SUBSTRING(cn.nombre1_cliente, 1, 
      CASE WHEN LOCATE(' ', cn.nombre1_cliente) > 0 THEN LOCATE(' ', cn.nombre1_cliente) - 1 ELSE LENGTH(cn.nombre1_cliente) END
    ),
    ' ',
    LEFT(IFNULL(cn.nombre2_cliente, ''), 1),
    '***'
  ) AS titular,
  CASE WHEN MAX(p.estado_pago) = 'NO PAGADO' AND MIN(p.monto_pago) > 0 THEN 'PENDIENTE' ELSE 'AL_DIA' END AS estado,
  SUM(IFNULL(p.monto_pago, 0)) AS saldoPendiente,
  MAX(l.fecha_lectura) AS ultimaLecturaFecha,
  MAX(l.lectura_actual) AS ultimaLectura,
  MAX(l.consumo) AS consumoM3,
  GROUP_CONCAT(
    DISTINCT CONCAT(
      DATE_FORMAT(p.fecha_pago, '%Y-%m-%d'), ':', p.monto_pago
    ) ORDER BY p.fecha_pago DESC SEPARATOR ';'
  ) ultimosPagos
FROM tb_contadores c
JOIN tb_clientes cn ON c.cliente_id = cn.id
LEFT JOIN tb_lecturas l ON c.id = l.contador_id
LEFT JOIN tb_pagos p ON l.id = p.lecturas_id
WHERE c.codigo_contador = ?
GROUP BY c.id, cn.nombre1_cliente, cn.nombre2_cliente
ORDER BY l.fecha_lectura DESC
LIMIT 1;
```

NOTAS:
- El enmascarado del titular: "Juan Pérez López" → "Juan P***". Si hay una sola palabra, se usa la inicial + "***".
- Nunca se devuelven: DPI, NIT, dirección, teléfono, correo, ni identificadores internos de la base de datos.
- El saldoPendiente y el estado se calculan con la misma lógica que el monolito Laravel (basado en la existencia y monto de pagos asociados a las lecturas).
- Si el contador no tiene lecturas, el resultado tendrá valores nulos/0.