import { Injectable } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  DatosPersonales
} from './datos-personales.api';

import {
  CatalogosApi,
  CodigoNombreDTO,
  Departamento,
  Ciudad
} from '../../../shared/catalogos/catalogos.api';

@Injectable({
  providedIn: 'root'
})
export class DatosPersonalesExporterService {

  constructor(
    private catalogos: CatalogosApi,
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    personas: DatosPersonales[]
  ): void {

    if (!personas || personas.length === 0) {
      alert('No hay registros de datos personales para exportar.');
      return;
    }

    const idsDepartamentos = [
      ...new Set(
        personas
          .map(p => p.idDepartamentoNacimiento)
          .filter((id): id is number => !!id)
      )
    ];

    const observablesCiudades =
      idsDepartamentos.length > 0
        ? idsDepartamentos.map(id =>
          this.catalogos
            .listarCiudadesPorDepartamento(Number(id))
            .pipe(
              catchError(() => of([] as Ciudad[]))
            )
        )
        : [of([] as Ciudad[])];

    forkJoin({

      paises:
        this.catalogos.listarPaises(),

      departamentos:
        this.catalogos.listarDepartamentos(),

      ciudadesPorDepto:
        forkJoin(observablesCiudades),

      generos:
        this.catalogos.listarGeneros(),

      estadosCiviles:
        this.catalogos.listarEstadosCiviles(),

      escolaridades:
        this.catalogos.listarNivelesEscolares(),

      tiposVivienda:
        this.catalogos.listarTiposViviendas(),

      ocupaciones:
        this.catalogos.listarOcupaciones(),

      sectoresEconomicos:
        this.catalogos.listarSectoresEconomicos()

    }).subscribe({

      next: cat => {

        const todasCiudades =
          (cat.ciudadesPorDepto || []).flat();

        const mapPaises =
          new Map<string | number, string>(
            (cat.paises || []).map((p: CodigoNombreDTO) => [
              p.codigo,
              p.nombre
            ])
          );

        const mapDeptos =
          new Map<number, string>(
            (cat.departamentos || []).map((d: Departamento) => [
              d.idDepartamento,
              d.nombreDepartamento
            ])
          );

        const mapCiudades =
          new Map<number, string>(
            todasCiudades.map((c: Ciudad) => [
              c.idCiudad,
              c.nombreCiudad
            ])
          );

        const mapGeneros =
          new Map<string | number, string>(
            (cat.generos || []).map((g: CodigoNombreDTO) => [
              g.codigo,
              g.nombre
            ])
          );

        const mapEstados =
          new Map<string | number, string>(
            (cat.estadosCiviles || []).map((e: CodigoNombreDTO) => [
              e.codigo,
              e.nombre
            ])
          );

        const mapEscolaridad =
          new Map<string | number, string>(
            (cat.escolaridades || []).map((e: CodigoNombreDTO) => [
              e.codigo,
              e.nombre
            ])
          );

        const mapTipoVivienda =
          new Map<string | number, string>(
            (cat.tiposVivienda || []).map((v: CodigoNombreDTO) => [
              v.codigo,
              v.nombre
            ])
          );

        const mapOcupacion =
          new Map<string | number, string>(
            (cat.ocupaciones || []).map((o: CodigoNombreDTO) => [
              o.codigo,
              o.nombre
            ])
          );

        const mapSectorEco =
          new Map<string | number, string>(
            (cat.sectoresEconomicos || []).map((s: CodigoNombreDTO) => [
              s.codigo,
              s.nombre
            ])
          );

        this.excelExport.exportar({

          nombreArchivo:
            `datos_personales_${this.fechaArchivo()}.xlsx`,

          hojas: [

            {
              nombreHoja:
                'Datos Personales',

              titulo:
                'DATOS PERSONALES',

              columnas: [
                'ID',
                'Tipo Documento',
                'Documento',
                'Tipo Persona',
                'Tiene RUT',
                'Dígito Verificación',
                'Fecha Documento',
                'País Expedición',
                'Departamento Expedición',
                'Ciudad Expedición',
                'Nombres',
                'Primer Apellido',
                'Segundo Apellido',
                'Fecha Nacimiento',
                'País Nacimiento',
                'Departamento Nacimiento',
                'Ciudad Nacimiento',
                'Género',
                'Estado Civil',
                'Escolaridad',
                'Cabeza Familia',
                'Estrato Social',
                'Tipo Vivienda',
                'Número Hijos',
                'Ocupación',
                'Sector Económico',
                'Actividad SES',
                'Actividad DIAN',
                'Comentario',
                'Fecha Creación',
                'Fecha Edición'
              ],

              filas: personas.map(p => [

                p.idDatosPersonal ?? '',

                p.tipoDocumento ?? '',

                p.documento ?? '',

                p.tipoPersona === '1'
                  ? 'Natural'
                  : 'Jurídica',

                p.tieneRut
                  ? 'Sí'
                  : 'No',

                p.digitoVerificacion ?? '',

                p.fechaDocumento ?? '',

                mapPaises.get(p.idPaisDocumento ?? '') ?? '',

                mapDeptos.get(p.idDepartamentoExpedicion ?? 0) ?? '',

                mapCiudades.get(p.idCiudadExpedicion ?? 0) ?? '',

                p.nombres ?? '',

                p.primerApellido ?? '',

                p.segundoApellido ?? '',

                p.fechaNacimiento ?? '',

                mapPaises.get(p.idPaisNacimiento ?? '') ?? '',

                mapDeptos.get(p.idDepartamentoNacimiento ?? 0) ?? '',

                mapCiudades.get(p.idCiudadNacimiento ?? 0) ?? '',

                mapGeneros.get(p.codigoGenero ?? '') ?? '',

                mapEstados.get(p.codigoEstadoCivil ?? '') ?? '',

                mapEscolaridad.get(p.codigoEscolaridad ?? '') ?? '',

                String(p.cabezaFamilia).trim() === '1'
                  ? 'Sí'
                  : 'No',

                p.estratoSocial ?? '',

                mapTipoVivienda.get(p.codigoTipoVivienda ?? '') ?? '',

                p.numeroHijos ?? '',

                mapOcupacion.get(p.codigoOcupacion ?? '') ?? '',

                mapSectorEco.get(p.codigoSectorEconomico ?? '') ?? '',

                p.codigoActividadSes ?? '',

                p.codigoActividadDian ?? '',

                p.comentario ?? '',

                p.fechaCreacion
                  ? new Date(p.fechaCreacion).toLocaleString()
                  : '',

                p.fechaEdicion
                  ? new Date(p.fechaEdicion).toLocaleString()
                  : ''

              ]),

              anchos: [
                10,
                18,
                18,
                16,
                12,
                16,
                18,
                24,
                24,
                24,
                30,
                24,
                24,
                18,
                24,
                24,
                24,
                18,
                20,
                24,
                16,
                16,
                20,
                14,
                24,
                24,
                18,
                18,
                42,
                22,
                22
              ]
            }

          ]

        });

      },

      error: err => {
        console.error('Error exportando datos personales:', err);
        alert('No se pudieron cargar los catálogos de referencia.');
      }

    });

  }

  private fechaArchivo(): string {

    const fecha =
      new Date();

    return `${fecha.getFullYear()}${String(fecha.getMonth() + 1).padStart(2, '0')}${String(fecha.getDate()).padStart(2, '0')}`;
  }

}
