package co.assip.erp.depositos.informes.rangos;

import lombok.Getter;
import lombok.Setter;

/**
 * 📤 RangosItemDTO — Resultado plano del informe
 * ------------------------------------------------------------
 * Este DTO representa una fila del resultado del informe de
 * rangos (edad, saldo o antigüedad).
 *
 * Contiene:
 *  - Datos de la cuenta
 *  - Datos de la persona
 *  - Ubicación (zona, subzona, ciudad)
 *  - Contacto
 *  - Fechas clave
 *  - Saldos
 *  - Cálculos de edad (años/meses)
 *  - Cálculos de antigüedad (años/meses)
 *
 * Es el contenido final que recibe el frontend para mostrar o
 * exportar a Excel.
 */
@Getter
@Setter
public class RangosItemDTO {

    // ============================
    // 🟦 Datos de la cuenta
    // ============================
    private String codigoCuenta;
    private String codigoAgencia;
    private String nombreAgencia;
    private String codigoForma;
    private String nombreForma;
    private String estadoCuentaCodigo;
    private String estadoCuentaNombre;

    // ============================
    // 🟩 Datos de la persona
    // ============================
    private String documento;
    private String nombreCompleto;
    private String genero;
    private String estadoCivil;
    private String tipoPersona;

    // ============================
    // 🟧 Ubicación
    // ============================
    private String zona;
    private String subZona;
    private String ciudad;
    private String departamento;
    private String pais;

    // ============================
    // 🟪 Contacto
    // ============================
    private String telefono;
    private String celular;
    private String correo;

    // ============================
    // 🟨 Fechas de referencia
    // ============================
    private String fechaNacimiento;
    private String fechaApertura;

    // ============================
    // 🟦 Saldos
    // ============================
    private Double saldo;

    // ============================
    // 🟥 Edad calculada (a corte)
    // ============================
    private Integer edadAnios;
    private Integer edadMeses;

    // ============================
    // 🟫 Antigüedad de la cuenta
    // ============================
    private Integer antiguedadMeses;
    private Integer antiguedadAnios;
}
