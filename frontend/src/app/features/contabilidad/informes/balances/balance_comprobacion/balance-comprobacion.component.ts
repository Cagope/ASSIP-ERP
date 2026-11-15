
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { SessionService } from '../../../../../core/auth/session.service';
import { MovimientosCuentaModalComponent } from '../../../../../shared/contabilidad/movimientos-cuenta/movimientos-cuenta-modal.component';
import {
  ComprobanteConsultaModalComponent,
  ReferenciaComprobanteModal
} from '../../../../../shared/contabilidad/comprobante-consulta-modal/comprobante-consulta-modal.component';
import { BalanceComprobacionApi } from './balance-comprobacion.api';
import {
  BalanceComprobacionFiltro,
  BalanceComprobacionRespuesta,
  BalanceFila,
  BalanceMovimientoCuenta,
  BalanceMovimientosRespuesta
} from './balance-comprobacion.models';

interface AgenciaOpcion {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
}

/**
 * Consulta el Balance de Comprobación y presenta sus resúmenes financieros.
 * Permite consultar movimientos y comprobantes sin abandonar el informe.
 */
@Component({
  selector: 'app-balance-comprobacion',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MovimientosCuentaModalComponent,
    ComprobanteConsultaModalComponent
  ],
  templateUrl: './balance-comprobacion.component.html',
  styleUrl: './balance-comprobacion.component.scss'
})
export class BalanceComprobacionComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly api = inject(BalanceComprobacionApi);

  readonly meses = [
    'Enero', 'Febrero', 'Marzo', 'Abril',
    'Mayo', 'Junio', 'Julio', 'Agosto',
    'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
  ];

  readonly niveles = [1, 2, 3, 4, 5, 6];

  readonly formato = new Intl.NumberFormat('es-CO', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });

  readonly porcentaje = new Intl.NumberFormat('es-CO', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });

  readonly tamanoPaginaMovimientos = 25;

  agencias: AgenciaOpcion[] = [];

  filtro: BalanceComprobacionFiltro = {
    idAgencia: 0,
    ano: new Date().getFullYear(),
    mes: new Date().getMonth() + 1,
    nivel: 6,
    soloBalance: false,
    compararAnoAnterior: false,
    incluirFirmas: true
  };

  informe: BalanceComprobacionRespuesta | null = null;
  cargando = false;
  exportando: 'pdf' | 'excel' | null = null;
  mensajeError = '';

  movimientos: BalanceMovimientosRespuesta | null = null;
  cuentaSeleccionada: BalanceFila | null = null;
  cargandoMovimientos = false;
  errorMovimientos = '';

  referenciaComprobanteSeleccionado: ReferenciaComprobanteModal | null = null;

  private version = 0;
  private versionMovimientos = 0;

  /** Mantiene los permisos utilizados por los balances contables. */
  get puedeConsultar(): boolean {
    return this.session.tienePermiso(
      'CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_VIEW'
    );
  }

  /** Autoriza el consolidado únicamente a funcionarios habilitados. */
  get puedeConsolidar(): boolean {
    return this.session.tienePermiso(
      'CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_CONSOLIDADO_VIEW'
    );
  }

  /** Calcula el primer día del período consultado. */
  get fechaInicialMovimientos(): string {
    if (!this.informe) {
      return '';
    }

    const { ano, mes } = this.informe;
    return `${ano}-${String(mes).padStart(2, '0')}-01`;
  }

  /** Calcula el último día del mes, incluyendo años bisiestos. */
  get fechaFinalMovimientos(): string {
    if (!this.informe) {
      return '';
    }

    const { ano, mes } = this.informe;
    const ultimoDia = new Date(ano, mes, 0).getDate();

    return `${ano}-${String(mes).padStart(2, '0')}-${String(ultimoDia).padStart(2, '0')}`;
  }

  /** Carga las agencias autorizadas y selecciona la agencia de sesión. */
  ngOnInit(): void {
    this.agencias = this.session.agenciasSig()
      .filter(a => a.idAgencia !== null && a.idAgencia !== 1)
      .map(a => ({
        idAgencia: a.idAgencia!,
        codigoAgencia: a.codigoAgencia ?? '',
        nombreAgencia: a.nombreAgencia ?? ''
      }));

    if (this.puedeConsultar && this.puedeConsolidar) {
      this.agencias.unshift({
        idAgencia: 1,
        codigoAgencia: '00',
        nombreAgencia: 'CONSOLIDADO INSTITUCIONAL'
      });
    }

    const activa = this.session.agenciaActivaSig()?.idAgencia;

    this.filtro.idAgencia =
      this.agencias.find(a => a.idAgencia === activa)?.idAgencia
      ?? (this.agencias.length === 1 ? this.agencias[0].idAgencia : 0);
  }

  /**
   * Invalida resultados anteriores cuando cambian los filtros.
   * Cierra los modales para evitar mezclar períodos o agencias.
   */
  cambiarFiltros(): void {
    this.version++;
    this.informe = null;
    this.mensajeError = '';
    this.cerrarMovimientos();
  }

  /** Valida permisos, agencia, año, mes y nivel contable. */
  private valido(): boolean {
    if (
      !this.puedeConsultar ||
      !this.agencias.some(a => a.idAgencia === this.filtro.idAgencia)
    ) {
      this.mensajeError =
        'No tiene autorización para la agencia seleccionada.';
      return false;
    }

    if (
      !Number.isInteger(this.filtro.ano) ||
      this.filtro.ano < 1900 ||
      this.filtro.ano > 9999 ||
      !Number.isInteger(this.filtro.mes) ||
      this.filtro.mes < 1 ||
      this.filtro.mes > 12 ||
      !this.niveles.includes(this.filtro.nivel)
    ) {
      this.mensajeError = 'Revise el año, mes y nivel contable.';
      return false;
    }

    return true;
  }

  /**
   * Consulta el balance y sus resúmenes financieros.
   * Descarta respuestas correspondientes a filtros anteriores.
   */
  consultar(): void {
    if (this.cargando || this.exportando) {
      return;
    }

    this.mensajeError = '';

    if (!this.valido()) {
      return;
    }

    const version = ++this.version;

    this.informe = null;
    this.cerrarMovimientos();
    this.cargando = true;

    this.api.consultar({ ...this.filtro })
      .pipe(
        finalize(() => {
          if (version === this.version) {
            this.cargando = false;
          }
        })
      )
      .subscribe({
        next: respuesta => {
          if (version === this.version) {
            this.informe = respuesta;
          }
        },
        error: err => {
          if (version === this.version) {
            this.mensajeError = this.error(
              err,
              'No se pudo consultar el Balance de Comprobación.'
            );
          }
        }
      });
  }

  /** Exporta el balance consultado conservando los filtros originales. */
  exportar(tipo: 'pdf' | 'excel'): void {
    if (
      !this.informe ||
      this.cargando ||
      this.exportando ||
      !this.valido()
    ) {
      return;
    }

    const filtro = { ...this.filtro };
    const codigoAgencia = this.informe.codigoAgencia;

    this.exportando = tipo;
    this.mensajeError = '';

    this.api.descargar(filtro, tipo)
      .pipe(
        finalize(() => {
          this.exportando = null;
        })
      )
      .subscribe({
        next: blob => {
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');

          a.href = url;
          a.download =
            `balance_comprobacion_${codigoAgencia}_${filtro.ano}_` +
            `${String(filtro.mes).padStart(2, '0')}.` +
            `${tipo === 'pdf' ? 'pdf' : 'xlsx'}`;

          document.body.appendChild(a);
          a.click();
          a.remove();
          URL.revokeObjectURL(url);
        },
        error: err => {
          this.mensajeError = this.error(
            err,
            'No se pudo exportar el Balance de Comprobación.'
          );
        }
      });
  }

  /** Abre los movimientos de una cuenta auxiliar operable. */

/** Selecciona la cuenta auxiliar y consulta sus movimientos. */
abrirMovimientos(cuenta: BalanceFila): void {
  if (!this.informe) {
    return;
  }

  this.cuentaSeleccionada = cuenta;
  this.consultarPaginaMovimientos(0);
}


  /**
   * Consulta una página de movimientos.
   * Ignora respuestas pendientes de otras cuentas o páginas.
   */
  consultarPaginaMovimientos(pagina: number): void {
    if (
      !this.informe ||
      !this.cuentaSeleccionada ||
      !Number.isInteger(pagina) ||
      pagina < 0
    ) {
      return;
    }

    const version = ++this.versionMovimientos;
    const informe = this.informe;
    const codigoCuenta = this.cuentaSeleccionada.codigoCuenta;

    this.movimientos = null;
    this.errorMovimientos = '';
    this.cargandoMovimientos = true;

    this.api.consultarMovimientos({
      idAgencia: informe.idAgencia,
      ano: informe.ano,
      mes: informe.mes,
      codigoCuenta,
      pagina,
      tamanoPagina: this.tamanoPaginaMovimientos
    })
      .pipe(
        finalize(() => {
          if (version === this.versionMovimientos) {
            this.cargandoMovimientos = false;
          }
        })
      )
      .subscribe({
        next: respuesta => {
          if (version === this.versionMovimientos) {
            this.movimientos = respuesta;
          }
        },
        error: err => {
          if (version === this.versionMovimientos) {
            this.errorMovimientos = this.error(
              err,
              'No se pudieron consultar los movimientos.'
            );
          }
        }
      });
  }

  /**
   * Cierra los movimientos y cualquier comprobante asociado.
   * Invalida solicitudes pendientes sin modificar el balance.
   */
  cerrarMovimientos(): void {
    this.versionMovimientos++;
    this.referenciaComprobanteSeleccionado = null;
    this.cuentaSeleccionada = null;
    this.movimientos = null;
    this.errorMovimientos = '';
    this.cargandoMovimientos = false;
  }

  /**
   * Abre el comprobante completo en el modal compartido.
   * Conserva la cuenta y la página de movimientos consultada.
   */
  seleccionarComprobante(movimiento: BalanceMovimientoCuenta): void {
    if (
      !this.cuentaSeleccionada ||
      !this.informe ||
      this.cargandoMovimientos ||
      this.referenciaComprobanteSeleccionado
    ) {
      return;
    }

    this.errorMovimientos = '';

    this.referenciaComprobanteSeleccionado = {
      idAgencia: movimiento.idAgencia,
      tipoComprobante: movimiento.tipoComprobante,
      numeroComprobante: movimiento.numeroComprobante
    };
  }

  /** Cierra solamente el comprobante, conservando los movimientos. */
  cerrarComprobante(): void {
    this.referenciaComprobanteSeleccionado = null;
  }

  /** Construye la identificación legible del comprobante. */
  referenciaComprobante(movimiento: BalanceMovimientoCuenta): string {
    return (
      `${movimiento.codigoAgencia} - ` +
      `${movimiento.tipoComprobante} ` +
      `${movimiento.numeroComprobante}`
    );
  }

  /** Formatea valores monetarios en formato colombiano. */
  dinero(valor: number | null | undefined): string {
    return valor == null
      ? '—'
      : this.formato.format(valor);
  }

  /** Formatea variaciones porcentuales en formato colombiano. */
  variacion(valor: number | null | undefined): string {
    return valor == null
      ? 'N/A'
      : `${this.porcentaje.format(valor)} %`;
  }

  /** Presenta los errores HTTP con mensajes comprensibles. */
  private error(
    err: {
      status?: number;
      error?: {
        message?: string;
        mensaje?: string;
      };
    },
    defecto: string
  ): string {
    if (err.status === 403) {
      return 'No tiene permisos para consultar este balance o agencia.';
    }

    return err.error?.message || err.error?.mensaje || defecto;
  }
}
