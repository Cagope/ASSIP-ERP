import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { LaboralesApi, LaboralDetail } from './laborales.api';
import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';
import { EmpresasApi, EmpresaDTO } from '../../../shared/general/empresas.api';
import { CatalogosApi, IdNombreDTO } from '../../../shared/catalogos/catalogos.api';

import { from, of } from 'rxjs';
import { concatMap, map, toArray, catchError, filter as rxFilter } from 'rxjs/operators';

type PersonaItem = any;
type Bloque = { persona: PersonaItem; laboral: LaboralDetail };

@Component({
  selector: 'app-laborales-print',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './laborales-print.component.html',
  styleUrls: ['./laborales-print.component.scss']
})
export class LaboralesPrintComponent implements OnInit, OnDestroy {
  private laboralesApi = inject(LaboralesApi);
  private personasApi = inject(DatosPersonalesApi);
  private empresasApi = inject(EmpresasApi);
  private cat = inject(CatalogosApi);

  nowStr = signal<string>('');
  empresaSigla = signal<string>('');
  empresaNit = signal<string>('');
  logoUrl = signal<string>('assets/logo-print.png');

  loading = signal<boolean>(true);
  bloques = signal<Bloque[]>([]);

  // mapas de catálogos
  paisMap = signal<Record<number, string>>({});
  dptoMap = signal<Record<number, string>>({});
  ciudadMap = signal<Record<number, string>>({});
  tiposEmpMap = signal<Record<string, string>>({});
  tiposContratoMap = signal<Record<string, string>>({});
  jornadasMap = signal<Record<string, string>>({});

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

    // catálogos base
    this.cat.paises().subscribe({
      next: (rows: IdNombreDTO[]) => {
        const m: Record<number, string> = {};
        const list = Array.isArray(rows) ? rows : [];
        list.forEach(r => { if (r?.id != null) m[r.id] = String(r.nombre ?? r.id); });
        this.paisMap.set(m);
      }, error: () => this.paisMap.set({}),
    });

    this.cat.departamentos().subscribe({
      next: (rows: IdNombreDTO[]) => {
        const m: Record<number, string> = {};
        const list = Array.isArray(rows) ? rows : [];
        list.forEach(r => { if (r?.id != null) m[r.id] = String(r.nombre ?? r.id); });
        this.dptoMap.set(m);
      }, error: () => this.dptoMap.set({}),
    });

    // ciudades — usa el método que exista en tu CatalogosApi
    {
      const ciudadesFn =
        (this.cat as any).ciudades ??
        (this.cat as any).ciudadesPlano ??
        (this.cat as any).todasLasCiudades;

      if (typeof ciudadesFn === 'function') {
        ciudadesFn.call(this.cat).subscribe({
          next: (rows: IdNombreDTO[] | any[]) => {
            const m: Record<number, string> = {};
            const list = Array.isArray(rows) ? rows : [];
            list.forEach((r: any) => {
              const id = r?.id ?? r?.idCiudad ?? r?.id_ciudad;
              const nombre = r?.nombre ?? r?.nombreCiudad ?? r?.ciudad;
              if (id != null) m[id] = String(nombre ?? id);
            });
            this.ciudadMap.set(m);
          },
          error: () => this.ciudadMap.set({}),
        });
      } else {
        this.ciudadMap.set({});
      }
    }

    // catálogos laborales
    this.cat.tiposEmpresas().subscribe({
      next: (rows: any[]) => {
        const m: Record<string, string> = {};
        const list = Array.isArray(rows) ? rows : [];
        list.forEach((t: any) => {
          const c = (t?.codigo ?? t?.codigo_tipo_empresa ?? '').toString();
          if (c) m[c] = (t?.nombre ?? t?.nombre_tipo_empresa ?? c).toString();
        });
        this.tiposEmpMap.set(m);
      }, error: () => this.tiposEmpMap.set({}),
    });

    this.cat.tiposContratos().subscribe({
      next: (rows: any[]) => {
        const m: Record<string, string> = {};
        const list = Array.isArray(rows) ? rows : [];
        list.forEach((t: any) => {
          const c = (t?.codigo ?? t?.codigo_tipo_contrato ?? '').toString();
          if (c) m[c] = (t?.nombre ?? t?.nombre_tipo_contrato ?? c).toString();
        });
        this.tiposContratoMap.set(m);
      }, error: () => this.tiposContratoMap.set({}),
    });

    (this.cat as any).jornadasLaborales?.().subscribe({
      next: (rows: any[]) => {
        const m: Record<string, string> = {};
        const list = Array.isArray(rows) ? rows : [];
        list.forEach((t: any) => {
          const c = (t?.codigo ?? t?.codigo_jornada ?? '').toString();
          if (c) m[c] = (t?.nombre ?? t?.nombre_jornada ?? c).toString();
        });
        this.jornadasMap.set(m);
      }, error: () => this.jornadasMap.set({}),
    });

    // Carga por persona
    this.loadDesdePersonas();
    window.addEventListener('beforeprint', this.beforePrintHandler);
  }

  ngOnDestroy(): void {
    window.removeEventListener('beforeprint', this.beforePrintHandler);
  }

  private loadDesdePersonas(): void {
    this.personasApi.list({ size: 2000 }).subscribe({
      next: (data: any) => {
        const personas: PersonaItem[] = Array.isArray(data) ? data : (data?.items ?? data?.content ?? []);
        from(personas ?? []).pipe(
          concatMap((p: PersonaItem) => {
            const id = this.personaId(p);
            if (!id) return of(null);
            return this.laboralesApi.getByPersona(id).pipe(
              map((l: LaboralDetail | null) => l ? ({ persona: p, laboral: l } as Bloque) : null),
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
  }

  imprimir(): void {
    this.preparePaginationFooter();
    window.print();
  }

  // Helpers persona
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

  // Helpers catálogos
  nomPais(id?: number | null)   { return id ? (this.paisMap()[id] ?? String(id)) : ''; }
  nomDpto(id?: number | null)   { return id ? (this.dptoMap()[id] ?? String(id)) : ''; }
  nomCiudad(id?: number | null) { return id ? (this.ciudadMap()[id] ?? String(id)) : ''; }

  nomTipoEmpresa(c?: string | null)  { if (!c) return ''; return this.tiposEmpMap()[c] ?? c; }
  nomTipoContrato(c?: string | null) { if (!c) return ''; return this.tiposContratoMap()[c] ?? c; }
  nomJornada(c?: string | null)      { if (!c) return ''; return this.jornadasMap()[c] ?? c; }

  // Footer “Página X / Y”
  private preparePaginationFooter(): void {
    try {
      const pxPerIn = 96;
      const pageHeightPx = 11 * pxPerIn;
      const topMarginPx = (12 / 25.4) * pxPerIn;
      const bottomMarginPx = (12 / 25.4) * pxPerIn;
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
