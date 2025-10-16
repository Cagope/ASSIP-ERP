package co.assip.erp.hojavida.service;

import java.util.Map;

public interface CapturaService {

    /**
     * Finaliza el proceso de captura de Hoja de Vida.
     * Crea todos los registros relacionados (datos personales, ubicaciones, etc.)
     * en una única transacción.
     *
     * @param data   Mapa con la estructura JSON enviada desde el frontend.
     * @param userId ID del usuario autenticado.
     * @return ID del registro de DatosPersonales creado.
     */
    Integer finalizarCaptura(Map<String, Object> data, Integer userId);
}
