import {
  CommonModule
} from '@angular/common';

import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  Router
} from '@angular/router';

import {
  HeaderActionsComponent
} from '../../../../shared/header-actions/header-actions.component';

import {
  DatosPersonales,
  DatosPersonalesApi
} from '../../../hoja-vida/datos-personales/datos-personales.api';


@Component({
  selector: 'app-expediente-asociado-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl:
    './expediente-asociado-list.component.html',
  styleUrls: [
    './expediente-asociado-list.component.scss'
  ]
})
export class ExpedienteAsociadoListComponent
implements OnInit {

  // ========================================================
  // SERVICIOS
  // ========================================================

  private readonly api =
    inject(DatosPersonalesApi);

  private readonly router =
    inject(Router);


  // ========================================================
  // DATOS
  // ========================================================

  personas: DatosPersonales[] = [];

  filtradas: DatosPersonales[] = [];


  // ========================================================
  // ESTADO
  // ========================================================

  cargando = false;

  error = '';


  // ========================================================
  // FILTROS
  // ========================================================

  filtros = {
    documento: '',
    nombres: '',
    primerApellido: '',
    segundoApellido: ''
  };


  // ========================================================
  // PAGINACIÓN
  // ========================================================

  pagina = 1;

  tamanoPagina = 10;


  // ========================================================
  // CICLO DE VIDA
  // ========================================================

  ngOnInit(): void {
    this.cargar();
  }


  // ========================================================
  // CARGA
  // ========================================================

  cargar(): void {

    if (this.cargando) {
      return;
    }

    this.cargando = true;

    this.error = '';

    this.api
      .listar()
      .subscribe({

        next: lista => {

          this.personas =
            [...(lista ?? [])]
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
              });

          this.buscar();

          this.cargando = false;
        },

        error: error => {

          console.error(
            'Error cargando asociados para el expediente:',
            error
          );

          this.personas = [];

          this.filtradas = [];

          this.pagina = 1;

          this.error =
            error?.error?.mensaje ??
            error?.error?.message ??
            'No fue posible cargar los asociados.';

          this.cargando = false;
        }
      });
  }


  // ========================================================
  // BÚSQUEDA
  // ========================================================

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
            !documento ||
            documentoPersona.includes(
              documento
            )
          )
          &&
          (
            !nombres ||
            nombresPersona.includes(
              nombres
            )
          )
          &&
          (
            !primerApellido ||
            primerApellidoPersona.includes(
              primerApellido
            )
          )
          &&
          (
            !segundoApellido ||
            segundoApellidoPersona.includes(
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


  // ========================================================
  // PAGINACIÓN
  // ========================================================

  get paginadas(): DatosPersonales[] {

    const inicio =
      (this.pagina - 1) *
      this.tamanoPagina;

    return this.filtradas.slice(
      inicio,
      inicio + this.tamanoPagina
    );
  }


  totalPaginas(): number {

    return Math.max(
      1,
      Math.ceil(
        this.filtradas.length /
        this.tamanoPagina
      )
    );
  }


  cambiarPagina(
    pagina: number
  ): void {

    if (
      pagina < 1 ||
      pagina > this.totalPaginas()
    ) {
      return;
    }

    this.pagina = pagina;
  }


  // ========================================================
  // ACCIONES
  // ========================================================

  gestionar(
    persona: DatosPersonales
  ): void {

    const idDatosPersonal =
      Number(
        persona.idDatosPersonal
      );

    if (
      !Number.isInteger(
        idDatosPersonal
      ) ||
      idDatosPersonal <= 0
    ) {
      this.error =
        'El registro seleccionado no tiene un identificador válido.';

      return;
    }

    this.error = '';

    this.router.navigate([
      '/gerencia/expediente-asociado',
      idDatosPersonal
    ]);
  }


  trackByPersona(
    index: number,
    persona: DatosPersonales
  ): number {

    return (
      persona.idDatosPersonal ??
      index
    );
  }


  // ========================================================
  // UTILIDADES
  // ========================================================

  nombreCompleto(
    persona: DatosPersonales
  ): string {

    return [
      persona.nombres,
      persona.primerApellido,
      persona.segundoApellido
    ]
      .filter(valor =>
        Boolean(
          String(valor ?? '').trim()
        )
      )
      .join(' ');
  }


  descripcionTipoPersona(
    tipoPersona:
      | string
      | null
      | undefined
  ): string {

    const codigo =
      String(tipoPersona ?? '')
        .trim()
        .toUpperCase();

    switch (codigo) {

      case '1':
      case 'N':
      case 'PN':
      case 'NATURAL':
        return 'Natural';

      case '2':
      case 'J':
      case 'PJ':
      case 'JURIDICA':
      case 'JURÍDICA':
        return 'Jurídica';

      case '':
        return '—';

      default:
        return codigo;
    }
  }

  private normalizarTexto(
    valor:
      | string
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
