package co.assip.erp.hojavida.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public final class ReferenciasPersonalesDTOs {
    private ReferenciasPersonalesDTOs() {}

    // Crear / actualizar (payload del cliente)
    public static class ReferenciaPersonalRequest {
        @JsonAlias({"nombreReferenciaPersonal","nombre_referencia_personal"})
        public String  nombreReferenciaPersonal;

        @JsonAlias({"direccionReferenciaPersonal","direccion_referencia_personal"})
        public String  direccionReferenciaPersonal;

        @JsonAlias({"idDepartamento","id_departamento"})
        public Integer idDepartamento;

        @JsonAlias({"idCiudad","id_ciudad"})
        public Integer idCiudad;

        @JsonAlias({"telefonoReferenciaPersonal","telefono_referencia_personal"})
        public String  telefonoReferenciaPersonal; // opcional

        @JsonAlias({"celularReferenciaPersonal","celular_referencia_personal"})
        public String  celularReferenciaPersonal;  // opcional
    }

    // Respuesta al cliente
    public static class ReferenciaPersonalResponse {
        public Integer id_referencia_personal;
        public Integer id_datos_personal;
        public String  nombre_referencia_personal;
        public String  direccion_referencia_personal;
        public Integer id_departamento;
        public Integer id_ciudad;
        public String  telefono_referencia_personal;
        public String  celular_referencia_personal;

        // NUEVO: nombre de la ciudad (lo poblaremos en el siguiente paso)
        public String  nombre_ciudad;
    }
}
