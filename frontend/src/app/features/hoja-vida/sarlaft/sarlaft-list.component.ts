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
  SarlaftApi
} from './sarlaft.api';

import {
  Sarlaft
} from '../../../shared/models/sarlaft.model';

import {
  SarlaftPrintService
} from './sarlaft-print.service';

import {
  SarlaftExporterService
} from './sarlaft-exporter.service';

type PersonaConSarlaft =
  DatosPersonales & {
    sarlaft: Sarlaft | null;
  };

type SarlaftInforme =
  Sarlaft & {
    documento?: string;
    nombrePersona?: string;
  };

@Component({
  selector: 'app-sarlaft-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './sarlaft-list.component.html',
  styleUrls: ['./sarlaft-list.component.scss']
})
export class SarlaftListComponent
  implements OnInit {

  private readonly dpApi =
    inject(DatosPersonalesApi);

  private readonly sarlaftApi =
    inject(SarlaftApi);

  private readonly router =
    inject(Router);

  private readonly printService =
    inject(SarlaftPrintService);

  private readonly exporter =
    inject(SarlaftExporterService);

  personas: PersonaConSarlaft[] = [];
  filtradas: PersonaConSarlaft[] = [];

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
      sarlafts: this.sarlaftApi.listar()
    })
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({
        next: resultado => {
          const mapaSarlaft =
            new Map<number, Sarlaft>();

          (resultado.sarlafts ?? []).forEach(
            sarlaft => {
              if (sarlaft.idDatosPersonal == null) {
                return;
              }

              mapaSarlaft.set(
                sarlaft.idDatosPersonal,
                sarlaft
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
              sarlaft:
                persona.idDatosPersonal != null
                  ? mapaSarlaft.get(
                      persona.idDatosPersonal
                    ) ?? null
                  : null
            }));

          this.buscar();
        },
        error: err => {
          console.error(
            'Error cargando datos SARLAFT:',
            err
          );

          this.error =
            'No fue posible cargar los datos SARLAFT.';

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

  get paginadas(): PersonaConSarlaft[] {
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
    sarlaft?: Sarlaft | null
  ): void {

    this.error = '';

    if (!persona.idDatosPersonal) {
      this.error =
        'Asociado inválido: no tiene idDatosPersonal.';

      return;
    }

    if (sarlaft?.idSarlaft) {
      this.router.navigate([
        '/hoja-vida/sarlaft',
        sarlaft.idSarlaft,
        'editar'
      ]);

      return;
    }

    this.router.navigate(
      [
        '/hoja-vida/sarlaft/nuevo'
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
        'No hay datos SARLAFT para imprimir.'
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
        'No hay datos SARLAFT para exportar.'
      );

      return;
    }

    this.exporter.exportarExcel(datos);
  }

  private construirDatosInforme():
    SarlaftInforme[] {

    return this.filtradas
      .filter(
        (
          persona
        ): persona is PersonaConSarlaft & {
          sarlaft: Sarlaft;
        } => persona.sarlaft != null
      )
      .map(persona => ({
        ...persona.sarlaft,
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
