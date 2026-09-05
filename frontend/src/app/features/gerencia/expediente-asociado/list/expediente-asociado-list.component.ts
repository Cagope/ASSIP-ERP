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
  ExpedienteAsociadoApi
} from '../expediente-asociado.api';

import {
  ExpedientePersonaBusqueda
} from '../expediente-asociado.dto';


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
  // CONSTANTES
  // ========================================================

  private static readonly STORAGE_HISTORIAL =
    'assip.expediente.historial-filtros';

  private static readonly MAX_HISTORIAL =
    10;


  // ========================================================
  // SERVICIOS
  // ========================================================

  private readonly api =
    inject(ExpedienteAsociadoApi);

  private readonly router =
    inject(Router);


  // ========================================================
  // DATOS
  // ========================================================

  resultados: ExpedientePersonaBusqueda[] = [];


  // ========================================================
  // HISTORIAL DE FILTROS
  // ========================================================

  historialDocumento: string[] = [];

  historialNombres: string[] = [];

  historialPrimerApellido: string[] = [];

  historialSegundoApellido: string[] = [];


  // ========================================================
  // ESTADO
  // ========================================================

  cargando = false;

  busquedaRealizada = false;

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
    this.cargarHistorial();
  }


  // ========================================================
  // BÚSQUEDA
  // ========================================================

  buscar(): void {

    const documento =
      String(this.filtros.documento ?? '')
        .trim();

    const nombres =
      String(this.filtros.nombres ?? '')
        .trim();

    const primerApellido =
      String(this.filtros.primerApellido ?? '')
        .trim();

    const segundoApellido =
      String(this.filtros.segundoApellido ?? '')
        .trim();

    if (
      !documento &&
      !nombres &&
      !primerApellido &&
      !segundoApellido
    ) {

      this.resultados = [];
      this.busquedaRealizada = false;
      this.pagina = 1;

      this.error =
        'Ingrese al menos un criterio de búsqueda.';

      return;
    }

    if (this.cargando) {
      return;
    }

    this.guardarHistorialBusqueda(
      documento,
      nombres,
      primerApellido,
      segundoApellido
    );

    this.cargando = true;
    this.busquedaRealizada = true;
    this.error = '';
    this.pagina = 1;

    this.api
      .buscarPersonas(
        documento,
        nombres,
        primerApellido,
        segundoApellido
      )
      .subscribe({

        next: lista => {

          this.resultados =
            [...(lista ?? [])];

          this.cargando = false;
        },

        error: error => {

          console.error(
            'Error buscando asociados para el expediente:',
            error
          );

          this.resultados = [];

          this.error =
            error?.error?.mensaje ??
            error?.error?.message ??
            'No fue posible realizar la búsqueda.';

          this.cargando = false;
        }
      });
  }


  limpiar(): void {

    this.filtros = {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: ''
    };

    this.resultados = [];

    this.busquedaRealizada = false;

    this.pagina = 1;

    this.error = '';
  }


  // ========================================================
  // PAGINACIÓN
  // ========================================================

  get paginadas():
    ExpedientePersonaBusqueda[] {

    const inicio =
      (this.pagina - 1) *
      this.tamanoPagina;

    return this.resultados.slice(
      inicio,
      inicio + this.tamanoPagina
    );
  }


  totalPaginas(): number {

    return Math.max(
      1,
      Math.ceil(
        this.resultados.length /
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
    persona: ExpedientePersonaBusqueda
  ): void {

    const idDatosPersonal =
      Number(persona.idDatosPersonal);

    if (
      !Number.isInteger(idDatosPersonal) ||
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
    persona: ExpedientePersonaBusqueda
  ): number {

    return (
      persona.idDatosPersonal ??
      index
    );
  }


  // ========================================================
  // HISTORIAL
  // ========================================================

  private cargarHistorial(): void {

    try {

      const contenido =
        localStorage.getItem(
          ExpedienteAsociadoListComponent
            .STORAGE_HISTORIAL
        );

      if (!contenido) {
        return;
      }

      const historial =
        JSON.parse(contenido);

      this.historialDocumento =
        this.normalizarHistorial(
          historial?.documento
        );

      this.historialNombres =
        this.normalizarHistorial(
          historial?.nombres
        );

      this.historialPrimerApellido =
        this.normalizarHistorial(
          historial?.primerApellido
        );

      this.historialSegundoApellido =
        this.normalizarHistorial(
          historial?.segundoApellido
        );

    } catch (error) {

      console.warn(
        'No fue posible recuperar el historial de filtros:',
        error
      );

      this.historialDocumento = [];
      this.historialNombres = [];
      this.historialPrimerApellido = [];
      this.historialSegundoApellido = [];
    }
  }


  private guardarHistorialBusqueda(
    documento: string,
    nombres: string,
    primerApellido: string,
    segundoApellido: string
  ): void {

    this.historialDocumento =
      this.agregarAlHistorial(
        this.historialDocumento,
        documento
      );

    this.historialNombres =
      this.agregarAlHistorial(
        this.historialNombres,
        nombres
      );

    this.historialPrimerApellido =
      this.agregarAlHistorial(
        this.historialPrimerApellido,
        primerApellido
      );

    this.historialSegundoApellido =
      this.agregarAlHistorial(
        this.historialSegundoApellido,
        segundoApellido
      );

    this.persistirHistorial();
  }


  private agregarAlHistorial(
    historial: string[],
    valor: string
  ): string[] {

    const limpio =
      String(valor ?? '')
        .trim();

    if (!limpio) {
      return historial;
    }

    return [
      limpio,
      ...historial.filter(
        actual =>
          actual.toLocaleUpperCase() !==
          limpio.toLocaleUpperCase()
      )
    ].slice(
      0,
      ExpedienteAsociadoListComponent
        .MAX_HISTORIAL
    );
  }


  private normalizarHistorial(
    valores: unknown
  ): string[] {

    if (!Array.isArray(valores)) {
      return [];
    }

    return valores
      .map(valor =>
        String(valor ?? '').trim()
      )
      .filter(valor =>
        Boolean(valor)
      )
      .slice(
        0,
        ExpedienteAsociadoListComponent
          .MAX_HISTORIAL
      );
  }


  private persistirHistorial(): void {

    try {

      localStorage.setItem(
        ExpedienteAsociadoListComponent
          .STORAGE_HISTORIAL,
        JSON.stringify({
          documento:
            this.historialDocumento,

          nombres:
            this.historialNombres,

          primerApellido:
            this.historialPrimerApellido,

          segundoApellido:
            this.historialSegundoApellido
        })
      );

    } catch (error) {

      console.warn(
        'No fue posible guardar el historial de filtros:',
        error
      );
    }
  }


  // ========================================================
  // UTILIDADES
  // ========================================================

  nombreCompleto(
    persona: ExpedientePersonaBusqueda
  ): string {

    const nombre =
      String(
        persona.nombreCompleto ?? ''
      ).trim();

    if (nombre) {
      return nombre;
    }

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
}
