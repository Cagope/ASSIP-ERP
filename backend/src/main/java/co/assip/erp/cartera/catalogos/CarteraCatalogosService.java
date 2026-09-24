package co.assip.erp.cartera.catalogos;

import co.assip.erp.cartera.catalogos.dto.CarteraCatalogoDTO;
import co.assip.erp.cartera.catalogos.dto.FondoGarantiaCatalogoDTO;
import co.assip.erp.cartera.catalogos.dto.LineaCreditoDTO;
import co.assip.erp.cartera.catalogos.dto.ClasificacionCreditoDTO;
import co.assip.erp.cartera.catalogos.dto.GarantiaCreditoDTO;
import co.assip.erp.cartera.catalogos.dto.DestinoEconomicoDTO;
import co.assip.erp.cartera.catalogos.dto.SubgarantiaCreditoDTO;
import co.assip.erp.cartera.catalogos.dto.FormaPagoDTO;
import co.assip.erp.cartera.catalogos.dto.ModalidadInteresDTO;
import co.assip.erp.cartera.catalogos.dto.TipoCuotaDTO;
import co.assip.erp.cartera.catalogos.dto.CentralRiesgoDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CarteraCatalogosService {

    private final CarteraCatalogosRepository repository;

    public CarteraCatalogosService(
            CarteraCatalogosRepository repository
    ) {
        this.repository = repository;
    }

    // =========================================================
    // LÍNEAS DE CRÉDITO
    // =========================================================

    public List<CarteraCatalogoDTO> listarLineasCredito() {
        return repository.listarLineasCredito();
    }

    // =========================================================
    // LÍNEAS DE CRÉDITO - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<LineaCreditoDTO> listarLineasCreditoDetalle(
            boolean soloActivas
    ) {
        return repository.listarLineasCreditoDetalle(soloActivas);
    }

    // =========================================================
    // EDADES DE RIESGO
    // =========================================================

    public List<CarteraCatalogoDTO> listarEdadesRiesgo() {
        return repository.listarEdadesRiesgo();
    }

    // =========================================================
    // CLASIFICACIONES DE CRÉDITO
    // =========================================================

    public List<CarteraCatalogoDTO> listarClasificacionesCredito() {
        return repository.listarClasificacionesCredito();
    }

    // =========================================================
    // CLASIFICACIONES DE CRÉDITO - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<ClasificacionCreditoDTO> listarClasificacionesCreditoDetalle(
            boolean soloActivas
    ) {
        return repository.listarClasificacionesCreditoDetalle(soloActivas);
    }

    // =========================================================
    // GARANTÍAS DE CRÉDITO
    // =========================================================

    public List<CarteraCatalogoDTO> listarGarantiasCredito() {
        return repository.listarGarantiasCredito();
    }

    // =========================================================
    // GARANTÍAS DE CRÉDITO - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<GarantiaCreditoDTO> listarGarantiasCreditoDetalle(
            boolean soloActivas
    ) {
        return repository.listarGarantiasCreditoDetalle(soloActivas);
    }

    // =========================================================
    // DESTINOS ECONÓMICOS - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<DestinoEconomicoDTO> listarDestinosEconomicosDetalle(
            boolean soloActivas
    ) {
        return repository.listarDestinosEconomicosDetalle(soloActivas);
    }

    // =========================================================
    // SUBGARANTÍAS DE CRÉDITO - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<SubgarantiaCreditoDTO> listarSubgarantiasCreditoDetalle(
            boolean soloActivas
    ) {
        return repository.listarSubgarantiasCreditoDetalle(soloActivas);
    }

    // =========================================================
    // FONDOS DE GARANTÍAS
    // =========================================================

    public List<FondoGarantiaCatalogoDTO> listarFondosGarantias() {
        return repository.listarFondosGarantias();
    }

    // =========================================================
    // ESTADOS DE CARTERA
    // =========================================================

    public List<CarteraCatalogoDTO> listarEstadosCartera() {
        return repository.listarEstadosCartera();
    }

    // =========================================================
    // ESTADOS JURÍDICOS
    // =========================================================

    public List<CarteraCatalogoDTO> listarEstadosJuridicos() {
        return repository.listarEstadosJuridicos();
    }

    // =========================================================
    // FORMAS DE PAGO
    // =========================================================

    public List<CarteraCatalogoDTO> listarFormasPago() {
        return repository.listarFormasPago();
    }

    // =========================================================
    // FORMAS DE PAGO - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<FormaPagoDTO> listarFormasPagoDetalle(
            boolean soloActivas
    ) {
        return repository.listarFormasPagoDetalle(soloActivas);
    }

    // =========================================================
    // MODALIDADES DE INTERÉS - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<ModalidadInteresDTO> listarModalidadesInteresesDetalle(
            boolean soloActivas
    ) {
        return repository.listarModalidadesInteresesDetalle(soloActivas);
    }

    // =========================================================
    // TIPOS DE CUOTA - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<TipoCuotaDTO> listarTiposCuotasDetalle(
            boolean soloActivas
    ) {
        return repository.listarTiposCuotasDetalle(soloActivas);
    }

    // =========================================================
    // CENTRALES DE RIESGO - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<CentralRiesgoDTO> listarCentralesRiesgoDetalle(
            boolean soloActivas
    ) {
        return repository.listarCentralesRiesgoDetalle(soloActivas);
    }

}