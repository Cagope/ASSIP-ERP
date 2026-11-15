import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { SessionService } from '../../../../../core/auth/session.service';
import { AuxiliarExportacionApi, FiltrosAuxiliarExportacion } from './auxiliar-exportacion.api';

interface AgenciaOpcion {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
}

@Component({
  selector: 'app-auxiliar-exportacion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auxiliar-exportacion.component.html',
  styleUrl: './auxiliar-exportacion.component.scss'
})
export class AuxiliarExportacionComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly api = inject(AuxiliarExportacionApi);
  agencias: AgenciaOpcion[] = [];
  cargando = false;
  mensajeError = '';
  filtros: FiltrosAuxiliarExportacion = {
    idAgencia: 0, fechaInicial: '', fechaFinal: '', modalidad: 'RESUMIDO'
  };

  ngOnInit(): void {
    this.agencias = this.session.agenciasSig()
      .filter(a => a.idAgencia !== null)
      .map(a => ({
        idAgencia: a.idAgencia!,
        codigoAgencia: a.codigoAgencia ?? '',
        nombreAgencia: a.nombreAgencia ?? ''
      }));
    const activa = this.session.agenciaActivaSig()?.idAgencia;
    this.filtros.idAgencia = this.agencias.find(a => a.idAgencia === activa)?.idAgencia
      ?? (this.agencias.length === 1 ? this.agencias[0].idAgencia : 0);
  }

  exportar(): void {
    this.mensajeError = '';
    if (this.cargando || !this.filtros.idAgencia || !this.filtros.fechaInicial ||
        !this.filtros.fechaFinal || this.filtros.fechaInicial > this.filtros.fechaFinal) {
      this.mensajeError = 'Seleccione agencia y un rango de fechas válido.';
      return;
    }
    this.cargando = true;
    this.api.exportar(this.filtros).pipe(finalize(() => this.cargando = false))
      .subscribe({
        next: archivo => {
          const url = URL.createObjectURL(archivo);
          const enlace = document.createElement('a');
          enlace.href = url;
          enlace.download = `auxiliar_${this.filtros.modalidad.toLowerCase()}_${this.filtros.fechaInicial}_${this.filtros.fechaFinal}.xlsx`;
          enlace.click();
          URL.revokeObjectURL(url);
        },
        error: () => this.mensajeError = 'No fue posible generar el archivo Excel.'
      });
  }
}
