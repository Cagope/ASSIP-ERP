
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';
import { finalize } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { ComprobantesConsultaComponent } from '../consulta/comprobantes-consulta.component';
import { ComprobantesConsultaService } from '../consulta/comprobantes-consulta.api';
import { ComprobanteCompleto, MovimientoEdicion, SolicitudEdicion, TipoControl } from '../consulta/comprobantes-consulta.models';
import { ComprobantesEditarService } from './comprobantes-editar.api';
import { NumericFormatDirective } from '../../../../shared/utils/numeric-format.directive';

interface CuentaApi {
  id: number;
  idAgencia: number;
  codigoCuenta: string;
  nombre: string;
  operable: boolean;
  controlEntradaSalida: boolean;
}
interface TerceroApi {
  idDatosPersonal: number;
  documento: string;
  nombreCompleto: string;
}
interface FilaEdicion extends MovimientoEdicion {
  cuentaTexto: string;
  terceroTexto: string;
  requiereControl: boolean;
  controlConocido: boolean;
}

/**
 * Edita movimientos de comprobantes manuales activos.
 * Presenta códigos y descripciones sin alterar los identificadores contables.
 */
@Component({
  selector: 'app-comprobantes-editar',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    ComprobantesConsultaComponent,
    NumericFormatDirective
  ],
  templateUrl: './comprobantes-editar.component.html',
  styleUrls: ['./comprobantes-editar.component.scss']
})
export class ComprobantesEditarComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  private readonly consulta = inject(ComprobantesConsultaService);
  private readonly edicion = inject(ComprobantesEditarService);
  private readonly base = environment.apiUrl;

  comprobante: ComprobanteCompleto | null = null;
  concepto = '';
  filas: FilaEdicion[] = [];
  tiposControl: TipoControl[] = [];
  cuentas: CuentaApi[] = [];
  terceros: TerceroApi[] = [];
  buscandoCuenta: number | null = null;
  buscandoTercero: number | null = null;
  filaActiva: number | null = null;
  guardando = false;
  cargando = false;
  error = '';
  mensaje = '';

  readonly moneda = new Intl.NumberFormat('es-CO', {
    maximumFractionDigits: 2
  });

  /** Inicializa los controles y recupera el comprobante solicitado. */
  ngOnInit(): void {
    this.consulta.tiposControl().subscribe({
      next: tipos => this.tiposControl = tipos,
      error: () => this.error = 'No se pudieron consultar los tipos de control.'
    });
    const params = this.route.snapshot.queryParamMap;
    const agencia = Number(params.get('idAgencia'));
    const tipo = params.get('tipoComprobante');
    const numero = params.get('numeroComprobante');
    if (agencia > 0 && tipo && numero) {
      this.cargando = true;
      this.consulta.consultar(agencia, tipo, numero)
        .pipe(finalize(() => this.cargando = false))
        .subscribe({
          next: datos => this.cargar(datos),
          error: err => this.error = err.error?.message || 'No se pudo recuperar el comprobante.'
        });
    }
  }

  /**
   * Carga los movimientos para edición utilizando los datos descriptivos
   * recibidos del backend y conservando sus identificadores originales.
   */
  cargar(datos: ComprobanteCompleto): void {
    this.error = '';
    this.mensaje = '';
    this.comprobante = datos;
    this.concepto = datos.cabecera.conceptoComprobante || '';
    this.filaActiva = null;
    this.cuentas = [];
    this.terceros = [];
    this.buscandoCuenta = null;
    this.buscandoTercero = null;
    this.filas = datos.movimientos.map(m => ({
      idAuxiliarContable: m.idAuxiliarContable,
      idCatalogoCuenta: m.idCatalogoCuenta,
      idDatosPersonal: m.idDatosPersonal || 0,
      detalleMovimiento: m.detalleMovimiento || '',
      valorDebito: Number(m.valorDebito || 0),
      valorCredito: Number(m.valorCredito || 0),
      valorBase: Number(m.valorBase || 0),
      idTiposControl: m.idTiposControl,
      numeroDocumento: m.numeroDocumento,
      cuentaTexto: m.codigoCuenta
        ? `${m.codigoCuenta} - ${m.nombreCuenta || 'Sin descripción'}`
        : `Cuenta #${m.idCatalogoCuenta}`,
      terceroTexto: m.documentoTercero
        ? `${m.documentoTercero} - ${m.nombreTercero || 'Sin nombre'}`
        : m.idDatosPersonal
          ? `Tercero #${m.idDatosPersonal}`
          : '',
      requiereControl: m.idTiposControl != null,
      controlConocido: m.idTiposControl != null
    }));
  }

  /** Habilita la edición de una sola fila y limpia las búsquedas anteriores. */
  seleccionarFila(fila: FilaEdicion): void {
    this.filaActiva = fila.idAuxiliarContable;
    this.cuentas = [];
    this.terceros = [];
    this.buscandoCuenta = null;
    this.buscandoTercero = null;
    this.error = '';
  }

  /** Busca cuentas operables por código o nombre para la agencia del comprobante. */
  buscarCuenta(fila: FilaEdicion, texto: string): void {
    fila.cuentaTexto = texto;
    fila.idCatalogoCuenta = 0;
    fila.controlConocido = false;
    this.buscandoCuenta = fila.idAuxiliarContable;
    this.cuentas = [];
    if (!this.comprobante || texto.trim().length < 2) return;
    this.http.get<CuentaApi[]>(`${this.base}/contabilidad/plan-cuentas/buscar`, {
      params: new HttpParams()
        .set('idAgencia', this.comprobante.cabecera.idAgencia)
        .set('q', texto.trim())
    }).subscribe({
      next: lista => {
        if (this.buscandoCuenta === fila.idAuxiliarContable) {
          this.cuentas = lista.filter(c => c.operable);
        }
      },
      error: () => this.error = 'No se pudieron buscar las cuentas.'
    });
  }

  /** Asigna la cuenta seleccionada y actualiza su control documental. */
  elegirCuenta(fila: FilaEdicion, cuenta: CuentaApi): void {
    if (!this.comprobante || !cuenta.operable ||
        cuenta.idAgencia !== this.comprobante.cabecera.idAgencia) return;
    fila.idCatalogoCuenta = cuenta.id;
    fila.cuentaTexto = `${cuenta.codigoCuenta} - ${cuenta.nombre}`;
    fila.requiereControl = !!cuenta.controlEntradaSalida;
    fila.controlConocido = true;
    fila.idTiposControl = null;
    fila.numeroDocumento = null;
    this.buscandoCuenta = null;
    this.cuentas = [];
  }

  /** Busca terceros por documento o nombre usando la API compartida. */
  buscarTercero(fila: FilaEdicion, texto: string): void {
    fila.terceroTexto = texto;
    fila.idDatosPersonal = 0;
    this.buscandoTercero = fila.idAuxiliarContable;
    this.terceros = [];
    if (texto.trim().length < 2) return;
    this.http.get<TerceroApi[]>(`${this.base}/shared/personas/buscar`, {
      params: new HttpParams().set('q', texto.trim())
    }).subscribe({
      next: lista => {
        if (this.buscandoTercero === fila.idAuxiliarContable) {
          this.terceros = lista;
        }
      },
      error: () => this.error = 'No se pudieron buscar los terceros.'
    });
  }

  /** Asigna el tercero seleccionado conservando su identificador real. */
  elegirTercero(fila: FilaEdicion, tercero: TerceroApi): void {
    fila.idDatosPersonal = tercero.idDatosPersonal;
    fila.terceroTexto = `${tercero.documento} - ${tercero.nombreCompleto}`;
    this.buscandoTercero = null;
    this.terceros = [];
  }

  /** Determina si un tipo de control exige número de documento. */
  pideDocumento(fila: FilaEdicion): boolean {
    return this.tiposControl.find(
      t => t.idTiposControl === fila.idTiposControl
    )?.pideDocumento ?? false;
  }

  /** Calcula los totales del comprobante antes de guardar. */
  get totalDebito(): number {
    return this.filas.reduce((n, f) => n + Number(f.valorDebito || 0), 0);
  }
  get totalCredito(): number {
    return this.filas.reduce((n, f) => n + Number(f.valorCredito || 0), 0);
  }
  get diferencia(): number {
    return Math.round((this.totalDebito - this.totalCredito) * 100) / 100;
  }

  /** Valida los movimientos y construye la solicitud de actualización. */
  private solicitudValida(): SolicitudEdicion | null {
    if (!this.concepto.trim()) {
      this.error = 'El concepto general es obligatorio.';
      return null;
    }
    if (this.filas.length < 2 || this.totalDebito <= 0 ||
        this.totalCredito <= 0 || this.diferencia !== 0) {
      this.error = 'El comprobante debe tener al menos dos líneas y estar cuadrado.';
      return null;
    }
    for (const [i, f] of this.filas.entries()) {
      const d = Number(f.valorDebito || 0);
      const c = Number(f.valorCredito || 0);
      const b = Number(f.valorBase || 0);
      if (!f.idCatalogoCuenta || !f.idDatosPersonal ||
          !f.detalleMovimiento.trim() || d < 0 || c < 0 || b < 0 ||
          (d > 0) === (c > 0)) {
        this.error = `Revise cuenta, tercero, detalle y valores de la línea ${i + 1}.`;
        return null;
      }
      if (f.requiereControl &&
          (!f.idTiposControl ||
           (this.pideDocumento(f) && !f.numeroDocumento?.trim()))) {
        this.error = `Complete el control documental de la línea ${i + 1}.`;
        return null;
      }
    }
    return {
      conceptoComprobante: this.concepto.trim(),
      movimientos: this.filas.map(f => ({
        idAuxiliarContable: f.idAuxiliarContable,
        idCatalogoCuenta: f.idCatalogoCuenta,
        idDatosPersonal: f.idDatosPersonal,
        detalleMovimiento: f.detalleMovimiento.trim(),
        valorDebito: Number(f.valorDebito || 0),
        valorCredito: Number(f.valorCredito || 0),
        valorBase: Number(f.valorBase || 0),
        idTiposControl: f.idTiposControl,
        numeroDocumento: f.numeroDocumento?.trim() || null
      }))
    };
  }

  /** Guarda los cambios y vuelve a consultar el comprobante confirmado. */
  guardar(): void {
    if (!this.comprobante || this.guardando) return;
    this.error = '';
    this.mensaje = '';
    const solicitud = this.solicitudValida();
    if (!solicitud) return;
    const c = this.comprobante.cabecera;
    this.guardando = true;
    this.edicion.editar(
      c.idAgencia,
      c.tipoComprobante,
      c.numeroComprobante,
      solicitud
    )
      .pipe(finalize(() => this.guardando = false))
      .subscribe({
        next: r => {
          this.mensaje = r.mensaje;
          this.consulta.consultar(
            c.idAgencia,
            c.tipoComprobante,
            c.numeroComprobante
          ).subscribe({
            next: actualizado => this.cargarConfirmado(actualizado),
            error: () => {
              this.error = 'Se guardó el comprobante, pero no se pudo actualizar la vista.';
            }
          });
        },
        error: err => {
          this.error = err.error?.message ||
            'No se pudo actualizar el comprobante. Verifique permisos y período contable.';
        }
      });
  }

  /** Refresca los datos sin perder el mensaje de confirmación. */
  private cargarConfirmado(datos: ComprobanteCompleto): void {
    const mensaje = this.mensaje;
    this.cargar(datos);
    this.mensaje = mensaje;
  }
}
