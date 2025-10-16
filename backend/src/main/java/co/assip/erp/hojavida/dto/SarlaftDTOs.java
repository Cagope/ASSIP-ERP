package co.assip.erp.hojavida.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.LocalDate;

public final class SarlaftDTOs {
    private SarlaftDTOs() {}

    // ===== Request (acepta snake o camel en JSON) =====
    public static class SarlaftRequest {
        // Exoneración UIAF
        @JsonAlias({"exoneracion_uiaf","exoneracionUiaf"})
        public Boolean exoneracionUiaf;
        @JsonAlias({"fecha_exoneracion","fechaExoneracion"})
        public LocalDate fechaExoneracion;

        // Asociado PEPs
        @JsonAlias({"asociado_peps","asociadoPeps"})
        public Boolean asociadoPeps;
        @JsonAlias({"tipo_peps","tipoPeps"})
        public String tipoPeps;
        @JsonAlias({"observaciones_peps","observacionesPeps"})
        public String observacionesPeps;
        @JsonAlias({"fecha_inicial_peps","fechaInicialPeps"})
        public LocalDate fechaInicialPeps;
        @JsonAlias({"fecha_final_peps","fechaFinalPeps"})
        public LocalDate fechaFinalPeps;

        // Familiar PEPs
        @JsonAlias({"familia_peps","familiaPeps"})
        public Boolean familiaPeps;
        @JsonAlias({"tipo_familia_peps","tipoFamiliaPeps"})
        public String tipoFamiliaPeps;
        @JsonAlias({"cedula_familia_peps","cedulaFamiliaPeps"})
        public String cedulaFamiliaPeps;
        @JsonAlias({"codigo_parentesco","codigoParentesco"})
        public String codigoParentesco;
        @JsonAlias({"nombre_familia_peps","nombreFamiliaPeps"})
        public String nombreFamiliaPeps;

        // Moneda extranjera (negocios)
        @JsonAlias({"moneda_extranjera","monedaExtranjera"})
        public Boolean monedaExtranjera;
        @JsonAlias({"observacion_moneda_extranjera","observacionMonedaExtranjera"})
        public String observacionMonedaExtranjera;

        // Cuentas en el extranjero
        @JsonAlias({"cuenta_extranjero","cuentaExtranjero"})
        public Boolean cuentaExtranjero;
        @JsonAlias({"tipo_moneda_extranjera","tipoMonedaExtranjera"})
        public String tipoMonedaExtranjera;
        @JsonAlias({"numero_cuenta_extranjero","numeroCuentaExtranjero"})
        public String numeroCuentaExtranjero;
        @JsonAlias({"nombre_banco_extranjero","nombreBancoExtranjero"})
        public String nombreBancoExtranjero;
        @JsonAlias({"ciudad_cuenta_extranjero","ciudadCuentaExtranjero"})
        public String ciudadCuentaExtranjero;
        @JsonAlias({"pais_cuenta_extranjero","paisCuentaExtranjero"})
        public String paisCuentaExtranjero;
    }

    // ===== Response (snake_case para el front) =====
    public static class SarlaftResponse {
        public Integer id_sarlaft;
        public Integer id_datos_personal;

        public Boolean exoneracion_uiaf;
        public String  fecha_exoneracion; // ISO yyyy-MM-dd

        public Boolean asociado_peps;
        public String  tipo_peps;
        public String  observaciones_peps;
        public String  fecha_inicial_peps;
        public String  fecha_final_peps;

        public Boolean familia_peps;
        public String  tipo_familia_peps;
        public String  cedula_familia_peps;
        public String  codigo_parentesco;
        public String  nombre_familia_peps;

        public Boolean moneda_extranjera;
        public String  observacion_moneda_extranjera;

        public Boolean cuenta_extranjero;
        public String  tipo_moneda_extranjera;
        public String  numero_cuenta_extranjero;
        public String  nombre_banco_extranjero;
        public String  ciudad_cuenta_extranjero;
        public String  pais_cuenta_extranjero;
    }
}
