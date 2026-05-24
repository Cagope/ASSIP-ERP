package co.assip.erp.depositos.formasahorro;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class FormaAhorroService {

    private final FormaAhorroRepository repo;
    private final UsuarioSesionService usuarioSesionService;

    public FormaAhorroService(
            FormaAhorroRepository repo,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repo = repo;
        this.usuarioSesionService = usuarioSesionService;
    }

    // ==========================================================
    // LISTAR
    // ==========================================================
    public List<FormaAhorro> listar() {
        return repo.findAll();
    }

    // ==========================================================
    // OBTENER
    // ==========================================================
    public FormaAhorro obtener(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("La forma de ahorro no existe."));
    }

    // ==========================================================
    // CREAR
    // ==========================================================
    public FormaAhorro crear(FormaAhorro f) {

        if (repo.existsByCodigoForma(f.getCodigoForma())) {
            throw new RuntimeException("El código ya existe.");
        }

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // 🔐 Auditoría
        f.setFkSeguridadCreacion(idUsuario);
        f.setFkSeguridadEdicion(idUsuario);

        return repo.save(f);
    }

    // ==========================================================
    // EDITAR
    // ==========================================================
    public FormaAhorro editar(Integer id, FormaAhorro f) {

        FormaAhorro db = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe la forma de ahorro."));

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // Código no se edita
        db.setNombreForma(f.getNombreForma());
        db.setConsecutivoForma(f.getConsecutivoForma());
        db.setTipoCaptacion(f.getTipoCaptacion());
        db.setTiempoLiquidacion(f.getTiempoLiquidacion());
        db.setCuentaFormaCorto(f.getCuentaFormaCorto());
        db.setCuentaFormaLargo(f.getCuentaFormaLargo());
        db.setCuentaGasto(f.getCuentaGasto());
        db.setCuentaCxpForma(f.getCuentaCxpForma());
        db.setCuentaGmfForma(f.getCuentaGmfForma());
        db.setTipoInteresForma(f.getTipoInteresForma());
        db.setFechaUltimaLiquidacion(f.getFechaUltimaLiquidacion());
        db.setAutorizadoForma(f.getAutorizadoForma());
        db.setDocumentoForma(f.getDocumentoForma());
        db.setPeriodoGracia(f.getPeriodoGracia());
        db.setValorMinimo(f.getValorMinimo());
        db.setTasaInteresForma(f.getTasaInteresForma());

        // 🔐 Auditoría
        db.setFkSeguridadEdicion(idUsuario);

        return repo.save(db);
    }

    // ==========================================================
    // ELIMINAR
    // ==========================================================
    public void eliminar(Integer id) {

        if (!repo.existsById(id)) {
            throw new RuntimeException("La forma de ahorro no existe.");
        }

        repo.deleteById(id);
    }
}
