package co.assip.erp.shared.cuentas_ahorro.dto;

public record CuentaAhorroSelectDTO(

        Integer idCuentaAhorro,
        Integer idDatosPersonal,

        Integer codigoForma,
        String nombreForma,

        String numeroCuenta,

        String estadoCodigo,
        String estadoNombre,

        String cuentaDisplay

) {}
