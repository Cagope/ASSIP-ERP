package co.assip.erp.ses.formato9027;

import co.assip.erp.cartera.pago_cuotas_v1.motor.PagoCuotasMotorRepository;
import co.assip.erp.ses.formato9027.dto.Formato9027DTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Formato9027Service {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final MathContext MC = MathContext.DECIMAL128;

    private final Formato9027Repository repository;
    private final PagoCuotasMotorRepository cuentas;

    @Transactional(readOnly = true)
    public List<Formato9027DTO> consultar(LocalDate fechaCorte) {
        if (fechaCorte == null || !fechaCorte.equals(YearMonth.from(fechaCorte).atEndOfMonth())
                || fechaCorte.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha debe ser el último día de un mes cerrado.");
        }
        if (!repository.existeCierre(fechaCorte)) {
            throw new IllegalStateException("No existe cierre definitivo de Cartera para " + fechaCorte);
        }
        List<Map<String, Object>> registros = repository.consultar(fechaCorte);
        List<Formato9027DTO> resultado = new ArrayList<>(registros.size());
        long consecutivo = 0;
        for (Map<String, Object> r : registros) {
            Formato9027DTO f = new Formato9027DTO();
            String pagare = cadena(r, "pagare");
            String numeroCredito = completar(cadena(r, "codigo_agencia"), 2)
                    + completar(cadena(r, "codigo_linea"), 2)
                    + completar(pagare, 6);
            String cuenta = resolverCuenta(r);
            BigDecimal mora = capitalEnMora(r);
            LocalDate fechaAvaluo = fecha(r, "fecha_avaluo");
            if (r.get("id_bien") != null && fechaAvaluo == null) {
                throw new IllegalStateException("Falta fecha histórica de avalúo para pagaré " + pagare);
            }
            if (fechaAvaluo == null) fechaAvaluo = fechaCorte;
            boolean libranza = r.get("empresa_libranza") != null;
            boolean reestructurado = Boolean.TRUE.equals(r.get("reestructurado"));

            // Orden oficial SES 9027: 62 columnas, incluso las vacías.
            poner(f, "CODENTIDAD", "3402");
            poner(f, "FECHACORTE", FECHA.format(fechaCorte));
            poner(f, "CONSECUTIVO", Long.toString(++consecutivo));
            poner(f, "TIPOIDEN", cadena(r, "tipo_documento"));
            poner(f, "NIT", cadena(r, "documento"));
            poner(f, "CODIGOCONTABLE", cuenta);
            poner(f, "MODIFICACIONESALCREDITO", cadena(r, "modificacion"));
            poner(f, "NROCREDITO", numeroCredito);
            poner(f, "FECHADESEMBOLSOINICIAL", fechaTexto(r, "desembolso"));
            poner(f, "FECHAVENCIMIENTO", fechaTexto(r, "vencimiento"));
            poner(f, "MOROSIDAD", numero(r, "dias_mora"));
            poner(f, "TIPOCUOTA", cadena(r, "tipo_cuota"));
            poner(f, "ALTURACUOTA", numero(r, "altura"));
            poner(f, "AMORTIZACION", entero(r, "amortizacion") * 30);
            poner(f, "MODALIDAD", "A".equalsIgnoreCase(cadena(r, "modalidad")) ? "1" : "2");
            poner(f, "TASAINTERESEFECTIVA", numero(r, "tasa_efectiva"));
            poner(f, "VALORPRESTAMO", numero(r, "valor_inicial"));
            poner(f, "VALORCUOTAFIJA", numero(r, "cuota"));
            poner(f, "SALDOCAPITAL", numero(r, "saldo"));
            poner(f, "SALDOINTERESES", numero(r, "intereses"));
            poner(f, "PAGOPORCUENTAASOCIADOS", numero(r, "costas"));
            poner(f, "GARANTIA", numero(r, "garantia"));
            poner(f, "FECHAAVALUO", FECHA.format(fechaAvaluo));
            poner(f, "DETERIORO", numero(r, "deterioro_capital"));
            poner(f, "DETERIOROINTERES", numero(r, "deterioro_intereses"));
            poner(f, "CONTINGENCIA", numero(r, "contingencia"));
            poner(f, "VALORCUOTAEXTRA", "0");
            poner(f, "MESESCUOTAEXTRA", "");
            poner(f, "FECHAULTIMOPAGO", fechaTexto(r, "ultimo_pago_capital"));
            poner(f, "CLASEGARANTIA", cadena(r, "clase_garantia"));
            poner(f, "DESTINOCREDITO", cadena(r, "destino"));
            poner(f, "CODOFICINA", "01");
            poner(f, "AMORTICAPITAL", Math.max(1, entero(r, "amortizacion")) * 30);
            poner(f, "VALORMORA", mora.toPlainString());
            poner(f, "TIPOVIVIENDA", "");
            poner(f, "VIS", "");
            poner(f, "RANGOTIPO", "");
            poner(f, "ENTIDADREDESCUENTO", "");
            poner(f, "MARGENREDESCUENTO", "");
            poner(f, "SUBSIDIO", "");
            poner(f, "DESEMBOLSO", "1");
            poner(f, "MONEDA", "1");
            poner(f, "APORTESSOCIALES", numero(r, "aportes"));
            poner(f, "LINEACREDENTIDAD", cadena(r, "linea_nombre"));
            poner(f, "NUMMODIFICACIONES", numero(r, "novaciones"));
            poner(f, "ESTADOCREDITO", cadena(r, "estado_juridico"));
            poner(f, "NITPATRONAL", libranza ? cadena(r, "documento_libranza") : "");
            poner(f, "NOMBREPATRONAL", libranza ? cadena(r, "nombre_libranza") : "");
            poner(f, "TARJCREDCUPROT", "10".equals(cadena(r, "codigo_linea")) ? "2" : "1");
            poner(f, "ENTOTORGARANT", "7");
            poner(f, "FECHAREESTRUCTURACION", reestructurado ? fechaTexto(r, "fecha_reestructuracion") : "");
            poner(f, "CATEGORIAREESTR", "");
            poner(f, "CLASIFICACION", "");
            poner(f, "PLAZOCREDITO", numero(r, "plazo"));
            poner(f, "TASAINTERESNOMINAL", numero(r, "tasa_nominal"));
            poner(f, "CATEGORIA", cadena(r, "categoria"));
            poner(f, "PERIOGRACIA", "");
            poner(f, "CALIFANTEMODCE1120", "");
            poner(f, "MODCREDCE1120", "");
            poner(f, "TIPOMODCE1120", "");
            poner(f, "FECHAMODCE1120", "");
            poner(f, "MODCREDCE1720", "");
            if (f.size() != 62) throw new IllegalStateException("El formato debe tener 62 columnas.");
            resultado.add(f);
        }
        return resultado;
    }

    private String resolverCuenta(Map<String, Object> r) {
        var credito = new PagoCuotasMotorRepository.DatosContablesCredito(
                entero(r, "id_credito"), entero(r, "agencia"),
                cadena(r, "clasificacion"), cadena(r, "forma_pago"),
                cadena(r, "tipo_garantia"), cadena(r, "edad_contable"));
        String codigo = cuentas.resolverCuenta(credito, "CAPITAL", null).codigoCuenta();
        if (codigo == null || codigo.length() < 6) {
            throw new IllegalStateException("Cuenta de capital inválida para crédito " + credito.idCredito());
        }
        return codigo.substring(0, 6);
    }

    /** Solo capital vencido, nunca intereses ni seguros; saldo histórico como límite. */
    private BigDecimal capitalEnMora(Map<String, Object> r) {
        int dias = entero(r, "dias_mora");
        BigDecimal saldo = decimal(r, "saldo");
        if (dias <= 0 || saldo.signum() <= 0) return BigDecimal.ZERO;
        int amortizacion = entero(r, "amortizacion");
        int plazo = entero(r, "plazo");
        int pagadas = entero(r, "altura");
        if (amortizacion <= 0 || plazo <= 0 || plazo % amortizacion != 0) {
            throw new IllegalStateException("Amortización inválida para crédito " + cadena(r, "pagare"));
        }
        int pendientes = Math.max(0, plazo / amortizacion - pagadas);
        if (pendientes == 0) return saldo;
        int vencidas = Math.max(1, dias / (amortizacion * 30));
        vencidas = Math.min(vencidas, pendientes);
        boolean fija = "1".equals(cadena(r, "tipo_cuota"));
        int periodo = entero(r, "periodo_interes");
        if (fija && periodo <= 0) {
            throw new IllegalStateException("Período de interés inválido para pagaré " + cadena(r, "pagare"));
        }
        BigDecimal cuota = fija
                ? repository.cuotaFija(periodo, plazo, decimal(r, "tasa_nominal"), decimal(r, "valor_inicial"))
                : decimal(r, "cuota");
        if (cuota == null || cuota.signum() <= 0) {
            throw new IllegalStateException("Cuota de capital inválida para pagaré " + cadena(r, "pagare"));
        }
        BigDecimal capital = BigDecimal.ZERO;
        BigDecimal restante = saldo;
        BigDecimal factor = fija ? decimal(r, "tasa_nominal")
                .multiply(BigDecimal.valueOf(periodo), MC)
                .divide(new BigDecimal("1200"), MC) : BigDecimal.ZERO;
        for (int i = 0; i < vencidas && restante.signum() > 0; i++) {
            BigDecimal interes = fija ? restante.multiply(factor, MC)
                    .setScale(0, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
            BigDecimal parteCapital = fija ? cuota.subtract(interes)
                    .setScale(0, RoundingMode.HALF_EVEN) : cuota;
            if (!fija && pagadas + i == 0 && decimal(r, "primera_cuota").signum() > 0) {
                parteCapital = decimal(r, "primera_cuota");
            }
            if (parteCapital.signum() < 0) {
                throw new IllegalStateException("Cuota menor que intereses para pagaré " + cadena(r, "pagare"));
            }
            parteCapital = parteCapital.min(restante);
            capital = capital.add(parteCapital);
            restante = restante.subtract(parteCapital);
        }
        return capital;
    }

    private static void poner(Formato9027DTO f, String clave, Object valor) {
        f.put(clave, valor == null ? "" : valor.toString());
    }
    private static String cadena(Map<String, Object> r, String clave) {
        Object valor = r.get(clave);
        return valor == null ? "" : valor.toString().trim();
    }
    private static BigDecimal decimal(Map<String, Object> r, String clave) {
        Object valor = r.get(clave);
        return valor == null ? BigDecimal.ZERO : new BigDecimal(valor.toString());
    }
    private static int entero(Map<String, Object> r, String clave) {
        return decimal(r, clave).intValue();
    }
    private static String numero(Map<String, Object> r, String clave) {
        return decimal(r, clave).stripTrailingZeros().toPlainString();
    }
    private static LocalDate fecha(Map<String, Object> r, String clave) {
        Object valor = r.get(clave);
        return valor == null ? null : valor instanceof LocalDate d ? d : LocalDate.parse(valor.toString());
    }
    private static String fechaTexto(Map<String, Object> r, String clave) {
        LocalDate valor = fecha(r, clave);
        return valor == null ? "" : FECHA.format(valor);
    }
    private static String completar(String valor, int longitud) {
        if (valor.length() > longitud) {
            throw new IllegalStateException("Código excede " + longitud + " posiciones: " + valor);
        }
        return "0".repeat(longitud - valor.length()) + valor;
    }
}
