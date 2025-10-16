package co.assip.erp.hojavida.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public final class PermisosEspecialesDTOs {
    private PermisosEspecialesDTOs() {}

    // Request (camelCase + alias snake); si un campo no viene => service lo trata como FALSE
    public static class PermisoEspecialRequest {
        @JsonAlias({"recibe_llamadas","recibeLlamadas"})
        public Boolean recibeLlamadas;
        @JsonAlias({"recibe_msm","recibeMsm"})
        public Boolean recibeMsm;
        @JsonAlias({"recibe_emails","recibeEmails"})
        public Boolean recibeEmails;
        @JsonAlias({"recibe_cartas","recibeCartas"})
        public Boolean recibeCartas;
        @JsonAlias({"recibe_redes_sociales","recibeRedesSociales"})
        public Boolean recibeRedesSociales;
    }

    // Response (snake_case) para el front
    public static class PermisoEspecialResponse {
        public Integer id_permiso_especial;
        public Integer id_datos_personal;
        public Boolean recibe_llamadas;
        public Boolean recibe_msm;
        public Boolean recibe_emails;
        public Boolean recibe_cartas;
        public Boolean recibe_redes_sociales;
    }
}
