import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class AsociadosExporterService {

  // 🔧 Formateador NIT para jurídicas
  private formatearNIT(num: string, dv: string): string {
    if (!num) return '';
    const n = num.padStart(9, '0');
    return `${n.substring(0,3)}-${n.substring(3,6)}-${n.substring(6,9)}-${dv ?? ''}`;
  }

  exportar(lista: any[], estadistica: any) {
    if (!lista || lista.length === 0) return;

    // ORDEN EXIGIDO POR LA SES
    const columnas = [
      'TipoIdentificacion',
      'NumeroIdentificacion',
      'PrimerApellido',
      'SegundoApellido',
      'Nombres',
      'FechaIngreso',
      'Telefono',
      'Direccion',
      'Asociado',
      'Activo',
      'ActividadEconomica',
      'CodigoMunicipio',
      'Email',
      'Genero',
      'Empleado',
      'TipoContrato',
      'NivelEscolaridad',
      'Estrato',
      'NivelIngresos',
      'FechaNacimiento',
      'EstadoCivil',
      'MujerCabezaFamilia',
      'Ocupacion',
      'SectorEconomico',
      'JornadaLaboral',
      'FechaRetiro',
      'AsistioUltAsamblea',
      'Celular'
    ];

    // --------------------------------------------
    // 📝 HOJA 1 — Detalle SES
    // --------------------------------------------

    const datos = lista.map(item => {

      const esJuridica = item.tipoIdentificacion === 'N';

      return {
        TipoIdentificacion: item.tipoIdentificacion,

        // 📌 Número identificación con formato SOLO jurídicas
        NumeroIdentificacion: esJuridica
          ? this.formatearNIT(item.numeroIdentificacion, item.digitoVerificacion)
          : item.numeroIdentificacion,

        PrimerApellido: item.primerApellido,
        SegundoApellido: item.segundoApellido,
        Nombres: item.nombres,

        FechaIngreso: item.fechaIngreso,
        Telefono: item.telefono,
        Direccion: item.direccion,

        Asociado: item.rolAsociado,
        Activo: item.activo,
        ActividadEconomica: item.actividadEconomica,
        CodigoMunicipio: item.codigoMunicipio,
        Email: item.email,
        Genero: String(item.genero),

        // 📌 Jurídica → Empleado = 0
        Empleado: esJuridica ? 0 : item.empleado,

        TipoContrato: item.tipoContrato,
        NivelEscolaridad: item.nivelEscolaridad,
        Estrato: item.estrato,

        NivelIngresos: item.nivelIngresos,  // ✔ NO TOCAR

        FechaNacimiento: item.fechaNacimiento, // ✔ NO TOCAR

        // 📌 Jurídica → EstadoCivil = 0
        EstadoCivil: esJuridica ? 0 : item.estadoCivil,

        // 📌 Jurídica → MujerCabezaFamilia = 0
        MujerCabezaFamilia: esJuridica ? 0 : item.mujerCabezaFamilia,

        Ocupacion: item.ocupacion,
        SectorEconomico: item.sectorEconomico,

        // 📌 Jurídica → JornadaLaboral = 0
        JornadaLaboral: esJuridica ? 0 : item.jornadaLaboral,

        FechaRetiro: item.fechaRetiro ?? '',

        AsistioUltAsamblea: item.asistioAsamblea,
        Celular: item.celular
      };
    });

    const ws1 = XLSX.utils.json_to_sheet(datos, { header: columnas });

    // --------------------------------------------
    // 📝 HOJA 2 — Estadística por Género
    // --------------------------------------------

    const estad = estadistica ?? {
      masculino: 0, femenino: 0, juridica: 0, otro: 0, total: 0
    };

    const ws2Data = [
      ['Categoría', 'Cantidad', 'Porcentaje'],
      ['Masculino', estad.masculino, (estad.masculino / estad.total || 0)],
      ['Femenino', estad.femenino, (estad.femenino / estad.total || 0)],
      ['Persona Jurídica', estad.juridica, (estad.juridica / estad.total || 0)],
      ['Otro / Sin clasificar', estad.otro, (estad.otro / estad.total || 0)],
      ['TOTAL', estad.total, 1]
    ];

    const ws2 = XLSX.utils.aoa_to_sheet(ws2Data);

    // --------------------------------------------
    // 📘 LIBRO FINAL
    // --------------------------------------------

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws1, 'Asociados SES');
    XLSX.utils.book_append_sheet(wb, ws2, 'Estadísticas');

    XLSX.writeFile(wb, 'asociados_ses.xlsx');
  }
}
