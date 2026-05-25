import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { GeneralApi } from '../../../../shared/general/general.api';

import { DocumentosSoporteApi } from './documentos-soporte.api';
import { DocumentosSoporteExporterService } from './documentos-soporte-exporter.service';

@Component({
  selector: 'app-documentos-soporte-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './documentos-soporte-list.component.html',
  styleUrls: ['./documentos-soporte-list.component.scss']
})
export class DocumentosSoporteListComponent
  implements OnInit {

  private readonly api =
    inject(DocumentosSoporteApi);

  private readonly generalApi =
    inject(GeneralApi);

  private readonly exporter =
    inject(DocumentosSoporteExporterService);

  agencias: any[] = [];

  filtros = {

    agencia: 0,

    fechaDesde:
      new Date()
        .toISOString()
        .split('T')[0],

    fechaHasta:
      new Date()
        .toISOString()
        .split('T')[0]

  };

  cargando = false;

  error = '';

  resumen: any = null;

  items: any[] = [];

  resumenAgencias: any[] = [];

  // ============================================================
  // 🧩 Agrupar resumen agencia / forma
  // ============================================================
  private agruparResumen(
    data: any[]
  ) {

    const grupos: any = {};

    for (const item of data) {

      const id =
        item.idAgencia;

      if (!grupos[id]) {

        grupos[id] = {

          codigoAgencia:
            item.codigoAgencia,

          nombreAgencia:
            item.nombreAgencia,

          formas: []

        };

      }

      grupos[id].formas.push({

        codigoForma:
          item.codigoForma,

        nombreForma:
          item.nombreForma,

        tipoDocumentoSoporte:
          item.tipoDocumentoSoporte,

        descripcionSoporte:
          item.descripcionSoporte,

        totalDocumentos:
          item.totalDocumentos,

        activos:
          item.activos,

        inactivos:
          item.inactivos,

        perdidos:
          item.perdidos,

        robados:
          item.robados,

        totalDocumentosFisicos:
          item.totalDocumentosFisicos

      });

    }

    return Object.values(grupos);

  }

  // ============================================================
  // 🔄 Inicializar
  // ============================================================
  async ngOnInit() {

    try {

      const data =
        await this.generalApi
          .listarAgencias()
          .toPromise();

      this.agencias =
        data ?? [];

    } catch (e) {

      console.error(
        'Error cargando agencias:',
        e
      );

      this.agencias = [];

    }

  }

  // ============================================================
  // 🔍 Buscar
  // ============================================================
  async buscar() {

    this.error = '';

    if (!this.filtros.fechaDesde) {

      this.error =
        'Debe indicar la fecha inicial.';

      return;

    }

    if (!this.filtros.fechaHasta) {

      this.error =
        'Debe indicar la fecha final.';

      return;

    }

    this.cargando = true;

    try {

      // ============================================================
      // 📄 Consulta principal
      // ============================================================
      const response =
        await this.api.consultar({

          agencia:
            String(this.filtros.agencia),

          fechaDesde:
            this.filtros.fechaDesde,

          fechaHasta:
            this.filtros.fechaHasta

        });

      this.resumen =
        response?.resumen ?? null;

      this.items =
        response?.items ?? [];

      // ============================================================
      // 📊 Resumen agencia / forma
      // ============================================================
      const resumenData =
        await this.api
          .resumenPorAgenciaForma({

            agencia:
              String(this.filtros.agencia),

            fechaDesde:
              this.filtros.fechaDesde,

            fechaHasta:
              this.filtros.fechaHasta

          }) || [];

      this.resumenAgencias =
        this.agruparResumen(
          resumenData
        );

    } catch (e) {

      console.error(
        'Error consultando informe:',
        e
      );

      this.error =
        'No fue posible consultar el informe.';

    } finally {

      this.cargando = false;

    }

  }

  // ============================================================
  // 🧹 Limpiar
  // ============================================================
  limpiar() {

    this.filtros = {

      agencia: 0,

      fechaDesde:
        new Date()
          .toISOString()
          .split('T')[0],

      fechaHasta:
        new Date()
          .toISOString()
          .split('T')[0]

    };

    this.resumen = null;

    this.items = [];

    this.resumenAgencias = [];

    this.error = '';

  }

  // ============================================================
  // 📤 Exportar
  // ============================================================
  exportar() {

    if (!this.items.length) {

      alert(
        'No hay información para exportar.'
      );

      return;

    }

    this.exporter.exportarExcel(
      this.items,
      this.filtros
    );

  }

}
