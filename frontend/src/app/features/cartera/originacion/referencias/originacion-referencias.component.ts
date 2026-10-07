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
  HttpErrorResponse
} from '@angular/common/http';

import {
  forkJoin,
  Observable
} from 'rxjs';

import {
  OriginacionReferenciasApi
} from './originacion-referencias.api';

import {
  SessionService
} from '../../../../core/auth/session.service';

import {
  EstadoReferencias,
  MedioEntrevista,
  ReferenciaContactoRequest,
  ReferenciaEntrevistaRequest,
  ReferenciasFiltros,
  SolicitudReferenciaListado,
  SolicitudReferenciaParticipante,
  SolicitudReferenciaPersonal,
  TipoCierreReferencias
} from './originacion-referencias.models';


interface RespuestasConcepto {

  tiempo: string;
  relacion: string;
  cumplimiento: string;
  comportamiento: string;
  observaciones: string;
}


@Component({
  selector: 'app-originacion-referencias',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './originacion-referencias.component.html',

  styleUrl:
    './originacion-referencias.component.scss'
})
export class OriginacionReferenciasComponent implements OnInit {

  // =========================================================
  // DEPENDENCIAS
  // =========================================================

  private readonly api =
    inject(OriginacionReferenciasApi);

  private readonly session =
    inject(SessionService);


  // =========================================================
  // PERMISOS
  // =========================================================

  get puedeEditar(): boolean {

    return this.session.tienePermiso(
      'CARTERA_ORIGINACION_EDIT'
    );
  }


  // =========================================================
  // FILTROS
  // =========================================================

  filtros: ReferenciasFiltros = {
    idAgencia: null,
    numeroSolicitud: '',
    documento: '',
    nombreSolicitante: '',
    idAsesor: null,
    idSolicitudProceso: null,
    estadoReferencias: ''
  };


  // =========================================================
  // DATOS
  // =========================================================

  solicitudes:
    SolicitudReferenciaListado[] = [];

  solicitud:
    SolicitudReferenciaListado | null = null;

  participantes:
    SolicitudReferenciaParticipante[] = [];

  participante:
    SolicitudReferenciaParticipante | null = null;

  referencias:
    SolicitudReferenciaPersonal[] = [];

  referenciaEditando:
    SolicitudReferenciaPersonal | null = null;

  referenciaEntrevistando:
    SolicitudReferenciaPersonal | null = null;


  // =========================================================
  // FORMULARIOS
  // =========================================================

  contacto:
    ReferenciaContactoRequest =
      this.contactoVacio();

  entrevista:
    ReferenciaEntrevistaRequest =
      this.entrevistaVacia();

  respuestas:
    RespuestasConcepto =
      this.respuestasVacias();

  conceptoEditadoManualmente = false;

  tipoCierre:
    TipoCierreReferencias =
      'CON_REFERENCIAS';

  observacionCierre = '';


  // =========================================================
  // ESTADO DE PANTALLA
  // =========================================================

  cargando = false;
  cargandoDetalle = false;
  guardando = false;

  error = '';
  mensaje = '';


  // =========================================================
  // CATÁLOGOS DE PANTALLA
  // =========================================================

  readonly medios: {
    valor: MedioEntrevista;
    texto: string;
  }[] = [
    {
      valor: 'CELULAR',
      texto: 'Llamada celular'
    },
    {
      valor: 'FIJO',
      texto: 'Llamada fija'
    },
    {
      valor: 'WHATSAPP',
      texto: 'WhatsApp'
    }
  ];

  readonly opcionesTiempo = [
    'Menos de 1 año',
    'Entre 1 y 3 años',
    'Entre 3 y 5 años',
    'Más de 5 años'
  ];

  readonly opcionesRelacion = [
    'Familiar',
    'Amistad',
    'Vecindad',
    'Laboral',
    'Comercial',
    'Otra'
  ];

  readonly opcionesCumplimiento = [
    'Cumple sus compromisos',
    'Cumple parcialmente',
    'No cumple',
    'No tiene información'
  ];

  readonly opcionesComportamiento = [
    'Responsable',
    'Adecuado',
    'Presenta dificultades',
    'No tiene información'
  ];


  // =========================================================
  // PROPIEDADES CALCULADAS
  // =========================================================

  get editable(): boolean {

    return (
      !!this.solicitud &&
      this.solicitud.estadoReferencias !==
        'CERRADO'
    );
  }


  get referenciasParticipante():
    SolicitudReferenciaPersonal[] {

    return this.referencias.filter(
      referencia =>
        referencia.idSolicitudDeudor ===
        this.participante?.idSolicitudDeudor
    );
  }


  get ocupado(): boolean {

    return (
      this.guardando ||
      this.cargandoDetalle
    );
  }


  // =========================================================
  // CICLO DE VIDA
  // =========================================================

  ngOnInit(): void {

    this.buscar();
  }


  // =========================================================
  // BANDEJA
  // =========================================================

  buscar(): void {

    this.cargando = true;
    this.error = '';

    this.api
      .listar(this.filtros)
      .subscribe({

        next: datos => {

          this.solicitudes =
            datos ?? [];

          this.cargando = false;
        },

        error: error => {

          this.cargando = false;

          this.error =
            this.mensajeError(error);
        }
      });
  }


  limpiarFiltros(): void {

    this.filtros = {
      idAgencia: null,
      numeroSolicitud: '',
      documento: '',
      nombreSolicitante: '',
      idAsesor: null,
      idSolicitudProceso: null,
      estadoReferencias: ''
    };

    this.buscar();
  }


  seleccionarSolicitud(
    item: SolicitudReferenciaListado
  ): void {

    this.solicitud = item;
    this.participante = null;

    this.referenciaEditando = null;
    this.referenciaEntrevistando = null;

    this.contacto =
      this.contactoVacio();

    this.entrevista =
      this.entrevistaVacia();

    this.respuestas =
      this.respuestasVacias();

    this.cargarDetalle(
      item.idSolicitudCredito
    );
  }


  volverBandeja(): void {

    this.solicitud = null;
    this.participante = null;

    this.referencias = [];
    this.participantes = [];

    this.error = '';
    this.mensaje = '';

    this.buscar();
  }


  // =========================================================
  // DETALLE
  // =========================================================

  cargarDetalle(
    id: number
  ): void {

    this.cargandoDetalle = true;
    this.error = '';

    forkJoin({

      solicitud:
        this.api.consultarSolicitud(id),

      participantes:
        this.api.listarParticipantes(id),

      referencias:
        this.api.listarReferencias(id)

    }).subscribe({

      next: resultado => {

        this.solicitud =
          resultado.solicitud;

        this.participantes =
          resultado.participantes ?? [];

        this.referencias =
          resultado.referencias ?? [];

        if (this.participante) {

          this.participante =
            this.participantes.find(
              participante =>
                participante.idSolicitudDeudor ===
                this.participante?.idSolicitudDeudor
            ) ?? null;
        }

        this.tipoCierre =
          this.referencias.length
            ? 'CON_REFERENCIAS'
            : 'SIN_REFERENCIAS';

        this.observacionCierre =
          resultado.solicitud
            .observacionCierre ?? '';

        this.cargandoDetalle = false;
      },

      error: error => {

        this.cargandoDetalle = false;

        this.error =
          this.mensajeError(error);
      }
    });
  }


  seleccionarParticipante(
    item: SolicitudReferenciaParticipante
  ): void {

    this.participante = item;

    this.cancelarContacto();
    this.cancelarEntrevista();

    this.error = '';
  }


  // =========================================================
  // INICIO DE GESTIÓN
  // =========================================================

  iniciar(): void {

    if (
      !this.solicitud ||
      !this.editable ||
      this.ocupado
    ) {
      return;
    }

    const id =
      this.solicitud.idSolicitudCredito;

    this.ejecutar(
      () => this.api.iniciar(id),
      'Gestión de referencias iniciada.'
    );
  }


  // =========================================================
  // CONTACTO
  // =========================================================

  editarContacto(
    referencia: SolicitudReferenciaPersonal
  ): void {

    this.referenciaEditando =
      referencia;

    this.contacto = {

      nombreCompleto:
        referencia.nombreCompleto,

      telefonoCelular:
        referencia.telefonoCelular,

      telefonoFijo:
        referencia.telefonoFijo
    };

    this.cancelarEntrevista();
  }


  cancelarContacto(): void {

    this.referenciaEditando = null;

    this.contacto =
      this.contactoVacio();
  }


  guardarContacto(): void {

    if (
      !this.solicitud ||
      !this.participante ||
      !this.editable ||
      this.ocupado
    ) {
      return;
    }

    const datos:
      ReferenciaContactoRequest = {

        nombreCompleto:
          this.contacto
            .nombreCompleto
            .trim(),

        telefonoCelular:
          this.contacto
            .telefonoCelular
            ?.trim() || null,

        telefonoFijo:
          this.contacto
            .telefonoFijo
            ?.trim() || null
      };

    if (
      !datos.nombreCompleto ||
      (
        !datos.telefonoCelular &&
        !datos.telefonoFijo
      )
    ) {

      this.error =
        'Registre el nombre y al menos un teléfono de contacto.';

      return;
    }

    const accion =
      this.referenciaEditando
        ? this.api.actualizar(
            this.referenciaEditando
              .idSolicitudReferenciaPersonal,
            datos
          )
        : this.api.crear(
            this.solicitud
              .idSolicitudCredito,
            this.participante
              .idSolicitudDeudor,
            datos
          );

    this.ejecutar(
      () => accion,
      'Referencia guardada correctamente.',
      () => this.cancelarContacto()
    );
  }


  eliminar(
    referencia: SolicitudReferenciaPersonal
  ): void {

    if (
      !this.editable ||
      this.ocupado ||
      !window.confirm(
        `¿Desactivar la referencia ${referencia.nombreCompleto}?`
      )
    ) {
      return;
    }

    this.ejecutar(
      () =>
        this.api.desactivar(
          referencia
            .idSolicitudReferenciaPersonal
        ),
      'Referencia desactivada.'
    );
  }


  // =========================================================
  // ENTREVISTA
  // =========================================================

  abrirEntrevista(
    referencia: SolicitudReferenciaPersonal
  ): void {

    this.referenciaEntrevistando =
      referencia;

    this.entrevista = {

      medioEntrevista:
        referencia.medioEntrevista ??
        (
          referencia.telefonoCelular
            ? 'CELULAR'
            : 'FIJO'
        ),

      contactoEstablecido:
        referencia.contactoEstablecido ??
        true,

      conceptoReferencia:
        referencia.conceptoReferencia ??
        ''
    };

    this.respuestas =
      this.respuestasVacias();

    this.conceptoEditadoManualmente =
      !!referencia
        .conceptoReferencia
        ?.trim();

    this.cancelarContacto();
  }


  cancelarEntrevista(): void {

    this.referenciaEntrevistando = null;

    this.entrevista =
      this.entrevistaVacia();

    this.respuestas =
      this.respuestasVacias();

    this.conceptoEditadoManualmente =
      false;
  }


  seleccionarRespuesta(): void {

    if (
      this.entrevista.contactoEstablecido &&
      !this.conceptoEditadoManualmente
    ) {
      this.generarConcepto();
    }
  }


  cambiarResultadoContacto(): void {

    if (
      this.conceptoEditadoManualmente
    ) {
      return;
    }

    if (
      this.entrevista.contactoEstablecido
    ) {

      this.generarConcepto();

    } else {

      this.entrevista
        .conceptoReferencia = '';
    }
  }


  editarConcepto(): void {

    this.conceptoEditadoManualmente =
      true;
  }


  regenerarConcepto(): void {

    this.conceptoEditadoManualmente =
      false;

    this.generarConcepto();
  }


  generarConcepto(): void {

    if (
      !this.referenciaEntrevistando
    ) {
      return;
    }

    const respuestas =
      this.respuestas;

    const partes = [

      `Se establece contacto con ${this.referenciaEntrevistando.nombreCompleto}.`,

      respuestas.relacion
        ? `Relación con el participante: ${respuestas.relacion.toLowerCase()}.`
        : '',

      respuestas.tiempo
        ? `Tiempo de conocimiento: ${respuestas.tiempo.toLowerCase()}.`
        : '',

      respuestas.cumplimiento
        ? `Sobre sus compromisos manifiesta: ${respuestas.cumplimiento.toLowerCase()}.`
        : '',

      respuestas.comportamiento
        ? `Concepto personal: ${respuestas.comportamiento.toLowerCase()}.`
        : '',

      respuestas.observaciones.trim()

    ].filter(Boolean);

    this.entrevista
      .conceptoReferencia =
        partes.join(' ');
  }


  guardarEntrevista(): void {

    if (
      !this.referenciaEntrevistando ||
      !this.editable ||
      this.ocupado
    ) {
      return;
    }

    const referencia =
      this.referenciaEntrevistando;

    if (
      this.entrevista
        .contactoEstablecido &&
      !this.entrevista
        .conceptoReferencia
        ?.trim()
    ) {

      this.error =
        'Registre el concepto de la referencia contactada.';

      return;
    }

    const requiereCelular =
      this.entrevista.medioEntrevista ===
        'CELULAR' ||
      this.entrevista.medioEntrevista ===
        'WHATSAPP';

    const requiereFijo =
      this.entrevista.medioEntrevista ===
        'FIJO';

    if (
      (
        requiereCelular &&
        !referencia.telefonoCelular
      ) ||
      (
        requiereFijo &&
        !referencia.telefonoFijo
      )
    ) {

      this.error =
        'La referencia no tiene teléfono registrado para el medio seleccionado.';

      return;
    }

    this.ejecutar(
      () =>
        this.api.entrevista(
          referencia
            .idSolicitudReferenciaPersonal,
          {
            medioEntrevista:
              this.entrevista
                .medioEntrevista,

            contactoEstablecido:
              this.entrevista
                .contactoEstablecido,

            conceptoReferencia:
              this.entrevista
                .conceptoReferencia
                ?.trim() || null
          }
        ),

      'Gestión telefónica registrada.',

      () =>
        this.cancelarEntrevista()
    );
  }


  // =========================================================
  // CIERRE
  // =========================================================

  cerrar(): void {

    if (
      !this.solicitud ||
      !this.editable ||
      this.ocupado
    ) {
      return;
    }

    const tipo:
      TipoCierreReferencias =
        this.referencias.length
          ? 'CON_REFERENCIAS'
          : 'SIN_REFERENCIAS';

    if (
      tipo === 'SIN_REFERENCIAS' &&
      !this.observacionCierre.trim()
    ) {

      this.error =
        'Justifique por qué se cierra la gestión sin referencias.';

      return;
    }

    if (
      !window.confirm(
        '¿Cerrar definitivamente la gestión de referencias? Después no podrá modificarla.'
      )
    ) {
      return;
    }

    const id =
      this.solicitud.idSolicitudCredito;

    this.ejecutar(
      () =>
        this.api.cerrar(
          id,
          {
            tipoCierre: tipo,

            observacionCierre:
              this.observacionCierre
                .trim() || null
          }
        ),
      'Gestión de referencias cerrada.'
    );
  }


  // =========================================================
  // PRESENTACIÓN
  // =========================================================

  estadoReferencia(
    referencia: SolicitudReferenciaPersonal
  ): string {

    if (
      referencia.contactoEstablecido ===
      null
    ) {
      return 'PENDIENTE';
    }

    return referencia.contactoEstablecido
      ? 'CONTACTADA'
      : 'NO CONTACTADA';
  }


  claseEstado(
    estado:
      EstadoReferencias |
      string |
      null
  ): string {

    if (
      estado === 'CERRADO' ||
      estado === 'CONTACTADA'
    ) {
      return 'badge--ok';
    }

    if (
      estado === 'EN_PROCESO' ||
      estado === 'NO CONTACTADA'
    ) {
      return 'badge--proceso';
    }

    return 'badge--pendiente';
  }


  // =========================================================
  // EJECUCIÓN DE OPERACIONES
  // =========================================================

  private ejecutar<T>(
    accion: () => Observable<T>,
    mensaje: string,
    despues?: () => void
  ): void {

    if (!this.puedeEditar) {

      this.error =
        'El usuario no tiene permiso para modificar la gestión de referencias.';

      return;
    }

    if (this.guardando) {
      return;
    }

    this.guardando = true;

    this.error = '';
    this.mensaje = '';

    accion().subscribe({

      next: () => {

        this.guardando = false;

        despues?.();

        this.mensaje =
          mensaje;

        if (this.solicitud) {

          this.cargarDetalle(
            this.solicitud
              .idSolicitudCredito
          );
        }
      },

      error: error => {

        this.guardando = false;

        this.error =
          this.mensajeError(error);
      }
    });
  }


  // =========================================================
  // VALORES INICIALES
  // =========================================================

  private contactoVacio():
    ReferenciaContactoRequest {

    return {
      nombreCompleto: '',
      telefonoCelular: '',
      telefonoFijo: ''
    };
  }


  private entrevistaVacia():
    ReferenciaEntrevistaRequest {

    return {
      medioEntrevista: 'CELULAR',
      contactoEstablecido: true,
      conceptoReferencia: ''
    };
  }


  private respuestasVacias():
    RespuestasConcepto {

    return {
      tiempo: '',
      relacion: '',
      cumplimiento: '',
      comportamiento: '',
      observaciones: ''
    };
  }


  // =========================================================
  // ERRORES
  // =========================================================

  private mensajeError(
    error: unknown
  ): string {

    const httpError =
      error as HttpErrorResponse;

    const respuesta =
      httpError?.error;

    if (
      typeof respuesta === 'string'
    ) {
      return respuesta;
    }

    return (
      respuesta?.message ||
      respuesta?.detail ||
      respuesta?.error ||
      'No fue posible completar la operación. Verifique la conexión y vuelva a intentar.'
    );
  }
}
