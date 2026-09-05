import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import {
  ReciprocidadAportesDetalle,
  ReciprocidadAportesPersona,
  ReciprocidadAportesResumen
} from './reciprocidad-aportes.models';

import {
  ReciprocidadAportesService
} from './reciprocidad-aportes.service';

import {
  ReciprocidadAportesExporterService
} from './reciprocidad-aportes-exporter.service';

type RangoFiltro =
  | 'TODOS'
  | '01. Menor a 5%'
  | '02. 5% a <10%'
  | '03. 10% a <20%'
  | '04. 20% a <50%'
  | '05. 50% a <100%'
  | '06. 100% o más';

@Component({
  selector: 'app-reciprocidad-aportes',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './reciprocidad-aportes.component.html',
  styleUrl: './reciprocidad-aportes.component.scss'
})
export class ReciprocidadAportesComponent implements OnInit {

  cortes: string[] = [];
  fechaCorte = '';

  resumen: ReciprocidadAportesResumen | null = null;
  personas: ReciprocidadAportesPersona[] = [];

  personaSeleccionada: ReciprocidadAportesPersona | null = null;
  detallePersona: ReciprocidadAportesDetalle[] = [];

  filtroDocumento = '';
  filtroNombre = '';
  filtroRango: RangoFiltro = 'TODOS';

  pagina = 1;
  tamanoPagina = 20;

  cargando = false;
  cargandoDetalle = false;
  exportando = false;

  error = '';
  errorDetalle = '';

  mostrarDetalle = false;

  readonly rangos: RangoFiltro[] = [
    'TODOS',
    '01. Menor a 5%',
    '02. 5% a <10%',
    '03. 10% a <20%',
    '04. 20% a <50%',
    '05. 50% a <100%',
    '06. 100% o más'
  ];

  constructor(
    private readonly service: ReciprocidadAportesService,
    private readonly exporter: ReciprocidadAportesExporterService
  ) {}

  ngOnInit(): void {
    this.cargarCortes();
  }

  cargarCortes(): void {
    this.cargando = true;
    this.error = '';

    this.service.listarCortes().subscribe({
      next: cortes => {
        this.cortes = cortes ?? [];
        this.fechaCorte = this.cortes[0] ?? '';

        // Terminó la carga de cortes.
        this.cargando = false;

        if (this.fechaCorte) {
          this.consultar();
        }
      },
      error: err => this.manejarError(
        err,
        'No fue posible consultar las fechas de corte disponibles.'
      )
    });
  }

  consultar(): void {
    if (!this.fechaCorte || this.cargando) {
      return;
    }

    this.cerrarDetalle();
    this.cargando = true;
    this.error = '';
    this.pagina = 1;

    forkJoin({
      resumen: this.service.obtenerResumen(this.fechaCorte),
      personas: this.service.listarPersonas(this.fechaCorte)
    }).subscribe({
      next: r => {
        this.resumen = r.resumen;
        this.personas = r.personas ?? [];
        this.cargando = false;
      },
      error: err => this.manejarError(
        err,
        'No fue posible cargar el análisis de reciprocidad de aportes.'
      )
    });
  }

  limpiarFiltros(): void {
    this.filtroDocumento = '';
    this.filtroNombre = '';
    this.filtroRango = 'TODOS';
    this.pagina = 1;
  }

  aplicarFiltros(): void {
    this.pagina = 1;
  }

  get personasFiltradas(): ReciprocidadAportesPersona[] {
    const documento = this.normalizar(this.filtroDocumento);
    const nombre = this.normalizar(this.filtroNombre);

    return this.personas.filter(p => {
      const okDocumento =
        !documento ||
        this.normalizar(p.documento).includes(documento);

      const okNombre =
        !nombre ||
        this.normalizar(p.nombreCompleto).includes(nombre);

      const okRango =
        this.filtroRango === 'TODOS' ||
        p.rangoReciprocidad === this.filtroRango;

      return okDocumento && okNombre && okRango;
    });
  }

  get totalPaginas(): number {
    return Math.max(
      Math.ceil(this.personasFiltradas.length / this.tamanoPagina),
      1
    );
  }

  get personasPaginadas(): ReciprocidadAportesPersona[] {
    if (this.pagina > this.totalPaginas) {
      this.pagina = this.totalPaginas;
    }

    const inicio = (this.pagina - 1) * this.tamanoPagina;
    return this.personasFiltradas.slice(
      inicio,
      inicio + this.tamanoPagina
    );
  }

  irPrimera(): void {
    this.pagina = 1;
  }

  irAnterior(): void {
    this.pagina = Math.max(this.pagina - 1, 1);
  }

  irSiguiente(): void {
    this.pagina = Math.min(this.pagina + 1, this.totalPaginas);
  }

  irUltima(): void {
    this.pagina = this.totalPaginas;
  }

  cambiarTamanoPagina(): void {
    this.pagina = 1;
  }

  verDetalle(
    persona: ReciprocidadAportesPersona
  ): void {
    this.personaSeleccionada = persona;
    this.detallePersona = [];
    this.errorDetalle = '';
    this.cargandoDetalle = true;
    this.mostrarDetalle = true;

    this.service
      .listarDetallePersona(
        persona.idDatosPersonal,
        this.fechaCorte
      )
      .subscribe({
        next: detalle => {
          this.detallePersona = detalle ?? [];
          this.cargandoDetalle = false;
        },
        error: err => {
          this.errorDetalle = this.obtenerMensajeError(
            err,
            'No fue posible consultar el detalle de créditos de la persona.'
          );
          this.cargandoDetalle = false;
        }
      });
  }

  cerrarDetalle(): void {
    this.mostrarDetalle = false;
    this.personaSeleccionada = null;
    this.detallePersona = [];
    this.errorDetalle = '';
    this.cargandoDetalle = false;
  }

  exportarExcel(): void {
    if (!this.fechaCorte || !this.resumen || !this.personas.length) {
      return;
    }

    this.exportando = true;
    this.error = '';

    this.service.listarDetalle(this.fechaCorte).subscribe({
      next: detalle => {
        this.exporter.exportar(
          this.resumen!,
          this.personas,
          detalle ?? [],
          this.fechaCorte
        );
        this.exportando = false;
      },
      error: err => {
        this.error = this.obtenerMensajeError(
          err,
          'No fue posible obtener el detalle auditable para exportar.'
        );
        this.exportando = false;
      }
    });
  }

  claseRango(
    rango: string | null | undefined
  ): string {
    if (!rango) {
      return 'estado';
    }

    if (rango.startsWith('06.')) {
      return 'estado estado--alto';
    }

    if (rango.startsWith('05.')) {
      return 'estado estado--medio';
    }

    return 'estado';
  }

  trackByPersona(
    _index: number,
    item: ReciprocidadAportesPersona
  ): number {
    return item.idDatosPersonal;
  }

  trackByCredito(
    _index: number,
    item: ReciprocidadAportesDetalle
  ): number {
    return item.idCierreCarteraCredito;
  }

  private normalizar(
    value: string | null | undefined
  ): string {
    return (value ?? '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim();
  }

  private manejarError(
    err: unknown,
    mensaje: string
  ): void {
    console.error(err);
    this.error = this.obtenerMensajeError(err, mensaje);
    this.cargando = false;
  }

  private obtenerMensajeError(
    err: any,
    mensaje: string
  ): string {
    return err?.error?.message ||
      err?.error?.error ||
      (typeof err?.error === 'string' ? err.error : null) ||
      mensaje;
  }
}
