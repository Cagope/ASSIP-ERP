import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { ParametrosApi, Parametro } from './parametros.api';
import { AgenciasApi } from '../agencias/agencias.api';
import { EmpresasApi, EmpresaDTO } from '../../../shared/general/empresas.api';

@Component({
  selector: 'app-parametros-print',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './parametros-print.component.html',
  styleUrls: ['./parametros-print.component.scss']
})
export class ParametrosPrintComponent implements OnInit, OnDestroy {
  private api = inject(ParametrosApi);
  private agenciasApi = inject(AgenciasApi);
  private empresasApi = inject(EmpresasApi);
  private route = inject(ActivatedRoute);

  q = signal<string>('');
  codigo = signal<number | null>(null);
  idAgencia = signal<number | null>(null);
  agenciaNombre = signal<string>('');
  parametros = signal<Parametro[]>([]);
  nowStr = signal<string>('');

  // Empresa
  empresaSigla = signal<string>('');
  empresaNit = signal<string>('');
  logoUrl = signal<string>('assets/logo-print.png');

  private beforePrintHandler = () => this.preparePaginationFooter();

  ngOnInit(): void {
    this.q.set(this.route.snapshot.queryParamMap.get('q') ?? '');
    const c = this.route.snapshot.queryParamMap.get('codigo');
    this.codigo.set(c ? Number(c) : null);
    const a = this.route.snapshot.queryParamMap.get('idAgencia');
    this.idAgencia.set(a ? Number(a) : null);

    this.nowStr.set(new Date().toLocaleString());

    if (this.idAgencia() !== null) {
      this.agenciasApi.get(this.idAgencia()!)
        .subscribe(ag => this.agenciaNombre.set((ag as any).nombreAgencia ?? ''));
    }

    this.api.list({
      idAgencia: this.idAgencia() ?? undefined,
      q: this.q() || undefined,
      codigo: this.codigo() ?? undefined,
      page: 0,
      size: 500,
      sort: 'nombreParametro,asc'
    }).subscribe(res => this.parametros.set(res.content));

    // Empresa principal
    this.empresasApi.getEmpresaPrincipal().subscribe({
      next: (e: EmpresaDTO | null) => {
        if (!e) return;
        const sigla = (e.siglaEmpresa ?? '').toString().trim();
        const razon = (e.razonSocial ?? '').toString().trim();
        this.empresaSigla.set(sigla || razon || '');

        const doc = (e.documentoEmpresa ?? '').toString().trim();
        const dv  = (e.digitoVerificacion ?? '').toString().trim();
        const nit = doc ? (dv ? `${doc}-${dv}` : doc) : '';
        if (nit) this.empresaNit.set(nit);

        const logo = (e.logoUrl ?? '').toString().trim();
        if (logo) this.logoUrl.set(logo);
      },
      error: () => {}
    });

    window.addEventListener('beforeprint', this.beforePrintHandler);
  }

  ngOnDestroy(): void {
    window.removeEventListener('beforeprint', this.beforePrintHandler);
  }

  imprimir(): void {
    this.preparePaginationFooter();
    window.print();
  }

  // ==== Helpers para el template ====

  /** Devuelve el comentario del parámetro tomando el primer campo disponible. */
  getComentario(p: Parametro): string {
    const anyP = p as any;
    return (
      anyP.comentarioParametro ??
      anyP.comentario ??
      anyP.observaciones ??
      anyP.descripcionParametro ??
      anyP.detalle ??
      ''
    );
  }

  /** Devuelve el nombre de agencia desde varias variantes de campo. */
  getAgenciaNombre(p: Parametro): string {
    const anyP = p as any;
    return (
      anyP.nombreAgencia ??
      anyP.agenciaNombre ??
      anyP.agencia ??
      (this.idAgencia() !== null ? this.agenciaNombre() : '') ??
      ''
    );
  }

  /** Calcula total m y fija modo estático si solo hay 1 página. */
  private preparePaginationFooter(): void {
    try {
      const pxPerIn = 96;
      const pageHeightPx = 11 * pxPerIn;
      const topMarginPx = (14 / 25.4) * pxPerIn;
      const bottomMarginPx = (20 / 25.4) * pxPerIn;
      const usableHeight = pageHeightPx - (topMarginPx + bottomMarginPx);

      const doc = document.querySelector('.print-container') as HTMLElement;
      const footer = document.getElementById('pageFooter');
      const pageText = footer?.querySelector('.page-text') as HTMLElement | null;

      if (!doc || !footer || !pageText) return;

      const totalHeight = doc.scrollHeight;
      const pages = Math.max(1, Math.ceil(totalHeight / usableHeight));

      document.documentElement.style.setProperty('--total-pages', `"${pages}"`);

      if (pages === 1) {
        footer.classList.add('static');
        pageText.setAttribute('data-content', 'Página 1 / 1');
      } else {
        footer.classList.remove('static');
        pageText.removeAttribute('data-content');
      }
    } catch {
      const footer = document.getElementById('pageFooter');
      const pageText = footer?.querySelector('.page-text') as HTMLElement | null;
      if (footer && pageText) {
        footer.classList.add('static');
        pageText.setAttribute('data-content', 'Página 1 / 1');
        document.documentElement.style.setProperty('--total-pages', `"1"`);
      }
    }
  }
}
