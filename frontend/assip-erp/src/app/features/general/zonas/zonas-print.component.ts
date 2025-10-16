import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { ZonasApi, Zona } from './zonas.api';
import { EmpresasApi, EmpresaDTO } from '../../../shared/general/empresas.api';

@Component({
  selector: 'app-zonas-print',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './zonas-print.component.html',
  styleUrls: ['./zonas-print.component.scss']
})
export class ZonasPrintComponent implements OnInit, OnDestroy {
  private api = inject(ZonasApi);
  private route = inject(ActivatedRoute);
  private empresasApi = inject(EmpresasApi);

  q = signal<string>('');
  zonas = signal<Zona[]>([]);
  nowStr = signal<string>('');

  // Encabezado empresa
  empresaSigla = signal<string>('');
  empresaNit = signal<string>('');
  logoUrl = signal<string>('assets/logo-print.png');

  private beforePrintHandler = () => this.preparePaginationFooter();

  ngOnInit(): void {
    const qParam = this.route.snapshot.queryParamMap.get('q') ?? '';
    this.q.set(qParam);

    // Fecha/hora legible
    this.nowStr.set(new Date().toLocaleString());

    // Empresa principal: sigla/NIT/logo
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
      error: () => { /* defaults */ }
    });

    // Datos de zonas
    this.api
      .list({ q: this.q(), page: 0, size: 500, sort: 'nombreZona,asc' })
      .subscribe(res => this.zonas.set(res.content));

    // Preparar paginación antes de imprimir (y al usar botón)
    window.addEventListener('beforeprint', this.beforePrintHandler);
  }

  ngOnDestroy(): void {
    window.removeEventListener('beforeprint', this.beforePrintHandler);
  }

  imprimir(): void {
    this.preparePaginationFooter();
    window.print();
  }

  /**
   * Calcula el total de páginas y ajusta el footer:
   * - Si es 1 página -> modo estático "Página 1 / 1" (evita 0/1).
   * - Si son varias -> modo dinámico con counter(page) y total calculado.
   */
  private preparePaginationFooter(): void {
    try {
      const pxPerIn = 96;
      const pageHeightPx = 11 * pxPerIn; // Letter height
      const topMarginPx = (14 / 25.4) * pxPerIn; // 14mm
      const bottomMarginPx = (20 / 25.4) * pxPerIn; // 20mm
      const usableHeight = pageHeightPx - (topMarginPx + bottomMarginPx);

      const doc = document.querySelector('.print-container') as HTMLElement;
      const footer = document.getElementById('pageFooter');
      const pageText = footer?.querySelector('.page-text') as HTMLElement | null;

      if (!doc || !footer || !pageText) return;

      const totalHeight = doc.scrollHeight;
      const pages = Math.max(1, Math.ceil(totalHeight / usableHeight));

      // expone total m como var CSS
      document.documentElement.style.setProperty('--total-pages', `"${pages}"`);

      if (pages === 1) {
        // Forzar "Página 1 / 1" y evitar counter(page) = 0 en algunos previews
        footer.classList.add('static');
        pageText.setAttribute('data-content', 'Página 1 / 1');
      } else {
        // Varias páginas: usar contador dinámico para n y var para m
        footer.classList.remove('static');
        pageText.removeAttribute('data-content');
      }
    } catch {
      // Fallback seguro: asumir 1/1
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
