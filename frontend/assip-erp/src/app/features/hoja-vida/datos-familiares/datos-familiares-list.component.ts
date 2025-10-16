import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatosFamiliaresApi, DatosFamiliar } from './datos-familiares.api';
import { SessionService } from '../../../core/auth/session.service';
import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';
import { CatalogosApi, CodigoNombreDTO } from '../../../shared/catalogos/catalogos.api';
import { DatosFamiliaresExportButtonComponent } from './datos-familiares-export-button.component';

type PersonaItem = {
  id?: number;
  idDatosPersonales?: number;
  id_datos_personales?: number;
  idDatosPersonal?: number;
  documento?: string;
  tipo_documento?: string;
  tipoDocumento?: string;
  primerNombre?: string;
  segundoNombre?: string;
  primerApellido?: string;
  segundoApellido?: string;
  nombres?: string;
  apellidos?: string;
  tipo_persona?: string | number;
  tipoPersona?: string | number;
  [k: string]: any;
};

@Component({
  standalone: true,
  selector: 'app-datos-familiares-list',
  imports: [CommonModule, RouterLink, DatosFamiliaresExportButtonComponent],
  templateUrl: './datos-familiares-list.component.html',
  styleUrls: ['./datos-familiares-list.component.scss'],
})
export class DatosFamiliaresListComponent implements OnInit {
  private api = inject(DatosFamiliaresApi);
  private personasApi = inject(DatosPersonalesApi);
  private cat = inject(CatalogosApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private session = inject(SessionService);

  idDatosPersonales = signal<number | null>(null);
  afiliadoNombre = signal<string>('');  // ✅ nombre completo para el encabezado

  loading = signal<boolean>(true);
  error = signal<string | null>(null);

  // Selector de personas (cuando NO hay :idDatosPersonales)
  personas = signal<PersonaItem[]>([]);
  term = signal<string>('');
  filteredPersonas = computed(() => {
    const t = this.term().trim().toLowerCase();
    const base = this.personas();
    if (!t) return base;
    return base.filter(p => {
      const full =
        `${p.nombres ?? ''} ${p.apellidos ?? ''} ${p.primerNombre ?? ''} ${p.segundoNombre ?? ''} ${p.primerApellido ?? ''} ${p.segundoApellido ?? ''}`.toLowerCase();
      return String(p.documento ?? '').toLowerCase().includes(t) || full.includes(t);
    });
  });

  // Estado: ¿cada persona tiene familiares?
  familiaresEstado = signal<Record<number, 'loading' | 'yes' | 'no'>>({});

  // Catálogo parentescos → mapa { codigo: nombre }
  parentescosMap = signal<Record<string, string>>({});

  // Lista de familiares (cuando SÍ hay :idDatosPersonales)
  items = signal<DatosFamiliar[]>([]);

  // Acciones visibles siempre
  canEdit = computed(() => true);

  ngOnInit(): void {
    // Cargar catálogo de parentescos a mapa
    this.cat.parentescos().subscribe({
      next: (rows: CodigoNombreDTO[]) => {
        const map: Record<string, string> = {};
        (rows ?? []).forEach(r => {
          if (r?.codigo) map[String(r.codigo)] = String(r.nombre ?? r.codigo);
        });
        this.parentescosMap.set(map);
      },
      error: () => this.parentescosMap.set({}),
    });

    const id = this.route.snapshot.paramMap.get('idDatosPersonales');
    this.idDatosPersonales.set(id ? Number(id) : null);

    if (this.idDatosPersonales()) {
      // ✅ Cargar nombre del afiliado para el encabezado
      this.loadAfiliadoNombre(this.idDatosPersonales()!);
      this.loadFamiliares(this.idDatosPersonales()!);
    } else {
      this.loadPersonas();
    }
  }

  // ===== Cargar nombre completo del afiliado
  private loadAfiliadoNombre(id: number): void {
    // Se asume que el API tiene get(id); si tu servicio usa otro método,
    // ajusta esta llamada.
    (this.personasApi as any).get(id)?.subscribe({
      next: (p: any) => this.afiliadoNombre.set(this.composePersonaNombre(p)),
      error: () => this.afiliadoNombre.set(''),
    });
  }

  private composePersonaNombre(p: any): string {
    const nombres = (p?.nombres ?? `${p?.primerNombre ?? ''} ${p?.segundoNombre ?? ''}`).toString().trim();
    const apellidos = (p?.apellidos ?? `${p?.primerApellido ?? ''} ${p?.segundoApellido ?? ''}`).toString().trim();
    return `${nombres} ${apellidos}`.replace(/\s+/g, ' ').trim();
  }

  onSearch(ev: Event): void {
    const v = (ev.target as HTMLInputElement)?.value ?? '';
    this.term.set(v);
  }

  // Personas para seleccionar a quién gestionar familiares
  loadPersonas(): void {
    this.loading.set(true);
    this.error.set(null);

    this.personasApi.list().subscribe({
      next: (data: any) => {
        const arr: PersonaItem[] = Array.isArray(data) ? data : (data?.items ?? data?.content ?? []);
        // Solo excluimos jurídicas (2). Si no viene el campo, incluimos.
        const naturales = (arr ?? []).filter(p => {
          const tipo = String(p?.tipo_persona ?? p?.tipoPersona ?? '').trim();
          if (tipo === '2') return false;
          return true;
        });
        this.personas.set(naturales);
        this.loading.set(false);
        this.chequearFamiliaresParaLista();
      },
      error: () => {
        this.error.set('No fue posible cargar el listado de personas.');
        this.loading.set(false);
      },
    });
  }

  private chequearFamiliaresParaLista(): void {
    const estado = { ...this.familiaresEstado() };
    const lista = this.personas().slice(0, 100); // limita llamadas
    for (const p of lista) {
      const id = this.personaId(p);
      if (!id) continue;
      estado[id] = 'loading';
      this.familiaresEstado.set({ ...estado });

      this.api.listByPersona(id).subscribe({
        next: (rows) => {
          estado[id] = rows && rows.length > 0 ? 'yes' : 'no';
          this.familiaresEstado.set({ ...estado });
        },
        error: () => {
          estado[id] = 'no';
          this.familiaresEstado.set({ ...estado });
        },
      });
    }
  }

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

  /** Usado en el template para pintar el estado */
  estadoPersona(id: number | null | undefined): 'loading' | 'yes' | 'no' | null {
    if (!id) return null;
    return this.familiaresEstado()[id] ?? null;
  }

  // Navegaciones
  irFamiliares(p: PersonaItem): void {
    const id = this.personaId(p);
    if (!id) {
      console.warn('No se encontró idDatosPersonales en', p);
      alert('No se encontró el identificador de la persona.');
      return;
    }
    this.router.navigate(['/hoja-vida/datos-familiares', id]);
  }

  irNuevoFamiliar(p: PersonaItem): void {
    const id = this.personaId(p);
    if (!id) {
      console.warn('No se encontró idDatosPersonales en', p);
      alert('No se encontró el identificador de la persona.');
      return;
    }
    this.router.navigate(['/hoja-vida/datos-familiares', id, 'new']);
  }

  // Vista de familiares por persona
  loadFamiliares(idDatos: number): void {
    this.loading.set(true);
    this.error.set(null);

    this.api.listByPersona(idDatos).subscribe({
      next: (rows) => {
        this.items.set(rows ?? []);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No fue posible cargar los datos familiares.');
        this.loading.set(false);
      },
    });
  }

  // Botón "Regresar" desde la vista de familiares al selector
  goBackToSelector(): void {
    this.router.navigate(['/hoja-vida/datos-familiares']);
  }

  // PK real de la tabla
  trackById = (_: number, it: DatosFamiliar) => it.id_datos_familiares!;

  // Eliminar usando RUTA ANIDADA
  onDelete(it: DatosFamiliar): void {
    const idFam = it.id_datos_familiares;
    const idPer = this.idDatosPersonales();
    if (!idFam || !idPer) return;

    if (!confirm('¿Eliminar el registro seleccionado?')) return;

    this.api.deleteForPersona(idPer, idFam).subscribe({
      next: () => this.loadFamiliares(idPer),
      error: () => alert('No fue posible eliminar el registro.'),
    });
  }

  // ===== Helpers de presentación (evitan 'as any' y '??' en el template) =====
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

  private parseLooseNumber(v: any): number | null {
    if (v === null || v === undefined) return null;
    let s = String(v).trim();
    if (!s) return null;

    const direct = Number(s);
    if (Number.isFinite(direct)) return direct;

    const comma = s.lastIndexOf(',');
    const dot = s.lastIndexOf('.');

    if (comma > -1 && dot > -1) {
      if (comma > dot) s = s.replace(/\./g, '').replace(',', '.');
      else s = s.replace(/,/g, '');
    } else if (comma > -1) {
      s = s.replace(/\./g, '').replace(',', '.');
    } else if (dot > -1) {
      const parts = s.split('.');
      if (parts.length > 2) {
        const last = s.lastIndexOf('.');
        s = s.slice(0, last).replace(/\./g, '') + '.' + s.slice(last + 1).replace(/\./g, '');
      } else if (parts.length === 2 && parts[1].length === 3) {
        s = parts.join('');
      }
    }

    const n = Number(s);
    return Number.isFinite(n) ? n : null;
  }

  ing(it: any): number | null {
    return (
      this.parseLooseNumber(it?.ingresos_datos_familiar) ??
      this.parseLooseNumber(it?.ingresosDatosFamiliar) ??
      this.parseLooseNumber(it?.ingresos) ?? null
    );
  }

  egr(it: any): number | null {
    return (
      this.parseLooseNumber(it?.egresos_datos_familiar) ??
      this.parseLooseNumber(it?.egresosDatosFamiliar) ??
      this.parseLooseNumber(it?.egresos) ?? null
    );
  }

  money(v: number | null | undefined): string {
    if (v === null || v === undefined) return '';
    const n = Number(v);
    if (!Number.isFinite(n)) return '';
    return new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(n);
  }

  ref(it: any): boolean {
    return Boolean(it?.referencia_familiar ?? it?.referenciaFamiliar ?? false);
  }

  idFam(it: any): number | null {
    return (it?.id_datos_familiares ?? it?.idDatosFamiliares ?? null) as number | null;
  }
}
