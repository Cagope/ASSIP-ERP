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
  DatosFamiliaresApi
} from './datos-familiares.api';

import {
  DatosFamiliar
} from '../../../shared/models/datos-familiar.model';

import {
  DatosFamiliaresPrintService
} from './datos-familiares-print.service';

import {
  DatosFamiliaresExporterService
} from './datos-familiares-exporter.service';

type PersonaConFamiliares =
  DatosPersonales & {
    familiares: DatosFamiliar[];
  };

type FamiliarInforme =
  DatosFamiliar & {
    documento?: string;
    nombrePersona?: string;
  };

@Component({
  selector: 'app-datos-familiares-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './datos-familiares-list.component.html',
  styleUrls: ['./datos-familiares-list.component.scss']
})
export class DatosFamiliaresListComponent
  implements OnInit {

  private readonly dpApi =
    inject(DatosPersonalesApi);

  private readonly famApi =
    inject(DatosFamiliaresApi);

  private readonly router =
    inject(Router);

  private readonly printService =
    inject(DatosFamiliaresPrintService);

  private readonly exporter =
    inject(DatosFamiliaresExporterService);

  personas: PersonaConFamiliares[] = [];
  filtradas: PersonaConFamiliares[] = [];

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
      familiares: this.famApi.listar()
    })
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({
        next: resultado => {
          const mapaFamiliares =
            new Map<number, DatosFamiliar[]>();

          (resultado.familiares ?? []).forEach(
            familiar => {
              if (familiar.idDatosPersonal == null) {
                return;
              }

              const lista =
                mapaFamiliares.get(
                  familiar.idDatosPersonal
                ) ?? [];

              lista.push(familiar);

              mapaFamiliares.set(
                familiar.idDatosPersonal,
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
              familiares:
                persona.idDatosPersonal != null
                  ? mapaFamiliares.get(
                      persona.idDatosPersonal
                    ) ?? []
                  : []
            }));

          this.buscar();
        },
        error: err => {
          console.error(
            'Error cargando datos familiares:',
            err
          );

          this.error =
            'No fue posible cargar los datos familiares.';

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

  get paginadas(): PersonaConFamiliares[] {
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
    familiar?: DatosFamiliar | null
  ): void {

    this.error = '';

    if (!persona.idDatosPersonal) {
      this.error =
        'Asociado inválido: no tiene idDatosPersonal.';

      return;
    }

    if (familiar?.idDatosFamiliares) {
      this.router.navigate([
        '/hoja-vida/datos-familiares',
        familiar.idDatosFamiliares,
        'editar'
      ]);

      return;
    }

    this.router.navigate(
      [
        '/hoja-vida/datos-familiares/nuevo'
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
    const datos = this.construirDatosInforme();

    if (datos.length === 0) {
      alert(
        'No hay registros familiares para imprimir.'
      );

      return;
    }

    this.printService.imprimir(datos);
  }

  exportar(): void {
    const datos = this.construirDatosInforme();

    if (datos.length === 0) {
      alert(
        'No hay registros familiares para exportar.'
      );

      return;
    }

    this.exporter.exportarExcel(datos);
  }

  private construirDatosInforme():
    FamiliarInforme[] {

    return this.filtradas.flatMap(
      persona =>
        persona.familiares.map(
          familiar => ({
            ...familiar,
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
