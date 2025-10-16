import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { DatosFamiliaresApi, DatosFamiliar } from './datos-familiares.api';
import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';
import { EmpresasApi, EmpresaDTO } from '../../../shared/general/empresas.api';
import { CatalogosApi, CodigoNombreDTO } from '../../../shared/catalogos/catalogos.api';

import { from, of, concatMap, map, toArray, catchError, filter } from 'rxjs';

type PersonaItem = any; // usa el tipo real si lo tienes
type Bloque = { persona: PersonaItem; familiares: DatosFamiliar[] };

@Component({
  selector: 'app-datos-familiares-print',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './datos-familiares-print.component.html',
  styleUrls: ['./datos-familiares-print.component.scss']
})
export class DatosFamiliaresPrintComponent implements OnInit, OnDestroy {
  private api = inject(DatosFamiliaresApi);
  private personasApi = inject(DatosPersonalesApi);
  private empresasApi = inject(EmpresasApi);
  private cat = inject(CatalogosApi);

  nowStr = signal<string>('');
  empresaSigla = signal<string>('');
  empresaNit = signal<string>('');
  logoUrl = signal<string>('assets/logo-print.png');

  loading = signal<boolean>(true);
  bloques = signal<Bloque[]>([]);

  // catálogo parentesco
  parentescosMap = signal<Record<string, string>>({});

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

    // Catálogo de parentescos
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

    // Personas + familiares: arma bloques solo con vinculados
    this.personasApi.list().subscribe({
      next: (data: any) => {
        const personas: PersonaItem[] = Array.isArray(data) ? data : (data?.items ?? data?.content ?? []);
        const lista = (personas ?? []);

        from(lista).pipe(
          concatMap((p: PersonaItem) => {
            const id = this.personaId(p);
            if (!id) return of(null);
            return this.api.listByPersona(id).pipe(
              map(rows => ({ persona: p, familiares: rows ?? [] } as Bloque)),
              catchError(() => of({ persona: p, familiares: [] } as Bloque))
            );
          }),
          filter((b: Bloque | null): b is Bloque => !!b && (b.familiares?.length ?? 0) > 0),
          toArray()
        ).subscribe({
          next: (bloques: Bloque[]) => {
            this.bloques.set(bloques);
            this.loading.set(false);
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

  // ===== Helpers: Persona =====
  personaId(p: PersonaItem): number {
    const candidates = [
      (p as any)?.id,
      (p as any)?.idDatosPersonales,
      (p as any)?.id_datos_personales,
      (p as any)?.idDatosPersonal,
    ];
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

  // ===== Helpers: Familiar =====
  codigoPar(it: any): string | null {
    return it?.codigo_parentesco ?? it?.codigoParentesco ?? null;
  }

  nombreParentescoFromItem(it: any): string {
    const codigo = this.codigoPar(it);
    if (!codigo) return '';
    const m = this.parentescosMap();
    return m[codigo] ?? String(codigo);
  }

  doc(it: any): string {
    return it?.documento_datos_familiar ?? it?.documentoDatosFamiliar ?? '';
  }

  nom(it: any): string {
    return it?.nombre_datos_familiar ?? it?.nombreDatosFamiliar ?? '';
  }

  tel(it: any): string {
    return it?.telefono_datos_familiar ?? it?.telefonoDatosFamiliar ?? '';
  }

  cel(it: any): string {
    return it?.celular_datos_familiar ?? it?.celularDatosFamiliar ?? '';
  }

  dir(it: any): string {
    return (
      it?.direccion_datos_familiar ??
      it?.direccionDatosFamiliar ??
      it?.direccion_familiar ??
      it?.direccionFamiliar ??
      it?.direccion ??
      ''
    );
  }

  ref(it: any): boolean {
    return Boolean(it?.referencia_familiar ?? it?.referenciaFamiliar ?? false);
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
