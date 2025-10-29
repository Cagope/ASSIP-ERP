import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { DatosPersonales } from './datos-personales.api';
import { CatalogosApi, CodigoNombreDTO, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';

/**
 * 📦 Servicio de exportación a Excel — Listado de Datos Personales
 * ------------------------------------------------------------
 * Exporta todos los campos, decodificando país, departamento,
 * ciudad, género, estado civil, escolaridad, vivienda, ocupación,
 * sector económico y demás códigos.
 */
@Injectable({ providedIn: 'root' })
export class DatosPersonalesExporterService {
  constructor(private catalogos: CatalogosApi) {}

  exportarExcel(personas: DatosPersonales[]): void {
    if (!personas || personas.length === 0) {
      alert('⚠️ No hay registros de datos personales para exportar.');
      return;
    }

    const idsDepartamentos = [
      ...new Set(personas.map(p => p.idDepartamentoNacimiento).filter((id): id is number => !!id))
    ];

    const observablesCiudades =
      idsDepartamentos.length > 0
        ? idsDepartamentos.map(id =>
            this.catalogos
              .listarCiudadesPorDepartamento(Number(id))
              .pipe(catchError(() => of([] as Ciudad[])))
          )
        : [of([] as Ciudad[])];

    forkJoin({
      paises: this.catalogos.listarPaises(),
      departamentos: this.catalogos.listarDepartamentos(),
      ciudadesPorDepto: forkJoin(observablesCiudades),
      generos: this.catalogos.listarGeneros(),
      estadosCiviles: this.catalogos.listarEstadosCiviles(),
      escolaridades: this.catalogos.listarNivelesEscolares(),   // ✅ corregido
      tiposVivienda: this.catalogos.listarTiposViviendas(),     // ✅ correcto
      ocupaciones: this.catalogos.listarOcupaciones(),          // ✅ correcto
      sectoresEconomicos: this.catalogos.listarSectoresEconomicos() // ✅ correcto
    }).subscribe({
      next: (cat) => {
        const todasCiudades = (cat.ciudadesPorDepto ?? []).flat();

        // 🗺️ Mapas de referencia
        const mapPaises = new Map<string | number, string>(
          (cat.paises ?? []).map((p: CodigoNombreDTO) => [p.codigo, p.nombre])
        );
        const mapDeptos = new Map<number, string>(
          (cat.departamentos ?? []).map((d: Departamento) => [d.idDepartamento, d.nombreDepartamento])
        );
        const mapCiudades = new Map<number, string>(
          todasCiudades.map((c: Ciudad) => [c.idCiudad, c.nombreCiudad])
        );
        const mapGeneros = new Map<string | number, string>(
          (cat.generos ?? []).map((g: CodigoNombreDTO) => [g.codigo, g.nombre])
        );
        const mapEstados = new Map<string | number, string>(
          (cat.estadosCiviles ?? []).map((e: CodigoNombreDTO) => [e.codigo, e.nombre])
        );
        const mapEscolaridad = new Map<string | number, string>(
          (cat.escolaridades ?? []).map((e: CodigoNombreDTO) => [e.codigo, e.nombre])
        );
        const mapTipoVivienda = new Map<string | number, string>(
          (cat.tiposVivienda ?? []).map((v: CodigoNombreDTO) => [v.codigo, v.nombre])
        );
        const mapOcupacion = new Map<string | number, string>(
          (cat.ocupaciones ?? []).map((o: CodigoNombreDTO) => [o.codigo, o.nombre])
        );
        const mapSectorEco = new Map<string | number, string>(
          (cat.sectoresEconomicos ?? []).map((s: CodigoNombreDTO) => [s.codigo, s.nombre])
        );

        // 🧩 Decodificar todos los campos
        const personasDecod = personas.map(p => ({
          'ID': p.idDatosPersonal ?? '',
          'Tipo Documento': p.tipoDocumento ?? '',
          'Documento': p.documento ?? '',
          'Tipo Persona': p.tipoPersona === '1' ? 'Natural' : 'Jurídica',
          'Tiene RUT': p.tieneRut ? 'Sí' : 'No',
          'Dígito Verificación': p.digitoVerificacion ?? '',
          'Fecha Documento': p.fechaDocumento ?? '',
          'País Expedición': mapPaises.get(p.idPaisDocumento ?? '') ?? '',
          'Departamento Expedición': mapDeptos.get(p.idDepartamentoExpedicion ?? 0) ?? '',
          'Ciudad Expedición': mapCiudades.get(p.idCiudadExpedicion ?? 0) ?? '',
          'Nombres': p.nombres ?? '',
          'Primer Apellido': p.primerApellido ?? '',
          'Segundo Apellido': p.segundoApellido ?? '',
          'Fecha Nacimiento': p.fechaNacimiento ?? '',
          'País Nacimiento': mapPaises.get(p.idPaisNacimiento ?? '') ?? '',
          'Departamento Nacimiento': mapDeptos.get(p.idDepartamentoNacimiento ?? 0) ?? '',
          'Ciudad Nacimiento': mapCiudades.get(p.idCiudadNacimiento ?? 0) ?? '',
          'Género': mapGeneros.get(p.codigoGenero ?? '') ?? '',
          'Estado Civil': mapEstados.get(p.codigoEstadoCivil ?? '') ?? '',
          'Escolaridad': mapEscolaridad.get(p.codigoEscolaridad ?? '') ?? '',
          'Cabeza Familia': (String(p.cabezaFamilia).trim() === '1') ? 'Sí' : 'No',
          'Estrato Social': p.estratoSocial ?? '',
          'Tipo Vivienda': mapTipoVivienda.get(p.codigoTipoVivienda ?? '') ?? '',
          'Número Hijos': p.numeroHijos ?? '',
          'Ocupación': mapOcupacion.get(p.codigoOcupacion ?? '') ?? '',
          'Sector Económico': mapSectorEco.get(p.codigoSectorEconomico ?? '') ?? '',
          'Actividad SES': p.codigoActividadSes ?? '',
          'Actividad DIAN': p.codigoActividadDian ?? '',
          'Comentario': p.comentario ?? '',
          'Fecha Creación': p.fechaCreacion ? new Date(p.fechaCreacion).toLocaleString() : '',
          'Fecha Edición': p.fechaEdicion ? new Date(p.fechaEdicion).toLocaleString() : ''
        }));

        // 📊 Generar hoja y libro Excel
        const ws = XLSX.utils.json_to_sheet(personasDecod);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'Datos Personales');

        // 📏 Ajuste de anchos de columna
        (ws as any)['!cols'] = Array(30).fill({ wch: 22 });

        // 🗓️ Nombre del archivo
        const fecha = new Date();
        const sufijo = `${fecha.getFullYear()}${String(fecha.getMonth() + 1).padStart(2, '0')}${String(fecha.getDate()).padStart(2, '0')}`;
        XLSX.writeFile(wb, `datos_personales_${sufijo}.xlsx`);
      },
      error: (err) => {
        console.error('Error exportando datos personales:', err);
        alert('No se pudieron cargar los catálogos de referencia.');
      }
    });
  }
}
