package co.assip.erp.hojavida.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class UbicacionDTOs {
    private UbicacionDTOs() {}

    // Payload de creación/actualización (camelCase + alias para snake_case)
    public static class UbicacionRequest {
        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 100, message = "La dirección no debe exceder 100 caracteres")
        @JsonAlias({"direccion"})
        public String direccion;

        @NotBlank(message = "El barrio es obligatorio")
        @Size(max = 100, message = "El barrio no debe exceder 100 caracteres")
        @JsonAlias({"barrio"})
        public String barrio;

        // Opcionales: se validan solo si vienen con valor
        @JsonAlias({"telefono"})
        public String telefono;

        @JsonAlias({"celular_uno","celularUno"})
        public String celularUno;

        @JsonAlias({"celular_dos","celularDos"})
        public String celularDos;

        @JsonAlias({"correo"})
        public String correo;

        // Jerarquía opcional
        @JsonAlias({"id_pais","idPais"})
        public Integer idPais;

        @JsonAlias({"id_departamento","idDepartamento"})
        public Integer idDepartamento;

        @JsonAlias({"id_ciudad","idCiudad"})
        public Integer idCiudad;

        @JsonAlias({"id_sub_zona","idSubZona"})
        public Integer idSubZona;
    }

    // Respuesta al cliente (snake_case), incluye nombres decodificados
    public static class UbicacionResponse {
        public Integer id_ubicacion;
        public Integer id_datos_personal;

        public String  direccion;
        public String  barrio;
        public String  telefono;
        public String  celular_uno;
        public String  celular_dos;
        public String  correo;

        public Integer id_pais;
        public Integer id_departamento;
        public Integer id_ciudad;
        public Integer id_sub_zona;

        public String  nombre_pais;
        public String  nombre_departamento;
        public String  nombre_ciudad;
        public String  nombre_zona;       // obtenido por sub-zona
        public String  nombre_sub_zona;
    }
}
