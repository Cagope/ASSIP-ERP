import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { FinancierosApi, FinancieroResponse } from './financieros.api';
import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';
import { EmpresasApi, EmpresaDTO } from '../../../shared/general/empresas.api';

import { from, of, concatMap, map, toArray, catchError, filter as rxFilter } from 'rxjs';

type PersonaItem = any;

type Bloque = {
  persona: PersonaItem;
  financiero: FinancieroResponse;
};

@Component({
  selector: 'app-financieros-print',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './financieros-print.component.html',
  styleUrls: ['./financieros-print.component.scss'],
})
export class FinancierosPrintComponent implements OnInit, OnDestroy {
  private financierosApi = inject(FinancierosApi);
  private personasApi = inject(DatosPersonalesApi);
  private empresasApi = inject(EmpresasApi);

  nowStr = signal<string>('');
  empresaSigla = signal<string>('');
  empresaNit = signal<string>('');
  logoUrl = signal<string>('assets/logo-print.png');

  loading = signal<boolean>(true);
  bloques = signal<Bloque[]>([]);

  private beforePrintHandler = () => this.preparePaginationFooter();

  ngOnInit(): void {
    this.nowStr.set(new Date().toLocaleString());

    // Empresa
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
      error: () => {},
    });

    // Personas con financiero: SOLO agrega las que tienen registro financiero
    this.personasApi.list().subscribe({
      next: (data: any) => {
        const personas: PersonaItem[] = Array.isArray(data) ? data : (data?.items ?? data?.content ?? []);
        from(personas ?? []).pipe(
          concatMap((p: PersonaItem) => {
            const id = this.personaId(p);
            if (!id) return of(null);
            return this.financierosApi.getByPersona(id).pipe(
              map((resp: any) => {
                // Soporta HttpResponse<FinancieroResponse> o body directo, y 204
                const status = resp?.status ?? 200;
                if (status === 204) return null;
                const row: FinancieroResponse | null = (resp?.body ?? resp) ?? null;
                return row ? ({ persona: p, financiero: row } as Bloque) : null;
              }),
              catchError(() => of(null))
            );
          }),
          rxFilter((b: Bloque | null): b is Bloque => !!b),
          toArray()
        ).subscribe({
          next: (bloques: Bloque[]) => { this.bloques.set(bloques); this.loading.set(false); },
          error: () => { this.bloques.set([]); this.loading.set(false); },
        });
      },
      error: () => { this.bloques.set([]); this.loading.set(false); },
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

  // ===== Helpers: Persona =====
  personaId(p: PersonaItem): number {
    const candidates = [p?.id, p?.idDatosPersonales, p?.id_datos_personales, p?.idDatosPersonal];
    const found = candidates.find(v => typeof v === 'number' && !Number.isNaN(v) && v > 0);
    return (found ?? 0) as number;
  }

  personaNombre(p: PersonaItem): string {
    const nombres = (p?.nombres ?? `${p?.primerNombre ?? ''} ${p?.segundoNombre ?? ''}`).toString().trim();
    const apellidos = (p?.apellidos ?? `${p?.primerApellido ?? ''} ${p?.segundoApellido ?? ''}`).toString().trim();
    return `${nombres} ${apellidos}`.replace(/\s+/g, ' ').trim();
  }

  personaDocumento(p: PersonaItem): string {
    const tipo = (p?.tipoDocumento ?? p?.tipo_documento ?? '').toString().trim();
    const doc  = (p?.documento ?? '').toString().trim();
    return [tipo, doc].filter(Boolean).join(' ');
  }

  // ===== Helpers: formateo y totales (coinciden con la lógica de edición) =====
  money(n?: number | null): string {
    const v = Number(n ?? 0);
    return new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(v);
  }

  nz(s?: string | null): string {
    const t = (s ?? '').toString().trim();
    return t || '';
  }

  totalIngresos(f: FinancieroResponse): number {
    return (f?.valor_salario ?? 0)
         + (f?.valor_pension ?? 0)
         + (f?.ingresos_arriendo ?? 0)
         + (f?.ingresos_comisiones ?? 0)
         + (f?.otros_ingresos ?? 0);
  }

  totalEgresosSinDeuda(f: FinancieroResponse): number {
    return (f?.egresos_familiares ?? 0)
         + (f?.egresos_arriendo ?? 0)
         + (f?.egresos_credito ?? 0)
         + (f?.otros_egresos ?? 0);
  }

  totalPatrimonio(f: FinancieroResponse): number {
    return (f?.total_activos ?? 0) - (f?.total_pasivos ?? 0);
  }

  /** Paginación: calcula total m y usa modo estático si solo hay 1 página */
  private preparePaginationFooter(): void {
    try {
      const pxPerIn = 96;
      const pageHeightPx = 11 * pxPerIn;            // Carta 11in alto
      const topMarginPx = (12 / 25.4) * pxPerIn;     // @page top 12mm
      const bottomMarginPx = (12 / 25.4) * pxPerIn;  // @page bottom 12mm aprox
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
