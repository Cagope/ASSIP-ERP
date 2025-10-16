import { inject, Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { firstValueFrom } from 'rxjs';

import {
  DatosPersonalesApi,
  DatosPersonalesListItem,
} from '../features/hoja-vida/datos-personales/datos-personales.api';

import {
  CatalogosApi,
  CodigoNombreDTO,
  IdNombreDTO,
  TipoRegimenDTO,
} from '../shared/catalogos/catalogos.api';

type IdNameMap = Map<number, string>;
type CodeNameMap = Map<string, string>;

@Injectable({ providedIn: 'root' })
export class DatosPersonalesExporter {
  private api = inject(DatosPersonalesApi);
  private cat = inject(CatalogosApi);

  async exportXlsx(params?: { q?: string }) {
    // 1) Recoger IDs paginando
    const ids: number[] = [];
    const size = 200;
    let page = 0;
    while (true) {
      const res = await firstValueFrom(
        this.api.list({ q: params?.q, page, size })
      );
      (res.content ?? []).forEach((it: DatosPersonalesListItem) =>
        ids.push(it.idDatosPersonal)
      );
      if (res.last || page + 1 >= res.totalPages) break;
      page++;
    }

    if (!ids.length) {
      alert('No hay registros para exportar.');
      return;
    }

    // 2) Obtener detalles en lotes
    const details = await this.inChunks<number, any>(ids, 25, async (chunk) => {
      const promises = chunk.map((id) => firstValueFrom(this.api.get(id)));
      return Promise.all(promises);
    });

    // 3) Catálogos base (códigos / país)
    const baseCats = await this.loadBaseCatalogMaps();

    // 4) Mapas de Departamento y Ciudad (descubrimiento dinámico de métodos)
    const deptoMap = await this.buildDeptoMapFromDetails(details);
    const ciudadMap = await this.buildCiudadMapFromDetails(details);

    // 5) Filas decodificadas (ahora con nombres de depto/ciudad)
    const rows = details.map((d) => ({
      idDatosPersonal: d.idDatosPersonal ?? '',
      tipoDocumento: d.tipoDocumento ?? '',
      documento: d.documento ?? '',
      tieneRut: d.tieneRut ? 'SÍ' : 'NO',
      digitoVerificacion: d.digitoVerificacion ?? '',

      nombres: d.nombres ?? '',
      primerApellido: d.primerApellido ?? '',
      segundoApellido: d.segundoApellido ?? '',

      fechaNacimiento: d.fechaNacimiento ?? '',
      fechaDocumento: d.fechaDocumento ?? '',
      fechaApertura: (d as any).fechaApertura ?? '',

      idPaisDocumento: d.idPaisDocumento ?? '',
      paisDocumentoNombre: this.decId(baseCats.paisMap, d.idPaisDocumento),

      idDepartamentoExpedicion: d.idDepartamentoExpedicion ?? '',
      departamentoExpedicionNombre: this.decId(
        deptoMap,
        d.idDepartamentoExpedicion
      ),
      idCiudadExpedicion: d.idCiudadExpedicion ?? '',
      ciudadExpedicionNombre: this.decId(ciudadMap, d.idCiudadExpedicion),

      idPaisNacimiento: d.idPaisNacimiento ?? '',
      paisNacimientoNombre: this.decId(baseCats.paisMap, d.idPaisNacimiento),

      idDepartamentoNacimiento: d.idDepartamentoNacimiento ?? '',
      departamentoNacimientoNombre: this.decId(
        deptoMap,
        d.idDepartamentoNacimiento
      ),
      idCiudadNacimiento: d.idCiudadNacimiento ?? '',
      ciudadNacimientoNombre: this.decId(ciudadMap, d.idCiudadNacimiento),

      comentario: d.comentario ?? '',

      codigoGenero: d.codigoGenero ?? '',
      generoNombre: this.decCode(baseCats.generoMap, d.codigoGenero),

      codigoEstadoCivil: d.codigoEstadoCivil ?? '',
      estadoCivilNombre: this.decCode(baseCats.estadoMap, d.codigoEstadoCivil),

      codigoEscolaridad: d.codigoEscolaridad ?? '',
      escolaridadNombre: this.decCode(
        baseCats.escolaridadMap,
        d.codigoEscolaridad
      ),

      codigoTipoVivienda: d.codigoTipoVivienda ?? '',
      tipoViviendaNombre: this.decCode(
        baseCats.tipoViviendaMap,
        d.codigoTipoVivienda
      ),

      estratoSocial: d.estratoSocial ?? '',
      numeroHijos: d.numeroHijos ?? '',

      codigoOcupacion: d.codigoOcupacion ?? '',
      ocupacionNombre: this.decCode(baseCats.ocupacionMap, d.codigoOcupacion),

      codigoSectorEconomico: d.codigoSectorEconomico ?? '',
      sectorEconomicoNombre: this.decCode(
        baseCats.sectorMap,
        d.codigoSectorEconomico
      ),

      codigoActividadSes: d.codigoActividadSes ?? '',
      codigoActividadDian: d.codigoActividadDian ?? '',

      codigoRetencion: d.codigoRetencion ?? '',
      regimenNombre: this.decCode(baseCats.regimenMap, d.codigoRetencion),
    }));

    // 6) XLSX
    const headers = [
      'idDatosPersonal',
      'tipoDocumento',
      'documento',
      'tieneRut',
      'digitoVerificacion',
      'nombres',
      'primerApellido',
      'segundoApellido',
      'fechaNacimiento',
      'fechaDocumento',
      'fechaApertura',

      'idPaisDocumento',
      'paisDocumentoNombre',

      'idDepartamentoExpedicion',
      'departamentoExpedicionNombre',
      'idCiudadExpedicion',
      'ciudadExpedicionNombre',

      'idPaisNacimiento',
      'paisNacimientoNombre',

      'idDepartamentoNacimiento',
      'departamentoNacimientoNombre',
      'idCiudadNacimiento',
      'ciudadNacimientoNombre',

      'comentario',

      'codigoGenero',
      'generoNombre',
      'codigoEstadoCivil',
      'estadoCivilNombre',
      'codigoEscolaridad',
      'escolaridadNombre',
      'codigoTipoVivienda',
      'tipoViviendaNombre',
      'estratoSocial',
      'numeroHijos',
      'codigoOcupacion',
      'ocupacionNombre',
      'codigoSectorEconomico',
      'sectorEconomicoNombre',
      'codigoActividadSes',
      'codigoActividadDian',
      'codigoRetencion',
      'regimenNombre',
    ];

    const ws = XLSX.utils.json_to_sheet(rows, { header: headers });
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'DatosPersonales');

    // @ts-ignore ancho columnas
    ws['!cols'] = headers.map((h) => ({ wch: Math.min(Math.max(h.length, 16), 30) }));

    const fecha = new Date().toISOString().slice(0, 10);
    XLSX.writeFile(wb, `datos_personales_${fecha}.xlsx`);
  }

  // ==== helpers ====
  private async inChunks<T, R>(
    items: T[],
    size: number,
    fn: (chunk: T[]) => Promise<R[]>
  ): Promise<R[]> {
    const out: R[] = [];
    for (let i = 0; i < items.length; i += size) {
      const part = items.slice(i, i + size);
      const res = await fn(part);
      out.push(...res);
    }
    return out;
  }

  private decCode(map: CodeNameMap, v?: string | null): string {
    return v ? map.get(v) ?? v : '';
  }
  private decId(map: IdNameMap, v?: number | null): string {
    return (v ?? null) !== null ? map.get(v!) ?? String(v) : '';
  }

  private async loadBaseCatalogMaps(): Promise<{
    generoMap: CodeNameMap;
    estadoMap: CodeNameMap;
    escolaridadMap: CodeNameMap;
    tipoViviendaMap: CodeNameMap;
    ocupacionMap: CodeNameMap;
    sectorMap: CodeNameMap;
    regimenMap: CodeNameMap;
    paisMap: IdNameMap;
  }> {
    const [
      generos,
      estados,
      escolaridades,
      tiposVivienda,
      ocupaciones,
      sectores,
      tiposRegimen,
      paises,
    ] = await Promise.all([
      firstValueFrom(this.cat.generos()),
      firstValueFrom(this.cat.estadosCiviles()),
      firstValueFrom(this.cat.nivelesEscolares()),
      firstValueFrom(this.cat.tiposVivienda()),
      firstValueFrom(this.cat.ocupaciones()),
      firstValueFrom(this.cat.sectoresEconomicos()),
      firstValueFrom(this.cat.tiposRegimen()),
      firstValueFrom(this.cat.paises()),
    ]);

    const toMapCode = (arr: (CodigoNombreDTO | TipoRegimenDTO)[] = []) =>
      new Map(arr.map((a) => [a.codigo, a.nombre]));
    const toMapId = (arr: IdNombreDTO[] = []) =>
      new Map(arr.map((a) => [a.id, a.nombre]));

    return {
      generoMap: toMapCode(generos as any[]),
      estadoMap: toMapCode(estados as any[]),
      escolaridadMap: toMapCode(escolaridades as any[]),
      tipoViviendaMap: toMapCode(tiposVivienda as any[]),
      ocupacionMap: toMapCode(ocupaciones as any[]),
      sectorMap: toMapCode(sectores as any[]),
      regimenMap: toMapCode(tiposRegimen as any[]),
      paisMap: toMapId(paises as any[]),
    };
  }

  private async buildDeptoMapFromDetails(details: any[]): Promise<IdNameMap> {
    const paisIds = new Set<number>();
    for (const d of details) {
      if (d?.idPaisDocumento != null) paisIds.add(d.idPaisDocumento);
      if (d?.idPaisNacimiento != null) paisIds.add(d.idPaisNacimiento);
    }
    if (!paisIds.size) return new Map();

    const catAny = this.cat as any;
    const deptoMethodName =
      ['departamentosPorPais', 'departamentosByPais', 'departamentosDePais', 'departamentosPorPaís', 'departamentos']
        .find(name => typeof catAny?.[name] === 'function');

    if (!deptoMethodName) return new Map();

    const out = new Map<number, string>();
    for (const paisId of paisIds) {
      try {
        const obs = catAny[deptoMethodName](paisId) as unknown;
        const listUnknown = await firstValueFrom(obs as any);
        const list = Array.isArray(listUnknown) ? listUnknown as IdNombreDTO[] : [];
        list.forEach((e: IdNombreDTO) => out.set(e.id, e.nombre));
      } catch {
        // ignorar fallas individuales
      }
    }
    return out;
  }

  private async buildCiudadMapFromDetails(details: any[]): Promise<IdNameMap> {
    const deptoIds = new Set<number>();
    for (const d of details) {
      if (d?.idDepartamentoExpedicion != null) deptoIds.add(d.idDepartamentoExpedicion);
      if (d?.idDepartamentoNacimiento != null) deptoIds.add(d.idDepartamentoNacimiento);
    }
    if (!deptoIds.size) return new Map();

    const catAny = this.cat as any;
    const ciudadMethodName =
      ['ciudadesPorDepartamento', 'ciudadesByDepartamento', 'ciudadesDeDepartamento', 'ciudadesPorDepto', 'ciudades']
        .find(name => typeof catAny?.[name] === 'function');

    if (!ciudadMethodName) return new Map();

    const out = new Map<number, string>();
    for (const deptoId of deptoIds) {
      try {
        const obs = catAny[ciudadMethodName](deptoId) as unknown;
        const listUnknown = await firstValueFrom(obs as any);
        const list = Array.isArray(listUnknown) ? listUnknown as IdNombreDTO[] : [];
        list.forEach((e: IdNombreDTO) => out.set(e.id, e.nombre));
      } catch {
        // ignorar fallas individuales
      }
    }
    return out;
  }
}
