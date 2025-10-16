import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatosPersonalesApi, DatosPersonalesListItem } from './datos-personales.api';
import { EmpresasApi, EmpresaDTO } from '../../../shared/general/empresas.api';

@Component({
  selector: 'app-datos-personales-print',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './datos-personales-print.component.html',
  styleUrls: ['./datos-personales-print.component.scss']
})
export class DatosPersonalesPrintComponent implements OnInit, OnDestroy {
  private api = inject(DatosPersonalesApi);
  private route = inject(ActivatedRoute);
  private empresasApi = inject(EmpresasApi);

  q = signal<string>('');
  personas = signal<DatosPersonalesListItem[]>([]);
  nowStr = signal<string>('');

  // Empresa
  empresaSigla = signal<string>('');
  empresaNit = signal<string>('');
  logoUrl = signal<string>('assets/logo-print.png');

  private beforePrintHandler = () => this.preparePaginationFooter();

  ngOnInit(): void {
    const qParam = this.route.snapshot.queryParamMap.get('q') ?? '';
    this.q.set(qParam);
    this.nowStr.set(new Date().toLocaleString());

    // Datos
    this.api.list({ q: this.q(), page: 0, size: 500, sort: 'nombres,asc' })
      .subscribe(res => this.personas.set(res.content ?? []));

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

  /** Calcula total m y aplica modo estático si solo hay 1 página */
  private preparePaginationFooter(): void {
    try {
      const pxPerIn = 96;
      const pageHeightPx = 11 * pxPerIn;           // Carta 11in alto
      const topMarginPx = (10 / 25.4) * pxPerIn;    // @page top 10mm
      const bottomMarginPx = (10 / 25.4) * pxPerIn; // @page bottom 10mm
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
