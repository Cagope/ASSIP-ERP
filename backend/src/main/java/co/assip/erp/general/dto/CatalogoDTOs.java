package co.assip.erp.general.dto;

public interface CatalogoDTOs {
    record DepartamentoDTO(Integer id, String nombre) {}
    record CiudadDTO(Integer id, String nombre, Integer idDepartamento) {}
}
