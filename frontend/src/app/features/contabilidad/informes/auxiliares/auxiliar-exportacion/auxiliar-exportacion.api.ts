import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../../../environments/environment';

export interface FiltrosAuxiliarExportacion {
  idAgencia: number;
  fechaInicial: string;
  fechaFinal: string;
  modalidad: 'RESUMIDO' | 'DETALLADO';
}

@Injectable({ providedIn: 'root' })
export class AuxiliarExportacionApi {
  private readonly http = inject(HttpClient);

  exportar(filtros: FiltrosAuxiliarExportacion) {
    const params = new HttpParams()
      .set('idAgencia', filtros.idAgencia)
      .set('fechaInicial', filtros.fechaInicial)
      .set('fechaFinal', filtros.fechaFinal)
      .set('modalidad', filtros.modalidad);
    return this.http.get(
      `${environment.apiUrl}/contabilidad/informes/auxiliares/exportacion/excel`,
      { params, responseType: 'blob' }
    );
  }
}
