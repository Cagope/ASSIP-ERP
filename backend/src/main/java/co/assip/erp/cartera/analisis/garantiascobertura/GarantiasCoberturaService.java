package co.assip.erp.cartera.analisis.garantiascobertura;
import co.assip.erp.cartera.analisis.garantiascobertura.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class GarantiasCoberturaService {
 private final GarantiasCoberturaRepository repository;
 public List<LocalDate> listarCortes(){return repository.listarCortes();}
 public GarantiasCoberturaResumenDTO obtenerResumen(LocalDate f){validarFecha(f); return repository.obtenerResumen(f).orElseThrow(()->new IllegalArgumentException("No existe información de garantías y cobertura para el corte "+f));}
 public List<GarantiasCoberturaBienDTO> listarBienes(LocalDate f){validarFecha(f);return repository.listarBienes(f);}
 public List<GarantiasCoberturaBienDTO> listarBienesInsuficientes(LocalDate f){validarFecha(f);return repository.listarBienesInsuficientes(f);}
 public List<GarantiasCoberturaCreditoDTO> listarCreditos(LocalDate f){validarFecha(f);return repository.listarCreditos(f);}
 public List<GarantiasCoberturaCreditoDTO> listarCreditosInsuficientes(LocalDate f){validarFecha(f);return repository.listarCreditosInsuficientes(f);}
 public List<GarantiasCoberturaDetalleDTO> listarDetalle(LocalDate f){validarFecha(f);return repository.listarDetalle(f);}
 public List<GarantiasCoberturaDetalleDTO> listarDetallePorBien(LocalDate f,Long id){validarFecha(f);validarId(id,"idBien");return repository.listarDetallePorBien(f,id);}
 public List<GarantiasCoberturaDetalleDTO> listarDetallePorCredito(LocalDate f,Long id){validarFecha(f);validarId(id,"idCarteraCredito");return repository.listarDetallePorCredito(f,id);}
 private void validarFecha(LocalDate f){if(f==null)throw new IllegalArgumentException("La fecha de corte es obligatoria.");}
 private void validarId(Long id,String c){if(id==null||id<=0)throw new IllegalArgumentException(c+" debe ser mayor que cero.");}
}
