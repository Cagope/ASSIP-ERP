import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { SessionService } from '../../../../../core/auth/session.service';
import { BalanceComparadoMensualApi } from './balance-comparado-mensual.api';
import { BalanceComparadoMensualFiltro, BalanceComparadoMensualRespuesta } from './balance-comparado-mensual.models';

interface AgenciaOpcion { idAgencia: number; codigoAgencia: string; nombreAgencia: string; }

/** Compara exclusivamente saldos de dos meses elegidos libremente. */
@Component({
  selector: 'app-balance-comparado-mensual',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './balance-comparado-mensual.component.html',
  styleUrl: './balance-comparado-mensual.component.scss'
})
export class BalanceComparadoMensualComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly api = inject(BalanceComparadoMensualApi);
  readonly meses = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
  readonly niveles = [1, 2, 3, 4, 5, 6];
  readonly formato = new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  readonly porcentajeFormato = new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  agencias: AgenciaOpcion[] = [];
  filtro: BalanceComparadoMensualFiltro = {
    idAgencia: 0,
    anoPeriodo1: new Date().getFullYear() - 1,
    mesPeriodo1: new Date().getMonth() + 1,
    anoPeriodo2: new Date().getFullYear(),
    mesPeriodo2: new Date().getMonth() + 1,
    nivel: 6,
    soloBalance: false,
    incluirFirmas: true
  };
  informe: BalanceComparadoMensualRespuesta | null = null;
  cargando = false;
  exportando: 'pdf' | 'excel' | null = null;
  mensajeError = '';
  private version = 0;

  get puedeConsultar(): boolean { return this.session.tienePermiso('CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_VIEW'); }
  get puedeConsolidar(): boolean { return this.session.tienePermiso('CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_CONSOLIDADO_VIEW'); }

  /** Restringe las agencias a la sesión y el consolidado al permiso específico. */
  ngOnInit(): void {
    this.agencias = this.session.agenciasSig()
      .filter(a => a.idAgencia !== null && a.idAgencia !== 1)
      .map(a => ({ idAgencia: a.idAgencia!, codigoAgencia: a.codigoAgencia ?? '', nombreAgencia: a.nombreAgencia ?? '' }));
    if (this.puedeConsultar && this.puedeConsolidar) {
      this.agencias.unshift({ idAgencia: 1, codigoAgencia: '00', nombreAgencia: 'CONSOLIDADO INSTITUCIONAL' });
    }
    const activa = this.session.agenciaActivaSig()?.idAgencia;
    this.filtro.idAgencia = this.agencias.find(a => a.idAgencia === activa)?.idAgencia
      ?? (this.agencias.length === 1 ? this.agencias[0].idAgencia : 0);
  }

  /** Invalida los resultados y evita exportar períodos diferentes a los consultados. */
  cambiarFiltros(): void {
    this.version++;
    this.informe = null;
    this.mensajeError = '';
  }

  /** Valida ambos períodos, agencia y nivel antes de consultar o exportar. */
  private valido(): boolean {
    if (!this.puedeConsultar || !this.agencias.some(a => a.idAgencia === this.filtro.idAgencia)) {
      this.mensajeError = 'No tiene autorización para la agencia seleccionada.';
      return false;
    }
    const actual = new Date().getFullYear();
    const anos = [this.filtro.anoPeriodo1, this.filtro.anoPeriodo2];
    const meses = [this.filtro.mesPeriodo1, this.filtro.mesPeriodo2];
    if (anos.some(a => !Number.isInteger(a) || a < 1900 || a > actual)
        || meses.some(m => !Number.isInteger(m) || m < 1 || m > 12)
        || !this.niveles.includes(this.filtro.nivel)) {
      this.mensajeError = 'Revise los años, meses y nivel contable.';
      return false;
    }
    return true;
  }

  /** Obtiene los saldos de los dos períodos desde el backend. */
  consultar(): void {
    if (this.cargando || this.exportando) return;
    this.mensajeError = '';
    if (!this.valido()) return;
    const version = ++this.version;
    this.informe = null;
    this.cargando = true;
    this.api.consultar({ ...this.filtro }).pipe(finalize(() => this.cargando = false)).subscribe({
      next: respuesta => { if (version === this.version) this.informe = respuesta; },
      error: err => { if (version === this.version) this.mensajeError = this.error(err, 'No se pudo consultar el balance.'); }
    });
  }

  /** Descarga el mismo balance consultado, identificando ambos períodos. */
  exportar(tipo: 'pdf' | 'excel'): void {
    if (!this.informe || this.cargando || this.exportando || !this.valido()) return;
    const filtro = { ...this.filtro };
    const codigoAgencia = this.informe.codigoAgencia;
    this.exportando = tipo;
    this.mensajeError = '';
    this.api.descargar(filtro, tipo).pipe(finalize(() => this.exportando = null)).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `balance_comparado_mensual_${codigoAgencia}_${filtro.anoPeriodo1}-${String(filtro.mesPeriodo1).padStart(2, '0')}_${filtro.anoPeriodo2}-${String(filtro.mesPeriodo2).padStart(2, '0')}.${tipo === 'pdf' ? 'pdf' : 'xlsx'}`;
        document.body.appendChild(enlace);
        enlace.click();
        enlace.remove();
        URL.revokeObjectURL(url);
      },
      error: err => { this.mensajeError = this.error(err, 'No se pudo exportar el informe.'); }
    });
  }

  /** Formatea valores monetarios y porcentajes sin alterar los datos del backend. */
  dinero(valor: number | null | undefined): string { return valor == null ? '—' : this.formato.format(valor); }
  variacion(valor: number | null | undefined): string { return valor == null ? 'N/A' : `${this.porcentajeFormato.format(valor)} %`; }

  private error(err: { status?: number; error?: { message?: string; mensaje?: string } }, defecto: string): string {
    if (err.status === 403) return 'No tiene permisos para consultar este balance o agencia.';
    return err.error?.message || err.error?.mensaje || defecto;
  }
}
