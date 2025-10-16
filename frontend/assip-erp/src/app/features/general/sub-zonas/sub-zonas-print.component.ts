import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { SubZonasApi, SubZona } from './sub-zonas.api';
import { ZonasApi } from '../zonas/zonas.api';
import { EmpresasApi, EmpresaDTO } from '../../../shared/general/empresas.api';

@Component({
  selector: 'app-sub-zonas-print',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './sub-zonas-print.component.html',
  styleUrls: ['./sub-zonas-print.component.scss']
})
export class SubZonasPrintComponent implements OnInit, OnDestroy {
  private api = inject(SubZonasApi);
  private zonasApi = inject(ZonasApi);
  private empresasApi = inject(EmpresasApi);
  private route = inject(ActivatedRoute);

  q = signal<string>('');
  idZona = signal<number | null>(null);
  zonaNombre = signal<string>('');
  subZonas = signal<SubZona[]>([]);
  nowStr = signal<string>('');

  // Empresa
  empresaSigla = signal<string>('');
  empresaNit = signal<string>('');
  logoUrl = signal<string>('assets/logo-print.png');

  private beforePrintHandler = () => this.preparePaginationFooter();

  ngOnInit(): void {
    const qParam = this.route.snapshot.queryParamMap.get('q') ?? '';
    const idZonaParam = this.route.snapshot.queryParamMap.get('idZona');
    this.q.set(qParam);
    this.idZona.set(idZonaParam ? Number(idZonaParam) : null);
    this.nowStr.set(new Date().toLocaleString());

    if (this.idZona() !== null) {
      this.zonasApi.get(this.idZona()!)
        .subscribe(z => this.zonaNombre.set(z.nombreZona ?? ''));
    }

    this.api.list({
      idZona: this.idZona() ?? undefined,
      q: this.q(),
      page: 0,
      size: 500,
      sort: 'nombreSubZona,asc'
    }).subscribe(res => this.subZonas.set(res.content));

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

  /** Calcula total m y fija modo estático si solo hay 1 página */
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
