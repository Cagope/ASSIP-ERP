package co.assip.erp.cajas.provisiones.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CajaUsuarioDTO {

    // =========================================================
    // Vinculación
    // =========================================================

    private Long idCajaUsuario;
    private LocalDate fechaJornada;
    private String estado;
    private LocalDateTime fechaVinculacion;
    private LocalDateTime fechaFinalizacion;
    private String motivoFinalizacion;

    // =========================================================
    // Caja
    // =========================================================

    private Long idCaja;
    private Integer idAgencia;
    private String codigoCaja;
    private String descripcionCaja;

    // =========================================================
    // Usuario operador
    // =========================================================

    private Integer idUsuario;
    private String username;
    private String nombreCompleto;
}