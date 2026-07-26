import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  Input
} from '@angular/core';

interface CreditoVista {
  idCredito: number | null;

  numeroCredito: string;
  nombreLinea: string;
  codigoLinea: string;
  nombreProducto: string;

  codigoEstado: string;
  nombreEstado: string;

  activo: boolean;
  cancelado: boolean;
  castigado: boolean;
  enMora: boolean;

  fechaDesembolso: string | null;
  fechaVencimiento: string | null;
  fechaCancelacion: string | null;

  plazoMeses: number;
  numeroCuotas: number;
  cuotasPagadas: number;
  cuotasPendientes: number;

  valorDesembolso: number;
  saldoCapital: number;
  saldoIntereses: number;
  saldoMora: number;
  otrosSaldos: number;
  saldoTotal: number;

  valorCuota: number;
  tasaInteres: number;

  diasMora: number;
  cuotasVencidas: number;

  edadRiesgo: string;
  nombreClasificacion: string;
  nivelRiesgo: string;

  provisionCapital: number;
  provisionIntereses: number;
  provisionTotal: number;

  nombreGarantia: string;
  valorGarantia: number;
  coberturaGarantia: number;
  garantiaSuficiente: boolean;

  reestructurado: boolean;
  novado: boolean;
  juridico: boolean;

  requiereRevision: boolean;
  cantidadAlertas: number;
  alertasCriticas: number;
  alertasAdvertencia: number;
  alertasInformativas: number;
  nivelAlerta: string;

  observaciones: string;
}

@Component({
  selector: 'app-expediente-creditos',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './expediente-creditos.component.html',
  styleUrls: ['./expediente-creditos.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExpedienteCreditosComponent {

  private creditosEntrada: unknown[] | null = [];

  @Input()
  set creditos(valor: unknown[] | null | undefined) {
    this.creditosEntrada = Array.isArray(valor)
      ? valor
      : [];
  }

  get creditos(): unknown[] | null {
    return this.creditosEntrada;
  }

  get lista(): CreditoVista[] {
    return (this.creditosEntrada ?? [])
      .map(item => this.normalizarCredito(item));
  }

  get tieneCreditos(): boolean {
    return this.lista.length > 0;
  }

  get cantidadCreditos(): number {
    return this.lista.length;
  }

  get cantidadActivos(): number {
    return this.lista.filter(
      credito => credito.activo
    ).length;
  }

  get cantidadCancelados(): number {
    return this.lista.filter(
      credito => credito.cancelado
    ).length;
  }

  get cantidadEnMora(): number {
    return this.lista.filter(
      credito => credito.enMora
    ).length;
  }

  get cantidadJuridicos(): number {
    return this.lista.filter(
      credito => credito.juridico
    ).length;
  }

  get cantidadCastigados(): number {
    return this.lista.filter(
      credito => credito.castigado
    ).length;
  }

  get cantidadReestructurados(): number {
    return this.lista.filter(
      credito => credito.reestructurado
    ).length;
  }

  get cantidadConRevision(): number {
    return this.lista.filter(
      credito => credito.requiereRevision
    ).length;
  }

  get cantidadConAlertas(): number {
    return this.lista.filter(
      credito => credito.cantidadAlertas > 0
    ).length;
  }

  get totalDesembolsado(): number {
    return this.sumar(
      credito => credito.valorDesembolso
    );
  }

  get saldoCapitalTotal(): number {
    return this.sumar(
      credito => credito.saldoCapital
    );
  }

  get saldoInteresesTotal(): number {
    return this.sumar(
      credito => credito.saldoIntereses
    );
  }

  get saldoMoraTotal(): number {
    return this.sumar(
      credito => credito.saldoMora
    );
  }

  get saldoTotalCreditos(): number {
    return this.sumar(
      credito => credito.saldoTotal
    );
  }

  get provisionTotal(): number {
    return this.sumar(
      credito => credito.provisionTotal
    );
  }

  get valorGarantiasTotal(): number {
    return this.sumar(
      credito => credito.valorGarantia
    );
  }

  get mayorDiasMora(): number {
    return this.lista.reduce(
      (mayor, credito) =>
        Math.max(mayor, credito.diasMora),
      0
    );
  }

  get mayorEdadRiesgo(): string {
    const edades = this.lista
      .map(credito => credito.edadRiesgo)
      .filter(valor => valor.length > 0)
      .sort((a, b) =>
        this.ordenEdadRiesgo(b) -
        this.ordenEdadRiesgo(a)
      );

    return edades[0] || 'Sin clasificar';
  }

  claseEstado(
    credito: CreditoVista
  ): string {
    if (credito.castigado) {
      return 'estado--castigado';
    }

    if (credito.juridico) {
      return 'estado--juridico';
    }

    if (credito.cancelado) {
      return 'estado--cancelado';
    }

    if (credito.enMora) {
      return 'estado--mora';
    }

    if (credito.activo) {
      return 'estado--activo';
    }

    return 'estado--neutral';
  }

  claseRiesgo(
    credito: CreditoVista
  ): string {
    const riesgo = this.normalizarTexto(
      credito.edadRiesgo ||
      credito.nivelRiesgo
    );

    if (
      ['D', 'E'].includes(riesgo) ||
      riesgo.includes('ALTO') ||
      riesgo.includes('CRITICO') ||
      riesgo.includes('CRÍTICO')
    ) {
      return 'riesgo--alto';
    }

    if (
      ['B', 'C'].includes(riesgo) ||
      riesgo.includes('MEDIO') ||
      riesgo.includes('MODERADO')
    ) {
      return 'riesgo--medio';
    }

    if (
      riesgo === 'A' ||
      riesgo.includes('BAJO')
    ) {
      return 'riesgo--bajo';
    }

    return 'riesgo--neutral';
  }

  claseAlerta(
    credito: CreditoVista
  ): string {
    const nivel = this.normalizarTexto(
      credito.nivelAlerta
    );

    if (
      credito.alertasCriticas > 0 ||
      nivel.includes('CRIT')
    ) {
      return 'alerta--critica';
    }

    if (
      credito.alertasAdvertencia > 0 ||
      nivel.includes('ADVERT')
    ) {
      return 'alerta--advertencia';
    }

    if (
      credito.alertasInformativas > 0 ||
      nivel.includes('INFORMAT')
    ) {
      return 'alerta--informativa';
    }

    return 'alerta--normal';
  }

  requiereAtencion(
    credito: CreditoVista
  ): boolean {
    return (
      credito.enMora ||
      credito.juridico ||
      credito.castigado ||
      credito.requiereRevision ||
      credito.cantidadAlertas > 0 ||
      credito.diasMora > 0
    );
  }

  porcentajePagado(
    credito: CreditoVista
  ): number {
    if (credito.numeroCuotas <= 0) {
      return 0;
    }

    return Math.min(
      100,
      Math.max(
        0,
        (
          credito.cuotasPagadas /
          credito.numeroCuotas
        ) * 100
      )
    );
  }

  textoFecha(
    fecha: string | null
  ): string | null {
    return fecha;
  }

  private normalizarCredito(
    origen: unknown
  ): CreditoVista {
    const item = this.comoRegistro(origen);

    const codigoEstado = this.texto(
      this.obtener(
        item,
        'codigoEstado',
        'estadoCredito',
        'codigoEstadoCredito'
      )
    );

    const nombreEstado = this.texto(
      this.obtener(
        item,
        'nombreEstado',
        'nombreEstadoCredito',
        'descripcionEstado'
      ),
      codigoEstado || 'Sin estado'
    );

    const diasMora = this.numero(
      this.obtener(
        item,
        'diasMora',
        'diasDeMora',
        'diasVencidos'
      )
    );

    const cancelado =
      this.booleano(
        this.obtener(
          item,
          'cancelado',
          'creditoCancelado'
        )
      ) ||
      ['C', 'K'].includes(
        this.normalizarTexto(codigoEstado)
      ) ||
      this.normalizarTexto(nombreEstado)
        .includes('CANCEL');

    const castigado =
      this.booleano(
        this.obtener(
          item,
          'castigado',
          'creditoCastigado'
        )
      ) ||
      this.normalizarTexto(nombreEstado)
        .includes('CASTIG');

    const juridico =
      this.booleano(
        this.obtener(
          item,
          'juridico',
          'enJuridico',
          'cobroJuridico'
        )
      ) ||
      this.normalizarTexto(
        this.obtener(
          item,
          'nombreEstadoJuridico',
          'estadoJuridico'
        )
      ).includes('JURID');

    const enMora =
      this.booleano(
        this.obtener(
          item,
          'enMora',
          'creditoEnMora'
        )
      ) ||
      diasMora > 0;

    const activo =
      this.booleano(
        this.obtener(
          item,
          'activo',
          'creditoActivo',
          'vigente'
        )
      ) ||
      (
        !cancelado &&
        !castigado &&
        ['A', 'V'].includes(
          this.normalizarTexto(codigoEstado)
        )
      );

    const saldoCapital = this.numero(
      this.obtener(
        item,
        'saldoCapital',
        'capitalPendiente',
        'saldoCapitalCredito'
      )
    );

    const saldoIntereses = this.numero(
      this.obtener(
        item,
        'saldoIntereses',
        'interesesPendientes',
        'saldoInteres'
      )
    );

    const saldoMora = this.numero(
      this.obtener(
        item,
        'saldoMora',
        'interesesMora',
        'valorMora'
      )
    );

    const otrosSaldos = this.numero(
      this.obtener(
        item,
        'otrosSaldos',
        'saldoOtrosConceptos',
        'otrosConceptos'
      )
    );

    const saldoTotalInformado = this.numero(
      this.obtener(
        item,
        'saldoTotal',
        'saldoTotalCredito',
        'totalObligacion'
      )
    );

    const saldoTotal =
      saldoTotalInformado !== 0
        ? saldoTotalInformado
        : saldoCapital +
          saldoIntereses +
          saldoMora +
          otrosSaldos;

    const numeroCuotas = this.numero(
      this.obtener(
        item,
        'numeroCuotas',
        'cantidadCuotas',
        'cuotasTotales'
      )
    );

    const cuotasPagadas = this.numero(
      this.obtener(
        item,
        'cuotasPagadas',
        'numeroCuotasPagadas'
      )
    );

    const cuotasPendientesInformadas = this.numero(
      this.obtener(
        item,
        'cuotasPendientes',
        'numeroCuotasPendientes'
      )
    );

    return {
      idCredito: this.numeroNullable(
        this.obtener(
          item,
          'idCredito',
          'idCarteraCredito'
        )
      ),

      numeroCredito: this.texto(
        this.obtener(
          item,
          'numeroCredito',
          'credito',
          'numeroObligacion',
          'codigoCredito'
        ),
        'Sin número'
      ),

      nombreLinea: this.texto(
        this.obtener(
          item,
          'nombreLineaCredito',
          'nombreLinea',
          'descripcionLinea'
        ),
        'Sin línea'
      ),

      codigoLinea: this.texto(
        this.obtener(
          item,
          'codigoLineaCredito',
          'codigoLinea'
        )
      ),

      nombreProducto: this.texto(
        this.obtener(
          item,
          'nombreProducto',
          'tipoProducto',
          'nombreTipoProducto'
        )
      ),

      codigoEstado,
      nombreEstado,

      activo,
      cancelado,
      castigado,
      enMora,

      fechaDesembolso: this.fecha(
        this.obtener(
          item,
          'fechaDesembolso',
          'fechaApertura',
          'fechaCredito'
        )
      ),

      fechaVencimiento: this.fecha(
        this.obtener(
          item,
          'fechaVencimiento',
          'fechaFinal'
        )
      ),

      fechaCancelacion: this.fecha(
        this.obtener(
          item,
          'fechaCancelacion',
          'fechaPagoTotal'
        )
      ),

      plazoMeses: this.numero(
        this.obtener(
          item,
          'plazoMeses',
          'plazo',
          'numeroMeses'
        )
      ),

      numeroCuotas,
      cuotasPagadas,

      cuotasPendientes:
        cuotasPendientesInformadas > 0
          ? cuotasPendientesInformadas
          : Math.max(
              0,
              numeroCuotas - cuotasPagadas
            ),

      valorDesembolso: this.numero(
        this.obtener(
          item,
          'valorDesembolso',
          'valorCredito',
          'capitalInicial',
          'montoAprobado'
        )
      ),

      saldoCapital,
      saldoIntereses,
      saldoMora,
      otrosSaldos,
      saldoTotal,

      valorCuota: this.numero(
        this.obtener(
          item,
          'valorCuota',
          'cuotaActual'
        )
      ),

      tasaInteres: this.numero(
        this.obtener(
          item,
          'tasaInteres',
          'tasa',
          'tasaEfectivaAnual'
        )
      ),

      diasMora,

      cuotasVencidas: this.numero(
        this.obtener(
          item,
          'cuotasVencidas',
          'numeroCuotasVencidas'
        )
      ),

      edadRiesgo: this.texto(
        this.obtener(
          item,
          'edadRiesgo',
          'codigoEdadRiesgo',
          'calificacion'
        )
      ),

      nombreClasificacion: this.texto(
        this.obtener(
          item,
          'nombreClasificacion',
          'nombreEdadRiesgo',
          'descripcionCalificacion'
        )
      ),

      nivelRiesgo: this.texto(
        this.obtener(
          item,
          'nivelRiesgo',
          'nombreNivelRiesgo'
        )
      ),

      provisionCapital: this.numero(
        this.obtener(
          item,
          'provisionCapital',
          'valorProvisionCapital'
        )
      ),

      provisionIntereses: this.numero(
        this.obtener(
          item,
          'provisionIntereses',
          'valorProvisionIntereses'
        )
      ),

      provisionTotal: this.numero(
        this.obtener(
          item,
          'provisionTotal',
          'valorProvision'
        )
      ),

      nombreGarantia: this.texto(
        this.obtener(
          item,
          'nombreGarantia',
          'tipoGarantia',
          'nombreTipoGarantia'
        ),
        'Sin garantía informada'
      ),

      valorGarantia: this.numero(
        this.obtener(
          item,
          'valorGarantia',
          'valorGarantias'
        )
      ),

      coberturaGarantia: this.numero(
        this.obtener(
          item,
          'coberturaGarantia',
          'porcentajeCoberturaGarantia'
        )
      ),

      garantiaSuficiente: this.booleano(
        this.obtener(
          item,
          'garantiaSuficiente',
          'garantiasSuficientes'
        )
      ),

      reestructurado: this.booleano(
        this.obtener(
          item,
          'reestructurado',
          'creditoReestructurado'
        )
      ),

      novado: this.booleano(
        this.obtener(
          item,
          'novado',
          'creditoNovado'
        )
      ),

      juridico,

      requiereRevision: this.booleano(
        this.obtener(
          item,
          'requiereRevision',
          'requiereRevisionManual'
        )
      ),

      cantidadAlertas: this.numero(
        this.obtener(
          item,
          'cantidadAlertas'
        )
      ),

      alertasCriticas: this.numero(
        this.obtener(
          item,
          'alertasCriticas'
        )
      ),

      alertasAdvertencia: this.numero(
        this.obtener(
          item,
          'alertasAdvertencia'
        )
      ),

      alertasInformativas: this.numero(
        this.obtener(
          item,
          'alertasInformativas'
        )
      ),

      nivelAlerta: this.texto(
        this.obtener(
          item,
          'nivelAlerta'
        ),
        'NORMAL'
      ),

      observaciones: this.texto(
        this.obtener(
          item,
          'observaciones',
          'comentario'
        )
      )
    };
  }

  private sumar(
    selector: (credito: CreditoVista) => number
  ): number {
    return this.lista.reduce(
      (total, credito) =>
        total + selector(credito),
      0
    );
  }

  private comoRegistro(
    valor: unknown
  ): Record<string, unknown> {
    if (
      valor !== null &&
      typeof valor === 'object' &&
      !Array.isArray(valor)
    ) {
      return valor as Record<string, unknown>;
    }

    return {};
  }

  private obtener(
    origen: Record<string, unknown>,
    ...claves: string[]
  ): unknown {
    for (const clave of claves) {
      const valor = origen[clave];

      if (
        valor !== null &&
        valor !== undefined &&
        String(valor).trim() !== ''
      ) {
        return valor;
      }
    }

    return null;
  }

  private texto(
    valor: unknown,
    predeterminado = ''
  ): string {
    const resultado = String(
      valor ?? ''
    ).trim();

    return resultado || predeterminado;
  }

  private normalizarTexto(
    valor: unknown
  ): string {
    return this.texto(valor)
      .toUpperCase();
  }

  private numero(
    valor: unknown
  ): number {
    const resultado = Number(
      valor ?? 0
    );

    return Number.isFinite(resultado)
      ? resultado
      : 0;
  }

  private numeroNullable(
    valor: unknown
  ): number | null {
    if (
      valor === null ||
      valor === undefined ||
      String(valor).trim() === ''
    ) {
      return null;
    }

    const resultado = Number(valor);

    return Number.isFinite(resultado)
      ? resultado
      : null;
  }

  private booleano(
    valor: unknown
  ): boolean {
    if (typeof valor === 'boolean') {
      return valor;
    }

    if (typeof valor === 'number') {
      return valor === 1;
    }

    return [
      'S',
      'SI',
      'SÍ',
      'TRUE',
      '1',
      'A',
      'ACTIVO'
    ].includes(
      this.normalizarTexto(valor)
    );
  }

  private fecha(
    valor: unknown
  ): string | null {
    const resultado = this.texto(valor);

    return resultado || null;
  }

  private ordenEdadRiesgo(
    edad: string
  ): number {
    const orden: Record<string, number> = {
      A: 1,
      B: 2,
      C: 3,
      D: 4,
      E: 5
    };

    return orden[
      this.normalizarTexto(edad)
    ] ?? 0;
  }
}
