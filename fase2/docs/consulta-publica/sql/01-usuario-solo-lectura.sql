-- Ejecutar manualmente en phpMyAdmin. NO lo ejecuta el agente.
CREATE USER 'hydro_ro'@'%' IDENTIFIED BY 'CAMBIAR_ESTA_CLAVE';
GRANT SELECT ON hydrogest.* TO 'hydro_ro'@'%';
FLUSH PRIVILEGES;

-- Prueba: conectado como hydro_ro, un SELECT funciona y un UPDATE falla con "command denied"
-- EXPECTED: SELECT sobre tb_contadores, tb_clientes, tb_lecturas devuelve filas
-- EXPECTED: UPDATE sobre cualquier tabla devuelve: ERROR 1142 (42000) at line X: command denied to user 'hydro_ro'@'%' for table '...'