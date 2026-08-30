package co.assip.erp.cartera.analisis.concentracioncartera.dto;

import java.math.BigDecimal;

public record ConcentracionDetalleDeudorDTO(
        Long posicion,
        Long idDatosPersonal,
        String documento,
        String nombreCompleto,
        Long cantidadCreditos,
        BigDecimal saldo,
        BigDecimal porcentajeCartera,
        BigDecimal porcentajeAcumulado,
        BigDecimal deterioro,
        BigDecimal porcentajeDeterioro,
        String agencias,
        String lineas,
        String mayorEdadContable,
        Integer diasMoraMaximos,
        String telefono,
        String celular,
        String correo
) {
}
