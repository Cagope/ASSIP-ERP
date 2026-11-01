
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { SessionService } from '../../../../../core/auth/session.service';
import { environment } from '../../../../../../environments/environment';
import { AuxiliarCuentasService } from './auxiliar-cuentas.service';
import {
  AuxiliarCuentasFiltro,
  AuxiliarCuentasResponse
} from './auxiliar-cuentas.models';

import { AuxiliarCuentasExporterService } from './auxiliar-cuentas-exporter.service';

/** Agencia autorizada para el usuario autenticado. */
interface AgenciaOpcion {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
}

/** Cuenta del catálogo disponible para seleccionar un rango. */
interface CuentaOpcion {
  idCatalogoCuenta: number;
  idAgencia: number;
  codigoCuenta: string;
  nombreCuenta: string;
  operable: boolean;
}

/** Estructura de la búsqueda existente del plan de cuentas. */
interface CuentaApi {
  id: number;
  idAgencia: number;
  codigoCuenta: string;
  nombre: string;
  operable: boolean;
  controlEntradaSalida: boolean;
}

/**
 * Consulta auxiliares contables por rango de cuentas y período.
 * Utiliza agencias autorizadas, búsqueda de cuentas y auditoría por permiso.
 */
@Component({
  selector: 'app-auxiliar-cuentas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auxiliar-cuentas.component.html',
  styleUrl: './auxiliar-cuentas.component.scss'
})
export class AuxiliarCuentasComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);
  private readonly auxiliarCuentasService = inject(AuxiliarCuentasService);
  private readonly apiUrl = environment.apiUrl;
  private readonly auxiliarCuentasExporter = inject(AuxiliarCuentasExporterService);

  agencias: AgenciaOpcion[] = [];
  cuentasIniciales: CuentaOpcion[] = [];
  cuentasFinales: CuentaOpcion[] = [];

  textoCuentaInicial = '';
  textoCuentaFinal = '';
  buscandoCuentaInicial = false;
  buscandoCuentaFinal = false;
  cargando = false;
  consultado = false;
  mensajeError = '';
  informe: AuxiliarCuentasResponse | null = null;

  auditoriaAbierta: number | null = null;

  filtros: AuxiliarCuentasFiltro = {
    idAgencia: 0,
    cuentaInicial: '',
    cuentaFinal: '',
    fechaInicial: '',
    fechaFinal: ''
  };

  /**
   * Determina si el usuario puede consultar la trazabilidad contable.
   * Utiliza los permisos efectivos de la sesión, incluidos sus múltiples roles.
   */
  get puedeVerAuditoria(): boolean {
    return this.session.tienePermiso(
      'CONTABILIDAD_AUXILIAR_CUENTAS_AUDIT_VIEW'
    );
  }

  /** Inicializa agencias autorizadas, agencia activa y período de consulta. */
  ngOnInit(): void {
    this.agencias = this.session.agenciasSig()
      .filter(agencia => agencia.idAgencia !== null)
      .map(agencia => ({
        idAgencia: agencia.idAgencia!,
        codigoAgencia: agencia.codigoAgencia ?? '',
        nombreAgencia: agencia.nombreAgencia ?? ''
      }));

    const activa = this.session.agenciaActivaSig()?.idAgencia;
    this.filtros.idAgencia = this.agencias.some(
      agencia => agencia.idAgencia === activa
    ) ? activa! : this.agencias.length === 1
      ? this.agencias[0].idAgencia
      : 0;

    const hoy = new Date();
    const anio = hoy.getFullYear();
    const mes = String(hoy.getMonth() + 1).padStart(2, '0');
    const dia = String(hoy.getDate()).padStart(2, '0');

    this.filtros.fechaInicial = `${anio}-${mes}-01`;
    this.filtros.fechaFinal = `${anio}-${mes}-${dia}`;
  }

  /** Limpia cuentas y resultados al cambiar la agencia seleccionada. */
  cambiarAgencia(): void {
    this.limpiarCuentas();
    this.limpiarResultado();
  }

  /** Busca cuentas iniciales por código o nombre en la agencia seleccionada. */
  buscarCuentasIniciales(texto: string): void {
    this.textoCuentaInicial = texto;
    this.filtros.cuentaInicial = '';
    this.cuentasIniciales = [];
    this.limpiarResultado();

    const idAgencia = this.filtros.idAgencia;
    const q = texto.trim();

    if (!idAgencia || q.length < 2) {
      return;
    }

    this.buscandoCuentaInicial = true;
    this.http.get<CuentaApi[]>(
      `${this.apiUrl}/contabilidad/plan-cuentas/buscar`,
      {
        params: new HttpParams()
          .set('idAgencia', idAgencia)
          .set('q', q)
      }
    ).pipe(
      finalize(() => this.buscandoCuentaInicial = false)
    ).subscribe({
      next: cuentas => {
        if (this.filtros.idAgencia !== idAgencia
            || this.textoCuentaInicial !== texto) {
          return;
        }

        this.cuentasIniciales = cuentas.map(cuenta => ({
          idCatalogoCuenta: cuenta.id,
          idAgencia: cuenta.idAgencia,
          codigoCuenta: cuenta.codigoCuenta,
          nombreCuenta: cuenta.nombre,
          operable: cuenta.operable
        }));
      },
      error: () => {
        if (this.filtros.idAgencia === idAgencia
            && this.textoCuentaInicial === texto) {
          this.mensajeError = 'No fue posible buscar las cuentas iniciales.';
        }
      }
    });
  }

  /** Busca cuentas finales por código o nombre en la agencia seleccionada. */
  buscarCuentasFinales(texto: string): void {
    this.textoCuentaFinal = texto;
    this.filtros.cuentaFinal = '';
    this.cuentasFinales = [];
    this.limpiarResultado();

    const idAgencia = this.filtros.idAgencia;
    const q = texto.trim();

    if (!idAgencia || q.length < 2) {
      return;
    }

    this.buscandoCuentaFinal = true;
    this.http.get<CuentaApi[]>(
      `${this.apiUrl}/contabilidad/plan-cuentas/buscar`,
      {
        params: new HttpParams()
          .set('idAgencia', idAgencia)
          .set('q', q)
      }
    ).pipe(
      finalize(() => this.buscandoCuentaFinal = false)
    ).subscribe({
      next: cuentas => {
        if (this.filtros.idAgencia !== idAgencia
            || this.textoCuentaFinal !== texto) {
          return;
        }

        this.cuentasFinales = cuentas.map(cuenta => ({
          idCatalogoCuenta: cuenta.id,
          idAgencia: cuenta.idAgencia,
          codigoCuenta: cuenta.codigoCuenta,
          nombreCuenta: cuenta.nombre,
          operable: cuenta.operable
        }));
      },
      error: () => {
        if (this.filtros.idAgencia === idAgencia
            && this.textoCuentaFinal === texto) {
          this.mensajeError = 'No fue posible buscar las cuentas finales.';
        }
      }
    });
  }

  /** Selecciona la cuenta inicial y conserva internamente su código. */
  seleccionarCuentaInicial(cuenta: CuentaOpcion): void {
    if (cuenta.idAgencia !== this.filtros.idAgencia) {
      this.mensajeError = 'La cuenta no pertenece a la agencia seleccionada.';
      return;
    }

    this.filtros.cuentaInicial = cuenta.codigoCuenta;
    this.textoCuentaInicial =
      `${cuenta.codigoCuenta} - ${cuenta.nombreCuenta}`;
    this.cuentasIniciales = [];
    this.mensajeError = '';
    this.limpiarResultado();
  }

  /** Selecciona la cuenta final y conserva internamente su código. */
  seleccionarCuentaFinal(cuenta: CuentaOpcion): void {
    if (cuenta.idAgencia !== this.filtros.idAgencia) {
      this.mensajeError = 'La cuenta no pertenece a la agencia seleccionada.';
      return;
    }

    this.filtros.cuentaFinal = cuenta.codigoCuenta;
    this.textoCuentaFinal =
      `${cuenta.codigoCuenta} - ${cuenta.nombreCuenta}`;
    this.cuentasFinales = [];
    this.mensajeError = '';
    this.limpiarResultado();
  }

  /** Consulta el informe después de validar los filtros seleccionados. */
  consultar(): void {
    if (this.cargando) {
      return;
    }

    this.mensajeError = '';
    this.limpiarResultado();

    const error = this.validarFiltros();
    if (error) {
      this.mensajeError = error;
      return;
    }

    this.cargando = true;
    this.auxiliarCuentasService.consultar({
      ...this.filtros
    }).pipe(
      finalize(() => this.cargando = false)
    ).subscribe({
      next: informe => {
        this.informe = informe;
        this.consultado = true;
      },
      error: error => {
        this.mensajeError = error?.error?.message
          || error?.error?.detail
          || 'No fue posible consultar el informe de auxiliares contables.';
      }
    });
  }

  /**
   * Abre o cierra la información de auditoría de un movimiento.
   * Requiere el permiso de auditoría y mantiene un único detalle abierto.
   */
  alternarAuditoria(idAuxiliarContable: number): void {
    if (!this.puedeVerAuditoria) {
      this.auditoriaAbierta = null;
      return;
    }

    this.auditoriaAbierta =
      this.auditoriaAbierta === idAuxiliarContable
        ? null
        : idAuxiliarContable;
  }

  /** Exporta el informe consultado con sus movimientos y trazabilidad autorizada. */
  exportarExcel(): void {
    if (this.cargando || !this.consultado || !this.informe) {
      return;
    }

    const informe = this.informe;
    const agencia = this.agencias.find(
      item => item.idAgencia === informe.idAgencia
    );

    if (!agencia) {
      this.mensajeError = 'La agencia del informe no está autorizada.';
      return;
    }

    const filtrosVigentes =
      this.filtros.idAgencia === informe.idAgencia
      && this.filtros.cuentaInicial === informe.cuentaInicial
      && this.filtros.cuentaFinal === informe.cuentaFinal
      && this.filtros.fechaInicial === informe.fechaInicial
      && this.filtros.fechaFinal === informe.fechaFinal;

    if (!filtrosVigentes) {
      this.mensajeError =
        'Los filtros cambiaron. Consulte nuevamente antes de exportar.';
      return;
    }

    this.mensajeError = '';
    this.auxiliarCuentasExporter.exportar(
      informe,
      {
        codigoAgencia: agencia.codigoAgencia,
        nombreAgencia: agencia.nombreAgencia
      },
      this.session.getUser() ?? 'No disponible',
      this.puedeVerAuditoria
    );
  }

  /** Valida agencia, cuentas seleccionadas y rango de fechas. */
  private validarFiltros(): string {
    if (!this.agencias.some(
      agencia => agencia.idAgencia === this.filtros.idAgencia
    )) {
      return 'Debe seleccionar una agencia autorizada.';
    }

    if (!this.filtros.cuentaInicial || !this.filtros.cuentaFinal) {
      return 'Debe seleccionar las cuentas inicial y final.';
    }

    if (this.filtros.cuentaInicial > this.filtros.cuentaFinal) {
      return 'La cuenta inicial no puede ser posterior a la cuenta final.';
    }

    if (!this.filtros.fechaInicial || !this.filtros.fechaFinal) {
      return 'Debe indicar las fechas inicial y final.';
    }

    if (this.filtros.fechaInicial > this.filtros.fechaFinal) {
      return 'La fecha inicial no puede ser posterior a la fecha final.';
    }

    return '';
  }

  /** Restablece las cuentas cuando cambia el contexto de agencia. */
  private limpiarCuentas(): void {
    this.filtros.cuentaInicial = '';
    this.filtros.cuentaFinal = '';
    this.textoCuentaInicial = '';
    this.textoCuentaFinal = '';
    this.cuentasIniciales = [];
    this.cuentasFinales = [];
    this.mensajeError = '';
  }

  /** Descarta resultados y cierra la auditoría sin modificar los filtros. */
  private limpiarResultado(): void {
    this.informe = null;
    this.consultado = false;
    this.auditoriaAbierta = null;
  }

  /** Indica si el informe contiene cuentas con saldo o movimientos. */
  get tieneResultados(): boolean {
    return (this.informe?.cuentas.length ?? 0) > 0;
  }
}
