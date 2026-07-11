import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';

import {
  HeaderActionsComponent
} from '../../../shared/header-actions/header-actions.component';

import {
  DatosPersonalesApi,
  DatosPersonales
} from '../datos-personales/datos-personales.api';

import {
  UbicacionesApi
} from './ubicaciones.api';

import {
  UbicacionesPrintService
} from './ubicaciones-print.service';

import {
  UbicacionesExporterService
} from './ubicaciones-exporter.service';

import {
  Ubicacion
} from '../../../shared/models/ubicacion.model';

type PersonaConUbicacion =
  DatosPersonales & {
    ubicacion: Ubicacion | null;
  };

type UbicacionInforme =
  Ubicacion & {
    documento?: string;
    nombrePersona?: string;
    nombrePais?: string;
    nombreDepartamento?: string;
    nombreCiudad?: string;
    nombreSubZona?: string;
  };

@Component({
  selector: 'app-ubicaciones-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './ubicaciones-list.component.html',
  styleUrls: ['./ubicaciones-list.component.scss']
})
export class UbicacionesListComponent
  implements OnInit {

  private readonly dpApi =
    inject(DatosPersonalesApi);

  private readonly ubApi =
    inject(UbicacionesApi);

  private readonly router =
    inject(Router);

  private readonly printService =
    inject(UbicacionesPrintService);

  private readonly exporter =
    inject(UbicacionesExporterService);

  personas: PersonaConUbicacion[] = [];
  filtradas: PersonaConUbicacion[] = [];

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

    forkJoin({
      personas: this.dpApi.listar(),
      ubicaciones: this.ubApi.listar()
    })
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({
        next: resultado => {

          const mapaUbicaciones =
            new Map<number, Ubicacion>();

          (resultado.ubicaciones ?? []).forEach(
            ubicacion => {

              if (ubicacion.idDatosPersonal == null) {
                return;
              }

              mapaUbicaciones.set(
                ubicacion.idDatosPersonal,
                ubicacion
              );
            }
          );

          this.personas = (resultado.personas ?? [])
            .sort((a, b) => {

              const fechaA =
                a.fechaActualizacion
                  ? Date.parse(a.fechaActualizacion)
                  : 0;

              const fechaB =
                b.fechaActualizacion
                  ? Date.parse(b.fechaActualizacion)
                  : 0;

              return fechaB - fechaA;

            })
            .map(persona => ({
              ...persona,
              ubicacion:
                persona.idDatosPersonal != null
                  ? mapaUbicaciones.get(
                      persona.idDatosPersonal
                    ) ?? null
                  : null
            }));

          this.buscar();
        },

        error: err => {

          console.error(
            'Error cargando ubicaciones:',
            err
          );

          this.error =
            'No fue posible cargar las ubicaciones.';

          this.personas = [];
          this.filtradas = [];
          this.pagina = 1;
        }
      });
  }

  buscar(): void {

    const documento =
      this.normalizarTexto(
        this.filtros.documento
      );

    const nombres =
      this.normalizarTexto(
        this.filtros.nombres
      );

    const primerApellido =
      this.normalizarTexto(
        this.filtros.primerApellido
      );

    const segundoApellido =
      this.normalizarTexto(
        this.filtros.segundoApellido
      );

    this.filtradas = this.personas.filter(
      persona => {

        const documentoPersona =
          this.normalizarTexto(
            persona.documento
          );

        const nombresPersona =
          this.normalizarTexto(
            persona.nombres
          );

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
    this.error = '';
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

  get paginadas():
    PersonaConUbicacion[] {

    const inicio =
      (this.pagina - 1)
      * this.tamanoPagina;

    return this.filtradas.slice(
      inicio,
      inicio + this.tamanoPagina
    );
  }

  totalPaginas(): number {

    return Math.max(
      1,
      Math.ceil(
        this.filtradas.length
        / this.tamanoPagina
      )
    );
  }

  cambiarPagina(
    pagina: number
  ): void {

    if (
      pagina < 1
      || pagina > this.totalPaginas()
    ) {
      return;
    }

    this.pagina = pagina;
  }

  gestionar(
    persona: DatosPersonales,
    ubicacion?: Ubicacion | null
  ): void {

    this.error = '';

    if (!persona.idDatosPersonal) {

      this.error =
        'Asociado inválido: no tiene idDatosPersonal.';

      return;
    }

    if (ubicacion?.idUbicacion) {

      this.router.navigate([
        '/hoja-vida/ubicaciones',
        ubicacion.idUbicacion,
        'editar'
      ]);

      return;
    }

    this.router.navigate(
      [
        '/hoja-vida/ubicaciones/nuevo'
      ],
      {
        queryParams: {
          idDatosPersonal:
            persona.idDatosPersonal
        }
      }
    );
  }

  imprimir(): void {

    const datos =
      this.construirDatosInforme();

    if (datos.length === 0) {

      alert(
        'No hay ubicaciones para imprimir.'
      );

      return;
    }

    this.printService.imprimir(datos);
  }

  exportar(): void {

    const datos =
      this.construirDatosInforme();

    if (datos.length === 0) {

      alert(
        'No hay ubicaciones para exportar.'
      );

      return;
    }

    this.exporter.exportarExcel(datos);
  }

  private construirDatosInforme():
    UbicacionInforme[] {

    return this.filtradas
      .filter(
        (
          persona
        ): persona is PersonaConUbicacion & {
          ubicacion: Ubicacion;
        } =>
          persona.ubicacion != null
      )
      .map(persona => ({
        ...persona.ubicacion,
        documento: persona.documento,
        nombrePersona: [
          persona.nombres,
          persona.primerApellido,
          persona.segundoApellido
        ]
          .filter(Boolean)
          .join(' ')
      }));
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
