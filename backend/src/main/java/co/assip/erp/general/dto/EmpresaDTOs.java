package co.assip.erp.general.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class EmpresaDTOs {
    private EmpresaDTOs() {}

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EmpresaHeader {
        private String nombre;
        private String documentoEmpresa;
        private String digitoVerificacion;
        private String telefono;
        private String celular;
        private String direccion;
        private String logoUrl;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EmpresaResponse {
        private String nombre;

        private String tipoDocumento;
        private String documentoEmpresa;
        private String digitoVerificacion;

        private String razonSocial;
        private String siglaEmpresa;
        private LocalDate fechaConstitucion;

        private Integer idPaisDocumento;
        private Integer idDepartamento;
        private Integer idCiudad;

        private String correoCorporativo;
        private String telefono;
        private String celular;
        private String sitioWeb;

        private String logoUrl;

        private Integer fkSeguridadCreacion;
        private LocalDateTime fechaCreacion;
        private Integer fkSeguridadActualizacion;
        private LocalDateTime fechaActualizacion;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EmpresaDetail {
        private String tipoDocumento;
        private String documentoEmpresa;
        private String digitoVerificacion;

        private String razonSocial;
        private String siglaEmpresa;
        private LocalDate fechaConstitucion;

        private Integer idPaisDocumento;
        private Integer idDepartamento;
        private Integer idCiudad;

        private String correoCorporativo;
        private String telefono;
        private String celular;
        private String sitioWeb;
        private String logoUrl;

        private Integer fkSeguridadCreacion;
        private LocalDateTime fechaCreacion;
        private Integer fkSeguridadActualizacion;
        private LocalDateTime fechaActualizacion;
    }
}
