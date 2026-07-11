import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';

import {
  DatosPersonalesApi,
  DatosPersonales
} from './datos-personales.api';

import {
  DatosPersonalesPrintService
} from './datos-personales-print.service';

import {
  DatosPersonalesExporterService
} from './datos-personales-exporter.service';

@Component({
  selector: 'app-datos-personales-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './datos-personales-list.component.html',
  styleUrls: ['./datos-personales-list.component.scss']
})
export class DatosPersonalesListComponent implements OnInit {

  private readonly api =
    inject(DatosPersonalesApi);

  private readonly router =
    inject(Router);

  private readonly printService =
    inject(DatosPersonalesPrintService);

  private readonly exporter =
    inject(DatosPersonalesExporterService);

  personas: DatosPersonales[] = [];
  filtradas: DatosPersonales[] = [];

  cargando = false;
  error = '';

  filtros = {
    documento: '',
    nombres: '',
    primerApellido: '',
    segundoApellido: ''
  };

  pagina = 1;
  tamanoPagina = 10;

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';

    this.api.listar().subscribe({
      next: lista => {
        this.personas = (lista ?? []).sort((a, b) => {
          const fechaA = a.fechaActualizacion
            ? Date.parse(a.fechaActualizacion)
            : 0;

          const fechaB = b.fechaActualizacion
            ? Date.parse(b.fechaActualizacion)
            : 0;

          return fechaB - fechaA;
        });

        this.buscar();
      },
      error: err => {
        console.error(
          'Error cargando datos personales:',
          err
        );

        this.error =
          'No fue posible cargar los datos personales.';

        this.personas = [];
        this.filtradas = [];
        this.pagina = 1;
        this.cargando = false;
      },
      complete: () => {
        this.cargando = false;
      }
    });
  }

  buscar(): void {
    const documento =
      this.normalizarTexto(this.filtros.documento);

    const nombres =
      this.normalizarTexto(this.filtros.nombres);

    const primerApellido =
      this.normalizarTexto(
        this.filtros.primerApellido
      );

    const segundoApellido =
      this.normalizarTexto(
        this.filtros.segundoApellido
      );

    this.filtradas = this.personas.filter(persona => {
      const documentoPersona =
        this.normalizarTexto(persona.documento);

      const nombresPersona =
        this.normalizarTexto(persona.nombres);

      const primerApellidoPersona =
        this.normalizarTexto(
          persona.primerApellido
        );

      const segundoApellidoPersona =
        this.normalizarTexto(
          persona.segundoApellido
        );

      return (
        (
          !documento
          || documentoPersona.includes(documento)
        )
        &&
        (
          !nombres
          || nombresPersona.includes(nombres)
        )
        &&
        (
          !primerApellido
          || primerApellidoPersona.includes(
            primerApellido
          )
        )
        &&
        (
          !segundoApellido
          || segundoApellidoPersona.includes(
            segundoApellido
          )
        )
      );
    });

    this.pagina = 1;
  }

  limpiar(): void {
    this.filtros = {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: ''
    };

    this.filtradas = [...this.personas];
    this.pagina = 1;
    this.error = '';
  }

  get paginadas(): DatosPersonales[] {
    const inicio =
      (this.pagina - 1) * this.tamanoPagina;

    return this.filtradas.slice(
      inicio,
      inicio + this.tamanoPagina
    );
  }

  totalPaginas(): number {
    return Math.max(
      1,
      Math.ceil(
        this.filtradas.length / this.tamanoPagina
      )
    );
  }

  cambiarPagina(pagina: number): void {
    if (
      pagina < 1
      || pagina > this.totalPaginas()
    ) {
      return;
    }

    this.pagina = pagina;
  }

  nuevo(): void {
    this.error = '';

    this.router.navigate([
      '/hoja-vida/datos-personales/nuevo'
    ]);
  }

  gestionar(persona: DatosPersonales): void {
    if (!persona.idDatosPersonal) {
      this.error =
        'Registro inválido: no tiene idDatosPersonal.';

      return;
    }

    this.error = '';

    this.router.navigate([
      '/hoja-vida/datos-personales',
      persona.idDatosPersonal,
      'editar'
    ]);
  }

  imprimir(): void {
    if (this.filtradas.length === 0) {
      alert('No hay registros para imprimir.');
      return;
    }

    this.printService.imprimir(
      this.filtradas
    );
  }

  exportar(): void {
    if (this.filtradas.length === 0) {
      alert('No hay registros para exportar.');
      return;
    }

    this.exporter.exportarExcel(
      this.filtradas
    );
  }

  private normalizarTexto(
    valor: string | number | null | undefined
  ): string {

    return String(valor ?? '')
      .trim()
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }
}
