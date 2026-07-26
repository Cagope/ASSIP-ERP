package co.assip.erp.gerencia.expedienteasociado.repository;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class ExpedienteRepositorySupport {

    protected static final String PARAM_ID_DATOS_PERSONAL =
            "idDatosPersonal";

    /*
     * Acceso JDBC compartido por los repositorios especializados.
     */
    protected NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // Constructores
    // =========================================================

    /**
     * Se conserva para repositorios que todavía no llaman
     * explícitamente al constructor de la clase soporte.
     */
    protected ExpedienteRepositorySupport() {
    }

    /**
     * Constructor utilizado por los repositorios que comparten
     * el NamedParameterJdbcTemplate mediante esta clase soporte.
     */
    protected ExpedienteRepositorySupport(
            NamedParameterJdbcTemplate jdbc
    ) {

        if (jdbc == null) {
            throw new IllegalArgumentException(
                    "El NamedParameterJdbcTemplate es obligatorio."
            );
        }

        this.jdbc = jdbc;
    }

    // =========================================================
    // Parámetros
    // =========================================================

    protected static MapSqlParameterSource parametros(
            Long idDatosPersonal
    ) {

        validarIdDatosPersonal(idDatosPersonal);

        return new MapSqlParameterSource()
                .addValue(
                        PARAM_ID_DATOS_PERSONAL,
                        idDatosPersonal
                );
    }

    // =========================================================
    // Validaciones
    // =========================================================

    protected static void validarIdDatosPersonal(
            Long idDatosPersonal
    ) {

        if (idDatosPersonal == null
                || idDatosPersonal <= 0) {

            throw new IllegalArgumentException(
                    "El idDatosPersonal es obligatorio."
            );
        }
    }

    /**
     * Se conserva como alias para no afectar repositorios
     * que todavía invoquen validarId(...).
     */
    protected static void validarId(
            Long idDatosPersonal
    ) {

        validarIdDatosPersonal(idDatosPersonal);
    }

    // =========================================================
    // Mapeadores
    // =========================================================

    protected static <T> BeanPropertyRowMapper<T> mapper(
            Class<T> tipo
    ) {

        if (tipo == null) {
            throw new IllegalArgumentException(
                    "El tipo del DTO es obligatorio."
            );
        }

        BeanPropertyRowMapper<T> mapper =
                BeanPropertyRowMapper.newInstance(tipo);

        /*
         * Permite que el DTO tenga más propiedades que las columnas
         * seleccionadas. Los atributos no disponibles quedan en null.
         */
        mapper.setCheckFullyPopulated(false);

        /*
         * Facilita el mapeo de columnas snake_case hacia propiedades
         * camelCase.
         */
        mapper.setPrimitivesDefaultedForNullValue(true);

        return mapper;
    }
}