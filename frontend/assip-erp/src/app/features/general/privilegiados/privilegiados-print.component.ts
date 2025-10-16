import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { EmpresasApi, EmpresaDTO } from '../../../shared/general/empresas.api';
import { CatalogosApi, CodigoNombreDTO } from '../../../shared/catalogos/catalogos.api';
import { PrivilegiadosApi, PrivilegiadoListItemDTO } from './privilegiados.api';

@Component({
  selector: 'app-privilegiados-print',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './privilegiados-print.component.html',
  styleUrls: ['./privilegiados-print.component.scss']
})
export class PrivilegiadosPrintComponent implements OnInit, OnDestroy {
  private api = inject(PrivilegiadosApi);
  private route = inject(ActivatedRoute);
  private empresasApi = inject(EmpresasApi);
  private catalogos = inject(CatalogosApi);

  // Filtro
  q = signal<string>('');
  idDirectivo = signal<number | null>(null);

  // Datos
  privilegiados = signal<PrivilegiadoListItemDTO[]>([]);
  nowStr = signal<string>('');

  // Encabezado empresa
  empresaSigla = signal<string>('');
  empresaNit = signal<string>('');
  logoUrl = signal<string>('assets/logo-print.png');

  // Catálogo parentescos (para nombre del parentesco)
  parentescosMap = signal<Record<string, string>>({});

  private beforePrintHandler = () => this.preparePaginationFooter();

  ngOnInit(): void {
    this.q.set(this.route.snapshot.queryParamMap.get('q') ?? '');

    // Lee idDirectivo de :param o ?query
    const idFromParam = Number(this.route.snapshot.paramMap.get('idDirectivo'));
    const idFromQuery = Number(this.route.snapshot.queryParamMap.get('idDirectivo'));
    const id = Number.isFinite(idFromParam) && idFromParam > 0
      ? idFromParam
      : (Number.isFinite(idFromQuery) && idFromQuery > 0 ? idFromQuery : NaN);

    if (Number.isFinite(id) && id > 0) {
      this.idDirectivo.set(id);
    } else {
      // Si no hay idDirectivo, no intentamos cargar y dejamos la hoja vacía
      console.warn('Falta idDirectivo en ruta o query (?idDirectivo=)');
    }

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

    // Catálogo de parentescos
    this.catalogos.parentescos().subscribe({
      next: (rows: CodigoNombreDTO[]) => {
        const m: Record<string, string> = {};
        (rows || []).forEach(r => m[r.codigo] = r.nombre);
        this.parentescosMap.set(m);
        this.load();
      },
      error: () => { this.parentescosMap.set({}); this.load(); }
    });

    window.addEventListener('beforeprint', this.beforePrintHandler);
  }

  ngOnDestroy(): void {
    window.removeEventListener('beforeprint', this.beforePrintHandler);
  }

  private load(): void {
    if (!this.idDirectivo()) {
      this.privilegiados.set([]);
      return;
    }
    this.api.list(this.idDirectivo()!, this.q()).subscribe({
      next: (res) => {
        const rows = Array.isArray(res) ? res : (res as any)?.content ?? [];
        this.privilegiados.set(rows as PrivilegiadoListItemDTO[]);
      },
      error: () => this.privilegiados.set([])
    });
  }

  imprimir(): void {
    this.preparePaginationFooter();
    window.print();
  }

  nomParentesco(codigo: string) {
    return this.parentescosMap()[codigo] ?? codigo;
  }

  /**
   * Calcula el total de páginas y ajusta el footer:
   * - Si es 1 página -> modo estático "Página 1 / 1".
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
