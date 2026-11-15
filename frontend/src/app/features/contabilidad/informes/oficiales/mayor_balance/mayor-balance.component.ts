import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { SessionService } from '../../../../../core/auth/session.service';
import { MayorBalanceApi } from './mayor-balance.api';
import { MayorBalanceFiltro, MayorBalanceFormato, MayorBalanceResultado } from './mayor-balance.models';

/** Libro Mayor y Balance nivel 3; consulta mensual o anual y exporta PDF/Excel. */
@Component({
  selector: 'app-mayor-balance', standalone: true, imports: [CommonModule, FormsModule],
  templateUrl: './mayor-balance.component.html', styleUrls: ['./mayor-balance.component.scss']
})
export class MayorBalanceComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly api = inject(MayorBalanceApi);
  readonly meses = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];
  readonly formatoNumero = new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  agencias: { idAgencia: number; codigoAgencia: string; nombreAgencia: string }[] = [];
  idAgencia: number | null = null;
  ano = new Date().getFullYear();
  mes = new Date().getMonth() + 1;
  acumuladoAnual = false;
  formato: MayorBalanceFormato = 'INTERNO';
  consultando = false;
  exportando: 'pdf' | 'excel' | null = null;
  error = '';
  resultado: MayorBalanceResultado | null = null;
  private filtroConsultado: MayorBalanceFiltro | null = null;
  private version = 0;
  ngOnInit(): void {
    this.agencias = this.session.agenciasSig().filter(a => a.idAgencia !== null && a.idAgencia !== 1)
      .map(a => ({ idAgencia: a.idAgencia!, codigoAgencia: a.codigoAgencia ?? '', nombreAgencia: a.nombreAgencia ?? '' }));
    const activa = this.session.agenciaActivaSig()?.idAgencia;
    this.idAgencia = this.agencias.find(a => a.idAgencia === activa)?.idAgencia
      ?? (this.agencias.length === 1 ? this.agencias[0].idAgencia : null);
  }
  get tienePermiso(): boolean { return this.session.tienePermiso('CONTABILIDAD_VIEW'); }
  get puedeConsultar(): boolean {
    return this.tienePermiso && !this.consultando && !this.exportando && this.idAgencia !== null
      && this.agencias.some(a => a.idAgencia === this.idAgencia)
      && Number.isInteger(this.ano) && this.ano >= 1901 && this.ano <= 9998
      && Number.isInteger(this.mes) && this.mes >= 1 && this.mes <= 12;
  }
  private filtro(): MayorBalanceFiltro {
    return { idAgencia: this.idAgencia!, ano: this.ano, mes: this.mes, acumuladoAnual: this.acumuladoAnual };
  }
  get puedeExportar(): boolean {
    return !this.consultando && !this.exportando && this.resultado !== null
      && this.filtroConsultado !== null && JSON.stringify(this.filtro()) === JSON.stringify(this.filtroConsultado);
  }
  limpiar(): void { ++this.version; this.resultado = null; this.filtroConsultado = null; this.error = ''; }
  consultar(): void {
    if (!this.puedeConsultar) return;
    const filtro = this.filtro(); const version = ++this.version;
    this.consultando = true; this.error = ''; this.resultado = null;
    this.api.consultar(filtro).pipe(finalize(() => { if (version === this.version) this.consultando = false; }))
      .subscribe({ next: r => { if (version === this.version) { this.resultado = r; this.filtroConsultado = filtro; } },
        error: e => { if (version === this.version) this.error = this.mensaje(e); } });
  }
  exportar(tipo: 'pdf' | 'excel'): void {
    if (!this.puedeExportar) return;
    const filtro = this.filtro(); this.exportando = tipo; this.error = '';
    const req = tipo === 'pdf' ? this.api.pdf(filtro, this.formato) : this.api.excel(filtro);
    req.pipe(finalize(() => this.exportando = null)).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob); const a = document.createElement('a');
        a.href = url; a.download = `mayor_balance_${this.ano}_${this.acumuladoAnual ? 'anual' : String(this.mes).padStart(2,'0')}.${tipo === 'pdf' ? 'pdf' : 'xlsx'}`;
        a.click(); URL.revokeObjectURL(url);
      }, error: e => this.error = this.mensaje(e)
    });
  }
  numero(n: number): string { return this.formatoNumero.format(n ?? 0); }
  private mensaje(e: HttpErrorResponse): string {
    return typeof e.error?.message === 'string' ? e.error.message : 'No se pudo completar la operación.';
  }
}
