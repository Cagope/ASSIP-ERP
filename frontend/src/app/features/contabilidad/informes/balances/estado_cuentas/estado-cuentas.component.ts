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
import { EstadoCuentasApi } from './estado-cuentas.api';
import {
  EstadoCuentasFiltro,
  EstadoCuentasRespuesta,
  EstadoCuentasFila,
  EstadoCuentasMovimiento,
  EstadoCuentasMovimientosRespuesta,
  CuentaPrincipal
} from './estado-cuentas.models';

interface AgenciaOpcion {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
}

/**
 * Consulta el Estado de Cuentas y permite visualizar
 * movimientos de cuentas y comprobantes contables en modales compartidos.
 */
@Component({
  selector: 'app-estado-cuentas',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MovimientosCuentaModalComponent,
    ComprobanteConsultaModalComponent
  ],
  templateUrl: './estado-cuentas.component.html',
  styleUrl: './estado-cuentas.component.scss'
})
export class EstadoCuentasComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly api = inject(EstadoCuentasApi);

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

  filtro: EstadoCuentasFiltro = {
    idAgencia: 0,
    fechaCorte: [new Date().getFullYear(), String(new Date().getMonth() + 1).padStart(2, '0'), String(new Date().getDate()).padStart(2, '0')].join('-'),
    codigoCuentaPrincipal: '',
    nivel: 6,
    incluirFirmas: false
  };

  cuentasPrincipales: CuentaPrincipal[] = [];
  cargandoCuentas = false;

  informe: EstadoCuentasRespuesta | null = null;
  cargando = false;
  exportando: 'pdf' | 'excel' | null = null;
  mensajeError = '';

  movimientos: EstadoCuentasMovimientosRespuesta | null = null;
  cuentaSeleccionada: EstadoCuentasFila | null = null;
  cargandoMovimientos = false;
  errorMovimientos = '';

  referenciaComprobanteSeleccionado: ReferenciaComprobanteModal | null = null;

  private version = 0;
  private versionMovimientos = 0;

  get puedeConsultar(): boolean {
    return this.session.tienePermiso(
      'CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_VIEW'
    );
  }

  get puedeConsolidar(): boolean {
    return this.session.tienePermiso(
      'CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_CONSOLIDADO_VIEW'
    );
  }

  /** Primer día del mes seleccionado. */
  get fechaInicialMovimientos(): string {
    return this.informe ? `${this.informe.fechaCorte.substring(0, 7)}-01` : '';
  }

  /** El auxiliar se consulta únicamente hasta la fecha de corte. */
  get fechaFinalMovimientos(): string {
    return this.informe?.fechaCorte ?? '';
  }

  /** Carga agencias autorizadas y ofrece consolidado únicamente con permiso. */
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

    this.cargarCuentasPrincipales();
  }

  /** Carga cuentas agrupadoras disponibles según la agencia seleccionada. */
  cambiarAgencia(): void {
    this.filtro.codigoCuentaPrincipal = '';
    this.cambiarFiltros();
    this.cargarCuentasPrincipales();
  }

  private cargarCuentasPrincipales(): void {
    this.cuentasPrincipales = [];
    if (!this.filtro.idAgencia || !this.puedeConsultar) return;
    this.cargandoCuentas = true;
    this.api.listarCuentas(this.filtro.idAgencia)
      .pipe(finalize(() => this.cargandoCuentas = false))
      .subscribe({
        next: cuentas => this.cuentasPrincipales = cuentas,
        error: err => this.mensajeError = this.error(err,
          'No se pudieron consultar las cuentas principales.')
      });
  }

  /** Invalida los resultados cuando se modifica algún filtro. */
  cambiarFiltros(): void {
    this.version++;
    this.informe = null;
    this.mensajeError = '';
    this.cerrarMovimientos();
  }

  /** Valida permisos, agencia y parámetros antes de consultar o exportar. */
  private valido(): boolean {
    if (
      !this.puedeConsultar ||
      !this.agencias.some(a => a.idAgencia === this.filtro.idAgencia)
    ) {
      this.mensajeError =
        'No tiene autorización para la agencia seleccionada.';
      return false;
    }

    if (!/^\d{4}-\d{2}-\d{2}$/.test(this.filtro.fechaCorte)
      || !this.niveles.includes(this.filtro.nivel)
      || this.filtro.fechaCorte > [new Date().getFullYear(), String(new Date().getMonth() + 1).padStart(2, '0'), String(new Date().getDate()).padStart(2, '0')].join('-')) {
      this.mensajeError = 'Revise la fecha de corte y el nivel contable.';
      return false;
    }

    return true;
  }

  /** Consulta el estado de cuentas y descarta respuestas de filtros anteriores. */
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
              'No se pudo consultar el estado de cuentas.'
            );
          }
        }
      });
  }

  /** Exporta el período consultado, conservando PDF y Excel existentes. */
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
            `estado_cuentas_${codigoAgencia}_${filtro.fechaCorte}.` +
            `${tipo === 'pdf' ? 'pdf' : 'xlsx'}`;

          document.body.appendChild(a);
          a.click();
          a.remove();
          URL.revokeObjectURL(url);
        },
        error: err => {
          this.mensajeError = this.error(
            err,
            'No se pudo exportar el informe.'
          );
        }
      });
  }

  /** Selecciona una cuenta operable para mostrar sus movimientos en el modal. */
  abrirMovimientos(cuenta: EstadoCuentasFila): void {
    if (
      !this.informe ||
      !cuenta.cuentaOperable ||
      this.cargando
    ) {
      return;
    }

    this.cuentaSeleccionada = cuenta;
    this.consultarPaginaMovimientos(0);
  }

  /** Consulta una página e ignora respuestas de selecciones anteriores. */
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
      fechaCorte: informe.fechaCorte,
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
   * Cierra el modal de movimientos e invalida consultas pendientes.
   * También cierra cualquier comprobante abierto desde ese modal.
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
   * Abre el comprobante en un modal independiente de solo consulta.
   * Conserva el informe, la cuenta y la página actual de movimientos.
   */
  seleccionarComprobante(movimiento: EstadoCuentasMovimiento): void {
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

  /**
   * Cierra únicamente el modal de comprobante.
   * Mantiene la cuenta y los movimientos previamente consultados.
   */
  cerrarComprobante(): void {
    this.referenciaComprobanteSeleccionado = null;
  }

  /** Construye la referencia textual del comprobante contable. */
  referenciaComprobante(movimiento: EstadoCuentasMovimiento): string {
    return (
      `${movimiento.codigoAgencia} - ` +
      `${movimiento.tipoComprobante} ` +
      `${movimiento.numeroComprobante}`
    );
  }

  /** Presenta importes monetarios sin modificar el valor numérico. */
  dinero(valor: number | null | undefined): string {
    return valor == null
      ? '—'
      : this.formato.format(valor);
  }

  /** Normaliza mensajes de error HTTP. */
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
      return 'No tiene permisos para consultar este informe o agencia.';
    }

    return err.error?.message || err.error?.mensaje || defecto;
  }
}
