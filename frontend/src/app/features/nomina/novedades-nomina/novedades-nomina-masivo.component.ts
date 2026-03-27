import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';

import {
  NovedadesNominaApi,
  NovedadMasivaRequestDTO,
  NovedadMasivaResultDTO
} from './novedades-nomina.api';

import {
  ConceptosNominaApi,
  ConceptoNominaListDTO
} from '../conceptos-nomina/conceptos-nomina.api';

import * as XLSX from 'xlsx';

@Component({
  standalone: true,
  selector: 'app-novedades-nomina-masivo',
  templateUrl: './novedades-nomina-masivo.component.html',
  styleUrls: ['./novedades-nomina-masivo.component.scss'],
  imports: [CommonModule, FormsModule, RouterModule, HeaderActionsComponent]
})
export class NovedadesNominaMasivoComponent implements OnInit {

  private readonly api = inject(NovedadesNominaApi);
  private readonly conceptosApi = inject(ConceptosNominaApi);
  private readonly router = inject(Router);

  conceptos: ConceptoNominaListDTO[] = [];

  loading = false;
  procesando = false;

  // ✅ flujo preview
  viendoPreview = false;
  preview: any[] = [];

  // resultado final (después de aplicar)
  resultado: NovedadMasivaResultDTO | null = null;


  // =========================
  // 🔢 TOTALES PREVIEW
  // =========================
  totalContratos = 0;
  totalNuevos = 0;
  totalExistentes = 0;

  totalValorNuevos = 0;
  totalValorGeneral = 0;

  // 🔥 comportamiento dinámico según tipo_calculo del concepto
  valorEditable = true;
  requiereCantidad = false;
  esAuxTransporte = false;

  form: NovedadMasivaRequestDTO = {
    codigoConcepto: '',
    cantidad: 0,
    valor: 0,
    observacion: null
  };

  ngOnInit(): void {
    this.cargarCatalogos();
  }

  private cargarCatalogos(): void {

    this.conceptosApi.listar().subscribe({
      next: d => this.conceptos = d ?? [],
      error: () => this.conceptos = []
    });
  }

  // =========================================================
  // 🔥 CAMBIO DE CONCEPTO (DINÁMICO)
  // =========================================================
  onConceptoChange(): void {

    // reset UI
    this.valorEditable = true;
    this.requiereCantidad = false;
    this.esAuxTransporte = false;

    // reset preview (cuando cambia concepto toca volver a previsualizar)
    this.viendoPreview = false;
    this.preview = [];
    this.resultado = null;

    const concepto =
      this.conceptos.find(c => c.codigoConcepto === this.form.codigoConcepto) ?? null;

    if (!concepto) return;

    switch (concepto.tipoCalculo) {

      case 'MANUAL':
        this.valorEditable = true;
        this.requiereCantidad = false;
        break;

      case 'POR_HORAS':
        this.valorEditable = false;
        this.requiereCantidad = true;
        this.form.valor = 0; // no se digita
        break;

      case 'POR_DIAS':
        this.valorEditable = false;
        this.requiereCantidad = true;
        this.form.valor = 0; // no se digita
        break;

      case 'POR_PORCENTAJE':
        this.valorEditable = false;
        this.requiereCantidad = false;
        this.form.valor = 0; // no se digita
        break;

      case 'AUX_TRANSPORTE':
        this.esAuxTransporte = true;
        this.valorEditable = false;
        this.requiereCantidad = true; // días
        this.form.valor = 0;          // NO se digita valor
        break;

      default:
        this.valorEditable = true;
        this.requiereCantidad = false;
        break;
    }

    if (!this.valorEditable) {
      this.form.valor = 0;
    }
  }

  // =========================================================
  // 🔍 PREVIEW (NO GRABA)
  // =========================================================
  previewMasivo(): void {

    if (!this.form.codigoConcepto) {
      alert('Debe seleccionar un concepto.');
      return;
    }

    if (this.requiereCantidad && (!this.form.cantidad || this.form.cantidad <= 0)) {
      alert('Debe ingresar una cantidad válida.');
      return;
    }

    if (this.valorEditable && (!this.form.valor || this.form.valor <= 0)) {
      alert('Debe ingresar un valor válido.');
      return;
    }

    this.procesando = true;
    this.resultado = null;

    // limpiar preview anterior
    this.preview = [];
    this.viendoPreview = false;

    this.api.previewMasivo(this.form).subscribe({
      next: (rows: any[]) => {
        this.preview = rows ?? [];
        this.viendoPreview = true;

        // 🔥 CALCULAR TOTALES
        this.calcularTotalesPreview();

        this.procesando = false;
      },

      error: () => {
        this.procesando = false;
        alert('No se pudo generar el preview.');
      }
    });
  }

  private calcularTotalesPreview(): void {

    this.totalContratos = this.preview.length;

    this.totalNuevos = this.preview.filter(x => !x.ya_existe).length;
    this.totalExistentes = this.preview.filter(x => x.ya_existe).length;

    this.totalValorGeneral = this.preview.reduce(
      (acc, x) => acc + Number(x.valor_calculado ?? 0),
      0
    );

    this.totalValorNuevos = this.preview
      .filter(x => !x.ya_existe)
      .reduce((acc, x) => acc + Number(x.valor_calculado ?? 0), 0);
  }



  // =========================================================
  // ✅ APLICAR / GRABAR
  // =========================================================
  generar(): void {

    if (!this.preview || this.preview.length === 0) {
      alert('Primero debe ejecutar Preview.');
      return;
    }

    this.procesando = true;
    this.resultado = null;

    this.api.generarMasivo(this.form).subscribe({
      next: (res) => {
        this.resultado = res;
        this.procesando = false;

        // ✅ después de aplicar, se puede mantener preview o limpiarlo
        // yo lo dejo visible para auditoría visual:
        // this.preview = [];
        // this.viendoPreview = false;
      },
      error: () => {
        this.procesando = false;
        alert('No se pudo aplicar la novedad masiva.');
      }
    });
  }

  volver(): void {
    this.router.navigate(['/nomina/novedades']);
  }

  exportarPreview(): void {

    if (!this.preview || this.preview.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    // ==================================================
    // 🔥 EXPORTAR SOLO NUEVOS
    // ==================================================
    const nuevos = this.preview.filter(x => !x.ya_existe);

    if (nuevos.length === 0) {
      alert('No hay registros NUEVOS para exportar.');
      return;
    }

    // ==================================================
    // ARMAR DATA PARA EXCEL (SOLO NUEVOS)
    // ==================================================
    const data = nuevos.map(x => ({
      Documento: x.documento,
      Empleado: x.nombre_empleado,
      Cantidad: x.cantidad ?? 0,
      'Salario Base': Number(x.salario_base ?? 0),
      'Valor Calculado': Number(x.valor_calculado ?? 0),
      Estado: 'Nuevo'
    }));

    // ==================================================
    // CREAR HOJA
    // ==================================================
    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(data);

    worksheet['!cols'] = [
      { wch: 15 }, // Documento
      { wch: 35 }, // Empleado
      { wch: 12 }, // Cantidad
      { wch: 18 }, // Salario Base
      { wch: 18 }, // Valor Calculado
      { wch: 12 }  // Estado
    ];

    // ==================================================
    // CREAR LIBRO
    // ==================================================
    const workbook: XLSX.WorkBook = {
      Sheets: { 'Nuevos': worksheet },
      SheetNames: ['Nuevos']
    };

    // ==================================================
    // NOMBRE DEL ARCHIVO
    // ==================================================
    const concepto = this.conceptos.find(
      c => c.codigoConcepto === this.form.codigoConcepto
    );

    const nombreConcepto = concepto
      ? `${concepto.codigoConcepto}_${concepto.nombreConcepto}`
      : 'Novedad';

    const nombreSeguro = nombreConcepto
      .replace(/[^a-zA-Z0-9_]/g, '_')
      .replace(/_+/g, '_');

    const fecha = new Date().toISOString().substring(0, 10);

    const fileName =
      `preview_novedades_NUEVOS_${nombreSeguro}_${fecha}.xlsx`;

    XLSX.writeFile(workbook, fileName);
  }

}
