package co.assip.erp.sarlaft.informes.dto;

/**
 * 📊 Informe — Datos demográficos de asociados
 * ------------------------------------------------------------
 * Basado en:
 *  - vw_hoja_vida_general_total_reciente
 *  - vw_depositos_cuentas_ahorro_total
 *  - vw_hoja_vida_sarlaft_total
 */
public class InformeDatosDemograficosDTO {

    public String tipoDocumento;          // 1  TIPO
    public String documento;              // 2  DOCUMENTO
    public String nombre;                 // 3  NOMBRE
    public String apellido1;              // 4  APELLIDO 1
    public String apellido2;              // 5  APELLIDO 2
    public String fechaApertura;          // 6  FECHA APERTURA
    public String direccion;              // 7  DIRECCION
    public String telefono;               // 8  TELEFONO
    public String municipio;              // 9  MUNICIPIO
    public String email;                  // 10 EMAIL
    public String genero;                 // 11 GENERO
    public String estudios;               // 12 ESTUDIOS
    public String estrato;                // 13 ESTRATO
    public Double nivelIngresos;          // 14 NIVEL INGRESOS (agregado)
    public String fechaNacimiento;        // 15 FECHA NAC/TO
    public String estadoCivil;            // 16 ESTADO CIVIL
    public String nombreConyuge;          // 17 NOMBRE CONYUGE (familiares parentesco=2)
    public String cedulaConyuge;          // 18 CEDULA CONYUGE
    public String cabezaFamilia;          // 19 CABEZA FAMILIA (S/N)
    public String ocupacion;              // 20 OCUPACION
    public String sectorEconomico;        // 21 SECTOR ECONOMICO
    public Integer personasCargo;         // 22 PERSONAS CARGO (numero_hijos)
    public Double valorSalario;           // 23 VALOR SALARIO
    public Double valorIngresos;          // 24 VALOR INGRESOS (salario+otros)
    public Double otrosIngresos;          // 25 OTROS INGRESOS
    public Double egresos;                // 26 EGRESOS/GASTOS
    public Double valorActivos;           // 27 VALOR ACTIVOS
    public Double valorPasivos;           // 28 VALOR PASIVOS
    public Double valorPatrimonio;        // 29 VALOR PATRIMONIO (activos - pasivos)
    public String tipoVivienda;           // 30 TIPO VIVIENDA
    public String nombreArrendador;       // 31 NOMBRE ARRENDADOR (ref. personal)
    public String telefonoArrendador;     // 32 TELEFO ARRENDADOR (cel ref. personal)
    public String ubicacionVivienda;      // 33 UBICACION VIVIENDA (barrio/ciudad)
    public String celular;                // 34 CELULAR
    public String fechaDocumento;         // 35 FEC. DOCUMENTO
    public String peps;                   // 36 PEPS (asociado_peps)
    public String fechaActualizacion;     // 37 FEC. ACTUALIZACION
    public String fechaPeps;              // 38 FEC. PEPS (fecha_inicial_peps)
    public String comentarioPeps;         // 39 COMENTARIO PEPS
    public String familiarPeps;           // 40 FAMILIAR PEPS (familia_peps)
    public String cedulaFamiliarPeps;     // 41 CED. FAM. PEPS (cedula_familia_peps)
    public String nombreFamiliarPeps;     // 42 NOM. FAM. PEPS
    public Boolean perLlamadas;           // 43 PER. LLAMADAS
    public Boolean perMensajes;           // 44 PER. MENSAJES
    public Boolean perRedes;              // 45 PER. REDES
    public Boolean perCorreos;            // 46 PER. CORREOS
    public Boolean perCartas;             // 47 PER. CARTAS
    public String zona;                   // 48 ZONA
    public String subZona;                // 49 SUB-ZONA
    public Integer edad;                  // 50 EDAD
    public Integer antiguedad;            // 51 ANTIGÜEDAD (años desde apertura)
    public String agencia;                // 52 AGENCIA
    public Double saldoAportes;           // EXTRA: saldo actual aportes (forma 01)
}
