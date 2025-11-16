import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { DemograficosExporterService } from './demograficos-exporter.service';

interface DemograficoDTO {
  tipo_documento: string;
  documento: string;
  nombres: string;
  primer_apellido: string;
  segundo_apellido: string;
  fecha_apertura: string;
  direccion: string;
  barrio: string;
  telefono: string;
  celular_uno: string;
  celular_dos: string;
  email: string;
  municipio: string;
  nombre_genero: string;
  nombre_escolaridad: string;
  estrato_social: number;
  nivel_ingresos: number;
  fecha_nacimiento: string;
  nombre_estado_civil: string;
  nombre_conyuge: string | null;
  cedula_conyuge: string | null;
  cabeza_familia: string;
  nombre_ocupacion: string;
  nombre_sector_economico: string;
  numero_hijos: number;
  valor_salario: number;
  otros_ingresos: number;
  egresos: number;
  total_activos: number;
  total_pasivos: number;
  nombre_tipo_vivienda: string;
  nombre_arrendador: string | null;
  telefono_arrendador: string | null;
  ubicacion_vivienda: string;
  celular: string;
  fecha_documento: string;
  peps: boolean;
  fecha_peps: string | null;
  comentario_peps: string | null;
  familia_peps: boolean | null;
  cedula_familia_peps: string | null;
  nombre_familia_peps: string | null;
  recibe_llamadas: boolean;
  recibe_msm: boolean;
  recibe_redes_sociales: boolean;
  recibe_emails: boolean;
  recibe_cartas: boolean;
  nombre_zona: string;
  nombre_sub_zona: string;
  edad: number;
  antiguedad: number;
  agencia: string;
  saldo_aportes: number;
}

@Component({
  standalone: true,
  selector: 'app-demograficos-list',
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './demograficos-list.component.html',
  styleUrls: ['./demograficos-list.component.scss']
})
export class DemograficosListComponent {

  private readonly http = inject(HttpClient);
  private readonly exporter = inject(DemograficosExporterService);

  fecha = '';
  cargando = false;
  error = '';
  registros: DemograficoDTO[] = [];

  /** ============================
   *  ESTADÍSTICAS
   * ============================ */
  stats = {
    total: 0,

    genero: {} as Record<string, number>,
    aportexGenero: {} as Record<string, number>,

    rangosEdad: {
      '0-12': 0,
      '13-17': 0,
      '18-25': 0,
      '26-40': 0,
      '41-60': 0,
      '61+': 0
    },
    aportexEdad: {} as Record<string, number>,

    tipoVivienda: {} as Record<string, number>,
    aportexVivienda: {} as Record<string, number>,

    zonas: {} as Record<string, number>,
    aportexZonas: {} as Record<string, number>,

    subzonas: {} as Record<string, number>,
    aportexSubzonas: {} as Record<string, number>,

    escolaridad: {} as Record<string, number>,
    aportexEscolaridad: {} as Record<string, number>,

    ocupacion: {} as Record<string, number>,
    aportexOcupacion: {} as Record<string, number>,

    municipio: {} as Record<string, number>,
    aportexMunicipio: {} as Record<string, number>,

    resumen: {
      totalAportes: 0,
      promedioAportes: 0
    }
  };

  buscar() {
    this.error = '';

    if (!this.fecha) {
      this.error = 'Debe seleccionar una fecha de corte.';
      return;
    }

    this.cargando = true;

    const body = {
      tipo: 'DATOS_DEMOGRAFICOS',
      filtros: { fecha: this.fecha }
    };

    this.http.post<DemograficoDTO[]>(
      `${environment.apiUrl}/sarlaft/informes`,
      body
    ).subscribe({
      next: (res) => {
        this.registros = res || [];
        this.cargando = false;
        this.calcularEstadisticas();
      },
      error: () => {
        this.error = 'Error al cargar el informe.';
        this.cargando = false;
      }
    });
  }

  limpiar() {
    this.fecha = '';
    this.registros = [];
    this.error = '';
    this.resetStats();
  }

  private resetStats() {
    this.stats = {
      total: 0,
      genero: {},
      aportexGenero: {},
      rangosEdad: {
        '0-12': 0,
        '13-17': 0,
        '18-25': 0,
        '26-40': 0,
        '41-60': 0,
        '61+': 0
      },
      aportexEdad: {},
      tipoVivienda: {},
      aportexVivienda: {},
      zonas: {},
      aportexZonas: {},
      subzonas: {},
      aportexSubzonas: {},
      escolaridad: {},
      aportexEscolaridad: {},
      ocupacion: {},
      aportexOcupacion: {},
      municipio: {},
      aportexMunicipio: {},
      resumen: {
        totalAportes: 0,
        promedioAportes: 0
      }
    };
  }

  /** ============================
   *  CALCULAR ESTADÍSTICAS
   * ============================ */
  private calcularEstadisticas() {
    const data = this.registros;

    if (data.length === 0) {
      this.resetStats();
      return;
    }

    this.stats.total = data.length;

    const add = (map: any, key: string, value: number = 1) =>
      map[key] = (map[key] || 0) + value;

    /** ---- ACUMULADORES ---- */

    data.forEach(d => {
      const aporte = d.saldo_aportes || 0;

      add(this.stats.genero, d.nombre_genero);
      add(this.stats.aportexGenero, d.nombre_genero, aporte);

      /** Rangos edad */
      let rango = '';
      if (d.edad <= 12) rango = '0-12';
      else if (d.edad <= 17) rango = '13-17';
      else if (d.edad <= 25) rango = '18-25';
      else if (d.edad <= 40) rango = '26-40';
      else if (d.edad <= 60) rango = '41-60';
      else rango = '61+';

      add(this.stats.rangosEdad, rango);
      add(this.stats.aportexEdad, rango, aporte);

      add(this.stats.tipoVivienda, d.nombre_tipo_vivienda);
      add(this.stats.aportexVivienda, d.nombre_tipo_vivienda, aporte);

      add(this.stats.zonas, d.nombre_zona);
      add(this.stats.aportexZonas, d.nombre_zona, aporte);

      add(this.stats.subzonas, d.nombre_sub_zona);
      add(this.stats.aportexSubzonas, d.nombre_sub_zona, aporte);

      add(this.stats.escolaridad, d.nombre_escolaridad);
      add(this.stats.aportexEscolaridad, d.nombre_escolaridad, aporte);

      add(this.stats.ocupacion, d.nombre_ocupacion);
      add(this.stats.aportexOcupacion, d.nombre_ocupacion, aporte);

      add(this.stats.municipio, d.municipio);
      add(this.stats.aportexMunicipio, d.municipio, aporte);
    });

    /** Totales */
    const totalAportes = data.reduce((s, d) => s + (d.saldo_aportes || 0), 0);
    this.stats.resumen.totalAportes = totalAportes;
    this.stats.resumen.promedioAportes = Math.round(totalAportes / data.length);
  }

  exportar() {
    if (this.registros.length === 0) {
      this.error = 'No hay datos para exportar.';
      return;
    }

    this.exporter.exportarListado(this.fecha, this.registros, this.stats);
  }

}
