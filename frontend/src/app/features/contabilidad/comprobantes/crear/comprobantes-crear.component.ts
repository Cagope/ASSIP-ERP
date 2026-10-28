import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { SessionService } from '../../../../core/auth/session.service';
import { environment } from '../../../../../environments/environment';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { NumericFormatDirective } from '../../../../shared/utils/numeric-format.directive';

import { ComprobantesCrearService } from './comprobantes-crear.service';

import {
  ComprobanteAgenciaOpcion,
  ComprobanteCrearFormulario,
  ComprobanteCrearRequest,
  ComprobanteCuentaOpcion,
  ComprobanteMovimientoFormulario,
  ComprobanteTerceroOpcion,
  ComprobanteTipoControlOpcion,
  ComprobanteTipoOpcion,
  ComprobanteTotales,
  crearMovimientoVacio
} from './comprobantes-crear.models';

/**
 * Pantalla de creación manual de comprobantes contables.
 *
 * Administra los movimientos, los controles documentales,
 * el cuadre y el envío al backend.
 */
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

interface TipoComprobanteApi {
  tipoComprobante: string;
  nombreTipoComprobante: string;
  comprobanteActivo: boolean;
}

/** Captura manual y consultas de apoyo al comprobante. */
@Component({
  selector: 'app-comprobantes-crear',
  standalone: true,
  imports: [CommonModule, FormsModule, NumericFormatDirective],
  templateUrl: './comprobantes-crear.component.html',
  styleUrls: ['./comprobantes-crear.component.scss']
})
export class ComprobantesCrearComponent implements OnInit {

  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);
  private readonly apiUrl = environment.apiUrl;

  buscandoCuentas = false;
  buscandoTerceros = false;
  cargandoTipos = false;
  cargandoTiposControl = false;

  private readonly comprobantesService =
    inject(ComprobantesCrearService);

  agencias: ComprobanteAgenciaOpcion[] = [];
  tiposComprobante: ComprobanteTipoOpcion[] = [];
  tiposControl: ComprobanteTipoControlOpcion[] = [];

  cuentas: ComprobanteCuentaOpcion[] = [];
  terceros: ComprobanteTerceroOpcion[] = [];

  filaBusquedaCuenta: number | null = null;
  filaBusquedaTercero: number | null = null;

  guardando = false;
  error = '';
  mensaje = '';
  numeroComprobanteCreado = '';

  private siguienteIdFila = 3;

  /** Inicializa agencias autorizadas y carga tipos de la agencia activa. */
  ngOnInit(): void {
    this.agencias = this.session.agenciasSig()
      .filter(a => a.idAgencia !== null)
      .map(a => ({
        idAgencia: a.idAgencia!,
        codigoAgencia: a.codigoAgencia ?? '',
        nombreAgencia: a.nombreAgencia ?? ''
      }));

    const activa = this.session.agenciaActivaSig()?.idAgencia;
    this.formulario.idAgencia = this.agencias.some(a => a.idAgencia === activa)
      ? activa!
      : this.agencias.length === 1 ? this.agencias[0].idAgencia : null;

    this.cargarTiposComprobante();
    this.cargarTiposControl();
  }

  /** Consulta el catálogo de controles documentales desde PostgreSQL. */
  cargarTiposControl(): void {
    this.tiposControl = [];
    this.cargandoTiposControl = true;

    this.comprobantesService.listarTiposControl()
      .pipe(finalize(() => this.cargandoTiposControl = false))
      .subscribe({
        next: tipos => {
          this.tiposControl = tipos;
          for (const fila of this.formulario.movimientos) {
            if (fila.idTiposControl !== null) {
              const tipo = tipos.find(t => t.idTiposControl === fila.idTiposControl);
              if (!tipo) {
                fila.idTiposControl = null;
                fila.documentoSoporte = '';
              }
              fila.requiereDocumentoSoporte = tipo?.pideDocumento ?? false;
            }
          }
        },
        error: () => {
          this.error = 'No se pudieron consultar los tipos de control documental.';
        }
      });
  }

  /** Consulta tipos activos para la agencia seleccionada. */
  cargarTiposComprobante(): void {
    this.tiposComprobante = [];
    this.formulario.tipoComprobante = '';
    const idAgencia = this.formulario.idAgencia;
    if (!idAgencia) return;

    this.cargandoTipos = true;
    this.http.get<TipoComprobanteApi[]>(
      `${this.apiUrl}/contabilidad/tipos-comprobantes/por-agencia`,
      { params: new HttpParams().set('idAgencia', idAgencia) }
    ).pipe(finalize(() => this.cargandoTipos = false))
      .subscribe({
        next: tipos => {
          if (this.formulario.idAgencia !== idAgencia) return;
          this.tiposComprobante = tipos
            .filter(t => t.comprobanteActivo !== false)
            .map(t => ({
              tipoComprobante: t.tipoComprobante,
              nombreTipo: t.nombreTipoComprobante
            }));
        },
        error: () => this.error = 'No se pudieron consultar los tipos de comprobante.'
      });
  }

  /** Busca cuentas operables por código o nombre, dentro de la agencia. */
  buscarCuentas(texto: string): void {
    const idAgencia = this.formulario.idAgencia;
    const q = texto.trim();
    this.cuentas = [];
    if (!idAgencia || q.length < 2) return;

    this.buscandoCuentas = true;
    this.http.get<CuentaApi[]>(
      `${this.apiUrl}/contabilidad/plan-cuentas/buscar`,
      { params: new HttpParams().set('idAgencia', idAgencia).set('q', q) }
    ).pipe(finalize(() => this.buscandoCuentas = false))
      .subscribe({
        next: cuentas => {
          if (this.formulario.idAgencia !== idAgencia) return;
          this.cuentas = cuentas.filter(c => c.operable).map(c => ({
            idCatalogoCuenta: c.id,
            idAgencia: c.idAgencia,
            codigoCuenta: c.codigoCuenta,
            nombreCuenta: c.nombre,
            operable: c.operable,
            controlEntradaSalida: !!c.controlEntradaSalida
          }));
        },
        error: () => this.error = 'No se pudieron buscar las cuentas contables.'
      });
  }

  /** Busca terceros por documento o nombre, reutilizando la API compartida. */
  buscarTerceros(texto: string): void {
    const q = texto.trim();
    this.terceros = [];
    if (q.length < 2) return;

    this.buscandoTerceros = true;
    this.http.get<TerceroApi[]>(
      `${this.apiUrl}/shared/personas/buscar`,
      { params: new HttpParams().set('q', q) }
    ).pipe(finalize(() => this.buscandoTerceros = false))
      .subscribe({
        next: terceros => {
          this.terceros = terceros.map(t => ({
            idDatosPersonal: t.idDatosPersonal,
            documento: t.documento,
            nombreCompleto: t.nombreCompleto
          }));
        },
        error: () => this.error = 'No se pudieron buscar los terceros.'
      });
  }


  formulario: ComprobanteCrearFormulario = {
    idAgencia: null,
    tipoComprobante: '',
    fechaContable: this.fechaActual(),
    conceptoComprobante: '',
    movimientos: [
      crearMovimientoVacio(1),
      crearMovimientoVacio(2)
    ]
  };

  /**
   * Calcula los totales sin incluir la base tributaria.
   */
  get totales(): ComprobanteTotales {
    const totalDebito = this.formulario.movimientos.reduce(
      (total, fila) => total + (Number(fila.valorDebito) || 0),
      0
    );

    const totalCredito = this.formulario.movimientos.reduce(
      (total, fila) => total + (Number(fila.valorCredito) || 0),
      0
    );

    const diferencia = Math.round(
      (totalDebito - totalCredito) * 100
    ) / 100;

    return {
      totalDebito,
      totalCredito,
      diferencia,
      cuadrado: totalDebito > 0
        && totalCredito > 0
        && diferencia === 0
    };
  }

  /**
   * Agrega una línea independiente al comprobante.
   */
  agregarMovimiento(): void {
    this.formulario.movimientos.push(
      crearMovimientoVacio(this.siguienteIdFila++)
    );
  }

  /**
   * Elimina una línea conservando al menos dos movimientos.
   */
  eliminarMovimiento(idFila: number): void {
    if (this.formulario.movimientos.length <= 2) {
      this.error = 'El comprobante requiere al menos dos movimientos.';
      return;
    }

    this.formulario.movimientos =
      this.formulario.movimientos.filter(
        fila => fila.idFila !== idFila
      );

    this.error = '';
  }

  /**
   * Asigna una cuenta y restablece sus controles documentales.
   * Rechaza cuentas no operables o de otra agencia.
   */
  seleccionarCuenta(
    fila: ComprobanteMovimientoFormulario,
    cuenta: ComprobanteCuentaOpcion
  ): void {
    if (!cuenta) return;
    if (!cuenta.operable
        || cuenta.idAgencia !== this.formulario.idAgencia) {
      this.error = 'La cuenta no está disponible para esta agencia.';
      return;
    }

    fila.idCatalogoCuenta = cuenta.idCatalogoCuenta;
    fila.codigoCuenta = cuenta.codigoCuenta;
    fila.nombreCuenta = cuenta.nombreCuenta;

    fila.requiereControlEntradaSalida =
      cuenta.controlEntradaSalida;

    fila.idTiposControl = null;
    fila.documentoSoporte = '';
    fila.requiereDocumentoSoporte = false;

    this.error = '';
  }

  /**
   * Asigna el tercero seleccionado a una línea contable.
   */
  seleccionarTercero(
    fila: ComprobanteMovimientoFormulario,
    tercero: ComprobanteTerceroOpcion
  ): void {
    if (!tercero) return;
    fila.idDatosPersonal = tercero.idDatosPersonal;
    fila.documentoTercero = tercero.documento;
    fila.nombreTercero = tercero.nombreCompleto;
  }

  /**
   * Actualiza la obligatoriedad documental según el tipo
   * de control seleccionado.
   */
  cambiarTipoControl(
    fila: ComprobanteMovimientoFormulario
  ): void {
    const tipo = this.tiposControl.find(
      opcion => opcion.idTiposControl === fila.idTiposControl
    );

    fila.requiereDocumentoSoporte =
      tipo?.pideDocumento ?? false;

    fila.documentoSoporte = '';
  }

  /**
   * Limpia las cuentas al cambiar de agencia para evitar
   * registrar movimientos con cuentas de otra oficina.
   */
  cambiarAgencia(): void {
    for (const fila of this.formulario.movimientos) {
      fila.idCatalogoCuenta = null;
      fila.codigoCuenta = '';
      fila.nombreCuenta = '';
      fila.idTiposControl = null;
      fila.documentoSoporte = '';
      fila.requiereControlEntradaSalida = false;
      fila.requiereDocumentoSoporte = false;
    }

    this.cuentas = [];
    this.cargarTiposComprobante();
    this.error = '';
    this.mensaje = '';
  }

  /**
   * Valida el formulario antes de solicitar la contabilización.
   */
  private validarFormulario(): string | null {
    if (!this.formulario.idAgencia) {
      return 'Debe seleccionar la agencia.';
    }

    if (!this.formulario.tipoComprobante) {
      return 'Debe seleccionar el tipo de comprobante.';
    }

    if (!this.formulario.fechaContable) {
      return 'Debe indicar la fecha contable.';
    }

    if (!this.formulario.conceptoComprobante.trim()) {
      return 'Debe indicar el concepto general.';
    }

    if (this.formulario.movimientos.length < 2) {
      return 'Debe registrar al menos dos movimientos.';
    }

    for (const [indice, fila] of
      this.formulario.movimientos.entries()) {

      const linea = indice + 1;

      if (!fila.idCatalogoCuenta) {
        return `La línea ${linea} requiere cuenta contable.`;
      }

      if (!fila.idDatosPersonal) {
        return `La línea ${linea} requiere tercero.`;
      }

      if (!fila.detalleMovimiento.trim()) {
        return `La línea ${linea} requiere detalle.`;
      }

      const debito = Number(fila.valorDebito);
      const credito = Number(fila.valorCredito);
      const base = Number(fila.valorBase);

      if (![debito, credito, base].every(Number.isFinite)
          || debito < 0 || credito < 0 || base < 0) {
        return `La línea ${linea} contiene valores inválidos.`;
      }

      if ((debito > 0 && credito > 0)
          || (debito === 0 && credito === 0)) {
        return `La línea ${linea} debe tener débito o crédito, no ambos.`;
      }

      if (fila.requiereControlEntradaSalida) {
        if (!fila.idTiposControl) {
          return `La línea ${linea} requiere tipo de control.`;
        }

        const tipo = this.tiposControl.find(
          opcion => opcion.idTiposControl === fila.idTiposControl
        );

        if (!tipo) {
          return `La línea ${linea} tiene un tipo de control inválido.`;
        }

        if (tipo.pideDocumento
            && !fila.documentoSoporte.trim()) {
          return `La línea ${linea} requiere documento soporte.`;
        }

        if (fila.documentoSoporte.trim().length > 20) {
          return `El documento de la línea ${linea} supera 20 caracteres.`;
        }
      } else if (fila.idTiposControl !== null
          || fila.documentoSoporte.trim()) {
        return `La línea ${linea} no admite control documental.`;
      }
    }

    if (!this.totales.cuadrado) {
      return 'El comprobante debe estar cuadrado y tener valores positivos.';
    }

    return null;
  }

  /**
   * Determina si el comprobante cumple las validaciones
   * necesarias para habilitar su registro.
   */
  get formularioValido(): boolean {
    return this.validarFormulario() === null;
  }


  /**
   * Construye el contrato HTTP sin campos exclusivos
   * de presentación.
   */
  private construirSolicitud(): ComprobanteCrearRequest {
    return {
      idAgencia: this.formulario.idAgencia!,
      tipoComprobante: this.formulario.tipoComprobante,
      fechaContable: this.formulario.fechaContable,
      conceptoComprobante: this.formulario.conceptoComprobante.trim(),
      movimientos: this.formulario.movimientos.map(fila => ({
        idCatalogoCuenta: fila.idCatalogoCuenta!,
        idDatosPersonal: fila.idDatosPersonal!,
        detalleMovimiento: fila.detalleMovimiento.trim(),
        valorDebito: Number(fila.valorDebito),
        valorCredito: Number(fila.valorCredito),
        valorBase: Number(fila.valorBase),
        idTiposControl: fila.requiereControlEntradaSalida
          ? fila.idTiposControl
          : null,
        documentoSoporte: fila.requiereControlEntradaSalida
          ? (fila.documentoSoporte.trim() || null)
          : null
      }))
    };
  }

  /**
   * Envía el comprobante una sola vez y presenta el
   * consecutivo devuelto por el backend.
   */
  guardar(): void {
    if (this.guardando) {
      return;
    }

    this.error = '';
    this.mensaje = '';

    const errorValidacion = this.validarFormulario();

    if (errorValidacion) {
      this.error = errorValidacion;
      return;
    }

    this.guardando = true;

    this.comprobantesService.crear(
      this.construirSolicitud()
    )
    .pipe(finalize(() => this.guardando = false))
    .subscribe({
      next: respuesta => {
        this.numeroComprobanteCreado =
          respuesta.numeroComprobante;

        this.mensaje = respuesta.mensaje
          || 'Comprobante creado correctamente.';

        this.formulario.movimientos = [
          crearMovimientoVacio(this.siguienteIdFila++),
          crearMovimientoVacio(this.siguienteIdFila++)
        ];

        this.formulario.conceptoComprobante = '';
      },
      error: () => {
        this.error =
          'No fue posible registrar el comprobante. '
          + 'Verifique los datos e intente nuevamente.';
      }
    });
  }

  /**
   * Obtiene la fecha local para iniciar la captura.
   * El backend valida el período contable definitivo.
   */
  private fechaActual(): string {
    const fecha = new Date();

    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');

    return `${anio}-${mes}-${dia}`;
  }

  /**
   * Cancela la captura actual y reinicia el formulario.
   * Conserva la agencia y la fecha contable, sin registrar movimientos.
   */
  cancelarComprobante(): void {
    if (this.guardando) {
      return;
    }

    this.formulario.conceptoComprobante = '';
    this.formulario.tipoComprobante = '';

    this.formulario.movimientos = [
      crearMovimientoVacio(this.siguienteIdFila++),
      crearMovimientoVacio(this.siguienteIdFila++)
    ];

    this.cuentas = [];
    this.terceros = [];

    this.error = '';
    this.mensaje = '';
    this.numeroComprobanteCreado = '';
  }


  /**
   * Activa la búsqueda de cuentas para una sola fila.
   */
  activarBusquedaCuenta(idFila: number, texto: string): void {
    this.filaBusquedaCuenta = idFila;
    this.filaBusquedaTercero = null;
    this.terceros = [];
    this.buscarCuentas(texto);
  }

  /**
   * Activa la búsqueda de terceros exclusivamente
   * para la fila que está siendo editada.
   */
  activarBusquedaTercero(idFila: number, texto: string): void {
    this.filaBusquedaTercero = idFila;
    this.filaBusquedaCuenta = null;

    this.cuentas = [];
    this.terceros = [];

    this.buscarTerceros(texto);
  }

}
