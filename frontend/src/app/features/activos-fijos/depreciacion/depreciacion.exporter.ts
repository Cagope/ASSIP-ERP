import * as XLSX from 'xlsx';
import { DepreciacionPreviewDTO } from './depreciacion.api';

/**
 * Exportador Excel para:
 * ✅ Listado detalle depreciación (REPORTE)
 * ✅ Auxiliares contables (IMPORTABLE a contabilidad.auxiliares_contables)
 */
export class DepreciacionExporter {

  // =========================================================
  // EXPORTAR LISTADO (DETALLE) — REPORTE
  // =========================================================
  static exportarListado(
    detalle: DepreciacionPreviewDTO[],
    params: {
      agenciaNombre: string;
      fechaPeriodo: string;
      fechaContabilizacion: string;
      tipoComprobante: string;
      numeroComprobante: string;
      concepto: string;
    }
  ): void {

    const header = [
      ['REPORTE DEPRECIACIÓN - LISTADO'],
      [`Agencia: ${params.agenciaNombre}`],
      [`Período: ${params.fechaPeriodo}`],
      [`Contabilización: ${params.fechaContabilizacion}`],
      [`Tipo: ${params.tipoComprobante}   Número: ${params.numeroComprobante}`],
      [`Concepto: ${params.concepto}`],
      []
    ];

    const rows = detalle.map((d, i) => ({
      '#': i + 1,
      'Placa': d.placaActivo,
      'Activo': d.nombreActivo,
      'Valor mensual': d.valorMensual,
      'Depreciado': d.depreciacionAcumulada,
      'Saldo pendiente': d.saldoPendiente,
      'Valor período': d.valorPeriodo
    }));

    const ws = XLSX.utils.aoa_to_sheet(header);

    XLSX.utils.sheet_add_json(
      ws,
      rows,
      { origin: 'A8' }
    );

    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(
      wb,
      ws,
      'Listado'
    );

    XLSX.writeFile(
      wb,
      `depreciacion_listado_${params.fechaPeriodo}.xlsx`
    );
  }

  // =========================================================
  // EXPORTAR AUXILIARES CONTABLES
  // =========================================================
  static exportarAuxiliaresContables(
    detalle: DepreciacionPreviewDTO[],
    params: {
      idAgencia: number;
      fechaAuxiliar: string;
      tipoComprobante: string;
      numeroComprobante: string;
      concepto: string;
      estadoMovimiento?: string;
      valorBase?: number;
      idUsuario: number;
    }
  ): void {

    const estado =
      params.estadoMovimiento ?? 'A';

    const base =
      params.valorBase ?? 0;

    const rows: any[] = [];

    detalle.forEach(d => {

      // =====================================================
      // TERCERO CONTABLE
      // =====================================================
      // El tercero corresponde a idDatosPersonal del proveedor.
      // No se debe sustituir por idUsuario.
      // =====================================================
      if (d.idDatosPersonalProveedor == null) {
        throw new Error(
          `El activo ${d.placaActivo} no tiene proveedor/tercero ` +
          'asociado y no puede generar el auxiliar contable.'
        );
      }

      const idTercero =
        Number(d.idDatosPersonalProveedor);

      // =====================================================
      // DETALLE DEL MOVIMIENTO
      // =====================================================
      const detalleMov =
        `Depreciación Activo ${d.placaActivo} - ${params.concepto}`
          .substring(0, 100);

      const valor =
        Number(d.valorPeriodo ?? 0);

      // =====================================================
      // DÉBITO — GASTO DE DEPRECIACIÓN
      // =====================================================
      rows.push({
        id_auxiliar_contable: '',
        id_catalogo_cuenta: Number(d.idCuentaGasto),
        id_agencia: params.idAgencia,
        id_datos_personal: idTercero,
        fecha_auxiliar: params.fechaAuxiliar,
        tipo_comprobante: params.tipoComprobante,
        numero_comprobante: params.numeroComprobante,
        detalle_movimiento: detalleMov,
        estado_movimiento: estado,
        valor_debito: valor,
        valor_credito: 0,
        valor_base: base,

        // Auditoría: usuario autenticado
        fk_seguridad_creacion: params.idUsuario,
        fecha_creacion: '',
        fk_seguridad_edicion: params.idUsuario,
        fecha_edicion: ''
      });

      // =====================================================
      // CRÉDITO — DEPRECIACIÓN ACUMULADA
      // =====================================================
      rows.push({
        id_auxiliar_contable: '',
        id_catalogo_cuenta: Number(d.idCuentaDepreciacion),
        id_agencia: params.idAgencia,
        id_datos_personal: idTercero,
        fecha_auxiliar: params.fechaAuxiliar,
        tipo_comprobante: params.tipoComprobante,
        numero_comprobante: params.numeroComprobante,
        detalle_movimiento: detalleMov,
        estado_movimiento: estado,
        valor_debito: 0,
        valor_credito: valor,
        valor_base: base,

        // Auditoría: usuario autenticado
        fk_seguridad_creacion: params.idUsuario,
        fecha_creacion: '',
        fk_seguridad_edicion: params.idUsuario,
        fecha_edicion: ''
      });

    });

    // =========================================================
    // COLUMNAS IMPORTABLES
    // =========================================================
    //
    // La auditoría NO se genera desde Angular.
    //
    // fk_seguridad_creacion
    // fecha_creacion
    // fk_seguridad_edicion
    // fecha_edicion
    //
    // deben ser determinados por el proceso backend que
    // persista/importa los auxiliares contables.
    // =========================================================
    const columns = [
      'id_auxiliar_contable',
      'id_catalogo_cuenta',
      'id_agencia',
      'id_datos_personal',
      'fecha_auxiliar',
      'tipo_comprobante',
      'numero_comprobante',
      'detalle_movimiento',
      'estado_movimiento',
      'valor_debito',
      'valor_credito',
      'valor_base'
    ];

    const ws = XLSX.utils.json_to_sheet(
      rows,
      { header: columns }
    );

    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(
      wb,
      ws,
      'auxiliares_contables'
    );

    const filename =
      `auxiliares_contables_depreciacion_${params.fechaAuxiliar}.xlsx`;

    XLSX.writeFile(
      wb,
      filename
    );
  }

}
