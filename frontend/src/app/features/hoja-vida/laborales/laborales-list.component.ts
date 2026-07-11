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
  LaboralesApi
} from './laborales.api';

import {
  Laboral
} from '../../../shared/models/laboral.model';

import {
  LaboralesPrintService
} from './laborales-print.service';

import {
  LaboralesExporterService
} from './laborales-exporter.service';

type PersonaConLaboral =
  DatosPersonales & {
    laboral: Laboral | null;
  };

type LaboralInforme =
  Laboral & {
    documento?: string;
    nombrePersona?: string;
  };

@Component({
  selector: 'app-laborales-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './laborales-list.component.html',
  styleUrls: ['./laborales-list.component.scss']
})
export class LaboralesListComponent
  implements OnInit {

  private readonly dpApi =
    inject(DatosPersonalesApi);

  private readonly labApi =
    inject(LaboralesApi);

  private readonly router =
    inject(Router);

  private readonly printService =
    inject(LaboralesPrintService);

  private readonly exporter =
    inject(LaboralesExporterService);

  personas: PersonaConLaboral[] = [];
  filtradas: PersonaConLaboral[] = [];

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
      laborales: this.labApi.listar()
    })
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({
        next: resultado => {
          const mapaLaborales =
            new Map<number, Laboral>();

          (resultado.laborales ?? []).forEach(
            laboral => {
              if (laboral.idDatosPersonal == null) {
                return;
              }

              mapaLaborales.set(
                laboral.idDatosPersonal,
                laboral
              );
            }
          );

          this.personas = (resultado.personas ?? [])
            .sort((a, b) => {
              const fechaA = a.fechaActualizacion
                ? Date.parse(a.fechaActualizacion)
                : 0;

              const fechaB = b.fechaActualizacion
                ? Date.parse(b.fechaActualizacion)
                : 0;

              return fechaB - fechaA;
            })
            .map(persona => ({
              ...persona,
              laboral:
                persona.idDatosPersonal != null
                  ? mapaLaborales.get(
                      persona.idDatosPersonal
                    ) ?? null
                  : null
            }));

          this.buscar();
        },
        error: err => {
          console.error(
            'Error cargando datos laborales:',
            err
          );

          this.error =
            'No fue posible cargar los datos laborales.';

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
            || documentoPersona.includes(
              documento
            )
          )
          &&
          (
            !nombres
            || nombresPersona.includes(
              nombres
            )
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
      }
    );

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

  get paginadas(): PersonaConLaboral[] {
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
        this.filtradas.length
        / this.tamanoPagina
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

  gestionar(
    persona: DatosPersonales,
    laboral?: Laboral | null
  ): void {

    this.error = '';

    if (!persona.idDatosPersonal) {
      this.error =
        'Asociado inválido: no tiene idDatosPersonal.';

      return;
    }

    if (laboral?.idLaboral) {
      this.router.navigate([
        '/hoja-vida/laborales',
        laboral.idLaboral,
        'editar'
      ]);

      return;
    }

    this.router.navigate(
      [
        '/hoja-vida/laborales/nuevo'
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
        'No hay datos laborales para imprimir.'
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
        'No hay datos laborales para exportar.'
      );

      return;
    }

    this.exporter.exportarExcel(datos);
  }

  private construirDatosInforme():
    LaboralInforme[] {

    return this.filtradas
      .filter(
        (
          persona
        ): persona is PersonaConLaboral & {
          laboral: Laboral;
        } => persona.laboral != null
      )
      .map(persona => ({
        ...persona.laboral,
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
