import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { SessionService } from '../../../../core/auth/session.service';
import { ComprobantesImportarService } from './comprobantes-importar.api';
import {
  ComprobanteImportacionAgencia,
  ComprobanteImportacionDatos,
  ComprobanteImportacionError,
  ComprobanteImportacionResponse,
  ComprobanteImportacionTipo
} from './comprobantes-importar.models';

/** Carga, valida, previsualiza y confirma un comprobante desde Excel. */
@Component({
  selector: 'app-comprobantes-importar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './comprobantes-importar.component.html',
  styleUrls: ['./comprobantes-importar.component.scss']
})
export class ComprobantesImportarComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly api = inject(ComprobantesImportarService);

  agencias: ComprobanteImportacionAgencia[] = [];
  tipos: ComprobanteImportacionTipo[] = [];
  idAgencia: number | null = null;
  tipoComprobante = '';
  fechaContable = this.fechaActual();
  conceptoComprobante = '';
  archivo: File | null = null;
  vistaPrevia: ComprobanteImportacionResponse | null = null;
  cargandoTipos = false;
  validando = false;
  confirmando = false;
  error = '';
  resultado = '';
  readonly moneda = new Intl.NumberFormat('es-CO', {
    style: 'currency', currency: 'COP', maximumFractionDigits: 2
  });

  /** Precarga las agencias de sesión y la agencia activa. */
  ngOnInit(): void {
    this.agencias = this.session.agenciasSig()
      .filter(a => a.idAgencia !== null)
      .map(a => ({
        idAgencia: a.idAgencia!,
        codigoAgencia: a.codigoAgencia ?? '',
        nombreAgencia: a.nombreAgencia ?? ''
      }));
    const activa = this.session.agenciaActivaSig()?.idAgencia;
    this.idAgencia = this.agencias.some(a => a.idAgencia === activa)
      ? activa! : this.agencias.length === 1 ? this.agencias[0].idAgencia : null;
    this.cambiarAgencia();
  }

  /** Invalida la vista previa y carga los tipos de la nueva agencia. */
  cambiarAgencia(): void {
    this.invalidarValidacion();
    this.tipos = [];
    this.tipoComprobante = '';
    const agencia = this.idAgencia;
    if (!agencia) return;
    this.cargandoTipos = true;
    this.api.listarTipos(agencia).pipe(finalize(() => this.cargandoTipos = false))
      .subscribe({
        next: tipos => {
          if (this.idAgencia !== agencia) return;
          this.tipos = tipos.filter(t => t.comprobanteActivo !== false);
        },
        error: () => this.error = 'No fue posible consultar los tipos de comprobante.'
      });
  }

  /** Acepta un único archivo Excel y elimina resultados anteriores. */
  seleccionarArchivo(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    this.invalidarValidacion();
    const archivo = input.files?.[0] ?? null;
    if (!archivo) { this.archivo = null; return; }
    if (!/\.(xlsx|xls)$/i.test(archivo.name)) {
      this.error = 'Seleccione un archivo Excel .xlsx o .xls.';
      this.archivo = null;
      input.value = '';
      return;
    }
    if (archivo.size > 10 * 1024 * 1024) {
      this.error = 'El archivo supera el límite de 10 MB.';
      this.archivo = null;
      input.value = '';
      return;
    }
    this.archivo = archivo;
  }

  /** Descarta la validación cuando cambian los datos de captura. */
  invalidarValidacion(): void {
    this.vistaPrevia = null;
    this.error = '';
    this.resultado = '';
  }

  /** Valida el archivo sin generar registros contables. */
  validar(): void {
    if (this.idAgencia === null || !this.archivo || this.validando || this.confirmando) return;
    this.invalidarValidacion();
    this.validando = true;
    this.api.validar(this.idAgencia, this.archivo)
      .pipe(finalize(() => this.validando = false))
      .subscribe({
        next: respuesta => this.vistaPrevia = respuesta,
        error: err => this.error = this.mensajeError(err, 'No fue posible validar el Excel.')
      });
  }

  /** Confirma exclusivamente una vista previa válida del mismo archivo y agencia. */
  confirmar(): void {
    if (!this.puedeConfirmar || this.confirmando || !this.archivo || this.idAgencia === null) return;
    const solicitud: ComprobanteImportacionDatos = {
      idAgencia: this.idAgencia,
      tipoComprobante: this.tipoComprobante,
      fechaContable: this.fechaContable,
      conceptoComprobante: this.conceptoComprobante.trim(),
      archivo: this.archivo
    };
    this.error = '';
    this.confirmando = true;
    this.api.confirmar(solicitud)
      .pipe(finalize(() => this.confirmando = false))
      .subscribe({
        next: numero => {
          this.resultado = numero;
          this.vistaPrevia = null;
          this.archivo = null;
        },
        error: err => this.error = this.mensajeError(err, 'No fue posible registrar el comprobante.')
      });
  }

  /** Impide confirmar si falta información o existen errores de validación. */
  get puedeConfirmar(): boolean {
    return !this.validando && !this.confirmando && !this.resultado
      && !!this.archivo && !!this.idAgencia && !!this.tipoComprobante
      && !!this.fechaContable && !!this.conceptoComprobante.trim()
      && this.vistaPrevia?.idAgencia === this.idAgencia
      && this.vistaPrevia?.nombreArchivo === this.archivo.name
      && this.vistaPrevia?.puedeGrabar === true;
  }

  /** Muestra los errores de una fila sin ocultar los generales. */
  erroresFila(numeroFila: number): ComprobanteImportacionError[] {
    return this.vistaPrevia?.errores.filter(e => e.numeroFila === numeroFila) ?? [];
  }

  /** Traduce respuestas HTTP conocidas a mensajes legibles. */
  private mensajeError(error: unknown, predeterminado: string): string {
    const respuesta = error as { error?: unknown; message?: string };
    if (typeof respuesta?.error === 'string' && respuesta.error.trim()) {
      return respuesta.error;
    }
    if (respuesta?.error && typeof respuesta.error === 'object') {
      const detalle = respuesta.error as { message?: string; mensaje?: string };
      return detalle.message || detalle.mensaje || predeterminado;
    }
    return predeterminado;
  }

  /** Calcula la fecha local para evitar desfases por zona horaria. */
  private fechaActual(): string {
    const hoy = new Date();
    const anio = hoy.getFullYear();
    const mes = String(hoy.getMonth() + 1).padStart(2, '0');
    const dia = String(hoy.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }
}
