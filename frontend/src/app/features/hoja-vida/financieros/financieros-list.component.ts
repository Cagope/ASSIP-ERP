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
  FinancierosApi
} from './financieros.api';

import {
  Financiero
} from '../../../shared/models/financiero.model';

import {
  FinancierosPrintService
} from './financieros-print.service';

import {
  FinancierosExporterService
} from './financieros-exporter.service';

type PersonaConFinanciero =
  DatosPersonales & {
    financiero: Financiero | null;
  };

type FinancieroInforme =
  Financiero & {
    documento?: string;
    nombrePersona?: string;
  };

@Component({
  selector: 'app-financieros-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './financieros-list.component.html',
  styleUrls: ['./financieros-list.component.scss']
})
export class FinancierosListComponent
  implements OnInit {

  private readonly dpApi =
    inject(DatosPersonalesApi);

  private readonly finApi =
    inject(FinancierosApi);

  private readonly router =
    inject(Router);

  private readonly printService =
    inject(FinancierosPrintService);

  private readonly exporter =
    inject(FinancierosExporterService);

  personas: PersonaConFinanciero[] = [];
  filtradas: PersonaConFinanciero[] = [];

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
      financieros: this.finApi.listar()
    })
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({
        next: resultado => {
          const mapaFinancieros =
            new Map<number, Financiero>();

          (resultado.financieros ?? []).forEach(
            financiero => {
              if (financiero.idDatosPersonal == null) {
                return;
              }

              mapaFinancieros.set(
                financiero.idDatosPersonal,
                financiero
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
              financiero:
                persona.idDatosPersonal != null
                  ? mapaFinancieros.get(
                      persona.idDatosPersonal
                    ) ?? null
                  : null
            }));

          this.buscar();
        },
        error: err => {
          console.error(
            'Error cargando datos financieros:',
            err
          );

          this.error =
            'No fue posible cargar los datos financieros.';

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

  get paginadas(): PersonaConFinanciero[] {
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
    financiero?: Financiero | null
  ): void {

    this.error = '';

    if (!persona.idDatosPersonal) {
      this.error =
        'Asociado inválido: no tiene idDatosPersonal.';

      return;
    }

    if (financiero?.idFinanciero) {
      this.router.navigate([
        '/hoja-vida/financieros',
        financiero.idFinanciero,
        'editar'
      ]);

      return;
    }

    this.router.navigate(
      [
        '/hoja-vida/financieros/nuevo'
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
        'No hay datos financieros para imprimir.'
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
        'No hay datos financieros para exportar.'
      );

      return;
    }

    this.exporter.exportarExcel(datos);
  }

  private construirDatosInforme():
    FinancieroInforme[] {

    return this.filtradas
      .filter(
        (
          persona
        ): persona is PersonaConFinanciero & {
          financiero: Financiero;
        } => persona.financiero != null
      )
      .map(persona => ({
        ...persona.financiero,
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
