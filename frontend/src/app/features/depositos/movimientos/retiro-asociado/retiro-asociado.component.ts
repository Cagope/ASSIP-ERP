import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NumericFormatDirective } from '../../../../shared/utils/numeric-format.directive';
import { SessionService } from '../../../../core/auth/session.service';
import { Agencia, AgenciasApi } from '../../../general/agencias/agencia.api';
import { PersonaBusquedaDTO } from '../../../../shared/personas/personas-busqueda.dto';
import {
  CajaAbiertaRetiro,
  RetiroAsociadoApi,
  RetiroPreviewContable
} from './retiro-asociado.api';

import {
  EstadoFinalRetiro,
  RetiroAsociadoCuenta,
  RetiroAsociadoDistribucion,
  RetiroAsociadoEntrada
} from './retiro-asociado.models';

@Component({
  selector: 'app-retiro-asociado',
  standalone: true,
  imports: [CommonModule, FormsModule, NumericFormatDirective],
  templateUrl: './retiro-asociado.component.html',
  styleUrls: ['./retiro-asociado.component.scss']
})
export class RetiroAsociadoComponent implements OnInit {
  private readonly api = inject(RetiroAsociadoApi);
  private readonly agenciasApi = inject(AgenciasApi);
  private readonly session = inject(SessionService);

  cargando = false;
  cargandoAgencias = false;
  error = '';
  mensaje = '';

  filtros = {
    documento: '',
    nombres: '',
    primerApellido: '',
    segundoApellido: ''
  };

  resultadosBusqueda: PersonaBusquedaDTO[] = [];
  asociado: PersonaBusquedaDTO | null = null;

  agencias: Agencia[] = [];
  idAgenciaOperacion: number | null = null;
  fechaProceso = this.fechaActual();

  estadoFinal: EstadoFinalRetiro = 'R';
  comentarioCuentas = '';
  numeroActa = '';
  fechaActa = '';
  private ultimaObservacionSugerida = '';

  idCaja: number | null = null;
  valorCaja = 0;

  cajasAbiertas: CajaAbiertaRetiro[] = [];
  cargandoCajas = false;

  cuentas: RetiroAsociadoCuenta[] = [];
  distribuciones: RetiroAsociadoDistribucion[] = [];
  preview: RetiroPreviewContable | null = null;

  ngOnInit(): void {
    this.cargarAgencias();
  }

  private cargarAgencias(): void {
    this.cargandoAgencias = true;

    this.agenciasApi.listar().subscribe({
      next: agencias => {
        this.agencias = agencias ?? [];

        const activa = this.session.agenciaActivaSig()?.idAgencia;

        if (
          activa != null &&
          this.agencias.some(a => a.idAgencia === activa)
        ) {
          this.idAgenciaOperacion = activa;
        }

        this.cargandoAgencias = false;
        this.cargarCajasAbiertas();
      },
      error: err => {
        this.cargandoAgencias = false;
        this.error = this.obtenerMensajeError(err);
      }
    });
  }

  cargarCajasAbiertas(): void {
    this.cajasAbiertas = [];
    this.idCaja = null;
    this.preview = null;

    if (
      !this.idAgenciaOperacion ||
      !this.fechaProceso ||
      this.totalCaja() <= 0
    ) {
      this.cargandoCajas = false;
      return;
    }

    this.cargandoCajas = true;

    this.api.obtenerCajasAbiertas(
      this.idAgenciaOperacion,
      this.fechaProceso
    ).subscribe({
      next: cajas => {
        this.cajasAbiertas = cajas ?? [];
        this.cargandoCajas = false;
      },
      error: (err: unknown) => {
        this.cargandoCajas = false;
        this.error = this.obtenerMensajeError(err);
      }
    });
  }

  buscar(): void {
    if (this.cargando) return;

    this.limpiarMensajes();
    this.limpiarSeleccion();

    const documento = this.limpiarTexto(this.filtros.documento);
    const nombres = this.limpiarTexto(this.filtros.nombres);
    const primerApellido = this.limpiarTexto(this.filtros.primerApellido);
    const segundoApellido = this.limpiarTexto(this.filtros.segundoApellido);

    if (
      !documento &&
      !nombres &&
      !primerApellido &&
      !segundoApellido
    ) {
      this.error = 'Debe ingresar al menos un criterio de búsqueda.';
      return;
    }

    this.cargando = true;

    this.api.buscarAsociados({
      documento,
      nombres,
      primerApellido,
      segundoApellido
    }).subscribe({
      next: personas => {
        this.resultadosBusqueda = personas ?? [];

        if (!this.resultadosBusqueda.length) {
          this.mensaje =
            'No se encontraron asociados con los criterios ingresados.';
        }

        this.cargando = false;
      },
      error: err => {
        this.cargando = false;
        this.error = this.obtenerMensajeError(err);
      }
    });
  }

  seleccionarAsociado(persona: PersonaBusquedaDTO): void {
    if (this.cargando) return;

    this.limpiarSeleccion();
    this.limpiarMensajes();

    this.asociado = persona;
    this.sugerirComentario();
    this.cargando = true;

    this.api.consultarCuentas(persona.idDatosPersonal).subscribe({
      next: cuentas => {
        this.cuentas = (cuentas ?? []).filter(
          c => !['C', 'R'].includes((c.estadoCuenta ?? '').trim())
        );

        this.valorCaja = this.totalSaldos();
        this.recalcularDistribuciones();
        this.cargarCajasAbiertas();

        this.cargando = false;

        if (!this.cuentas.length) {
          this.mensaje =
            'El asociado no tiene cuentas pendientes de retiro.';
        }
      },
      error: err => {
        this.cargando = false;
        this.cuentas = [];
        this.distribuciones = [];
        this.error = this.obtenerMensajeError(err);
      }
    });
  }

  consultarPreview(): void {
    if (this.cargando) return;

    this.preview = null;
    this.limpiarMensajes();

    if (!this.validarDatosRetiro()) return;

    this.cargando = true;

    this.api.preview(this.construirEntrada()).subscribe({
      next: resultado => {
        this.preview = resultado;
        this.cargando = false;
      },
      error: err => {
        this.preview = null;
        this.cargando = false;
        this.error = this.obtenerMensajeError(err);
      }
    });
  }

  consultarCuentas(): void {
    this.consultarPreview();
  }

  ejecutar(): void {
    if (!this.puedeEjecutar() || !this.validarDatosRetiro()) {
      return;
    }

    this.cargando = true;
    this.limpiarMensajes();

    this.api.aplicar(this.construirEntrada()).subscribe({
      next: () => {
        this.cargando = false;
        this.limpiarSeleccion();
        this.mensaje =
          'Retiro integral registrado correctamente. ' +
          'Los pagos quedan pendientes en Caja y/o Tesorería.';
      },
      error: err => {
        this.cargando = false;
        this.preview = null;
        this.error = this.obtenerMensajeError(err);
      }
    });
  }

  private validarDatosRetiro(): boolean {
    if (!this.asociado || !this.cuentas.length) {
      this.error =
        'Debe seleccionar un asociado con cuentas para retirar.';
      return false;
    }

    if (
      !this.idAgenciaOperacion ||
      this.idAgenciaOperacion !==
        this.session.agenciaActivaSig()?.idAgencia
    ) {
      this.error =
        'La agencia de operación debe ser la agencia activa.';
      return false;
    }

    if (this.fechaProceso !== this.fechaActual()) {
      this.error =
        'La fecha del retiro debe corresponder al día actual.';
      return false;
    }

    if (this.estadoFinal !== 'C' && this.estadoFinal !== 'R') {
      this.error = 'Seleccione un estado final válido.';
      return false;
    }

    const comentario = this.comentarioCuentas.trim();

    if (!comentario || comentario.length > 500) {
      this.error =
        'El motivo del retiro es obligatorio y no puede superar 500 caracteres.';
      return false;
    }

    if (this.tieneCuentasNoOperativas()) {
      this.error =
        'Existen cuentas con saldo positivo en estado no operativo.';
      return false;
    }

    if (
      !Number.isFinite(Number(this.valorCaja)) ||
      Number(this.valorCaja) < 0 ||
      this.enCentavos(this.valorCaja) >
        this.enCentavos(this.totalSaldos())
    ) {
      this.error =
        'El valor de Caja debe estar entre cero y el saldo total.';
      return false;
    }

    if (
      this.totalCaja() > 0 &&
      (
        !this.idCaja ||
        !this.cajasAbiertas.some(c => c.idCaja === this.idCaja)
      )
    ) {
      this.error =
        'Debe seleccionar la caja que atenderá el pago en efectivo.';
      return false;
    }

    if (this.diferencia() !== 0) {
      this.error =
        'La distribución del retiro no coincide con los saldos.';
      return false;
    }

    return true;
  }

  private construirEntrada(): RetiroAsociadoEntrada {
    return {
      idDatosPersonal: this.asociado!.idDatosPersonal,
      idAgenciaOperacion: this.idAgenciaOperacion!,
      idCaja: this.totalCaja() > 0 ? this.idCaja : null,
      fechaProceso: this.fechaProceso,
      estadoFinal: this.estadoFinal,
      comentarioCuentas: this.comentarioCuentas.trim(),
      numeroActa: this.numeroActa.trim() || null,
      fechaActa: this.fechaActa || null,
      mediosPago: [
        {
          destino: 'CAJA',
          valor: this.totalCaja()
        },
        {
          destino: 'CXP',
          valor: this.totalCxp()
        }
      ],
      distribuciones: this.distribuciones.map(d => ({ ...d }))
    };
  }

  private enCentavos(valor: number | string): number {
    return Math.round(Number(valor) * 100);
  }

  private recalcularDistribuciones(): void {
    let cajaRestante = Math.max(
      0,
      this.enCentavos(this.valorCaja)
    );

    this.distribuciones = [...this.cuentas]
      .sort((a, b) => a.idCuentaAhorro - b.idCuentaAhorro)
      .map(cuenta => {
        const saldo = this.enCentavos(cuenta.saldoActual);
        const caja = Math.min(saldo, cajaRestante);

        cajaRestante -= caja;

        return {
          idCuentaAhorro: cuenta.idCuentaAhorro,
          valorCaja: caja / 100,
          valorCxp: (saldo - caja) / 100
        };
      });
  }

  totalSaldos(): number {
    return this.cuentas.reduce(
      (s, c) => s + this.enCentavos(c.saldoActual),
      0
    ) / 100;
  }

  totalCaja(): number {
    return this.enCentavos(this.valorCaja) / 100;
  }

  totalCxp(): number {
    return (
      this.enCentavos(this.totalSaldos()) -
      this.enCentavos(this.valorCaja)
    ) / 100;
  }

  diferencia(): number {
    return (
      this.enCentavos(this.totalSaldos()) -
      this.enCentavos(this.totalCaja()) -
      this.enCentavos(this.totalCxp())
    ) / 100;
  }

  cuentaOperativa(cuenta: RetiroAsociadoCuenta): boolean {
    return cuenta.estadoOperativo === true;
  }

  tieneCuentasNoOperativas(): boolean {
    return this.cuentas.some(
      c => Number(c.saldoActual) > 0 && !this.cuentaOperativa(c)
    );
  }

  distribucionCuenta(idCuentaAhorro: number): RetiroAsociadoDistribucion | undefined {
    return this.distribuciones.find(d => d.idCuentaAhorro === idCuentaAhorro);
  }

  sugerirComentario(): void {
    // Conservar las modificaciones manuales del funcionario.
    if (this.comentarioCuentas !== this.ultimaObservacionSugerida) return;

    const tipo = this.estadoFinal === 'C' ? 'Retiro voluntario' : 'Retiro forzoso';
    const numero = this.numeroActa.trim();
    const fecha = this.fechaActa
      ? `${this.fechaActa.slice(8, 10)}/${this.fechaActa.slice(5, 7)}/${this.fechaActa.slice(0, 4)}`
      : '';
    const referencia = numero
      ? `, según acta No. ${numero}${fecha ? ` del ${fecha}` : ''}`
      : fecha ? `, según acta del ${fecha}` : '';

    this.ultimaObservacionSugerida = `${tipo} del asociado${referencia}.`;
    this.comentarioCuentas = this.ultimaObservacionSugerida;
    this.preview = null;
  }

  actualizarDatosAdministrativos(): void {
    this.sugerirComentario();
    this.actualizarDistribucion();
  }

  actualizarDistribucion(): void {
    this.recalcularDistribuciones();
    this.preview = null;
    this.limpiarMensajes();
  }

  actualizarValorCaja(): void {
    this.actualizarDistribucion();
    this.cargarCajasAbiertas();
  }

  actualizarAgenciaOFecha(): void {
    this.actualizarDistribucion();
    this.cargarCajasAbiertas();
  }

  puedeConsultar(): boolean {
    return (
      !this.cargando &&
      !!this.asociado &&
      this.cuentas.length > 0 &&
      !!this.idAgenciaOperacion
    );
  }

  puedeEjecutar(): boolean {
    return (
      !this.cargando &&
      !!this.preview &&
      !!this.asociado &&
      this.cuentas.length > 0 &&
      this.diferencia() === 0 &&
      !this.tieneCuentasNoOperativas() &&
      (
        this.totalCaja() === 0 ||
        (
          this.idCaja !== null &&
          this.cajasAbiertas.some(c => c.idCaja === this.idCaja)
        )
      )
    );
  }

  limpiar(): void {
    this.filtros = {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: ''
    };

    this.resultadosBusqueda = [];
    this.limpiarSeleccion();

    this.fechaProceso = this.fechaActual();
    this.estadoFinal = 'R';
    this.comentarioCuentas = '';
    this.ultimaObservacionSugerida = '';
    this.numeroActa = '';
    this.fechaActa = '';
    this.idCaja = null;

    this.limpiarMensajes();
  }

  private limpiarSeleccion(): void {
    this.asociado = null;
    this.cuentas = [];
    this.distribuciones = [];
    this.valorCaja = 0;
    this.idCaja = null;
    this.cajasAbiertas = [];
    this.preview = null;
    this.numeroActa = '';
    this.fechaActa = '';
    this.comentarioCuentas = '';
    this.ultimaObservacionSugerida = '';
  }

  private limpiarMensajes(): void {
    this.error = '';
    this.mensaje = '';
  }

  private limpiarTexto(valor?: string | null): string {
    return valor?.trim() ?? '';
  }

  private obtenerMensajeError(err: any): string {
    const respuesta = err?.error;

    if (typeof respuesta === 'string') {
      return respuesta;
    }

    return (
      respuesta?.message ||
      respuesta?.mensaje ||
      respuesta?.error ||
      err?.message ||
      'No fue posible procesar el retiro integral.'
    );
  }

  private fechaActual(): string {
    const fecha = new Date();

    return (
      `${fecha.getFullYear()}-` +
      `${String(fecha.getMonth() + 1).padStart(2, '0')}-` +
      `${String(fecha.getDate()).padStart(2, '0')}`
    );
  }
}
