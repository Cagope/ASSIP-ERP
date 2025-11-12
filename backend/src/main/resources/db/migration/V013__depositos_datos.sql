/*---------------------------------------------------------------------
 📘 Migración: V013__depositos_datos.sql
 Esquema: depositos
 Autor: Carlos González Pérez — ERP ASSIP SOLIDARIA Y FINANCIERA
 Fecha: 2025-11-08
 ----------------------------------------------------------------------
 🔹 Descripción:
   Carga los datos iniciales del módulo DEPÓSITOS:
   - Catálogos base: tipos, estados, acciones, módulos, GMF.
   - Formas de ahorro con campo “codigo_agencia”.
   - Tipos de movimiento.
   - Incluye campo BOOLEAN “operativo” para estados.
 ----------------------------------------------------------------------*/

SET search_path = depositos, contabilidad, catalogos, seguridad, general, public;

-- ================================================================
-- 🧱 Ajustes estructurales
-- ================================================================

-- Agregar campo "operativo" a catálogos de estado
ALTER TABLE IF EXISTS depositos.estados_ahorros
ADD COLUMN IF NOT EXISTS operativo BOOLEAN DEFAULT TRUE;

COMMENT ON COLUMN depositos.estados_ahorros.operativo IS
'Establece si el estado permite realizar operaciones (TRUE = permite, FALSE = bloquea movimientos).';

ALTER TABLE IF EXISTS depositos.estados_documentos_soporte
ADD COLUMN IF NOT EXISTS operativo BOOLEAN DEFAULT TRUE;

COMMENT ON COLUMN depositos.estados_documentos_soporte.operativo IS
'Establece si el documento soporte puede seguir utilizándose en operaciones.';

-- Agregar campo "codigo_agencia" en formas de ahorro
ALTER TABLE IF EXISTS depositos.formas_ahorro
ADD COLUMN IF NOT EXISTS codigo_agencia INTEGER REFERENCES general.datos_agencias(id_agencia);

COMMENT ON COLUMN depositos.formas_ahorro.codigo_agencia IS
'Agencia a la cual pertenece la forma de ahorro.';


-- ================================================================
-- 📚 Carga de catálogos base
-- ================================================================

-- 1️⃣ Tipos de documentos soporte
INSERT INTO depositos.tipos_documentos_soporte (codigo_soporte, descripcion_soporte, cantidad_soporte)
VALUES
('N','NINGUNO',0),
('L','LIBRETAS',50),
('T','TARJETAS',1),
('O','ORDENES',50)
ON CONFLICT DO NOTHING;


-- 2️⃣ Tipos de captaciones
INSERT INTO depositos.tipos_captaciones (codigo_captacion, descripcion_captacion, corto_largo_captacion)
VALUES
('1','APORTES SOCIALES',FALSE),
('2','AHORRO A LA VISTA',FALSE),
('3','CONTRACTUAL',TRUE),
('4','PERMANENTE',FALSE)
ON CONFLICT DO NOTHING;


-- 3️⃣ Tipos de GMF
INSERT INTO depositos.tipos_gmf (codigo_tipo_gmf, descripcion_tipos_gmf, observaciones_tipos_gmf)
VALUES
('S','Si','Aplica GMF'),
('N','No','No aplica GMF'),
('U','Carta Unica','No aplica y se generó carta')
ON CONFLICT DO NOTHING;


-- 4️⃣ Acciones cuentas conjuntas
INSERT INTO depositos.acciones_cuentas_conjuntas (codigo_accion, descripcion_accion)
VALUES
('N','Ninguna'),
('T','TODOS FIRMAN'),
('U','SOLO UNO FIRMA')
ON CONFLICT DO NOTHING;


-- 5️⃣ Módulos
INSERT INTO depositos.modulos (codigo_modulo, descripcion_modulo)
VALUES
('01','CONTABILIDAD'),
('02','DEPOSITOS'),
('03','CARTERA'),
('04','CDAT'),
('05','CAJAS'),
('06','TARJETAS'),
('99','USUARIOS Y PERFILES')
ON CONFLICT DO NOTHING;


-- 6️⃣ Estados de cuentas
INSERT INTO depositos.estados_ahorros (codigo_estado_ahorro, descripcion_estado_ahorro, observaciones_estado_ahorro, operativo)
VALUES
('A','ACTIVO','NP',TRUE),
('C','CANCELADO','NP',FALSE),
('E','EMBARGADO','NP',FALSE),
('F','FALLECIDO','NP',FALSE),
('I','INACTIVO','NP',FALSE),
('R','RETIRADO','NP',FALSE),
('T','TRASLADO','NP',FALSE)
ON CONFLICT DO NOTHING;


-- 7️⃣ Estados de documentos soporte
INSERT INTO depositos.estados_documentos_soporte (codigo_estado_documento_soporte, descripcion_codigo_documento_soporte, operativo)
VALUES
('A','ACTIVO',TRUE),
('I','INACTIVO',FALSE),
('P','PERDIDO',FALSE),
('R','ROBADO',FALSE)
ON CONFLICT DO NOTHING;


-- ================================================================
-- 💰 Formas de ahorro (con código_agencia actualizado)
-- ================================================================
INSERT INTO depositos.formas_ahorro (
    codigo_agencia, codigo_forma, nombre_forma, consecutivo_forma, tipo_captacion_forma,
    tiempo_liquidacion, cuenta_forma_corto, cuenta_forma_largo,
    cuenta_gasto, cuenta_cxp_forma, cuenta_gmf_forma, tipo_interes_forma,
    fecha_ultima_liquidacion, autorizado_forma, documento_forma,
    periodo_gracia, valor_minimo, tasa_interes_forma,
    fk_seguridad_creacion, fk_seguridad_edicion
)
VALUES
-- 🔸 Agencia 2
(2,'01','APORTES SOCIALES',9656,'1',360,865,865,865,865,865,1,'2004-10-30',FALSE,'L',0,1,1,1,1),
(2,'02','AHORRO ORDINARIO',9816,'2',30,623,623,623,623,623,1,'2025-10-30',FALSE,'L',3,1,1,1,1),
(2,'03','AHORRO ESCOLAR',9335,'2',30,624,624,624,624,624,1,'2025-10-30',FALSE,'L',3,1,1,1,1),
(2,'04','CON ORDEN DE PAGO',9330,'2',30,625,625,625,625,625,1,'2025-10-30',FALSE,'O',3,1,1,1,1),
(2,'05','AHORRO PAGA DIARIO',9330,'2',1,626,626,626,626,626,1,'2025-11-06',FALSE,'L',0,1,1,1,1),
(2,'06','TARJETA DEBITO',100000,'2',30,627,627,627,627,627,1,'2025-10-30',FALSE,'T',3,1,1,1,1),
(2,'07','T.A.C. PROYECTAMOS',201,'3',30,645,647,645,645,645,1,'2025-10-30',FALSE,'O',0,1,1,1,1),

-- 🔸 Agencia 3
(3,'01','APORTES SOCIALES',9656,'1',360,2292,2292,2292,2292,2292,1,'2004-10-30',FALSE,'L',0,1,1,1,1),
(3,'02','AHORRO ORDINARIO',9816,'2',30,2057,2057,2057,2057,2057,1,'2025-10-30',FALSE,'L',3,1,1,1,1),
(3,'03','AHORRO ESCOLAR',9335,'2',30,2058,2058,2058,2058,2058,1,'2025-10-30',FALSE,'L',3,1,1,1,1),
(3,'04','CON ORDEN DE PAGO',9330,'2',30,2059,2059,2059,2059,2059,1,'2025-10-30',FALSE,'O',3,1,1,1,1),
(3,'05','AHORRO PAGA DIARIO',9330,'2',1,2060,2060,2060,2060,2060,1,'2025-11-06',FALSE,'L',0,1,1,1,1),
(3,'06','TARJETA DEBITO',100000,'2',30,2061,2061,2061,2061,2061,1,'2025-10-30',FALSE,'T',3,1,1,1,1),
(3,'07','T.A.C. PROYECTAMOS',201,'3',30,2080,2082,2080,2080,2080,1,'2025-10-30',FALSE,'O',0,1,1,1,1)
ON CONFLICT DO NOTHING;


-- ================================================================
-- 💹 Tipos de movimiento
-- ================================================================
INSERT INTO depositos.tipo_movimiento (codigo_movimiento, descripcion, accion_movimiento, contabilizacion_diaria, genera_gmf,
    fk_seguridad_creacion, fk_seguridad_edicion)
VALUES
('001','CONSIGNACIONES','S',TRUE,FALSE,1,1),
('005','PAGO DE INTERESES','S',FALSE,FALSE,1,1),
('006','RETORNO COOPERATIVO','S',FALSE,FALSE,1,1),
('115','TRASL. CAPITAL CDAT','S',FALSE,FALSE,1,1),
('221','NOTA CREDITO','S',FALSE,FALSE,1,1),
('222','NOTA CREDITO CARTERA','S',FALSE,FALSE,1,1),
('223','NOTA CREDITO CDAT','S',FALSE,FALSE,1,1),
('224','NOTA CONSIG NOMINA','S',FALSE,FALSE,1,1),
('225','INGRESO POR SERVICIO','S',FALSE,FALSE,1,1),
('335','CON/ON TARJETA EFEC.','S',FALSE,FALSE,1,1),
('336','CON/ON TARJETA CHEQ.','S',FALSE,FALSE,1,1),
('337','COMISION TARJETA','S',FALSE,FALSE,1,1),
('338','REVERSO RETIRO','S',FALSE,FALSE,1,1),
('339','TRASLADO EFE. AGENCI','S',FALSE,FALSE,1,1),
('441','DEVOLUCION 3xMIL','S',FALSE,FALSE,1,1),
('442','TRASLADO ENTRE CTAS','S',FALSE,FALSE,1,1),
('551','RETIRO EFECTIVO','R',TRUE,FALSE,1,1),
('555','COBRO INTERESES','R',FALSE,FALSE,1,1),
('556','COBRO RETENCION','R',FALSE,FALSE,1,1),
('771','NOTA DEBITO','R',FALSE,FALSE,1,1),
('772','NOTA DEBITO CARTERA','R',FALSE,FALSE,1,1),
('773','NOTA DEBITO CDAT','R',FALSE,FALSE,1,1),
('774','NOTA DEBITO NOMINA','R',FALSE,FALSE,1,1),
('778','TRASLADO EFE. AGENCI','R',FALSE,FALSE,1,1),
('880','CHEQUE COOPCENTRAL','R',FALSE,FALSE,1,1),
('881','RETIRO EN CHEQUE','R',FALSE,FALSE,1,1),
('882','TRASLADO ENTRE CTAS','R',FALSE,FALSE,1,1),
('885','RETIRO TARJETA EFEC.','R',FALSE,FALSE,1,1),
('886','RETIRO TARJETA CHEQ.','R',FALSE,FALSE,1,1),
('887','COMISION TARJETA','R',FALSE,FALSE,1,1),
('888','REVERSO CONSIGNACION','R',FALSE,FALSE,1,1),
('991','DECRETO 3258,3xMIL','R',FALSE,FALSE,1,1),
('992','TRASLADO ENTRE CUENTAS','R',FALSE,FALSE,1,1)
ON CONFLICT DO NOTHING;


-- ================================================================
-- ✅ FIN DE MIGRACIÓN
-- ================================================================
