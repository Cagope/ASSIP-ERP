import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin, finalize } from 'rxjs';
import { SessionService } from '../../../../core/auth/session.service';
import { CierreMensualApi } from './cierre-mensual.api';
import { CierreMensualCuenta, CierreMensualResumen } from './cierre-mensual.models';

interface AgenciaOpcion { idAgencia: number; codigoAgencia: string; nombreAgencia: string; }
/** Presenta la consolidación mensual y confirma el cierre autorizado. */
@Component({
  selector: 'app-cierre-mensual', standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cierre-mensual.component.html',
  styleUrl: './cierre-mensual.component.scss'
})
export class CierreMensualComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly api = inject(CierreMensualApi);
  readonly meses = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];
  readonly moneda = new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  agencias: AgenciaOpcion[] = [];
  idAgencia = 0;
  ano = new Date().getFullYear();
  mes = new Date().getMonth() + 1;
  resumen: CierreMensualResumen | null = null;
  cuentas: CierreMensualCuenta[] = [];
  mostrarDetalle = false;
  mostrarReapertura = false;
  justificacionReapertura = "";
  cargando = false;
  mensajeError = '';
  mensajeExito = '';
  private version = 0;
  ngOnInit(): void {
    this.agencias = this.session.agenciasSig().filter(a => a.idAgencia !== null).map(a => ({
      idAgencia: a.idAgencia!, codigoAgencia: a.codigoAgencia ?? '', nombreAgencia: a.nombreAgencia ?? ''
    }));
    const activa = this.session.agenciaActivaSig()?.idAgencia;
    this.idAgencia = this.agencias.find(a => a.idAgencia === activa)?.idAgencia
      ?? (this.agencias.length === 1 ? this.agencias[0].idAgencia : 0);
    if (this.idAgencia) this.consultar();
  }
  /** Invalida resultados cuando cambia el período o la agencia. */
  cambiarFiltros(): void {
    this.version++;
    this.resumen = null;
    this.cuentas = [];
    this.mostrarDetalle = false;
    this.mostrarReapertura = false;
    this.justificacionReapertura = "";
    this.mensajeError = '';
    this.mensajeExito = '';
  }
  private filtrosValidos(): boolean {
    if (!this.agencias.some(a => a.idAgencia === this.idAgencia)) {
      this.mensajeError = 'Seleccione una agencia autorizada.'; return false;
    }
    if (!Number.isInteger(this.ano) || this.ano < 1900 || this.ano > 9999 ||
        !Number.isInteger(this.mes) || this.mes < 1 || this.mes > 12) {
      this.mensajeError = 'Seleccione un período válido.'; return false;
    }
    return true;
  }
  /** Consulta resumen y detalle del mismo período. */
  consultar(conservarExito = false): void {
    if (this.cargando) return;
    this.mensajeError = '';
    if (!conservarExito) this.mensajeExito = '';
    if (!this.filtrosValidos()) return;
    const id = this.idAgencia, ano = this.ano, mes = this.mes, version = ++this.version;
    this.resumen = null;
    this.cuentas = [];
    this.mostrarDetalle = false;
    this.cargando = true;
    forkJoin({ resumen: this.api.consultarResumen(id, ano, mes), cuentas: this.api.consultarDetalle(id, ano, mes) })
      .pipe(finalize(() => this.cargando = false)).subscribe({
        next: data => {
          if (version !== this.version || id !== this.idAgencia || ano !== this.ano || mes !== this.mes) return;
          this.resumen = data.resumen;
          this.cuentas = data.cuentas;
        },
        error: error => { if (version === this.version) this.mostrarError(error, 'No fue posible consultar el cierre mensual.'); }
      });
  }
  /** Separa la advertencia general del listado extenso de cuentas. */
  get validacionPrincipal(): string {
    const mensaje = this.resumen?.mensajeValidacion ?? '';
    const indice = mensaje.indexOf('Faltan saldos anteriores para cuentas existentes:');
    return indice >= 0 ? mensaje.substring(0, indice).trim() : mensaje;
  }

  get cuentasSinSaldoAnterior(): string[] {
    const mensaje = this.resumen?.mensajeValidacion ?? '';
    const etiqueta = 'Faltan saldos anteriores para cuentas existentes:';
    const indice = mensaje.indexOf(etiqueta);
    if (indice < 0) return [];
    return mensaje.substring(indice + etiqueta.length).trim()
      .replace(/\.\s*$/, '')
      .split(/,\s*/)
      .map(cuenta => cuenta.trim())
      .filter(Boolean);
  }

  /** Oculta acumulados no confiables si falta el cierre anterior. */
  get acumuladosDisponibles(): boolean {
    const r = this.resumen;
    if (!r) return false;
    if (r.estadoPeriodo === 'CERRADO') return true;
    const mensaje = r.mensajeValidacion ?? '';
    return !mensaje.includes('no está cerrado')
      && !mensaje.includes('No existen saldos consolidados')
      && !mensaje.includes('No existe consolidación anual');
  }

  /** Calcula indicadores por clase contable, únicamente sobre cuentas operables. */
  get indicadoresFinancieros(): { nombre: string; mes: number; acumulado: number }[] {
    const totales = new Map<string, { mes: number; acumulado: number }>();
    for (const cuenta of this.cuentas) {
      if (!cuenta.cuentaOperable) continue;
      const clase = cuenta.codigoCuenta?.trim().charAt(0);
      if (!clase || !'1234567'.includes(clase)) continue;
      const signo = clase === '1' || '567'.includes(clase) ? 1 : -1;
      const actual = totales.get(clase) ?? { mes: 0, acumulado: 0 };
      actual.mes += signo * (cuenta.debitoMes - cuenta.creditoMes);
      actual.acumulado += signo * (cuenta.debitoAcumulado - cuenta.creditoAcumulado);
      totales.set(clase, actual);
    }
    const valor = (clase: string, tipo: 'mes' | 'acumulado') => totales.get(clase)?.[tipo] ?? 0;
    const gastos = (tipo: 'mes' | 'acumulado') => ['5', '6', '7'].reduce((total, clase) => total + valor(clase, tipo), 0);
    return [
      { nombre: 'Activo', mes: valor('1', 'mes'), acumulado: valor('1', 'acumulado') },
      { nombre: 'Pasivo', mes: valor('2', 'mes'), acumulado: valor('2', 'acumulado') },
      { nombre: 'Patrimonio', mes: valor('3', 'mes'), acumulado: valor('3', 'acumulado') },
      { nombre: 'Ingresos', mes: valor('4', 'mes'), acumulado: valor('4', 'acumulado') },
      { nombre: 'Costos y gastos', mes: gastos('mes'), acumulado: gastos('acumulado') },
      { nombre: 'Ganancias o pérdidas', mes: valor('4', 'mes') - gastos('mes'), acumulado: valor('4', 'acumulado') - gastos('acumulado') }
    ];
  }

  /** Permite iniciar la reapertura solo de un período cerrado y autorizado. */
  get sessionTienePermisoReabrir(): boolean {
    return this.session.tienePermiso('CONTABILIDAD_PERIODOS_REOPEN');
  }
  get puedeReabrir(): boolean {
    return !this.cargando && !!this.resumen && this.resumen.estadoPeriodo === 'CERRADO' &&
      this.resumen.idAgencia === this.idAgencia && this.resumen.ano === this.ano &&
      this.resumen.mes === this.mes && this.session.tienePermiso('CONTABILIDAD_PERIODOS_REOPEN');
  }
  /** Registra la reapertura justificada; el backend valida secuencia y cierre anual. */
  reabrir(): void {
    if (!this.puedeReabrir || !this.mostrarReapertura) return;
    const justificacion = this.justificacionReapertura.trim();
    if (!justificacion) {
      this.mensajeError = 'La observación de reapertura es obligatoria.';
      return;
    }
    const id = this.idAgencia, ano = this.ano, mes = this.mes;
    if (!window.confirm(`¿Confirma la reapertura de ${this.meses[mes - 1]} de ${ano}? Se retirará la consolidación vigente y quedará registrada su observación en el historial.`)) return;
    this.cargando = true;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.api.reabrir(id, ano, mes, justificacion).pipe(finalize(() => this.cargando = false)).subscribe({
      next: () => {
        this.mostrarReapertura = false;
        this.justificacionReapertura = '';
        this.mensajeExito = 'El período se reabrió correctamente y la observación quedó registrada en el historial.';
        this.cargando = false;
        this.consultar(true);
      },
      error: error => this.mostrarError(error, 'No fue posible reabrir el período mensual.')
    });
  }
  get puedeCerrar(): boolean {
    return !this.cargando && !!this.resumen && this.resumen.idAgencia === this.idAgencia &&
      this.resumen.ano === this.ano && this.resumen.mes === this.mes &&
      this.resumen.puedeCerrar && this.session.tienePermiso('CONTABILIDAD_PERIODOS_ADMIN');
  }
  /** Solicita confirmación y actualiza el resultado después del cierre. */
  cerrar(): void {
    if (!this.puedeCerrar) return;
    const id = this.idAgencia, ano = this.ano, mes = this.mes;
    const nombre = this.agencias.find(a => a.idAgencia === id)?.nombreAgencia ?? '';
    if (!window.confirm(`¿Confirma el cierre mensual de ${this.meses[mes - 1]} de ${ano} para ${nombre}?`)) return;
    this.cargando = true;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.api.cerrar(id, ano, mes).pipe(finalize(() => this.cargando = false)).subscribe({
      next: () => { this.mensajeExito = 'El cierre mensual se registró correctamente.'; this.cargando = false; this.consultar(true); },
      error: error => { this.resumen = null; this.cuentas = []; this.mostrarError(error, 'No fue posible confirmar el cierre mensual.'); }
    });
  }
  formato(valor: number | null | undefined): string { return this.moneda.format(valor ?? 0); }
  private mostrarError(error: any, alternativo: string): void {
    this.mensajeError = error?.error?.message ?? error?.error?.detail ?? alternativo;
  }
}
