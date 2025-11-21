package co.assip.erp.depositos.formasahorro;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FormaAhorroRepository extends JpaRepository<FormaAhorro, Integer> {

    boolean existsByCodigoForma(String codigoForma);
}
