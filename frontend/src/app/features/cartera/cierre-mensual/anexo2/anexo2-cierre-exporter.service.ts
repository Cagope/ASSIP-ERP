import {
  Injectable
} from '@angular/core';

import * as XLSX from 'xlsx';

import {
  DetalleAnexo2,
  MoraAnexo2,
  ResumenAnexo2,
  TrabajoAnexo2
} from './anexo2-cierre.api';


export interface DatosExportacionAnexo2 {

  idModeloPe: number;

  fechaCorte: string;

  resumen: ResumenAnexo2[];

  detalle: DetalleAnexo2[];

  trabajo: TrabajoAnexo2[];

  mora: MoraAnexo2[];

}


@Injectable({
  providedIn: 'root'
})
export class Anexo2CierreExporterService {


  // =========================================================
  // EXPORTAR MODELO
  // =========================================================

  exportarModelo(
    datos: DatosExportacionAnexo2
  ): void {

    if (!datos) {
      return;
    }

    if (
      !datos.detalle?.length
      && !datos.trabajo?.length
      && !datos.mora?.length
      && !datos.resumen?.length
    ) {
      return;
    }

    const wb =
      XLSX.utils.book_new();


    // =======================================================
    // 1. RESUMEN
    // =======================================================

    this.agregarResumen(
      wb,
      datos
    );


    // =======================================================
    // 2. RESULTADO
    // =======================================================

    this.agregarResultado(
      wb,
      datos.detalle
    );


    // =======================================================
    // 3. HOJA DE TRABAJO
    // =======================================================

    this.agregarHojaTrabajo(
      wb,
      datos.trabajo
    );


    // =======================================================
    // 4. MORA
    // =======================================================

    this.agregarMora(
      wb,
      datos.mora
    );


    // =======================================================
    // ARCHIVO
    // =======================================================

    const nombreArchivo =
      `ANEXO2_MODELO_${datos.idModeloPe}_${this.periodo(datos.fechaCorte)}.xlsx`;

    XLSX.writeFile(
      wb,
      nombreArchivo
    );

  }


  // =========================================================
  // HOJA: RESUMEN
  // =========================================================

  private agregarResumen(
    wb: XLSX.WorkBook,
    datos: DatosExportacionAnexo2
  ): void {

    const nombreModelo =
      this.obtenerNombreModelo(
        datos
      );

    const filas: any[][] = [];


    // =======================================================
    // CABECERA
    // =======================================================

    filas.push(
      [
        'ANEXO 2 - PÉRDIDA ESPERADA'
      ],
      [
        'Modelo',
        datos.idModeloPe
      ],
      [
        'Nombre modelo',
        nombreModelo
      ],
      [
        'Fecha de corte',
        datos.fechaCorte ?? ''
      ],
      [
        'Total créditos',
        datos.detalle?.length ?? 0
      ],
      []
    );


    // =======================================================
    // DISTRIBUCIÓN POR CALIFICACIÓN
    // =======================================================

    filas.push([
      'Calificación',
      'Créditos',
      '% Créditos',
      'Saldo capital',
      '% Saldo',
      'Pérdida esperada',
      '% Pérdida esperada'
    ]);


    for (
      const fila of datos.resumen ?? []
    ) {

      filas.push([
        fila.calificacion,
        fila.cantidadCreditos,
        fila.porcentajeCantidad,
        fila.saldoCapital,
        fila.porcentajeSaldo,
        fila.perdidaEsperada,
        fila.porcentajePerdidaEsperada
      ]);

    }


    // =======================================================
    // TOTAL
    // =======================================================

    if (datos.resumen?.length) {

      filas.push([
        'TOTAL',
        this.sumar(
          datos.resumen,
          x => x.cantidadCreditos
        ),
        100,
        this.sumar(
          datos.resumen,
          x => x.saldoCapital
        ),
        100,
        this.sumar(
          datos.resumen,
          x => x.perdidaEsperada
        ),
        100
      ]);

    }


    const ws =
      XLSX.utils.aoa_to_sheet(
        filas
      );


    // =======================================================
    // FORMATOS
    // =======================================================

    ws['!cols'] = [
      { wch: 24 },
      { wch: 18 },
      { wch: 18 },
      { wch: 20 },
      { wch: 16 },
      { wch: 22 },
      { wch: 20 }
    ];


    this.formatearColumnaNumerica(
      ws,
      1,
      7,
      filas.length - 1,
      '#,##0'
    );

    this.formatearColumnaNumerica(
      ws,
      2,
      7,
      filas.length - 1,
      '0.00'
    );

    this.formatearColumnaNumerica(
      ws,
      3,
      7,
      filas.length - 1,
      '#,##0'
    );

    this.formatearColumnaNumerica(
      ws,
      4,
      7,
      filas.length - 1,
      '0.00'
    );

    this.formatearColumnaNumerica(
      ws,
      5,
      7,
      filas.length - 1,
      '#,##0'
    );

    this.formatearColumnaNumerica(
      ws,
      6,
      7,
      filas.length - 1,
      '0.00'
    );


    ws['!autofilter'] = {
      ref: `A7:G${filas.length}`
    };


    XLSX.utils.book_append_sheet(
      wb,
      ws,
      'RESUMEN'
    );

  }


  // =========================================================
  // HOJA: RESULTADO
  // =========================================================

  private agregarResultado(
    wb: XLSX.WorkBook,
    detalle: DetalleAnexo2[]
  ): void {

    const filas =
      (detalle ?? []).map(
        fila => ({

          'ID Cierre':
            fila.idCierreCartera,

          'Fecha Corte':
            fila.fechaCorte,

          'ID Crédito':
            fila.idCarteraCredito,

          'ID Foto Crédito':
            fila.idCierreCarteraCredito,

          'ID Resultado':
            fila.idCierreCarteraResultado,

          'ID Agencia':
            fila.idAgencia,

          'ID Persona':
            fila.idDatosPersonal,

          'ID Línea':
            fila.idLineaCredito,

          'Código Línea':
            fila.codigoLineaCredito,

          'Línea Crédito':
            fila.nombreLineaCredito,

          'Pagaré':
            fila.pagareCartera,

          'Tipo Documento':
            fila.tipoDocumento,

          'Documento':
            fila.documento,

          'Nombres':
            fila.nombres,

          'Primer Apellido':
            fila.primerApellido,

          'Segundo Apellido':
            fila.segundoApellido,

          'Nombre Completo':
            fila.nombreCompleto,

          'ID Modelo PE':
            fila.idModeloPe,

          'Código Modelo':
            fila.codigoModeloPe,

          'Modelo PE':
            fila.nombreModeloPe,

          'Código Clasificación':
            fila.codigoClasificacionCredito,

          'Clasificación':
            fila.descripcionClasificacionCredito,

          'Tipo Persona':
            fila.tipoPersona,

          'Código Garantía':
            fila.codigoGarantiaCredito,

          'Garantía':
            fila.descripcionGarantiaCredito,

          'Tipo Garantía':
            fila.tipoGarantia,

          'Valor Garantías Crédito':
            fila.valorGarantiasCredito,

          '% Garantías Crédito':
            fila.porcentajeGarantiasCredito,

          'Fecha Desembolso':
            fila.fechaDesembolso,

          'Fecha Vencimiento':
            fila.fechaVencimiento,

          'Saldo Capital':
            fila.saldoCapital,

          'Saldo Intereses':
            fila.saldoIntereses,

          'Saldo Otros Conceptos':
            fila.saldoOtrosConceptos,

          'Saldo Aportes':
            fila.saldoAportes,

          'Aportes Aplicados':
            fila.valorAportesAplicados,

          'VEA':
            fila.vea,

          'PI':
            fila.pi,

          'PDI':
            fila.pdi,

          'Pérdida Esperada':
            fila.perdidaEsperada,

          '% Pérdida Esperada':
            fila.porcentajePerdidaEsperada,

          'Deterioro Capital':
            fila.deterioroCapital,

          'Deterioro Intereses':
            fila.deterioroIntereses,

          'Deterioro Otros':
            fila.deterioroOtros,

          'Días Mora':
            fila.diasMora,

          'Riesgo Inicial':
            fila.edadRiesgoInicial,

          'Edad Mora':
            fila.edadDeMora,

          'Edad Riesgo':
            fila.edadDeRiesgo,

          'Edad PE':
            fila.edadPe,

          'Edad Homologada':
            fila.edadHomologada,

          'Edad Contable':
            fila.edadContable,

          'Forma Pago':
            fila.codigoFormaPago,

          'Estado Jurídico':
            fila.codigoEstadoJuridico,

          'Es Libranza':
            this.siNo(
              fila.esLibranza
            ),

          'Es Reestructurado':
            this.siNo(
              fila.esReestructurado
            ),

          'Método Cálculo':
            fila.codigoMetodoCalculo

        })
      );

    const ws =
      XLSX.utils.json_to_sheet(
        filas
      );

    this.ajustarHoja(
      ws,
      filas.length,
      56
    );

    this.formatearColumnasPorNombre(
      ws,
      [
        'Valor Garantías Crédito',
        'Saldo Capital',
        'Saldo Intereses',
        'Saldo Otros Conceptos',
        'Saldo Aportes',
        'Aportes Aplicados',
        'VEA',
        'Pérdida Esperada',
        'Deterioro Capital',
        'Deterioro Intereses',
        'Deterioro Otros'
      ],
      '#,##0'
    );

    this.formatearColumnasPorNombre(
      ws,
      [
        '% Garantías Crédito',
        'PI',
        'PDI',
        '% Pérdida Esperada'
      ],
      '0.0000'
    );

    XLSX.utils.book_append_sheet(
      wb,
      ws,
      'RESULTADO'
    );
  }

  // =========================================================
  // HOJA: HOJA_TRABAJO
  // =========================================================

  private agregarHojaTrabajo(
    wb: XLSX.WorkBook,
    trabajo: TrabajoAnexo2[]
  ): void {

    const filas =
      (trabajo ?? []).map(
        fila => ({

          // ===================================================
          // IDENTIFICACIÓN
          // ===================================================

          'ID Cierre':
            fila.idCierreCartera,

          'Fecha Corte':
            fila.fechaCorte,

          'ID Crédito':
            fila.idCarteraCredito,

          'ID Foto Crédito':
            fila.idCierreCarteraCredito,

          'ID Persona':
            fila.idDatosPersonal,

          'ID Agencia':
            fila.idAgencia,

          'ID Línea':
            fila.idLineaCredito,

          'Código Línea':
            fila.codigoLineaCredito,

          'Línea Crédito':
            fila.nombreLineaCredito,

          'Pagaré':
            fila.pagareCartera,

          'Documento':
            fila.documento,

          'Nombre Completo':
            fila.nombreCompleto,

          'ID Modelo PE':
            fila.idModeloPe,

          'Modelo PE':
            fila.nombreModeloPe,

          'Clasificación Crédito':
            fila.codigoClasificacionCredito,

          'Forma Pago':
            fila.codigoFormaPago,

          'ID Empresa Libranza':
            fila.idEmpresaLibranza,

          'Tipo Persona':
            fila.tipoPersona,

          'Código Garantía':
            fila.codigoGarantiaCredito,

          'Fecha Desembolso':
            fila.fechaDesembolso,

          // ===================================================
          // ESTADO DE ENTRADA
          // ===================================================

          'Días Mora Actual':
            fila.diasMoraActual,

          'Edad Mora Entrada PE':
            fila.edadMoraEntradaPe,

          'Edad Riesgo Entrada PE':
            fila.edadRiesgoEntradaPe,

          'Saldo Actual':
            fila.saldoActual,

          'Saldo Intereses Causados':
            fila.saldoInteresesCausados,

          'Saldo Aportes Fecha Corte':
            fila.saldoAportesFechaCorte,

          'Valor Aportes Crédito':
            fila.valorAportesCredito,

          'Costas Judiciales':
            fila.valorCostasJudiciales,

          'Otros Conceptos':
            fila.valorOtrosConceptos,


          // ===================================================
          // HISTÓRICO / VARIABLES BASE
          // ===================================================

          'Mora Máx. 3M':
            fila.moraMax3m,

          'Mora Máx. 12M':
            fila.moraMax12m,

          'Mora Máx. 24M':
            fila.moraMax24m,

          'Mora Máx. 36M':
            fila.moraMax36m,

          'Cantidad Mora 31-60 3M':
            fila.cantidadMora3160_3m,


          // ===================================================
          // VARIABLES MODELO
          // ===================================================

          'EA':
            fila.ea,

          'EA Contenido':
            fila.eaContenido,

          'FE':
            fila.fe,

          'FE Contenido':
            fila.feContenido,

          'VALCUOTA':
            fila.valcuota,

          'VALCUOTA Contenido':
            fila.valcuotaContenido,

          'FONDPLAZO':
            fila.fondplazo,

          'FONDPLAZO Contenido':
            fila.fondplazoContenido,

          'MORA1230':
            fila.mora1230,

          'MORA1230 Contenido':
            fila.mora1230Contenido,

          'MORA1260':
            fila.mora1260,

          'MORA1260 Contenido':
            fila.mora1260Contenido,

          'SINMORA':
            fila.sinmora,

          'SINMORA Contenido':
            fila.sinmoraContenido,

          'MORA2430N':
            fila.mora2430n,

          'MORA2430N Contenido':
            fila.mora2430nContenido,

          'MORA315':
            fila.mora315,

          'MORA315 Contenido':
            fila.mora315Contenido,

          'MORTRIM':
            fila.mortrim,

          'MORTRIM Contenido':
            fila.mortrimContenido,

          'MORA3660':
            fila.mora3660,

          'MORA3660 Máx. 36M':
            fila.mora3660MoraMax36m,

          'MORA3660 Máx. 24M':
            fila.mora3660MoraMax24m,


          // ===================================================
          // BETAS
          // ===================================================

          'Beta Intercepto':
            fila.betaIntercepto,

          'Beta EA':
            fila.betaEa,

          'Beta FE':
            fila.betaFe,

          'Beta VALCUOTA':
            fila.betaValcuota,

          'Beta FONDPLAZO':
            fila.betaFondplazo,

          'Beta MORA1230':
            fila.betaMora1230,

          'Beta MORA1260':
            fila.betaMora1260,

          'Beta SINMORA':
            fila.betaSinmora,

          'Beta MORA2430N':
            fila.betaMora2430n,

          'Beta MORA315':
            fila.betaMora315,

          'Beta MORTRIM':
            fila.betaMortrim,

          'Beta MORA3660':
            fila.betaMora3660,


          // ===================================================
          // APORTE A Z
          // ===================================================

          'Aporte Z Intercepto':
            fila.aporteZIntercepto,

          'Aporte Z EA':
            fila.aporteZEa,

          'Aporte Z FE':
            fila.aporteZFe,

          'Aporte Z VALCUOTA':
            fila.aporteZValcuota,

          'Aporte Z FONDPLAZO':
            fila.aporteZFondplazo,

          'Aporte Z MORA1230':
            fila.aporteZMora1230,

          'Aporte Z MORA1260':
            fila.aporteZMora1260,

          'Aporte Z SINMORA':
            fila.aporteZSinmora,

          'Aporte Z MORA2430N':
            fila.aporteZMora2430n,

          'Aporte Z MORA315':
            fila.aporteZMora315,

          'Aporte Z MORTRIM':
            fila.aporteZMortrim,

          'Aporte Z MORA3660':
            fila.aporteZMora3660,


          // ===================================================
          // RESULTADO MODELO
          // ===================================================

          'Z':
            fila.z,

          'Puntaje':
            fila.puntaje,

          'Calificación Modelo':
            fila.calificacionModelo,

          'Días Default Modelo':
            fila.diasDefaultModelo,

          'Default PE':
            fila.defaultPe,

          'Calificación PE':
            fila.calificacionPe,

          'Edad Deterioro':
            fila.edadDeterioro,

          'Tipo Entidad PE':
            fila.tipoEntidadPe,

          'Calificación Base PI':
            fila.calificacionBasePi,

          'PI':
            fila.pi,


          // ===================================================
          // VEA
          // ===================================================

          'Base VEA Capital':
            fila.baseVeaCapital,

          'Base VEA Intereses':
            fila.baseVeaIntereses,

          'Base VEA Costas Judiciales':
            fila.baseVeaCostasJudiciales,

          'Base VEA Otros':
            fila.baseVeaOtros,

          'Base VEA Aportes':
            fila.baseVeaAportes,

          'Base VEA Ahorro Permanente':
            fila.baseVeaAhorroPermanente,

          'VEA Bruto':
            fila.veaBruto,

          'VEA Deducciones':
            fila.veaDeducciones,

          'VEA':
            fila.vea,


          // ===================================================
          // GARANTÍA / PDI
          // ===================================================

          'Código Garantía PDI':
            fila.codigoGarantiaPdi,

          'Garantía PDI':
            fila.nombreGarantiaPdi,

          'Valor Garantía':
            fila.valorGarantia,

          '% Garantía Reconocido':
            fila.porcentajeGarantiaReconocido,

          'Valor Garantía Reconocido':
            fila.valorGarantiaReconocido,

          'Días Mora PDI':
            fila.diasMoraPdi,

          'Tramo PDI':
            fila.tramoPdi,

          'Días Desde PDI':
            fila.diasDesdePdi,

          'Días Hasta PDI':
            fila.diasHastaPdi,

          'PDI':
            fila.pdi,


          // ===================================================
          // PÉRDIDA ESPERADA
          // ===================================================

          'Pérdida Esperada':
            fila.perdidaEsperada,

          '% Pérdida':
            fila.porcentajePerdida,


          // ===================================================
          // BASES DETERIORO
          // ===================================================

          'Base Deterioro Capital':
            fila.baseDeterioroCapital,

          'Base Deterioro Intereses':
            fila.baseDeterioroIntereses,

          'Base Deterioro Otros':
            fila.baseDeterioroOtros,

          'Base Deterioro Total':
            fila.baseDeterioroTotal,

          '% Deterioro Capital':
            fila.porcentajeDeterioroCapital,

          '% Deterioro Intereses':
            fila.porcentajeDeterioroIntereses,

          '% Deterioro Otros':
            fila.porcentajeDeterioroOtros,

          'Valor Pérdida Capital':
            fila.valorPerdidaCapital,

          'Valor Pérdida Intereses':
            fila.valorPerdidaIntereses,

          'Valor Pérdida Otros':
            fila.valorPerdidaOtros,


          // ===================================================
          // DETERIORO PE
          // ===================================================

          'Deterioro Capital PE':
            fila.deterioroCapitalPe,

          'Deterioro Intereses PE':
            fila.deterioroInteresesPe,

          'Deterioro Otros PE':
            fila.deterioroOtrosPe,

          'Deterioro Total PE':
            fila.deterioroTotalPe,


          // ===================================================
          // HOMOLOGACIÓN
          // ===================================================

          'Días Mora Homologación':
            fila.diasMoraHomologacion,

          'Edad Homologada Individual':
            fila.edadHomologadaIndividual,

          'Edad Contable PE':
            fila.edadContablePe

        })
      );


    const ws =
      XLSX.utils.json_to_sheet(
        filas
      );


    this.ajustarHoja(
      ws,
      filas.length,
      130
    );


    // =======================================================
    // VALORES MONETARIOS
    // =======================================================

    this.formatearColumnasPorNombre(
      ws,
      [
        'Saldo Actual',
        'Saldo Intereses Causados',
        'Saldo Aportes Fecha Corte',
        'Valor Aportes Crédito',
        'Costas Judiciales',
        'Otros Conceptos',

        'Base VEA Capital',
        'Base VEA Intereses',
        'Base VEA Costas Judiciales',
        'Base VEA Otros',
        'Base VEA Aportes',
        'Base VEA Ahorro Permanente',

        'VEA Bruto',
        'VEA Deducciones',
        'VEA',

        'Valor Garantía',
        'Valor Garantía Reconocido',

        'Pérdida Esperada',

        'Base Deterioro Capital',
        'Base Deterioro Intereses',
        'Base Deterioro Otros',
        'Base Deterioro Total',

        'Valor Pérdida Capital',
        'Valor Pérdida Intereses',
        'Valor Pérdida Otros',

        'Deterioro Capital PE',
        'Deterioro Intereses PE',
        'Deterioro Otros PE',
        'Deterioro Total PE'
      ],
      '#,##0'
    );


    // =======================================================
    // PORCENTAJES / DECIMALES
    // =======================================================

    this.formatearColumnasPorNombre(
      ws,
      [
        'PI',
        'PDI',
        '% Garantía Reconocido',
        '% Pérdida',
        '% Deterioro Capital',
        '% Deterioro Intereses',
        '% Deterioro Otros'
      ],
      '0.0000'
    );


    this.formatearColumnasPorNombre(
      ws,
      [
        'EA',
        'FE',
        'VALCUOTA',
        'FONDPLAZO',
        'MORA1230',
        'MORA1260',
        'SINMORA',
        'MORA2430N',
        'MORA315',
        'MORTRIM',
        'MORA3660',

        'Beta Intercepto',
        'Beta EA',
        'Beta FE',
        'Beta VALCUOTA',
        'Beta FONDPLAZO',
        'Beta MORA1230',
        'Beta MORA1260',
        'Beta SINMORA',
        'Beta MORA2430N',
        'Beta MORA315',
        'Beta MORTRIM',
        'Beta MORA3660',

        'Aporte Z Intercepto',
        'Aporte Z EA',
        'Aporte Z FE',
        'Aporte Z VALCUOTA',
        'Aporte Z FONDPLAZO',
        'Aporte Z MORA1230',
        'Aporte Z MORA1260',
        'Aporte Z SINMORA',
        'Aporte Z MORA2430N',
        'Aporte Z MORA315',
        'Aporte Z MORTRIM',
        'Aporte Z MORA3660',

        'Z',
        'Puntaje'
      ],
      '0.000000'
    );


    XLSX.utils.book_append_sheet(
      wb,
      ws,
      'HOJA_TRABAJO'
    );

  }


  // =========================================================
  // HOJA: MORA
  // =========================================================

 private agregarMora(
   wb: XLSX.WorkBook,
   mora: MoraAnexo2[]
 ): void {

   const datos =
     mora ?? [];

   // =======================================================
   // PERÍODOS DISPONIBLES
   // =======================================================

   const periodosMap =
     new Map<
       number,
       string
     >();

   for (const fila of datos) {

     if (
       fila.periodo == null
       || !fila.fechaReferencia
     ) {
       continue;
     }

     periodosMap.set(
       fila.periodo,
       fila.fechaReferencia
     );
   }

   const periodos =
     Array.from(
       periodosMap.entries()
     )
       .sort(
         (a, b) =>
           a[0] - b[0]
       )
       .map(
         ([periodo, fecha]) => ({
           periodo,
           fecha
         })
       );


   // =======================================================
   // AGRUPAR POR CRÉDITO
   // =======================================================

   interface MoraCredito {

     idCierreCartera: number;
     fechaCorte: string;

     idCarteraCredito: number;
     idCierreCarteraCredito: number;

     idAgencia: number;

     idLineaCredito: number;
     codigoLineaCredito: string;
     nombreLineaCredito: string;

     pagareCartera: string;
     documento: string;
     nombreCompleto: string;

     idModeloPe: number;
     nombreModeloPe: string;

     moras: Map<number, number>;
   }


   const creditos =
     new Map<
       string,
       MoraCredito
     >();


   for (const fila of datos) {

     const llave =
       [
         fila.idAgencia,
         fila.idLineaCredito,
         fila.pagareCartera,
         fila.idCarteraCredito
       ].join('|');


     let credito =
       creditos.get(
         llave
       );


     if (!credito) {

       credito = {

         idCierreCartera:
           fila.idCierreCartera,

         fechaCorte:
           fila.fechaCorte,

         idCarteraCredito:
           fila.idCarteraCredito,

         idCierreCarteraCredito:
           fila.idCierreCarteraCredito,

         idAgencia:
           fila.idAgencia,

         idLineaCredito:
           fila.idLineaCredito,

         codigoLineaCredito:
           fila.codigoLineaCredito,

         nombreLineaCredito:
           fila.nombreLineaCredito,

         pagareCartera:
           fila.pagareCartera,

         documento:
           fila.documento,

         nombreCompleto:
           fila.nombreCompleto,

         idModeloPe:
           fila.idModeloPe,

         nombreModeloPe:
           fila.nombreModeloPe,

         moras:
           new Map<number, number>()

       };


       creditos.set(
         llave,
         credito
       );
     }


     credito.moras.set(
       fila.periodo,
       fila.diasMora ?? 0
     );
   }


   // =======================================================
   // UNA FILA POR CRÉDITO
   // =======================================================

   const filas =
     Array.from(
       creditos.values()
     )
       .sort(
         (a, b) => {

           if (
             a.idAgencia
             !== b.idAgencia
           ) {
             return (
               a.idAgencia
               - b.idAgencia
             );
           }

           if (
             a.idLineaCredito
             !== b.idLineaCredito
           ) {
             return (
               a.idLineaCredito
               - b.idLineaCredito
             );
           }

           return String(
             a.pagareCartera
           ).localeCompare(
             String(
               b.pagareCartera
             ),
             undefined,
             {
               numeric: true
             }
           );
         }
       )
       .map(
         credito => {

           const fila:
             Record<string, string | number> = {

               'ID Cierre':
                 credito.idCierreCartera,

               'Fecha Corte':
                 credito.fechaCorte,

               'ID Crédito':
                 credito.idCarteraCredito,

               'ID Foto Crédito':
                 credito.idCierreCarteraCredito,

               'ID Agencia':
                 credito.idAgencia,

               'ID Línea':
                 credito.idLineaCredito,

               'Código Línea':
                 credito.codigoLineaCredito,

               'Línea Crédito':
                 credito.nombreLineaCredito,

               'Pagaré':
                 credito.pagareCartera,

               'Documento':
                 credito.documento,

               'Nombre Completo':
                 credito.nombreCompleto,

               'ID Modelo PE':
                 credito.idModeloPe,

               'Modelo PE':
                 credito.nombreModeloPe

             };


           for (
             const periodo of periodos
           ) {

             const fecha =
               periodo.fecha
                 ?.substring(
                   0,
                   7
                 )
               ?? '';

             const nombreColumna =
               `Mora ${fecha}`;

             fila[nombreColumna] =
               credito.moras.get(
                 periodo.periodo
               )
               ?? 0;
           }


           return fila;
         }
       );


   // =======================================================
   // EXCEL
   // =======================================================

   const ws =
     XLSX.utils.json_to_sheet(
       filas
     );


   const cantidadColumnas =
     13
     + periodos.length;


   this.ajustarHoja(
     ws,
     filas.length,
     cantidadColumnas
   );


   // =======================================================
   // FORMATO COLUMNAS DE MORA
   // =======================================================

   const columnasMora =
     periodos.map(
       periodo => {

         const fecha =
           periodo.fecha
             ?.substring(
               0,
               7
             )
           ?? '';

         return `Mora ${fecha}`;
       }
     );


   this.formatearColumnasPorNombre(
     ws,
     columnasMora,
     '#,##0'
   );


   // =======================================================
   // ANCHOS
   // =======================================================

   if (ws['!cols']) {

     ws['!cols'][6] = {
       wch: 14
     };

     ws['!cols'][7] = {
       wch: 28
     };

     ws['!cols'][8] = {
       wch: 16
     };

     ws['!cols'][9] = {
       wch: 18
     };

     ws['!cols'][10] = {
       wch: 34
     };


     for (
       let i = 13;
       i < cantidadColumnas;
       i++
     ) {

       ws['!cols'][i] = {
         wch: 13
       };
     }
   }


   XLSX.utils.book_append_sheet(
     wb,
     ws,
     'MORA'
   );
 }


  // =========================================================
  // AJUSTAR HOJA
  // =========================================================

  private ajustarHoja(
    ws: XLSX.WorkSheet,
    cantidadFilas: number,
    cantidadColumnas: number
  ): void {

    const anchoColumnas:
      XLSX.ColInfo[] = [];

    for (
      let i = 0;
      i < cantidadColumnas;
      i++
    ) {

      anchoColumnas.push({
        wch: 16
      });

    }


    ws['!cols'] =
      anchoColumnas;


    if (cantidadFilas > 0) {

      const ultimaColumna =
        XLSX.utils.encode_col(
          Math.max(
            cantidadColumnas - 1,
            0
          )
        );

      ws['!autofilter'] = {
        ref:
          `A1:${ultimaColumna}${cantidadFilas + 1}`
      };

    }

  }


  // =========================================================
  // FORMATEAR COLUMNAS POR NOMBRE
  // =========================================================

  private formatearColumnasPorNombre(
    ws: XLSX.WorkSheet,
    nombresColumnas: string[],
    formato: string
  ): void {

    if (!ws['!ref']) {
      return;
    }


    const rango =
      XLSX.utils.decode_range(
        ws['!ref']
      );


    for (
      let columna = rango.s.c;
      columna <= rango.e.c;
      columna++
    ) {

      const cabeceraDireccion =
        XLSX.utils.encode_cell({
          r: 0,
          c: columna
        });

      const cabecera =
        ws[cabeceraDireccion];


      if (!cabecera) {
        continue;
      }


      const nombre =
        String(
          cabecera.v ?? ''
        ).trim();


      if (
        !nombresColumnas.includes(
          nombre
        )
      ) {
        continue;
      }


      this.formatearColumnaNumerica(
        ws,
        columna,
        1,
        rango.e.r,
        formato
      );

    }

  }


  // =========================================================
  // FORMATEAR COLUMNA NUMÉRICA
  // =========================================================

  private formatearColumnaNumerica(
    ws: XLSX.WorkSheet,
    columna: number,
    filaInicio: number,
    filaFin: number,
    formato: string
  ): void {

    for (
      let fila = filaInicio;
      fila <= filaFin;
      fila++
    ) {

      const direccion =
        XLSX.utils.encode_cell({
          r: fila,
          c: columna
        });

      const celda =
        ws[direccion];


      if (
        !celda
        || celda.t !== 'n'
      ) {
        continue;
      }


      celda.z =
        formato;

    }

  }


  // =========================================================
  // NOMBRE DEL MODELO
  // =========================================================

  private obtenerNombreModelo(
    datos: DatosExportacionAnexo2
  ): string {

    const desdeResumen =
      datos.resumen
        ?.find(
          x =>
            !!x.nombreModeloPe
        )
        ?.nombreModeloPe;


    if (desdeResumen) {
      return desdeResumen;
    }


    const desdeDetalle =
      datos.detalle
        ?.find(
          x =>
            !!x.nombreModeloPe
        )
        ?.nombreModeloPe;


    if (desdeDetalle) {
      return desdeDetalle;
    }


    const desdeTrabajo =
      datos.trabajo
        ?.find(
          x =>
            !!x.nombreModeloPe
        )
        ?.nombreModeloPe;


    if (desdeTrabajo) {
      return desdeTrabajo;
    }


    return `Modelo ${datos.idModeloPe}`;

  }


  // =========================================================
  // PERÍODO AAAAMM
  // =========================================================

  private periodo(
    fechaCorte: string
  ): string {

    if (!fechaCorte) {
      return '';
    }


    const valor =
      fechaCorte
        .trim()
        .substring(
          0,
          10
        );


    const partes =
      valor.split('-');


    if (
      partes.length < 2
    ) {
      return valor.replace(
        /[^0-9]/g,
        ''
      );
    }


    return `${partes[0]}${partes[1]}`;

  }


  // =========================================================
  // SI / NO
  // =========================================================

  private siNo(
    valor: boolean | null | undefined
  ): string {

    return valor
      ? 'SI'
      : 'NO';

  }


  // =========================================================
  // SUMAR
  // =========================================================

  private sumar<T>(
    filas: T[],
    obtenerValor: (fila: T) => number | null | undefined
  ): number {

    return (
      filas ?? []
    ).reduce(
      (
        total,
        fila
      ) =>
        total
        +
        Number(
          obtenerValor(
            fila
          )
          ?? 0
        ),
      0
    );

  }

}
