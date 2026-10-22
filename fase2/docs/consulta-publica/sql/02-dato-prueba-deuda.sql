-- Dato de prueba para la consulta publica: deja a CTR-002 con una lectura SIN pago (estado PENDIENTE).
-- Ejecutar como root en la base hidrogest. No modifica migraciones ni seeders del monolito.
INSERT INTO tb_lecturas (numero_recibo, lectura_anterior, lectura_actual, monto, fecha_lectura, tarifa_id, usuario_id, contador_id, periodo_id, created_at, updated_at)
SELECT 'REC-PRUEBA-DEUDA',
       COALESCE((SELECT MAX(l.lectura_actual) FROM tb_lecturas l WHERE l.contador_id = c.id), 0),
       COALESCE((SELECT MAX(l.lectura_actual) FROM tb_lecturas l WHERE l.contador_id = c.id), 0) + 20,
       20 * t.monto_por_unidad, NOW(), t.id, (SELECT MIN(id) FROM tb_usuarios), c.id, p.id, NOW(), NOW()
FROM tb_contadores c CROSS JOIN tb_tarifas t CROSS JOIN tb_periodos p
WHERE c.codigo_contador = 'CTR-002'
  AND t.id = (SELECT id FROM tb_tarifas ORDER BY vigente_desde DESC LIMIT 1)
  AND p.id = (SELECT p2.id FROM tb_periodos p2
              WHERE NOT EXISTS (SELECT 1 FROM tb_lecturas l2 WHERE l2.contador_id = c.id AND l2.periodo_id = p2.id)
              ORDER BY p2.id DESC LIMIT 1);

-- Para quitarlo:
-- DELETE FROM tb_lecturas WHERE numero_recibo = 'REC-PRUEBA-DEUDA';