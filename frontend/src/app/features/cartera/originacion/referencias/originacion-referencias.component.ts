import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { OriginacionReferenciasApi } from './originacion-referencias.api';
import {
  EstadoReferencias, MedioEntrevista, ReferenciaContactoRequest, ReferenciaEntrevistaRequest,
  ReferenciasFiltros, SolicitudReferenciaListado, SolicitudReferenciaParticipante,
  SolicitudReferenciaPersonal, TipoCierreReferencias
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
  imports: [CommonModule, FormsModule],
  templateUrl: './originacion-referencias.component.html',
  styleUrl: './originacion-referencias.component.scss'
})
export class OriginacionReferenciasComponent implements OnInit {
  private readonly api = inject(OriginacionReferenciasApi);

  filtros: ReferenciasFiltros = {
    idAgencia: null, numeroSolicitud: '', documento: '', nombreSolicitante: '',
    idAsesor: null, idSolicitudProceso: null, estadoReferencias: ''
  };
  solicitudes: SolicitudReferenciaListado[] = [];
  solicitud: SolicitudReferenciaListado | null = null;
  participantes: SolicitudReferenciaParticipante[] = [];
  participante: SolicitudReferenciaParticipante | null = null;
  referencias: SolicitudReferenciaPersonal[] = [];
  referenciaEditando: SolicitudReferenciaPersonal | null = null;
  referenciaEntrevistando: SolicitudReferenciaPersonal | null = null;

  contacto: ReferenciaContactoRequest = this.contactoVacio();
  entrevista: ReferenciaEntrevistaRequest = this.entrevistaVacia();
  respuestas: RespuestasConcepto = this.respuestasVacias();
  conceptoEditadoManualmente = false;
  tipoCierre: TipoCierreReferencias = 'CON_REFERENCIAS';
  observacionCierre = '';
  cargando = false;
  cargandoDetalle = false;
  guardando = false;
  error = '';
  mensaje = '';

  readonly medios: { valor: MedioEntrevista; texto: string }[] = [
    { valor: 'CELULAR', texto: 'Llamada celular' },
    { valor: 'FIJO', texto: 'Llamada fija' },
    { valor: 'WHATSAPP', texto: 'WhatsApp' }
  ];
  readonly opcionesTiempo = ['Menos de 1 año', 'Entre 1 y 3 años', 'Entre 3 y 5 años', 'Más de 5 años'];
  readonly opcionesRelacion = ['Familiar', 'Amistad', 'Vecindad', 'Laboral', 'Comercial', 'Otra'];
  readonly opcionesCumplimiento = ['Cumple sus compromisos', 'Cumple parcialmente', 'No cumple', 'No tiene información'];
  readonly opcionesComportamiento = ['Responsable', 'Adecuado', 'Presenta dificultades', 'No tiene información'];

  get editable(): boolean { return !!this.solicitud && this.solicitud.estadoReferencias !== 'CERRADO'; }
  get referenciasParticipante(): SolicitudReferenciaPersonal[] {
    return this.referencias.filter(r => r.idSolicitudDeudor === this.participante?.idSolicitudDeudor);
  }
  get ocupado(): boolean { return this.guardando || this.cargandoDetalle; }

  ngOnInit(): void { this.buscar(); }

  buscar(): void {
    this.cargando = true;
    this.error = '';
    this.api.listar(this.filtros).subscribe({
      next: datos => { this.solicitudes = datos ?? []; this.cargando = false; },
      error: e => { this.cargando = false; this.error = this.mensajeError(e); }
    });
  }

  limpiarFiltros(): void {
    this.filtros = { idAgencia: null, numeroSolicitud: '', documento: '', nombreSolicitante: '',
      idAsesor: null, idSolicitudProceso: null, estadoReferencias: '' };
    this.buscar();
  }

  seleccionarSolicitud(item: SolicitudReferenciaListado): void {
    this.solicitud = item;
    this.participante = null;
    this.referenciaEditando = null;
    this.referenciaEntrevistando = null;
    this.contacto = this.contactoVacio();
    this.entrevista = this.entrevistaVacia();
    this.respuestas = this.respuestasVacias();
    this.cargarDetalle(item.idSolicitudCredito);
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

  cargarDetalle(id: number): void {
    this.cargandoDetalle = true;
    this.error = '';
    forkJoin({
      solicitud: this.api.consultarSolicitud(id),
      participantes: this.api.listarParticipantes(id),
      referencias: this.api.listarReferencias(id)
    }).subscribe({
      next: r => {
        this.solicitud = r.solicitud;
        this.participantes = r.participantes ?? [];
        this.referencias = r.referencias ?? [];
        if (this.participante) {
          this.participante = this.participantes.find(p => p.idSolicitudDeudor === this.participante?.idSolicitudDeudor) ?? null;
        }
        this.tipoCierre = this.referencias.length ? 'CON_REFERENCIAS' : 'SIN_REFERENCIAS';
        this.observacionCierre = r.solicitud.observacionCierre ?? '';
        this.cargandoDetalle = false;
      },
      error: e => { this.cargandoDetalle = false; this.error = this.mensajeError(e); }
    });
  }

  seleccionarParticipante(item: SolicitudReferenciaParticipante): void {
    this.participante = item;
    this.cancelarContacto();
    this.cancelarEntrevista();
    this.error = '';
  }

  iniciar(): void {
    if (!this.solicitud || !this.editable || this.ocupado) { return; }
    const id = this.solicitud.idSolicitudCredito;
    this.ejecutar(() => this.api.iniciar(id), 'Gestión de referencias iniciada.');
  }

  editarContacto(ref: SolicitudReferenciaPersonal): void {
    this.referenciaEditando = ref;
    this.contacto = {
      nombreCompleto: ref.nombreCompleto,
      telefonoCelular: ref.telefonoCelular,
      telefonoFijo: ref.telefonoFijo
    };
    this.cancelarEntrevista();
  }

  cancelarContacto(): void { this.referenciaEditando = null; this.contacto = this.contactoVacio(); }

  guardarContacto(): void {
    if (!this.solicitud || !this.participante || !this.editable || this.ocupado) { return; }
    const datos: ReferenciaContactoRequest = {
      nombreCompleto: this.contacto.nombreCompleto.trim(),
      telefonoCelular: this.contacto.telefonoCelular?.trim() || null,
      telefonoFijo: this.contacto.telefonoFijo?.trim() || null
    };
    if (!datos.nombreCompleto || (!datos.telefonoCelular && !datos.telefonoFijo)) {
      this.error = 'Registre el nombre y al menos un teléfono de contacto.';
      return;
    }
    const accion = this.referenciaEditando
      ? this.api.actualizar(this.referenciaEditando.idSolicitudReferenciaPersonal, datos)
      : this.api.crear(this.solicitud.idSolicitudCredito, this.participante.idSolicitudDeudor, datos);
    this.ejecutar(() => accion, 'Referencia guardada correctamente.', () => this.cancelarContacto());
  }

  eliminar(ref: SolicitudReferenciaPersonal): void {
    if (!this.editable || this.ocupado || !window.confirm(`¿Desactivar la referencia ${ref.nombreCompleto}?`)) { return; }
    this.ejecutar(() => this.api.desactivar(ref.idSolicitudReferenciaPersonal), 'Referencia desactivada.');
  }

  abrirEntrevista(ref: SolicitudReferenciaPersonal): void {
    this.referenciaEntrevistando = ref;
    this.entrevista = {
      medioEntrevista: ref.medioEntrevista ?? (ref.telefonoCelular ? 'CELULAR' : 'FIJO'),
      contactoEstablecido: ref.contactoEstablecido ?? true,
      conceptoReferencia: ref.conceptoReferencia ?? ''
    };
    this.respuestas = this.respuestasVacias();
    this.conceptoEditadoManualmente = !!ref.conceptoReferencia?.trim();
    this.cancelarContacto();
  }

  cancelarEntrevista(): void {
    this.referenciaEntrevistando = null;
    this.entrevista = this.entrevistaVacia();
    this.respuestas = this.respuestasVacias();
    this.conceptoEditadoManualmente = false;
  }

  seleccionarRespuesta(): void {
    if (this.entrevista.contactoEstablecido && !this.conceptoEditadoManualmente) {
      this.generarConcepto();
    }
  }

  cambiarResultadoContacto(): void {
    if (!this.conceptoEditadoManualmente) {
      if (this.entrevista.contactoEstablecido) {
        this.generarConcepto();
      } else {
        this.entrevista.conceptoReferencia = '';
      }
    }
  }

  editarConcepto(): void {
    this.conceptoEditadoManualmente = true;
  }

  regenerarConcepto(): void {
    this.conceptoEditadoManualmente = false;
    this.generarConcepto();
  }

  generarConcepto(): void {
    if (!this.referenciaEntrevistando) { return; }
    const r = this.respuestas;
    const partes = [
      `Se establece contacto con ${this.referenciaEntrevistando.nombreCompleto}.`,
      r.relacion ? `Relación con el participante: ${r.relacion.toLowerCase()}.` : '',
      r.tiempo ? `Tiempo de conocimiento: ${r.tiempo.toLowerCase()}.` : '',
      r.cumplimiento ? `Sobre sus compromisos manifiesta: ${r.cumplimiento.toLowerCase()}.` : '',
      r.comportamiento ? `Concepto personal: ${r.comportamiento.toLowerCase()}.` : '',
      r.observaciones.trim()
    ].filter(Boolean);
    this.entrevista.conceptoReferencia = partes.join(' ');
  }

  guardarEntrevista(): void {
    if (!this.referenciaEntrevistando || !this.editable || this.ocupado) { return; }
    const ref = this.referenciaEntrevistando;
    if (this.entrevista.contactoEstablecido && !this.entrevista.conceptoReferencia?.trim()) {
      this.error = 'Registre el concepto de la referencia contactada.';
      return;
    }
    if ((this.entrevista.medioEntrevista === 'CELULAR' || this.entrevista.medioEntrevista === 'WHATSAPP') && !ref.telefonoCelular
      || this.entrevista.medioEntrevista === 'FIJO' && !ref.telefonoFijo) {
      this.error = 'La referencia no tiene teléfono registrado para el medio seleccionado.';
      return;
    }
    this.ejecutar(
      () => this.api.entrevista(ref.idSolicitudReferenciaPersonal, {
        medioEntrevista: this.entrevista.medioEntrevista,
        contactoEstablecido: this.entrevista.contactoEstablecido,
        conceptoReferencia: this.entrevista.conceptoReferencia?.trim() || null
      }),
      'Gestión telefónica registrada.', () => this.cancelarEntrevista()
    );
  }

  cerrar(): void {
    if (!this.solicitud || !this.editable || this.ocupado) { return; }
    const tipo: TipoCierreReferencias = this.referencias.length ? 'CON_REFERENCIAS' : 'SIN_REFERENCIAS';
    if (tipo === 'SIN_REFERENCIAS' && !this.observacionCierre.trim()) {
      this.error = 'Justifique por qué se cierra la gestión sin referencias.';
      return;
    }
    if (!window.confirm('¿Cerrar definitivamente la gestión de referencias? Después no podrá modificarla.')) { return; }
    const id = this.solicitud.idSolicitudCredito;
    this.ejecutar(() => this.api.cerrar(id, {
      tipoCierre: tipo, observacionCierre: this.observacionCierre.trim() || null
    }), 'Gestión de referencias cerrada.');
  }

  estadoReferencia(ref: SolicitudReferenciaPersonal): string {
    return ref.contactoEstablecido === null ? 'PENDIENTE' : ref.contactoEstablecido ? 'CONTACTADA' : 'NO CONTACTADA';
  }

  claseEstado(estado: EstadoReferencias | string | null): string {
    return estado === 'CERRADO' || estado === 'CONTACTADA' ? 'badge--ok'
      : estado === 'EN_PROCESO' || estado === 'NO CONTACTADA' ? 'badge--proceso' : 'badge--pendiente';
  }

  private ejecutar<T>(accion: () => import('rxjs').Observable<T>, mensaje: string, despues?: () => void): void {
    if (this.guardando) { return; }
    this.guardando = true;
    this.error = '';
    this.mensaje = '';
    accion().subscribe({
      next: () => {
        this.guardando = false;
        despues?.();
        this.mensaje = mensaje;
        if (this.solicitud) { this.cargarDetalle(this.solicitud.idSolicitudCredito); }
      },
      error: e => { this.guardando = false; this.error = this.mensajeError(e); }
    });
  }

  private contactoVacio(): ReferenciaContactoRequest { return { nombreCompleto: '', telefonoCelular: '', telefonoFijo: '' }; }
  private entrevistaVacia(): ReferenciaEntrevistaRequest { return { medioEntrevista: 'CELULAR', contactoEstablecido: true, conceptoReferencia: '' }; }
  private respuestasVacias(): RespuestasConcepto { return { tiempo: '', relacion: '', cumplimiento: '', comportamiento: '', observaciones: '' }; }
  private mensajeError(error: unknown): string {
    const e = error as HttpErrorResponse;
    const respuesta = e?.error;
    return (typeof respuesta === 'string' ? respuesta : respuesta?.message || respuesta?.detail || respuesta?.error)
      || 'No fue posible completar la operación. Verifique la conexión y vuelva a intentar.';
  }
}
