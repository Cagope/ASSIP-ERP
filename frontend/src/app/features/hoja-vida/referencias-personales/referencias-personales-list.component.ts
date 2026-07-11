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
  ReferenciasPersonalesApi
} from './referencias-personales.api';

import {
  ReferenciasPersonalesPrintService
} from './referencias-personales-print.service';

import {
  ReferenciasPersonalesExporterService
} from './referencias-personales-exporter.service';

import {
  ReferenciaPersonal
} from '../../../shared/models/referencia-personal.model';

type PersonaConReferencias =
  DatosPersonales & {
    referencias: ReferenciaPersonal[];
  };

type ReferenciaPersonalInforme =
  ReferenciaPersonal & {
    documento?: string;
    nombrePersona?: string;
  };

@Component({
  selector: 'app-referencias-personales-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './referencias-personales-list.component.html',
  styleUrls: ['./referencias-personales-list.component.scss']
})
export class ReferenciasPersonalesListComponent
  implements OnInit {

  private readonly dpApi =
    inject(DatosPersonalesApi);

  private readonly refApi =
    inject(ReferenciasPersonalesApi);

  private readonly router =
    inject(Router);

  private readonly printService =
    inject(ReferenciasPersonalesPrintService);

  private readonly exporter =
    inject(ReferenciasPersonalesExporterService);

  personas: PersonaConReferencias[] = [];
  filtradas: PersonaConReferencias[] = [];

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
      referencias: this.refApi.listar()
    })
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({
        next: resultado => {
          const mapaReferencias =
            new Map<number, ReferenciaPersonal[]>();

          (resultado.referencias ?? []).forEach(
            referencia => {
              if (
                referencia.idDatosPersonal == null
              ) {
                return;
              }

              const lista =
                mapaReferencias.get(
                  referencia.idDatosPersonal
                ) ?? [];

              lista.push(referencia);

              mapaReferencias.set(
                referencia.idDatosPersonal,
                lista
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
              referencias:
                persona.idDatosPersonal != null
                  ? mapaReferencias.get(
                      persona.idDatosPersonal
                    ) ?? []
                  : []
            }));

          this.buscar();
        },
        error: err => {
          console.error(
            'Error cargando referencias personales:',
            err
          );

          this.error =
            'No fue posible cargar las referencias personales.';

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

  get paginadas(): PersonaConReferencias[] {
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
    referencia?: ReferenciaPersonal | null
  ): void {

    this.error = '';

    if (!persona.idDatosPersonal) {
      this.error =
        'Asociado inválido: no tiene idDatosPersonal.';

      return;
    }

    if (referencia?.idReferenciaPersonal) {
      this.router.navigate([
        '/hoja-vida/referencias-personales',
        referencia.idReferenciaPersonal,
        'editar'
      ]);

      return;
    }

    this.router.navigate(
      [
        '/hoja-vida/referencias-personales/nuevo'
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
        'No hay referencias personales para imprimir.'
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
        'No hay referencias personales para exportar.'
      );

      return;
    }

    this.exporter.exportarExcel(datos);
  }

  private construirDatosInforme():
    ReferenciaPersonalInforme[] {

    return this.filtradas.flatMap(
      persona =>
        persona.referencias.map(
          referencia => ({
            ...referencia,
            documento: persona.documento,
            nombrePersona: [
              persona.nombres,
              persona.primerApellido,
              persona.segundoApellido
            ]
              .filter(Boolean)
              .join(' ')
          })
        )
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
