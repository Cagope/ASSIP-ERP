
import { CommonModule } from '@angular/common';

import {
  Component,
  inject,
  OnInit
} from '@angular/core';

import { FormsModule } from '@angular/forms';

import {
  PersonaBusquedaDTO
} from '../../../../shared/personas/personas-busqueda.dto';

import {
  Agencia,
  AgenciasApi
} from '../../../general/agencias/agencia.api';

import {
  SessionService
} from '../../../../core/auth/session.service';

import {
  TrasladoCuentasApi
} from './traslado-cuentas.api';

import {
  TrasladoEntrada,
  TrasladoCuenta,
  TrasladoPreview,
  TrasladoResultado
} from './traslado-cuentas.models';


// =========================================================
// COMPONENTE - TRASLADO INTEGRAL DE CUENTAS
// =========================================================

@Component({
  selector: 'app-traslado-cuentas',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './traslado-cuentas.component.html',
  styleUrls: ['./traslado-cuentas.component.scss']
})
export class TrasladoCuentasComponent implements OnInit {

  // =========================================================
  // SERVICIOS
  // =========================================================

  private readonly api = inject(TrasladoCuentasApi);

  private readonly agenciasApi = inject(AgenciasApi);

  private readonly session = inject(SessionService);


  // =========================================================
  // FILTROS DE BÚSQUEDA
  // =========================================================

  filtros = {
    documento: '',
    nombres: '',
    primerApellido: '',
    segundoApellido: ''
  };

  resultados: PersonaBusquedaDTO[] = [];

  asociado: PersonaBusquedaDTO | null = null;


  // =========================================================
  // AGENCIAS
  // =========================================================

  agencias: Agencia[] = [];

  origen: number | null = null;

  destino: number | null = null;


  // =========================================================
  // DATOS DEL TRASLADO
  // =========================================================

  fechaProceso = this.obtenerFechaActual();

  comentario = '';


  // =========================================================
  // COMENTARIO SUGERIDO
  // =========================================================

  comentarioSugerido = '';

  comentarioPersonalizado = false;


  // =========================================================
  // CUENTAS Y RESULTADOS
  // =========================================================

  cuentas: TrasladoCuenta[] = [];

  vista: TrasladoPreview | null = null;

  resultado: TrasladoResultado | null = null;


  // =========================================================
  // ESTADO DEL COMPONENTE
  // =========================================================

  cargando = false;

  error = '';

  mensaje = '';


  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    this.cargarAgencias();
  }


  // =========================================================
  // CONSULTAR AGENCIAS OPERATIVAS
  // =========================================================

  private cargarAgencias(): void {

    this.agenciasApi.listar().subscribe({

      next: (respuesta) => {

        // Excluir agencia 1 (no operativa).

        this.agencias = (respuesta ?? []).filter(
          agencia => agencia.idAgencia !== 1
        );

        // Obtener agencia activa del usuario.

        const idAgenciaActiva =
          this.session.agenciaActivaSig()?.idAgencia ?? null;

        // Seleccionar origen únicamente si es operativa.

        this.origen = this.agencias.some(
          agencia => agencia.idAgencia === idAgenciaActiva
        )
          ? idAgenciaActiva
          : null;

        // Reiniciar destino si ya no es válido.

        if (
          this.destino !== null &&
          !this.agencias.some(
            agencia => agencia.idAgencia === this.destino
          )
        ) {
          this.destino = null;
        }

        // Actualizar comentario sugerido.

        this.actualizarComentarioSugerido();
      },

      error: () => {

        this.error =
          'No fue posible consultar las agencias.';
      }

    });
  }


  // =========================================================
  // BUSCAR ASOCIADOS
  // =========================================================

  buscar(): void {

    if (this.cargando) {
      return;
    }

    const filtros = {
      documento: this.filtros.documento.trim(),
      nombres: this.filtros.nombres.trim(),
      primerApellido: this.filtros.primerApellido.trim(),
      segundoApellido: this.filtros.segundoApellido.trim()
    };

    if (
      !Object.values(filtros).some(
        valor => valor.length > 0
      )
    ) {

      this.error =
        'Ingrese al menos un criterio de búsqueda.';

      return;
    }

    this.error = '';
    this.mensaje = '';

    this.resultados = [];
    this.asociado = null;
    this.cuentas = [];

    this.vista = null;
    this.resultado = null;

    this.destino = null;

    this.comentario = '';
    this.comentarioSugerido = '';
    this.comentarioPersonalizado = false;

    this.cargando = true;

    this.api.buscarAsociados(filtros).subscribe({

      next: (respuesta) => {

        this.resultados = respuesta ?? [];

        this.cargando = false;
      },

      error: (error) => {

        this.fallar(error);
      }

    });
  }


  // =========================================================
  // SELECCIONAR ASOCIADO
  // =========================================================

  seleccionar(persona: PersonaBusquedaDTO): void {

    if (this.cargando) {
      return;
    }

    this.asociado = persona;

    this.resultados = [];
    this.cuentas = [];

    this.vista = null;
    this.resultado = null;

    this.destino = null;

    this.comentario = '';
    this.comentarioSugerido = '';
    this.comentarioPersonalizado = false;

    this.error = '';
    this.mensaje = '';

    this.actualizarComentarioSugerido();

    this.consultarCuentas(persona.idDatosPersonal);
  }


  // =========================================================
  // CONSULTAR CUENTAS DEL ASOCIADO
  // =========================================================

  private consultarCuentas(
    idDatosPersonal: number
  ): void {

    this.cargando = true;

    this.api.consultarCuentas(idDatosPersonal).subscribe({

      next: (respuesta) => {

        this.cuentas = respuesta ?? [];

        this.cargando = false;
      },

      error: (error) => {

        this.fallar(error);
      }

    });
  }


  // =========================================================
  // LIMPIAR BÚSQUEDA
  // =========================================================

  limpiar(): void {

    if (this.cargando) {
      return;
    }

    this.filtros = {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: ''
    };

    this.resultados = [];
    this.asociado = null;
    this.cuentas = [];

    this.vista = null;
    this.resultado = null;

    // Restablecer agencia origen operativa.

    const idAgenciaActiva =
      this.session.agenciaActivaSig()?.idAgencia ?? null;

    this.origen = this.agencias.some(
      agencia => agencia.idAgencia === idAgenciaActiva
    )
      ? idAgenciaActiva
      : null;

    this.destino = null;

    this.fechaProceso = this.obtenerFechaActual();

    this.comentario = '';
    this.comentarioSugerido = '';
    this.comentarioPersonalizado = false;

    this.error = '';
    this.mensaje = '';
  }


  // =========================================================
  // CAMBIAR ASOCIADO
  // =========================================================

  cambiarAsociado(): void {

    if (this.cargando) {
      return;
    }

    this.asociado = null;

    this.cuentas = [];
    this.resultados = [];

    this.vista = null;
    this.resultado = null;

    this.destino = null;

    this.comentario = '';
    this.comentarioSugerido = '';
    this.comentarioPersonalizado = false;

    this.error = '';
    this.mensaje = '';
  }


  // =========================================================
  // TOTAL DE SALDOS CONSULTADOS
  // =========================================================

  totalSaldos(): number {

    return this.cuentas.reduce(
      (total, cuenta) =>
        total + Number(cuenta.saldoActual ?? 0),
      0
    );
  }



  // =========================================================
  // GENERAR COMENTARIO SUGERIDO
  // =========================================================

  private generarComentarioSugerido(): string {

    if (
      !this.asociado ||
      this.origen === null ||
      this.destino === null ||
      this.origen === this.destino
    ) {
      return '';
    }

    const agenciaOrigen = this.agencias.find(
      agencia => agencia.idAgencia === this.origen
    );

    const agenciaDestino = this.agencias.find(
      agencia => agencia.idAgencia === this.destino
    );

    if (!agenciaOrigen || !agenciaDestino) {
      return '';
    }

    return (
      `Traslado de cuentas de agencia ` +
      `${agenciaOrigen.idAgencia} a ` +
      `${agenciaDestino.idAgencia}.`
    );
  }


  // =========================================================
  // ACTUALIZAR COMENTARIO SUGERIDO
  // =========================================================

  actualizarComentarioSugerido(): void {

    const sugerencia =
      this.generarComentarioSugerido();

    this.comentarioSugerido = sugerencia;

    // Respetar comentarios personalizados.

    if (!this.comentarioPersonalizado) {
      this.comentario = sugerencia;
    }

    // La vista previa anterior deja de ser válida.

    this.vista = null;
  }


  // =========================================================
  // REGISTRAR EDICIÓN MANUAL DEL COMENTARIO
  // =========================================================

  comentarioModificado(): void {

    this.comentarioPersonalizado =
      this.comentario !== this.comentarioSugerido;

    this.vista = null;
  }


  // =========================================================
  // RESTABLECER COMENTARIO SUGERIDO
  // =========================================================

  restablecerComentario(): void {

    this.comentarioPersonalizado = false;

    this.actualizarComentarioSugerido();
  }


  // =========================================================
  // CAMBIO DE AGENCIA
  // =========================================================

  cambiarAgencia(): void {

    this.actualizarComentarioSugerido();

    this.resultado = null;
  }


  // =========================================================
  // CONSTRUIR ENTRADA DEL TRASLADO
  // =========================================================

  private entrada(): TrasladoEntrada {

    if (!this.asociado) {

      throw new Error(
        'Seleccione un asociado.'
      );
    }

    if (
      this.origen == null ||
      this.destino == null
    ) {

      throw new Error(
        'Seleccione las agencias de origen y destino.'
      );
    }

    if (this.origen === this.destino) {

      throw new Error(
        'La agencia de destino debe ser diferente a la de origen.'
      );
    }

    if (!this.fechaProceso) {

      throw new Error(
        'Seleccione la fecha del proceso.'
      );
    }

    // -----------------------------------------------------
    // VALIDAR COMENTARIO
    // -----------------------------------------------------

    const comentario = this.comentario.trim();

    if (!comentario) {

      throw new Error(
        'Ingrese un comentario para realizar el traslado.'
      );
    }

    if (comentario.length > 350) {

      throw new Error(
        'El comentario no puede superar 350 caracteres.'
      );
    }

    // -----------------------------------------------------
    // CONSTRUIR SOLICITUD
    // -----------------------------------------------------

    return {
      idDatosPersonal: this.asociado.idDatosPersonal,
      idAgenciaOrigen: this.origen,
      idAgenciaDestino: this.destino,
      fechaProceso: this.fechaProceso,
      comentario
    };
  }


  // =========================================================
  // VISTA PREVIA
  // =========================================================

  verPreview(): void {

    if (this.cargando) {
      return;
    }

    this.error = '';
    this.mensaje = '';

    this.vista = null;
    this.resultado = null;

    let entrada: TrasladoEntrada;

    try {

      entrada = this.entrada();

    } catch (error) {

      this.fallar(error);

      return;
    }

    this.cargando = true;

    this.api.preview(entrada).subscribe({

      next: (respuesta) => {

        this.vista = respuesta;

        this.cargando = false;
      },

      error: (error) => {

        this.fallar(error);
      }

    });
  }


  // =========================================================
  // APLICAR TRASLADO
  // =========================================================

  aplicar(): void {

    if (
      this.cargando ||
      !this.vista?.validacion.permitido
    ) {
      return;
    }

    let entrada: TrasladoEntrada;

    try {

      entrada = this.entrada();

    } catch (error) {

      this.fallar(error);

      return;
    }

    const confirmado = window.confirm(
      '¿Confirma el traslado integral de las cuentas entre agencias? ' +
      'Esta operación generará dos comprobantes contables.'
    );

    if (!confirmado) {
      return;
    }

    this.error = '';
    this.mensaje = '';

    this.cargando = true;

    this.api.aplicar(entrada).subscribe({

      next: (respuesta) => {

        this.resultado = respuesta;

        this.vista = null;

        this.mensaje =
          'Traslado aplicado correctamente.';

        this.cargando = false;

        // Actualizar las cuentas del asociado.

        if (this.asociado) {

          this.consultarCuentas(
            this.asociado.idDatosPersonal
          );
        }
      },

      error: (error) => {

        this.fallar(error);
      }

    });
  }


  // =========================================================
  // FECHA ACTUAL
  // =========================================================

  private obtenerFechaActual(): string {

    const fecha = new Date();

    const anio = fecha.getFullYear();

    const mes = String(
      fecha.getMonth() + 1
    ).padStart(2, '0');

    const dia = String(
      fecha.getDate()
    ).padStart(2, '0');

    return `${anio}-${mes}-${dia}`;
  }


  // =========================================================
  // MANEJO DE ERRORES
  // =========================================================

  private fallar(error: unknown): void {

    let mensaje =
      'No fue posible realizar la operación.';

    if (error instanceof Error) {

      mensaje = error.message;
    }

    if (
      typeof error === 'object' &&
      error !== null &&
      'error' in error
    ) {

      const respuesta = error.error as {
        message?: string;
        mensaje?: string;
      } | null;

      mensaje =
        respuesta?.message ??
        respuesta?.mensaje ??
        mensaje;
    }

    this.error = mensaje;

    this.cargando = false;
  }

}
