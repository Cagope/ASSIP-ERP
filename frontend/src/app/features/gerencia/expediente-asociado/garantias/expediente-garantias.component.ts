import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  Input
} from '@angular/core';

interface GarantiaVista {
  idGarantia: number | null;
  idCredito: number | null;
  idBien: number | null;

  numeroCredito: string;
  codigoGarantia: string;

  nombreTipoGarantia: string;
  descripcion: string;

  estado: string;
  activa: boolean;
  cancelada: boolean;

  fechaConstitucion: string | null;
  fechaVencimiento: string | null;

  valorGarantia: number;
  valorAvaluo: number;
  saldoCredito: number;
  porcentajeCobertura: number;

  garantiaSuficiente: boolean;
  garantiaCompartida: boolean;

  nombrePropietario: string;
  documentoPropietario: string;

  entidadGarantia: string;
  numeroDocumentoGarantia: string;

  tieneAvaluo: boolean;
  fechaAvaluo: string | null;
  avaluoVigente: boolean;
  avaluoVencido: boolean;
  avaluoProximoVencer: boolean;

  tieneSeguro: boolean;
  aseguradora: string;
  numeroPoliza: string;
  valorAsegurado: number;
  fechaVencimientoSeguro: string | null;
  seguroVigente: boolean;
  seguroVencido: boolean;
  seguroProximoVencer: boolean;

  requiereRevision: boolean;
  cantidadAlertas: number;
  alertasCriticas: number;
  alertasAdvertencia: number;
  alertasInformativas: number;
  nivelAlerta: string;

  observaciones: string;
}

@Component({
  selector: 'app-expediente-garantias',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './expediente-garantias.component.html',
  styleUrls: ['./expediente-garantias.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExpedienteGarantiasComponent {

  private garantiasEntrada: unknown[] | null = [];

  @Input()
  set garantias(valor: unknown[] | null | undefined) {
    this.garantiasEntrada = Array.isArray(valor)
      ? valor
      : [];
  }

  get garantias(): unknown[] | null {
    return this.garantiasEntrada;
  }

  get lista(): GarantiaVista[] {
    return (this.garantiasEntrada ?? [])
      .map(item => this.normalizarGarantia(item));
  }

  get tieneGarantias(): boolean {
    return this.lista.length > 0;
  }

  get cantidadGarantias(): number {
    return this.lista.length;
  }

  get cantidadActivas(): number {
    return this.lista.filter(
      garantia => garantia.activa
    ).length;
  }

  get cantidadCanceladas(): number {
    return this.lista.filter(
      garantia => garantia.cancelada
    ).length;
  }

  get cantidadSuficientes(): number {
    return this.lista.filter(
      garantia => garantia.garantiaSuficiente
    ).length;
  }

  get cantidadInsuficientes(): number {
    return this.lista.filter(
      garantia =>
        garantia.activa &&
        !garantia.garantiaSuficiente
    ).length;
  }

  get cantidadCompartidas(): number {
    return this.lista.filter(
      garantia => garantia.garantiaCompartida
    ).length;
  }

  get cantidadAvaluosVencidos(): number {
    return this.lista.filter(
      garantia => garantia.avaluoVencido
    ).length;
  }

  get cantidadSegurosVencidos(): number {
    return this.lista.filter(
      garantia => garantia.seguroVencido
    ).length;
  }

  get cantidadConRevision(): number {
    return this.lista.filter(
      garantia => garantia.requiereRevision
    ).length;
  }

  get cantidadConAlertas(): number {
    return this.lista.filter(
      garantia => garantia.cantidadAlertas > 0
    ).length;
  }

  get valorGarantiasTotal(): number {
    return this.sumar(
      garantia => garantia.valorGarantia
    );
  }

  get valorAvaluosTotal(): number {
    return this.sumar(
      garantia => garantia.valorAvaluo
    );
  }

  get saldoCreditosTotal(): number {
    return this.sumar(
      garantia => garantia.saldoCredito
    );
  }

  get valorAseguradoTotal(): number {
    return this.sumar(
      garantia => garantia.valorAsegurado
    );
  }

  get coberturaGlobal(): number {
    if (this.saldoCreditosTotal <= 0) {
      return 0;
    }

    return (
      this.valorGarantiasTotal /
      this.saldoCreditosTotal
    ) * 100;
  }

  claseEstado(
    garantia: GarantiaVista
  ): string {
    if (garantia.cancelada) {
      return 'estado--cancelada';
    }

    if (garantia.activa) {
      return 'estado--activa';
    }

    return 'estado--neutral';
  }

  claseCobertura(
    garantia: GarantiaVista
  ): string {
    if (garantia.garantiaSuficiente) {
      return 'cobertura--suficiente';
    }

    if (garantia.porcentajeCobertura > 0) {
      return 'cobertura--insuficiente';
    }

    return 'cobertura--sin-calcular';
  }

  claseAlerta(
    garantia: GarantiaVista
  ): string {
    const nivel = this.normalizarTexto(
      garantia.nivelAlerta
    );

    if (
      garantia.alertasCriticas > 0 ||
      garantia.seguroVencido ||
      garantia.avaluoVencido ||
      nivel.includes('CRIT')
    ) {
      return 'alerta--critica';
    }

    if (
      garantia.alertasAdvertencia > 0 ||
      garantia.requiereRevision ||
      garantia.seguroProximoVencer ||
      garantia.avaluoProximoVencer ||
      nivel.includes('ADVERT')
    ) {
      return 'alerta--advertencia';
    }

    if (
      garantia.alertasInformativas > 0 ||
      nivel.includes('INFORMAT')
    ) {
      return 'alerta--informativa';
    }

    return 'alerta--normal';
  }

  requiereAtencion(
    garantia: GarantiaVista
  ): boolean {
    return (
      garantia.requiereRevision ||
      garantia.seguroVencido ||
      garantia.seguroProximoVencer ||
      garantia.avaluoVencido ||
      garantia.avaluoProximoVencer ||
      garantia.cantidadAlertas > 0 ||
      (
        garantia.activa &&
        !garantia.garantiaSuficiente
      )
    );
  }

  textoAvaluo(
    garantia: GarantiaVista
  ): string {
    if (!garantia.tieneAvaluo) {
      return 'Sin avalúo';
    }

    if (garantia.avaluoVencido) {
      return 'Avalúo vencido';
    }

    if (garantia.avaluoProximoVencer) {
      return 'Próximo a vencer';
    }

    if (garantia.avaluoVigente) {
      return 'Avalúo vigente';
    }

    return 'Avalúo registrado';
  }

  textoSeguro(
    garantia: GarantiaVista
  ): string {
    if (!garantia.tieneSeguro) {
      return 'Sin seguro';
    }

    if (garantia.seguroVencido) {
      return 'Seguro vencido';
    }

    if (garantia.seguroProximoVencer) {
      return 'Próximo a vencer';
    }

    if (garantia.seguroVigente) {
      return 'Seguro vigente';
    }

    return 'Seguro registrado';
  }

  private normalizarGarantia(
    origen: unknown
  ): GarantiaVista {
    const item = this.comoRegistro(origen);

    const estado = this.texto(
      this.obtener(
        item,
        'nombreEstadoGarantia',
        'nombreEstado',
        'estadoGarantia',
        'estado'
      ),
      'Sin estado'
    );

    const codigoEstado = this.normalizarTexto(
      this.obtener(
        item,
        'codigoEstadoGarantia',
        'codigoEstado',
        'estadoGarantia'
      )
    );

    const cancelada =
      this.booleano(
        this.obtener(
          item,
          'cancelada',
          'garantiaCancelada'
        )
      ) ||
      codigoEstado === 'C' ||
      this.normalizarTexto(estado)
        .includes('CANCEL');

    const activa =
      this.booleano(
        this.obtener(
          item,
          'activa',
          'garantiaActiva',
          'vigente'
        )
      ) ||
      (
        !cancelada &&
        ['A', 'V'].includes(codigoEstado)
      );

    const valorGarantia = this.numero(
      this.obtener(
        item,
        'valorGarantia',
        'valorComercial',
        'valorRespaldo'
      )
    );

    const saldoCredito = this.numero(
      this.obtener(
        item,
        'saldoCredito',
        'saldoCapitalCredito',
        'saldoObligacion',
        'saldoTotalCredito'
      )
    );

    const coberturaInformada = this.numero(
      this.obtener(
        item,
        'porcentajeCobertura',
        'coberturaGarantia'
      )
    );

    const porcentajeCobertura =
      coberturaInformada !== 0
        ? coberturaInformada
        : saldoCredito > 0
          ? (
              valorGarantia /
              saldoCredito
            ) * 100
          : 0;

    const tieneAvaluo =
      this.booleano(
        this.obtener(
          item,
          'tieneAvaluo'
        )
      ) ||
      this.numero(
        this.obtener(
          item,
          'valorAvaluo',
          'valorAvaluoComercial'
        )
      ) > 0;

    const tieneSeguro =
      this.booleano(
        this.obtener(
          item,
          'tieneSeguro'
        )
      ) ||
      this.texto(
        this.obtener(
          item,
          'numeroPoliza'
        )
      ).length > 0;

    return {
      idGarantia: this.numeroNullable(
        this.obtener(
          item,
          'idGarantia',
          'idCreditoGarantia'
        )
      ),

      idCredito: this.numeroNullable(
        this.obtener(
          item,
          'idCredito'
        )
      ),

      idBien: this.numeroNullable(
        this.obtener(
          item,
          'idBien'
        )
      ),

      numeroCredito: this.texto(
        this.obtener(
          item,
          'numeroCredito',
          'numeroObligacion',
          'codigoCredito'
        ),
        'Sin crédito relacionado'
      ),

      codigoGarantia: this.texto(
        this.obtener(
          item,
          'codigoGarantia',
          'numeroGarantia'
        )
      ),

      nombreTipoGarantia: this.texto(
        this.obtener(
          item,
          'nombreTipoGarantia',
          'nombreGarantia',
          'tipoGarantia'
        ),
        'Garantía sin clasificar'
      ),

      descripcion: this.texto(
        this.obtener(
          item,
          'descripcionGarantia',
          'descripcion',
          'descripcionBien'
        ),
        'Sin descripción'
      ),

      estado,
      activa,
      cancelada,

      fechaConstitucion: this.fecha(
        this.obtener(
          item,
          'fechaConstitucion',
          'fechaRegistro',
          'fechaGarantia'
        )
      ),

      fechaVencimiento: this.fecha(
        this.obtener(
          item,
          'fechaVencimiento',
          'fechaVencimientoGarantia'
        )
      ),

      valorGarantia,

      valorAvaluo: this.numero(
        this.obtener(
          item,
          'valorAvaluo',
          'valorAvaluoComercial'
        )
      ),

      saldoCredito,
      porcentajeCobertura,

      garantiaSuficiente:
        this.booleano(
          this.obtener(
            item,
            'garantiaSuficiente',
            'garantiasSuficientes'
          )
        ) ||
        porcentajeCobertura >= 100,

      garantiaCompartida: this.booleano(
        this.obtener(
          item,
          'garantiaCompartida',
          'compartida'
        )
      ),

      nombrePropietario: this.texto(
        this.obtener(
          item,
          'nombrePropietario',
          'nombreTitular',
          'nombreCompleto'
        ),
        'Sin información'
      ),

      documentoPropietario: this.texto(
        this.obtener(
          item,
          'documentoPropietario',
          'documentoTitular',
          'documento'
        )
      ),

      entidadGarantia: this.texto(
        this.obtener(
          item,
          'nombreEntidadGarantia',
          'entidadGarantia',
          'entidad'
        )
      ),

      numeroDocumentoGarantia: this.texto(
        this.obtener(
          item,
          'numeroDocumentoGarantia',
          'numeroEscritura',
          'numeroMatricula',
          'numeroPolizaGarantia'
        )
      ),

      tieneAvaluo,

      fechaAvaluo: this.fecha(
        this.obtener(
          item,
          'fechaAvaluo'
        )
      ),

      avaluoVigente: this.booleano(
        this.obtener(
          item,
          'avaluoVigente'
        )
      ),

      avaluoVencido: this.booleano(
        this.obtener(
          item,
          'avaluoVencido'
        )
      ),

      avaluoProximoVencer: this.booleano(
        this.obtener(
          item,
          'avaluoProximoVencer'
        )
      ),

      tieneSeguro,

      aseguradora: this.texto(
        this.obtener(
          item,
          'aseguradora'
        ),
        'Sin información'
      ),

      numeroPoliza: this.texto(
        this.obtener(
          item,
          'numeroPoliza'
        )
      ),

      valorAsegurado: this.numero(
        this.obtener(
          item,
          'valorAsegurado'
        )
      ),

      fechaVencimientoSeguro: this.fecha(
        this.obtener(
          item,
          'fechaVencimientoSeguro'
        )
      ),

      seguroVigente: this.booleano(
        this.obtener(
          item,
          'seguroVigente'
        )
      ),

      seguroVencido: this.booleano(
        this.obtener(
          item,
          'seguroVencido'
        )
      ),

      seguroProximoVencer: this.booleano(
        this.obtener(
          item,
          'seguroProximoVencer'
        )
      ),

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
    selector: (garantia: GarantiaVista) => number
  ): number {
    return this.lista.reduce(
      (total, garantia) =>
        total + selector(garantia),
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
    return this.texto(valor).toUpperCase();
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
      'ACTIVO',
      'VIGENTE'
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
}
