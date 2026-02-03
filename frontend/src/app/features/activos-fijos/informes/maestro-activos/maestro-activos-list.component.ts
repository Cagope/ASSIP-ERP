import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { ActivosFijosInformesApi } from '../activos-fijos-informes.api';
import { SessionService } from '../../../../core/auth/session.service';

import { GeneralApi } from '../../../../shared/general/general.api';
import { BloquesApi, BloqueListDTO } from '../../bloques/bloques.api';
import { LocalizacionesApi, LocalizacionListDTO } from '../../localizaciones/localizaciones.api';

import { PersonasApi, PersonaBusquedaDTO } from '../../../../shared/personas/personas.api';
import { MaestroActivosPrintService } from './maestro-activos-print.service';
import { MaestroActivosExporterService } from './maestro-activos-exporter.service';



@Component({
  standalone: true,
  selector: 'app-maestro-activos-list',
  templateUrl: './maestro-activos-list.component.html',
  styleUrls: ['./maestro-activos-list.component.scss'],
  imports: [CommonModule, FormsModule, HeaderActionsComponent]
})
export class MaestroActivosListComponent implements OnInit {

  private readonly api = inject(ActivosFijosInformesApi);
  private readonly session = inject(SessionService);

  private readonly generalApi = inject(GeneralApi);
  private readonly bloquesApi = inject(BloquesApi);
  private readonly localizacionesApi = inject(LocalizacionesApi);
  private readonly personasApi = inject(PersonasApi);
  private readonly printService = inject(MaestroActivosPrintService);
  private readonly exporter = inject(MaestroActivosExporterService);

  // ============================================================
  // ✅ DATA
  // ============================================================
  rows: any[] = [];
  rowsFiltradas: any[] = [];

  // ============================================================
  // ✅ UI
  // ============================================================
  loading = false;
  errorMsg = '';

  // ============================================================
  // ✅ COMBOS
  // ============================================================
  agencias: any[] = [];
  bloques: BloqueListDTO[] = [];
  localizaciones: LocalizacionListDTO[] = [];

  // ============================================================
  // ✅ FILTROS (selección)
  // ============================================================
  idAgencia: number | null = null;
  idBloque: number | null = null;
  idLocalizacion: number | null = null;

  idResponsable: number | null = null;
  idProveedor: number | null = null;

  // ============================================================
  // ✅ AUTOCOMPLETE PERSONAS
  // ============================================================
  responsableText = '';
  proveedorText = '';

  responsables: PersonaBusquedaDTO[] = [];
  proveedores: PersonaBusquedaDTO[] = [];

  // ============================================================
  // ✅ INIT
  // ============================================================
  ngOnInit(): void {

    // ===============================
    // ✅ Cargar combos
    // ===============================
    this.cargarAgencias();
    this.cargarBloques();
    this.cargarLocalizaciones();

    // ===============================
    // ✅ Agencia activa por defecto (robusta)
    // ===============================
    try {
      const agActiva = (this.session as any).getAgenciaActiva?.();
      const id = agActiva?.idAgencia ?? agActiva?.id ?? null;
      if (id != null) {
        this.idAgencia = Number(id);
      }
    } catch {
      // nada
    }
  }

  // ============================================================
  // ✅ COMBOS
  // ============================================================
  private cargarAgencias(): void {
    this.generalApi.listarAgencias().subscribe({
      next: (data: any[]) => this.agencias = data || [],
      error: (err) => {
        console.error(err);
        this.agencias = [];
      }
    });
  }

  private cargarBloques(): void {
    this.bloquesApi.listar().subscribe({
      next: (data: any[]) => this.bloques = data || [],
      error: (err) => {
        console.error(err);
        this.bloques = [];
      }
    });
  }

  private cargarLocalizaciones(): void {
    this.localizacionesApi.listar().subscribe({
      next: (data: any[]) => this.localizaciones = data || [],
      error: (err) => {
        console.error(err);
        this.localizaciones = [];
      }
    });
  }

  // ============================================================
  // ✅ AUTOCOMPLETE RESPONSABLE
  // ============================================================
  buscarResponsable(): void {
    const text = (this.responsableText || '').trim();

    if (text.length < 3) {
      this.responsables = [];
      return;
    }

    this.personasApi.buscar(text).subscribe({
      next: (data) => this.responsables = data || [],
      error: (err) => {
        console.error(err);
        this.responsables = [];
      }
    });
  }

  seleccionarResponsable(p: PersonaBusquedaDTO): void {
    this.idResponsable = p.idDatosPersonal;
    this.responsableText = `${p.nombreCompleto} (${p.documento})`;
    this.responsables = [];

    // ✅ Filtrar inmediatamente
    this.aplicarFiltrosSeleccion();
  }

  limpiarResponsable(): void {
    this.idResponsable = null;
    this.responsableText = '';
    this.responsables = [];

    // ✅ Filtrar inmediatamente
    this.aplicarFiltrosSeleccion();
  }

  // ============================================================
  // ✅ AUTOCOMPLETE PROVEEDOR
  // ============================================================
  buscarProveedor(): void {
    const text = (this.proveedorText || '').trim();

    if (text.length < 3) {
      this.proveedores = [];
      return;
    }

    this.personasApi.buscar(text).subscribe({
      next: (data) => this.proveedores = data || [],
      error: (err) => {
        console.error(err);
        this.proveedores = [];
      }
    });
  }

  seleccionarProveedor(p: PersonaBusquedaDTO): void {
    this.idProveedor = p.idDatosPersonal;
    this.proveedorText = `${p.nombreCompleto} (${p.documento})`;
    this.proveedores = [];

    // ✅ Filtrar inmediatamente
    this.aplicarFiltrosSeleccion();
  }

  limpiarProveedor(): void {
    this.idProveedor = null;
    this.proveedorText = '';
    this.proveedores = [];

    // ✅ Filtrar inmediatamente
    this.aplicarFiltrosSeleccion();
  }

  // ============================================================
  // ✅ Cuando cambies un select (agencia/bloque/localización)
  // ============================================================
  onCambioFiltro(): void {
    this.aplicarFiltrosSeleccion();
  }

  // ============================================================
  // ✅ CONSULTAR (backend una sola vez)
  // ============================================================
  consultar(): void {

    if (this.idAgencia == null) {
      this.errorMsg = 'Debe seleccionar una agencia para consultar el maestro de activos.';
      return;
    }

    this.loading = true;
    this.errorMsg = '';
    this.rows = [];
    this.rowsFiltradas = [];

    this.api.maestroActivos().subscribe({
      next: (data: any[]) => {

        this.rows = data || [];

        // ✅ aplicar filtros selección
        this.aplicarFiltrosSeleccion();

        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'No fue posible cargar el maestro de activos.';
        this.loading = false;
      }
    });
  }

  // ============================================================
  // ✅ FILTROS SELECCIÓN (FRONT)
  // ============================================================
  aplicarFiltrosSeleccion(): void {

    // ✅ si aún no hay data, no hacer nada
    if (!this.rows || this.rows.length === 0) {
      this.rowsFiltradas = [];
      return;
    }

    let data = [...this.rows];

    // ✅ Agencia
    if (this.idAgencia != null) {
      data = data.filter(r => Number(r?.id_agencia ?? 0) === Number(this.idAgencia));
    }

    // ✅ Bloque
    if (this.idBloque != null) {
      data = data.filter(r => Number(r?.id_bloque ?? 0) === Number(this.idBloque));
    }

    // ✅ Localización
    if (this.idLocalizacion != null) {
      data = data.filter(r => Number(r?.id_localizacion ?? 0) === Number(this.idLocalizacion));
    }

    // ✅ Responsable
    if (this.idResponsable != null) {
      data = data.filter(r =>
        Number(r?.id_datos_personal_responsable ?? 0) === Number(this.idResponsable)
      );
    }

    // ✅ Proveedor
    if (this.idProveedor != null) {
      data = data.filter(r =>
        Number(r?.id_datos_personal_proveedor ?? 0) === Number(this.idProveedor)
      );
    }

    this.rowsFiltradas = data;
  }

  // ============================================================
  // ✅ LIMPIAR FILTROS (sin tocar agencia)
  // ============================================================
  limpiarFiltros(): void {

    this.idBloque = null;
    this.idLocalizacion = null;

    this.idResponsable = null;
    this.responsableText = '';
    this.responsables = [];

    this.idProveedor = null;
    this.proveedorText = '';
    this.proveedores = [];

    // ✅ recalcular con datos cargados
    this.aplicarFiltrosSeleccion();
  }

  imprimir(): void {

    if (!this.rowsFiltradas || this.rowsFiltradas.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    // ✅ Nombre de agencia (si aplica)
    const nombreAgencia =
      this.agencias.find(a => Number(a.idAgencia) === Number(this.idAgencia))
        ?.nombreAgencia ?? 'TODAS';

    // ✅ imprime SOLO lo filtrado
    setTimeout(() => {
      this.printService.imprimir(this.rowsFiltradas, nombreAgencia);
    }, 50);

  }

  exportar(): void {

    if (!this.rowsFiltradas || this.rowsFiltradas.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const nombreAgencia =
      this.agencias.find(a => Number(a.idAgencia) === Number(this.idAgencia))
        ?.nombreAgencia ?? 'TODAS';

    // ✅ FULL EXPORT (solo filtrado)
    this.exporter.exportar(this.rowsFiltradas, nombreAgencia);
  }


  // ============================================================
  // ✅ Helpers
  // ============================================================
  get total(): number {
    return this.rowsFiltradas.length;
  }
}
