import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { UbicacionesApi, UbicacionResponse } from './ubicaciones.api';
import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';
import { EmpresasApi, EmpresaDTO } from '../../../shared/general/empresas.api';
import { CatalogosApi, IdNombreDTO } from '../../../shared/catalogos/catalogos.api';
import { ZonasApi } from '../../general/zonas/zonas.api';
import { SubZonasApi } from '../../general/sub-zonas/sub-zonas.api';

import { from, of, concatMap, map, toArray, catchError, filter as rxFilter } from 'rxjs';

type PersonaItem = any;
type Bloque = { persona: PersonaItem; ubicacion: UbicacionResponse };

@Component({
  selector: 'app-ubicaciones-print',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './ubicaciones-print.component.html',
  styleUrls: ['./ubicaciones-print.component.scss']
})
export class UbicacionesPrintComponent implements OnInit, OnDestroy {
  private ubicApi = inject(UbicacionesApi);
  private personasApi = inject(DatosPersonalesApi);
  private empresasApi = inject(EmpresasApi);
  private cat = inject(CatalogosApi);
  private zonasApi = inject(ZonasApi);
  private subZonasApi = inject(SubZonasApi);

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
  zonaMap = signal<Record<number, string>>({});
  subZonaMap = signal<Record<number, string>>({});

  // sub-zona -> idZona
  subToZonaMap = signal<Record<number, number>>({});

  private beforePrintHandler = () => this.preparePaginationFooter();

  ngOnInit(): void {
    this.nowStr.set(new Date().toLocaleString());

    // ===== Empresa
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

    // ===== catálogos base
    this.cat.paises().subscribe({
      next: (rows: IdNombreDTO[]) => {
        const m: Record<number, string> = {};
        (rows ?? []).forEach(r => { if (r?.id != null) m[r.id] = String(r.nombre ?? r.id); });
        this.paisMap.set(m);
      }, error: () => this.paisMap.set({}),
    });

    this.cat.departamentos().subscribe({
      next: (rows: IdNombreDTO[]) => {
        const m: Record<number, string> = {};
        (rows ?? []).forEach(r => { if (r?.id != null) m[r.id] = String(r.nombre ?? r.id); });
        this.dptoMap.set(m);
      }, error: () => this.dptoMap.set({}),
    });

    // ciudades — usa el método que exista
    {
      const ciudadesFn =
        (this.cat as any).ciudades ??
        (this.cat as any).ciudadesPlano ??
        (this.cat as any).todasLasCiudades;

      if (typeof ciudadesFn === 'function') {
        ciudadesFn.call(this.cat).subscribe({
          next: (rows: IdNombreDTO[]) => {
            const m: Record<number, string> = {};
            (rows ?? []).forEach((r: any) => {
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

    // zonas
    this.zonasApi.list({ q: '', page: 0, size: 1000 }).subscribe({
      next: (p: any) => {
        const list: any[] = Array.isArray(p) ? p : (p?.items ?? p?.content ?? []);
        const m: Record<number, string> = {};
        (list ?? []).forEach(z => {
          const id = z?.idZona ?? z?.id_zona ?? z?.id;
          if (id != null) m[id] = `${z?.codigoZona ?? z?.codigo ?? ''} ${z?.nombreZona ?? z?.nombre ?? ''}`.trim();
        });
        this.zonaMap.set(m);
      }, error: () => this.zonaMap.set({}),
    });

    // sub-zonas + mapa sub->zona
    this.subZonasApi.list({ q: '', page: 0, size: 2000 }).subscribe({
      next: (p: any) => {
        const list: any[] = Array.isArray(p) ? p : (p?.items ?? p?.content ?? []);
        const subNombre: Record<number, string> = {};
        const s2z: Record<number, number> = {};
        (list ?? []).forEach(s => {
          const idSub = s?.idSubZona ?? s?.id_sub_zona ?? s?.id;
          const idZona = s?.idZona ?? s?.id_zona ?? s?.zonaId ?? s?.zona_id;
          const nombre = `${s?.codigoSubZona ?? s?.codigo ?? ''} ${s?.nombreSubZona ?? s?.nombre ?? ''}`.trim();
          if (idSub != null) subNombre[idSub] = nombre || String(idSub);
          if (idSub != null && idZona != null) s2z[idSub] = Number(idZona);
        });
        this.subZonaMap.set(subNombre);
        this.subToZonaMap.set(s2z);
      }, error: () => { this.subZonaMap.set({}); this.subToZonaMap.set({}); },
    });

    // ===== SOLO registros que existen en ubicaciones (Opción A estricta)
    this.loadDesdeUbicaciones();
    window.addEventListener('beforeprint', this.beforePrintHandler);
  }

  ngOnDestroy(): void {
    window.removeEventListener('beforeprint', this.beforePrintHandler);
  }

  // --------- Carga solo desde ubicaciones (preferido). Si no existe método masivo, fallback estricto ----------
  private loadDesdeUbicaciones(): void {
    const api: any = this.ubicApi as any;
    const fnName = ['list', 'findAll', 'search', 'listAll', 'all'].find(n => typeof api[n] === 'function');

    if (fnName) {
      // ---- Modo ideal: partir de ubicaciones existentes
      api[fnName]({ page: 0, size: 5000 }).subscribe({
        next: (resp: any) => {
          const ubicRows: any[] = Array.isArray(resp) ? resp : (resp?.content ?? resp?.items ?? []);
          const ubicaciones = (ubicRows ?? []).map(this.normUbic).filter((u: any) => this.isUbicValida(u)) as UbicacionResponse[];

          const idsPersona = Array.from(new Set(ubicaciones.map(u => u.id_datos_personal).filter(Boolean)));
          from(idsPersona).pipe(
            concatMap((id: number) =>
              this.personasApi.get(id).pipe(
                map((p: any) => ({ id, persona: p })),
                catchError(() => of({ id, persona: null }))
              )
            ),
            toArray(),
            map((pairs: {id:number; persona:any}[]) => {
              const pMap = new Map(pairs.filter(x => x.persona).map(x => [x.id, x.persona]));
              return ubicaciones
                .map(u => pMap.has(u.id_datos_personal) ? ({ persona: pMap.get(u.id_datos_personal)!, ubicacion: u }) : null)
                .filter(Boolean) as Bloque[];
            })
          ).subscribe({
            next: (bloques: Bloque[]) => { this.bloques.set(bloques); this.loading.set(false); },
            error: () => { this.bloques.set([]); this.loading.set(false); }
          });
        },
        error: () => { this.bloques.set([]); this.loading.set(false); }
      });
    } else {
      // ---- Fallback estricto: consulta por persona, pero acepta solo ubicaciones "reales"
      this.personasApi.list().subscribe({
        next: (data: any) => {
          const personas: PersonaItem[] = Array.isArray(data) ? data : (data?.items ?? data?.content ?? []);
          from(personas ?? []).pipe(
            concatMap((p: PersonaItem) => {
              const id = this.personaId(p);
              if (!id) return of(null);
              return this.ubicApi.getByPersona(id).pipe(
                map((resp: any) => {
                  const u: UbicacionResponse | null = this.normUbic(resp?.body ?? resp);
                  return u && this.isUbicValida(u) ? ({ persona: p, ubicacion: u } as Bloque) : null;
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
    }
  }

  // Normaliza posibles nombres de campos en distintas respuestas
  private normUbic = (u: any): UbicacionResponse | null => {
    if (!u) return null;
    return {
      id_ubicacion:       u.id_ubicacion ?? u.id ?? 0,
      id_datos_personal:  u.id_datos_personal ?? u.idDatosPersonal ?? u.id_datos_personales ?? 0,
      direccion:          u.direccion ?? '',
      barrio:             u.barrio ?? '',
      telefono:           u.telefono ?? '',
      celular_uno:        u.celular_uno ?? u.celularUno ?? '',
      celular_dos:        u.celular_dos ?? u.celularDos ?? '',
      correo:             u.correo ?? '',
      id_pais:            u.id_pais ?? null,
      id_departamento:    u.id_departamento ?? null,
      id_ciudad:          u.id_ciudad ?? null,
      id_sub_zona:        u.id_sub_zona ?? u.idSubZona ?? null,
    } as UbicacionResponse;
  };

  // Acepta solo ubicaciones "reales": id o al menos un dato clave relleno
  private isUbicValida = (u: UbicacionResponse | null): u is UbicacionResponse => {
    if (!u) return false;
    if (u.id_ubicacion && u.id_datos_personal) return true;
    return Boolean(
      (u.direccion && u.direccion.trim()) ||
      (u.barrio && u.barrio.trim()) ||
      u.id_ciudad || u.id_departamento || u.id_pais || u.id_sub_zona
    );
  };

  imprimir(): void {
    this.preparePaginationFooter();
    window.print();
  }

  // ===== Helpers persona
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

  // ===== Helpers ubicación
  nomPais(id?: number | null)   { return id ? (this.paisMap()[id] ?? String(id)) : ''; }
  nomDpto(id?: number | null)   { return id ? (this.dptoMap()[id] ?? String(id)) : ''; }
  nomCiudad(id?: number | null) { return id ? (this.ciudadMap()[id] ?? String(id)) : ''; }
  nomZona(id?: number | null)   { return id ? (this.zonaMap()[id] ?? String(id)) : ''; }
  nomSubZona(id?: number | null){ return id ? (this.subZonaMap()[id] ?? String(id)) : ''; }

  // Zona desde sub-zona
  nomZonaFromSub(idSub?: number | null): string {
    if (!idSub) return '';
    const zId = this.subToZonaMap()[idSub];
    return zId ? this.nomZona(zId) : '';
  }

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

      (document.documentElement as any).style.setProperty('--total-pages', `"${pages}"`);
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
        (document.documentElement as any).style.setProperty('--total-pages', `"1"`);
      }
    }
  }
}
