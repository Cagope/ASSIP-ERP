import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  SimpleChanges,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { ExtractoModalComponent } from './consulta-extracto-modal.component';

import { ReportingService } from '../../../../shared/reporting/reporting.service';
import { ReportQueryRequest } from '../../../../shared/reporting/reporting.api';

@Component({
  selector: 'app-cuentas-ahorro-detalle',
  standalone: true,
  imports: [
    CommonModule,
    ExtractoModalComponent
  ],
  templateUrl: './consulta-cuentas-ahorro-detalle.component.html',
  styleUrls: ['./consulta-cuentas-ahorro-detalle.component.scss'],
})
export class CuentasAhorroDetalleComponent implements OnChanges {

  @Input() cuenta: any;
  @Output() cerrar = new EventEmitter<void>();

  private readonly reporting = inject(ReportingService);

  mostrarModalExtracto = false;
  imagenAmpliada: string | null = null;

  cargandoHojaVida = false;

  private ultimoIdDatosPersonal?: number;

  ngOnChanges(changes: SimpleChanges): void {

    if (
      changes['cuenta']
      && this.cuenta?.id_datos_personal
      && this.cuenta.id_datos_personal !== this.ultimoIdDatosPersonal
    ) {

      this.ultimoIdDatosPersonal =
        this.cuenta.id_datos_personal;

      this.cargarHojaVidaCompleta();
    }
  }

  async cargarHojaVidaCompleta(): Promise<void> {

    if (!this.cuenta?.id_datos_personal) {
      return;
    }

    this.cargandoHojaVida = true;

    try {

      const req: ReportQueryRequest = {
        schema: 'reporting',
        view: 'vw_hoja_vida_general_total_reciente',
        scope: 'GLOBAL',
        filters: {
          id_datos_personal: this.cuenta.id_datos_personal
        }
      };

      const res =
        await this.reporting.ejecutarReporte(req);

      const hv = res.data?.[0];

      if (hv) {

        const sarlaftActual =
          this.cuenta?.sarlaft;

        this.cuenta = {
          ...this.cuenta,
          ...hv,
          sarlaft: sarlaftActual
        };
      }

    } catch (error) {

      console.error(
        'Error cargando hoja de vida completa:',
        error
      );

    } finally {

      this.cargandoHojaVida = false;
    }
  }

  onCerrar(): void {
    this.cerrar.emit();
  }

  abrirExtracto(): void {
    this.mostrarModalExtracto = true;
  }

  cerrarExtracto(): void {
    this.mostrarModalExtracto = false;
  }

  getImagen(
    nombreArchivo: string | null,
    tipo: 'foto' | 'firma'
  ): string {

    if (
      !nombreArchivo
      || nombreArchivo.trim() === ''
    ) {

      return tipo === 'foto'
        ? '/assets/fotos/foto_muestra.jpg'
        : '/assets/firmas/firma1_muestra.jpg';
    }

    const nombreNormalizado = nombreArchivo
      .trim()
      .replace(/^fi/i, 'FI')
      .replace(/^ft/i, 'FT')
      .replace(/\.jpeg$/i, '.jpg');

    const carpeta =
      tipo === 'foto'
        ? 'fotos'
        : 'firmas';

    return `/assets/${carpeta}/${nombreNormalizado}`;
  }

  onImageError(
    event: Event,
    tipo: 'foto' | 'firma'
  ): void {

    const img =
      event.target as HTMLImageElement;

    img.src =
      tipo === 'foto'
        ? '/assets/fotos/foto_muestra.jpg'
        : '/assets/firmas/firma1_muestra.jpg';
  }

  abrirImagen(src: string): void {
    this.imagenAmpliada = src;
  }

  cerrarImagen(): void {
    this.imagenAmpliada = null;
  }
}
