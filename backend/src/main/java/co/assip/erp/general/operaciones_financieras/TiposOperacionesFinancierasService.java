
package co.assip.erp.general.operaciones_financieras;

import co.assip.erp.general.operaciones_financieras.dto.TipoOperacionFinancieraDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TiposOperacionesFinancierasService {

    private final TiposOperacionesFinancierasRepository repository;

    /**
     * Valida una operación financiera mediante su origen.
     * Conserva compatibilidad con los procesos existentes.
     * Rechaza configuraciones ambiguas desde el repositorio.
     */
    @Transactional(readOnly = true)
    public TipoOperacionFinancieraDTO validarOperacion(
            String moduloOrigen,
            String procesoOrigen,
            String tablaOrigen,
            String ejecutor,
            String naturaleza,
            boolean ejecucionParcial
    ) {
        String modulo = normalizar(moduloOrigen, "módulo de origen");
        String proceso = normalizar(procesoOrigen, "proceso de origen");
        String tabla = normalizarTabla(tablaOrigen);

        TipoOperacionFinancieraDTO operacion = repository.buscarActivo(
                modulo,
                proceso,
                tabla
        ).orElseThrow(() -> new IllegalStateException(
                "El proceso financiero no está registrado o está inactivo: "
                        + modulo + " / " + proceso + " / " + tabla
        ));

        validarConfiguracion(
                operacion,
                ejecutor,
                naturaleza,
                ejecucionParcial
        );

        return operacion;
    }

    /**
     * Valida una operación financiera específica mediante su código funcional.
     * Verifica su origen, ejecutor, naturaleza y modalidad de ejecución.
     * No modifica documentos ni operaciones pendientes.
     */
    @Transactional(readOnly = true)
    public TipoOperacionFinancieraDTO validarOperacionPorCodigo(
            String moduloOrigen,
            String procesoOrigen,
            String tablaOrigen,
            String codigoOperacionFinanciera,
            String ejecutor,
            String naturaleza,
            boolean ejecucionParcial
    ) {
        String modulo = normalizar(moduloOrigen, "módulo de origen");
        String proceso = normalizar(procesoOrigen, "proceso de origen");
        String tabla = normalizarTabla(tablaOrigen);
        String codigo = normalizar(
                codigoOperacionFinanciera,
                "código de operación financiera"
        );

        TipoOperacionFinancieraDTO operacion = repository.buscarActivoPorCodigo(
                modulo,
                proceso,
                tabla,
                codigo
        ).orElseThrow(() -> new IllegalStateException(
                "La operación financiera no está registrada o está inactiva: "
                        + modulo + " / " + proceso + " / "
                        + tabla + " / " + codigo
        ));

        validarConfiguracion(
                operacion,
                ejecutor,
                naturaleza,
                ejecucionParcial
        );

        return operacion;
    }

    /**
     * Aplica las reglas comunes de autorización funcional del catálogo.
     * Comprueba ejecutor, naturaleza, parciales y perfil sin modificar datos.
     */
    private void validarConfiguracion(
            TipoOperacionFinancieraDTO operacion,
            String ejecutor,
            String naturaleza,
            boolean ejecucionParcial
    ) {
        String destino = normalizar(ejecutor, "ejecutor");
        String tipoNaturaleza = normalizar(naturaleza, "naturaleza");

        if (!"CAJA".equals(destino) && !"TESORERIA".equals(destino)) {
            throw new IllegalArgumentException(
                    "El ejecutor debe ser CAJA o TESORERIA."
            );
        }

        if ("CAJA".equals(destino)
                && !Boolean.TRUE.equals(operacion.getPermiteCaja())) {
            throw new IllegalStateException(
                    "El proceso financiero no está habilitado para Caja."
            );
        }

        if ("TESORERIA".equals(destino)
                && !Boolean.TRUE.equals(operacion.getPermiteTesoreria())) {
            throw new IllegalStateException(
                    "El proceso financiero no está habilitado para Tesorería."
            );
        }

        if (!"ENTRADA".equals(tipoNaturaleza)
                && !"SALIDA".equals(tipoNaturaleza)) {
            throw new IllegalArgumentException(
                    "La naturaleza debe ser ENTRADA o SALIDA."
            );
        }

        if (!tipoNaturaleza.equals(operacion.getNaturaleza())) {
            throw new IllegalStateException(
                    "La naturaleza solicitada no corresponde "
                            + "a la configuración del proceso financiero."
            );
        }

        if (ejecucionParcial
                && !Boolean.TRUE.equals(operacion.getPermiteParciales())) {
            throw new IllegalStateException(
                    "El proceso financiero no permite ejecuciones parciales."
            );
        }

        if (operacion.getPerfilEjecucion() == null
                || operacion.getPerfilEjecucion().isBlank()) {
            throw new IllegalStateException(
                    "El proceso financiero no tiene perfil de ejecución."
            );
        }
    }

    /**
     * Consulta las operaciones activas disponibles para Tesorería.
     * No autoriza pagos, recaudos ni contabilizaciones.
     */
    @Transactional(readOnly = true)
    public List<TipoOperacionFinancieraDTO> listarParaTesoreria() {
        return repository.listarParaTesoreria();
    }

    /**
     * Consulta las operaciones activas disponibles para Caja.
     * No reemplaza el catálogo operativo de Caja.
     */
    @Transactional(readOnly = true)
    public List<TipoOperacionFinancieraDTO> listarParaCaja() {
        return repository.listarParaCaja();
    }

    /**
     * Normaliza identificadores funcionales y rechaza valores vacíos.
     * Mantiene una convención uniforme para las búsquedas del catálogo.
     */
    private String normalizar(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "El campo " + nombreCampo + " es obligatorio."
            );
        }

        return valor.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Normaliza el nombre físico de la tabla de origen.
     * Respeta la convención de esquemas y tablas de PostgreSQL.
     */
    private String normalizarTabla(String tablaOrigen) {
        if (tablaOrigen == null || tablaOrigen.isBlank()) {
            throw new IllegalArgumentException(
                    "El campo tabla de origen es obligatorio."
            );
        }

        return tablaOrigen.trim().toLowerCase(Locale.ROOT);
    }
}
