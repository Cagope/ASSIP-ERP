import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ReportingApi, ReportQueryRequest, ReportResult, ReportMetadata } from './reporting.api';

/**
 * 💼 Servicio central del módulo Reporting.
 * ------------------------------------------------------------
 * Simplifica el acceso a los endpoints del módulo Reporting.
 * Usa Promises (async/await) para un flujo más limpio.
 *
 * 🔹 Ahora también soporta la consulta de extractos
 *    (usando las vistas del esquema depositos).
 */
@Injectable({ providedIn: 'root' })
export class ReportingService {
  private readonly api = inject(ReportingApi);

  /** 🔹 Lista las vistas disponibles (reporting_metadata) */
  async listarVistas(): Promise<ReportMetadata[]> {
    try {
      const res = await firstValueFrom(this.api.listarMetadata());
      return res ?? [];
    } catch (err) {
      console.error('❌ Error al listar vistas de reporting:', err);
      return [];
    }
  }

  /** 🔹 Ejecuta un reporte dinámico genérico (vista + filtros) */
  async ejecutarReporte(req: ReportQueryRequest): Promise<ReportResult> {
    try {
      const res = await firstValueFrom(this.api.ejecutarConsulta(req));
      return {
        data: res?.data ?? [],
        total: res?.total ?? (res?.data?.length ?? 0),
        durationMs: res?.durationMs ?? 0
      };
    } catch (err) {
      console.error('❌ Error al ejecutar reporte:', err);
      return { data: [], total: 0 };
    }
  }

  // ===========================================================
  // 🧩 NUEVO: Consulta especializada — Extractos de cuentas
  // ===========================================================

  /**
   * 🔹 Consulta extracto de una cuenta de ahorros entre dos fechas.
   * Usa la vista: depositos.vw_extracto_cuentas_persona
   */
  /** 🔹 Consulta extracto de una cuenta de ahorros entre dos fechas */
  async obtenerExtractoCuenta(
    codigoCuenta: string,
    fechaInicial: string,
    fechaFinal: string
  ): Promise<ReportResult> {
    if (!codigoCuenta || !fechaInicial || !fechaFinal) {
      console.warn('⚠️ Faltan parámetros para consultar el extracto.');
      return { data: [], total: 0 };
    }

    const req: ReportQueryRequest = {
      schema: 'depositos',
      view: 'vw_depositos_cuentas_ahorro_detalle', // ✅ vista correcta
      filters: {
        codigo_cuenta: codigoCuenta,
        fecha_desde: fechaInicial, // ✅ nombres estándar
        fecha_hasta: fechaFinal
      }
    };

    try {
      const res = await firstValueFrom(this.api.ejecutarConsulta(req));
      return {
        data: res?.data ?? [],
        total: res?.total ?? 0,
        durationMs: res?.durationMs ?? 0
      };
    } catch (err) {
      console.error('❌ Error al obtener extracto de cuenta:', err);
      return { data: [], total: 0 };
    }
  }

  // ===========================================================
  // 🧾 NUEVO: Consulta avanzada por forma + cuenta + fechas
  // ===========================================================
  async obtenerExtractoCuentaAvanzado(
    idAgencia: number,
    formaAhorro: number,
    codigoCuenta: string,
    fechaInicial: string,
    fechaFinal: string
  ): Promise<ReportResult> {

    if (!idAgencia || !formaAhorro || !codigoCuenta || !fechaInicial || !fechaFinal) {
      console.warn('⚠️ Faltan parámetros para consultar el extracto.');
      return { data: [], total: 0 };
    }

    const req: ReportQueryRequest = {
      schema: 'depositos',
      view: 'vw_extracto_cuentas_persona_total',
      filters: {
        id_agencia: idAgencia,
        id_forma_ahorro: formaAhorro,
        codigo_cuenta: codigoCuenta,
        fecha_inicial: fechaInicial,
        fecha_final: fechaFinal
      }
    };

    console.log('REQ EXTRACTO =>', req);

    try {
      const res = await firstValueFrom(this.api.ejecutarConsulta(req));
      return {
        data: res?.data ?? [],
        total: res?.total ?? 0,
        durationMs: res?.durationMs ?? 0
      };

    console.log('RES EXTRACTO =>', res);

    } catch (err) {
      console.error('❌ Error al obtener extracto:', err);
      return { data: [], total: 0 };
    }
  }
}
