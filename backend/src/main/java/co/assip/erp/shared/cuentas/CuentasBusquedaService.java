package co.assip.erp.shared.cuentas;

import co.assip.erp.shared.cuentas.dto.CuentaBusquedaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentasBusquedaService {

    private final CuentasBusquedaRepository repository;

    public List<CuentaBusquedaDTO> buscar(String texto, List<Integer> agenciasUsuario) {

        // 🟢 Regla:
        // - Si viene agencia 1 (central) → ver todo
        // - Si no → filtrar por la primera (agencia activa)
        boolean esCentral = agenciasUsuario != null && agenciasUsuario.contains(1);

        Integer idAgencia = esCentral || agenciasUsuario == null || agenciasUsuario.isEmpty()
                ? null
                : agenciasUsuario.get(0);

        return repository.buscar(texto, idAgencia, esCentral);
    }

    // =========================================================
    // 2) OBTENER CUENTA POR ID
    // =========================================================
    public CuentaBusquedaDTO obtenerPorId(Long id, List<Integer> agenciasUsuario) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Id de cuenta no válido.");
        }

        boolean esCentral = agenciasUsuario != null && agenciasUsuario.contains(1);

        Integer idAgencia = esCentral || agenciasUsuario == null || agenciasUsuario.isEmpty()
                ? null
                : agenciasUsuario.get(0);

        return repository.obtenerPorId(id, idAgencia, esCentral);
    }

    public List<CuentaBusquedaDTO> buscarBancos(String texto, List<Integer> agenciasUsuario) {

        boolean esCentral = agenciasUsuario != null && agenciasUsuario.contains(1);

        Integer idAgencia = esCentral || agenciasUsuario == null || agenciasUsuario.isEmpty()
                ? null
                : agenciasUsuario.get(0);

        return repository.buscarBancos(texto, idAgencia, esCentral);
    }

    public List<CuentaBusquedaDTO> buscarTrasladosAgencias(String texto, List<Integer> agenciasUsuario) {

        boolean esCentral = agenciasUsuario != null && agenciasUsuario.contains(1);

        Integer idAgencia = esCentral || agenciasUsuario == null || agenciasUsuario.isEmpty()
                ? null
                : agenciasUsuario.get(0);

        return repository.buscarTrasladosAgencias(texto, idAgencia, esCentral);
    }

}
