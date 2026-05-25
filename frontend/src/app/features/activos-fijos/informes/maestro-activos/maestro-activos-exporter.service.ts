import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class MaestroActivosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    rows: any[],
    nombreAgencia?: string
  ): void {

    if (!rows || rows.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        this.buildFileName(nombreAgencia),

      hojas: [

        {
          nombreHoja: 'Maestro Activos',

          titulo: 'Maestro general de activos fijos',

          columnas: [

            'ID Activo',
            'Placa',
            'Nombre Activo',

            'Fecha Ingreso',
            'Fecha Garantía',
            'Fecha Baja',
            'Fecha Última Depreciación',

            'ID Agencia',
            'Código Agencia',
            'Agencia',

            'ID Estado',
            'Código Estado',
            'Estado',

            'ID Bloque',
            'Código Bloque',
            'Bloque',

            'ID Localización',
            'Localización',

            'ID Tipo Adquisición',
            'Código Tipo Adquisición',
            'Tipo Adquisición',

            'ID Forma Depreciación',
            'Código Forma Depreciación',
            'Forma Depreciación',

            'Meses Depreciación',

            'Valor Adquisición',
            'Valor Mensual Depreciación',
            'Depreciación Acumulada',
            'Valor Neto',

            'ID Responsable',
            'Responsable (Documento)',

            'ID Proveedor',
            'Proveedor (Documento)',

            'ID Cta Activo',
            'Cta Activo Código',
            'Cta Activo Nombre',

            'ID Cta Depreciación',
            'Cta Depreciación Código',
            'Cta Depreciación Nombre',

            'ID Cta Gasto',
            'Cta Gasto Código',
            'Cta Gasto Nombre',

            'ID Cta Control',
            'Cta Control Código',
            'Cta Control Nombre'
          ],

          filas: rows.map(r => {

            const responsable =
              this.personaConDocumento(
                r?.nombre_responsable,
                r?.documento_responsable
              );

            const proveedor =
              this.personaConDocumento(
                r?.nombre_proveedor,
                r?.documento_proveedor
              );

            return [

              r?.id_activo_fijo ?? '',
              r?.placa_activo ?? '',
              r?.nombre_activo ?? '',

              r?.fecha_ingreso ?? '',
              r?.fecha_garantia ?? '',
              r?.fecha_baja ?? '',
              r?.fecha_ultima_depreciacion ?? '',

              r?.id_agencia ?? '',
              r?.codigo_agencia ?? '',
              r?.nombre_agencia ?? '',

              r?.id_estado_activo ?? '',
              r?.codigo_estado ?? '',
              r?.nombre_estado ?? '',

              r?.id_bloque ?? '',
              r?.codigo_bloque ?? '',
              r?.nombre_bloque ?? '',

              r?.id_localizacion ?? '',
              r?.nombre_localizacion ?? '',

              r?.id_tipo_adquisicion ?? '',
              r?.codigo_adquisicion ?? '',
              r?.nombre_tipo_adquisicion ?? '',

              r?.id_forma_depreciacion ?? '',
              r?.codigo_forma ?? '',
              r?.nombre_forma ?? '',

              Number(r?.meses_depreciacion ?? 0),

              Number(r?.valor_adquisicion ?? 0),
              Number(r?.valor_mensual ?? 0),
              Number(r?.valor_depreciacion_acumulada ?? 0),
              Number(r?.valor_neto ?? 0),

              r?.id_datos_personal_responsable ?? '',
              responsable,

              r?.id_datos_personal_proveedor ?? '',
              proveedor,

              r?.id_catalogo_cuenta_activo ?? '',
              r?.codigo_cuenta_activo ?? '',
              r?.nombre_cuenta_activo ?? '',

              r?.id_catalogo_cuenta_depreciacion ?? '',
              r?.codigo_cuenta_depreciacion ?? '',
              r?.nombre_cuenta_depreciacion ?? '',

              r?.id_catalogo_cuenta_gasto ?? '',
              r?.codigo_cuenta_gasto ?? '',
              r?.nombre_cuenta_gasto ?? '',

              r?.id_catalogo_cuenta_control ?? '',
              r?.codigo_cuenta_control ?? '',
              r?.nombre_cuenta_control ?? ''

            ];

          }),

          anchos: [
            14, 16, 40,
            14, 14, 14, 18,
            12, 16, 28,
            12, 16, 24,
            12, 16, 24,
            14, 24,
            16, 18, 28,
            16, 18, 28,
            18,
            18, 22, 22, 18,
            14, 34,
            14, 34,
            14, 18, 34,
            14, 18, 34,
            14, 18, 34,
            14, 18, 34
          ]
        }

      ]

    });

  }

  private personaConDocumento(
    nombre: string,
    documento: string
  ): string {

    const n =
      (nombre || '').trim();

    const d =
      (documento || '').trim();

    if (!n && !d) {
      return '';
    }

    if (n && d) {
      return `${n} (${d})`;
    }

    return n || d;
  }

  private buildFileName(
    nombreAgencia?: string
  ): string {

    const hoy = new Date();

    const yyyy =
      hoy.getFullYear();

    const mm =
      String(hoy.getMonth() + 1)
        .padStart(2, '0');

    const dd =
      String(hoy.getDate())
        .padStart(2, '0');

    const ag =
      (nombreAgencia || 'TODAS')
        .replace(/\s+/g, '_')
        .replace(/[^\w\-]/g, '');

    return `maestro_activos_full_${ag}_${yyyy}-${mm}-${dd}.xlsx`;
  }
}
