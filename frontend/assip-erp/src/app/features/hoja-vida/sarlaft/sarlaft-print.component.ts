import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';
import { EmpresasApi, EmpresaDTO } from '../../../shared/general/empresas.api';
import { CatalogosApi, CodigoNombreDTO } from '../../../shared/catalogos/catalogos.api';
import { SarlaftApi, SarlaftResponse } from './sarlaft.api';

import { from, of, concatMap, map, toArray, catchError, filter } from 'rxjs';

type PersonaItem = any;
type Bloque = { persona: PersonaItem; sarlaft: SarlaftResponse };

@Component({
  selector: 'app-sarlaft-print',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './sarlaft-print.component.html',
  styleUrls: ['./sarlaft-print.component.scss']
})
export class SarlaftPrintComponent implements OnInit, OnDestroy {
  private personasApi = inject(DatosPersonalesApi);
  private empresasApi = inject(EmpresasApi);
  private cat = inject(CatalogosApi);
  private sarlaftApi = inject(SarlaftApi);

  nowStr = signal<string>('');
  empresaSigla = signal<string>('');
  empresaNit = signal<string>('');
  logoUrl = signal<string>('assets/logo-print.png');

  loading = signal<boolean>(true);
  bloques = signal<Bloque[]>([]);

  // catálogos
  tiposPepsMap = signal<Record<string, string>>({});
  parentescosMap = signal<Record<string, string>>({});

  private beforePrintHandler = () => this.preparePaginationFooter();

  ngOnInit(): void {
    try { this.nowStr.set(new Date().toLocaleString('es-CO')); }
    catch { this.nowStr.set(new Date().toLocaleString()); }

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

    // Catálogo Tipos de PEP (sin ordenar, usa lo que venga del back)
    this.cat.tiposPeps().subscribe({
      next: (rows: CodigoNombreDTO[]) => {
        const map0: Record<string, string> = {};
        (rows ?? []).forEach(r => {
          if (r?.codigo) map0[String(r.codigo)] = String(r.nombre ?? r.codigo);
        });
        this.tiposPepsMap.set(map0);
      },
      error: () => this.tiposPepsMap.set({}),
    });

    // Catálogo Parentescos
    this.cat.parentescos().subscribe({
      next: (rows: CodigoNombreDTO[]) => {
        const map0: Record<string, string> = {};
        (rows ?? []).forEach(r => {
          if (r?.codigo) map0[String(r.codigo)] = String(r.nombre ?? r.codigo);
        });
        this.parentescosMap.set(map0);
      },
      error: () => this.parentescosMap.set({}),
    });

    // Personas + SARLAFT (solo las que sí tienen)
    this.personasApi.list().subscribe({
      next: (data: any) => {
        const personas: PersonaItem[] = Array.isArray(data) ? data : (data?.items ?? data?.content ?? []);
        const lista = (personas ?? []);

        from(lista).pipe(
          concatMap((p: PersonaItem) => {
            const id = this.personaId(p);
            if (!id) return of(null);
            return this.sarlaftApi.getByPersona(id).pipe(
              map((resp: any) => {
                // Soporta HttpResponse con 204 y body
                if (resp?.status === 204) return null;
                const row: SarlaftResponse | null = resp?.body ?? resp ?? null;
                return row ? ({ persona: p, sarlaft: row } as Bloque) : null;
              }),
              catchError(() => of(null))
            );
          }),
          filter((b: Bloque | null): b is Bloque => !!b),
          toArray()
        ).subscribe({
          next: (bloques: Bloque[]) => {
            this.bloques.set(bloques);
            this.loading.set(false);
            setTimeout(() => this.preparePaginationFooter(), 0);
          },
          error: () => {
            this.bloques.set([]);
            this.loading.set(false);
          },
        });
      },
      error: () => {
        this.bloques.set([]);
        this.loading.set(false);
      },
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

  // ===== Helpers Persona =====
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

  // ===== Helpers SARLAFT (nombres cat) =====
  yesno(v: any): string { return v ? 'Sí' : 'No'; }

  nombreTipoPeps(cod?: string | null): string {
    if (!cod) return '';
    return this.tiposPepsMap()[String(cod)] ?? String(cod);
    // Si quieres ocultar el código y solo mostrar nombre, deja solo el nombre del mapa.
  }

  nombreParentesco(cod?: string | null): string {
    if (!cod) return '';
    return this.parentescosMap()[String(cod)] ?? String(cod);
  }

  /** Paginación: calcula total m y usa modo estático si solo hay 1 página */
  private preparePaginationFooter(): void {
    try {
      const pxPerIn = 96;
      const pageHeightPx = 11 * pxPerIn;           // Carta 11in alto
      const topMarginPx = (12 / 25.4) * pxPerIn;    // @page top 12mm
      const bottomMarginPx = (12 / 25.4) * pxPerIn; // @page bottom 12mm aprox
      const usableHeight = pageHeightPx - (topMarginPx + bottomMarginPx);

      const docEl = document.querySelector('.print-container') as HTMLElement;
      const footer = document.getElementById('pageFooter');
      const pageText = footer?.querySelector('.page-text') as HTMLElement | null;

      if (!docEl || !footer || !pageText) return;

      const totalHeight = docEl.scrollHeight;
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
