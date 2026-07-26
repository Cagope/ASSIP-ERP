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
  DatosPersonalesApi,
  DatosPersonales
} from '../datos-personales/datos-personales.api';

import {
  ResidenciaFiscal,
  ResidenciaFiscalApi
} from './residencia-fiscal.api';

import {
  ResidenciaFiscalPrintService
} from './residencia-fiscal-print.service';

import {
  ResidenciaFiscalExporterService
} from './residencia-fiscal-exporter.service';

/**
 * Persona del listado general con su información
 * de residencia fiscal asociada.
 */
type PersonaConResidenciaFiscal =
  DatosPersonales & {
    residenciaFiscal: ResidenciaFiscal | null;
  };

/**
 * Estructura utilizada para impresión y exportación.
 */
export type ResidenciaFiscalInforme =
  ResidenciaFiscal & {
    documento?: string;
    nombrePersona?: string;
  };

@Component({
  selector: 'app-residencia-fiscal-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl:
    './residencia-fiscal-list.component.html',
  styleUrls: [
    './residencia-fiscal-list.component.scss'
  ]
})
export class ResidenciaFiscalListComponent
  implements OnInit {

  private readonly datosPersonalesApi =
    inject(DatosPersonalesApi);

  private readonly residenciaFiscalApi =
    inject(ResidenciaFiscalApi);

  private readonly router =
    inject(Router);

  private readonly printService =
    inject(ResidenciaFiscalPrintService);

  private readonly exporter =
    inject(ResidenciaFiscalExporterService);

  personas: PersonaConResidenciaFiscal[] = [];

  filtradas: PersonaConResidenciaFiscal[] = [];

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

      residencias:
        this.residenciaFiscalApi.listar()
    })
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({
        next: resultado => {
          const mapaResidencias =
            new Map<number, ResidenciaFiscal>();

          (resultado.residencias ?? []).forEach(
            residencia => {
              if (
                residencia.idDatosPersonal == null
              ) {
                return;
              }

              mapaResidencias.set(
                residencia.idDatosPersonal,
                residencia
              );
            }
          );

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

                residenciaFiscal:
                  persona.idDatosPersonal != null
                    ? mapaResidencias.get(
                        persona.idDatosPersonal
                      ) ?? null
                    : null
              }));

          this.buscar();
        },

        error: err => {
          console.error(
            'Error cargando residencia fiscal:',
            err
          );

          this.error =
            'No fue posible cargar la información de residencia fiscal.';

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
    PersonaConResidenciaFiscal[] {

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
    residenciaFiscal?:
      ResidenciaFiscal | null
  ): void {

    this.error = '';

    if (!persona.idDatosPersonal) {
      this.error =
        'Asociado inválido: no tiene idDatosPersonal.';

      return;
    }

    if (
      residenciaFiscal
        ?.idResidenciaFiscal
    ) {
      this.router.navigate([
        '/hoja-vida/residencia-fiscal',
        residenciaFiscal
          .idResidenciaFiscal,
        'editar'
      ]);

      return;
    }

    this.router.navigate(
      [
        '/hoja-vida/residencia-fiscal/nuevo'
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
        'No hay información de residencia fiscal para imprimir.'
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
        'No hay información de residencia fiscal para exportar.'
      );

      return;
    }

    this.exporter.exportarExcel(
      datos
    );
  }

  private construirDatosInforme():
    ResidenciaFiscalInforme[] {

    return this.filtradas
      .filter(
        (
          persona
        ): persona is
          PersonaConResidenciaFiscal & {
            residenciaFiscal:
              ResidenciaFiscal;
          } =>
          persona.residenciaFiscal
            != null
      )
      .map(persona => ({
        ...persona.residenciaFiscal,

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
