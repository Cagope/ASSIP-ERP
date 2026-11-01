package co.assip.erp.cartera.informes.reversiones;

import co.assip.erp.cartera.informes.reversiones.dto.InformeReversionDTO;
import co.assip.erp.cartera.informes.reversiones.dto.InformeReversionFiltroDTO;
import co.assip.erp.cartera.informes.reversiones.dto.InformeReversionAsociadoDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/** Aplica reglas de consulta y límites de agencia antes de acceder al repositorio. */
@Service
@RequiredArgsConstructor
public class InformeReversionService {
    private final InformeReversionRepository repository;
    private final UsuarioSesionService sesion;

    /** Sugiere asociados sin exponer datos de agencias ajenas. */
    public List<InformeReversionAsociadoDTO> buscarAsociados(String texto) {
        if (texto == null || texto.trim().length() < 2) {
            throw new IllegalArgumentException("Ingrese al menos dos caracteres para buscar asociados.");
        }
        return repository.buscarAsociados(texto, sesion.agencias(), sesion.tieneAccesoTotal());
    }

    /** Valida fechas y agencia; limita la consulta a un máximo de 24 meses. */
    public List<InformeReversionDTO> consultar(InformeReversionFiltroDTO filtro) {
        if (filtro.fechaInicial() == null || filtro.fechaFinal() == null
                || filtro.fechaInicial().isAfter(filtro.fechaFinal())) {
            throw new IllegalArgumentException("Indique un rango de fechas válido.");
        }
        if (filtro.fechaInicial().plusMonths(24).isBefore(filtro.fechaFinal())) {
            throw new IllegalArgumentException("El rango máximo permitido es de 24 meses.");
        }
        if (filtro.idAgencia() != null) {
            sesion.validarAgencia(filtro.idAgencia());
        }
        return repository.consultar(filtro, sesion.agencias(), sesion.tieneAccesoTotal());
    }
}
