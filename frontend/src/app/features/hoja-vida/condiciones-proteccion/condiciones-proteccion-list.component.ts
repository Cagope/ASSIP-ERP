import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormsModule
} from '@angular/forms';

import {
  Router
} from '@angular/router';

import {
  finalize,
  forkJoin
} from 'rxjs';

import {
  HeaderActionsComponent
} from '../../../shared/header-actions/header-actions.component';

import {
  DatosPersonales,
  DatosPersonalesApi
} from '../datos-personales/datos-personales.api';

import {
  CondicionProteccion,
  CondicionesProteccionApi
} from './condiciones-proteccion.api';

import {
  CondicionesProteccionPrintService
} from './condiciones-proteccion-print.service';

import {
  CondicionesProteccionExporterService
} from './condiciones-proteccion-exporter.service';

/**
 * Persona del listado general con su información
 * de condiciones de protección asociada.
 */
type PersonaConCondicionProteccion =
  DatosPersonales & {
    condicionProteccion:
      CondicionProteccion | null;
  };

/**
 * Estructura utilizada para impresión
 * y exportación.
 */
export type CondicionProteccionInforme =
  CondicionProteccion & {
    documento?: string;
    nombrePersona?: string;
  };

@Component({
  selector:
    'app-condiciones-proteccion-list',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],

  templateUrl:
    './condiciones-proteccion-list.component.html',

  styleUrls: [
    './condiciones-proteccion-list.component.scss'
  ]
})
export class CondicionesProteccionListComponent
  implements OnInit {

  private readonly datosPersonalesApi =
    inject(DatosPersonalesApi);

  private readonly condicionesProteccionApi =
    inject(CondicionesProteccionApi);

  private readonly router =
    inject(Router);

  private readonly printService =
    inject(CondicionesProteccionPrintService);

  private readonly exporter =
    inject(CondicionesProteccionExporterService);

  personas:
    PersonaConCondicionProteccion[] = [];

  filtradas:
    PersonaConCondicionProteccion[] = [];

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

  // ============================================================
  // CARGA INICIAL
  // ============================================================

  cargar(): void {
    this.cargando = true;
    this.error = '';

    forkJoin({
      personas:
        this.datosPersonalesApi.listar(),

      condiciones:
        this.condicionesProteccionApi.listar()
    })
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({
        next: resultado => {
          const mapaCondiciones =
            new Map<
              number,
              CondicionProteccion
            >();

          (
            resultado.condiciones ?? []
          ).forEach(condicion => {
            if (
              condicion.idDatosPersonal == null
            ) {
              return;
            }

            mapaCondiciones.set(
              condicion.idDatosPersonal,
              condicion
            );
          });

          this.personas =
            (resultado.personas ?? [])
              .sort((a, b) => {
                const fechaA =
                  a.fechaActualizacion
                    ? Date.parse(
                        a.fechaActualizacion
                      )
                    : 0;

                const fechaB =
                  b.fechaActualizacion
                    ? Date.parse(
                        b.fechaActualizacion
                      )
                    : 0;

                return fechaB - fechaA;
              })
              .map(persona => ({
                ...persona,

                condicionProteccion:
                  persona.idDatosPersonal != null
                    ? mapaCondiciones.get(
                        persona.idDatosPersonal
                      ) ?? null
                    : null
              }));

          this.buscar();
        },

        error: err => {
          console.error(
            'Error cargando condiciones de protección:',
            err
          );

          this.error =
            'No fue posible cargar la información de condiciones de protección.';

          this.personas = [];
          this.filtradas = [];
          this.pagina = 1;
        }
      });
  }

  // ============================================================
  // FILTROS
  // ============================================================

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

    this.filtradas =
      this.personas.filter(persona => {
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

    this.filtradas = [
      ...this.personas
    ];

    this.pagina = 1;
    this.error = '';
  }

  // ============================================================
  // PAGINACIÓN
  // ============================================================

  get paginadas():
    PersonaConCondicionProteccion[] {

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

  // ============================================================
  // GESTIONAR
  // ============================================================

  gestionar(
    persona: DatosPersonales,
    condicionProteccion?:
      CondicionProteccion | null
  ): void {

    this.error = '';

    if (!persona.idDatosPersonal) {
      this.error =
        'Asociado inválido: no tiene idDatosPersonal.';

      return;
    }

    if (
      condicionProteccion
        ?.idCondicionProteccion
    ) {
      this.router.navigate([
        '/hoja-vida/condiciones-proteccion',
        condicionProteccion
          .idCondicionProteccion,
        'editar'
      ]);

      return;
    }

    this.router.navigate(
      [
        '/hoja-vida/condiciones-proteccion/nuevo'
      ],
      {
        queryParams: {
          idDatosPersonal:
            persona.idDatosPersonal
        }
      }
    );
  }

  // ============================================================
  // IMPRESIÓN Y EXPORTACIÓN
  // ============================================================

  imprimir(): void {
    const datos =
      this.construirDatosInforme();

    if (datos.length === 0) {
      alert(
        'No hay información de condiciones de protección para imprimir.'
      );

      return;
    }

    this.printService.imprimir(
      datos
    );
  }

  exportar(): void {
    const datos =
      this.construirDatosInforme();

    if (datos.length === 0) {
      alert(
        'No hay información de condiciones de protección para exportar.'
      );

      return;
    }

    this.exporter.exportarExcel(
      datos
    );
  }

  private construirDatosInforme():
    CondicionProteccionInforme[] {

    return this.filtradas
      .filter(
        (
          persona
        ): persona is
          PersonaConCondicionProteccion & {
            condicionProteccion:
              CondicionProteccion;
          } =>
          persona.condicionProteccion
            != null
      )
      .map(persona => ({
        ...persona.condicionProteccion,

        documento:
          persona.documento,

        nombrePersona: [
          persona.nombres,
          persona.primerApellido,
          persona.segundoApellido
        ]
          .filter(Boolean)
          .join(' ')
      }));
  }

  // ============================================================
  // PRESENTACIÓN
  // ============================================================

  tieneAlgunaCondicion(
    condicion:
      CondicionProteccion | null
  ): boolean {

    if (!condicion) {
      return false;
    }

    return Boolean(
      condicion.administraRecursosPublicos
      || condicion
        .grupoProteccionEspecialConstitucional
      || condicion.personaMayor60Anos
      || condicion.discapacidadFisica
      || condicion.victimaConflictoArmado
      || condicion.pobrezaExtrema
      || condicion.poblacionIndigena
      || condicion.poblacionAfrodescendiente
      || condicion.poblacionLgbtiqMas
      || condicion
        .perteneceGrupoProteccionConstitucional
    );
  }

  contarCondiciones(
    condicion:
      CondicionProteccion | null
  ): number {

    if (!condicion) {
      return 0;
    }

    const condiciones = [
      condicion.administraRecursosPublicos,
      condicion
        .grupoProteccionEspecialConstitucional,
      condicion.personaMayor60Anos,
      condicion.discapacidadFisica,
      condicion.victimaConflictoArmado,
      condicion.pobrezaExtrema,
      condicion.poblacionIndigena,
      condicion.poblacionAfrodescendiente,
      condicion.poblacionLgbtiqMas,
      condicion
        .perteneceGrupoProteccionConstitucional
    ];

    return condiciones.filter(
      valor => valor === true
    ).length;
  }

  siNo(
    valor:
      boolean
      | null
      | undefined
  ): string {

    return valor === true
      ? 'Sí'
      : 'No';
  }

  trackByPersona(
    indice: number,
    persona:
      PersonaConCondicionProteccion
  ): number {

    return persona.idDatosPersonal
      ?? indice;
  }

  // ============================================================
  // UTILIDADES
  // ============================================================

  private normalizarTexto(
    valor:
      string
      | number
      | null
      | undefined
  ): string {

    return String(valor ?? '')
      .trim()
      .toLowerCase()
      .normalize('NFD')
      .replace(
        /[\u0300-\u036f]/g,
        ''
      );
  }
}
