import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExtractoModalComponent } from './consulta-extracto-modal.component'; // ✅ Import del modal


/**
 * 🧾 Detalle de Cuenta de Ahorro
 * ------------------------------------------------------------
 * Muestra la información detallada de la cuenta seleccionada:
 * titular, datos básicos, estado, saldos, fotos y firmas.
 */
@Component({
  selector: 'app-cuentas-ahorro-detalle',
  standalone: true,
  imports: [CommonModule, ExtractoModalComponent], // ✅ Incluir el modal aquí
  templateUrl: './consulta-cuentas-ahorro-detalle.component.html',
  styleUrls: ['./consulta-cuentas-ahorro-detalle.component.scss']
})
export class CuentasAhorroDetalleComponent {
  @Input() cuenta: any;
  @Output() cerrar = new EventEmitter<void>();

  mostrarModalExtracto = false;

  ngOnInit(): void {
    console.log("🟡 Cuenta recibida en DETALLE:", this.cuenta);
    console.log("🟠 SARLAFT en detalle:", this.cuenta?.sarlaft);
  }

  /** 🔙 Cierra la vista de detalle */
  onCerrar(): void {
    this.cerrar.emit();
  }

  /** 🧾 Abre el modal de extracto */
  abrirExtracto(): void {
    this.mostrarModalExtracto = true;
  }

  /** 🧾 Cierra el modal de extracto */
  cerrarExtracto(): void {
    this.mostrarModalExtracto = false;
  }

  /**
   * 🖼️ Retorna la ruta completa del archivo, según el tipo.
   * Si el campo está vacío, usa la imagen de muestra por defecto.
   */
  getImagen(nombreArchivo: string | null, tipo: 'foto' | 'firma'): string {
    if (!nombreArchivo || nombreArchivo.trim() === '') {
      return tipo === 'foto'
        ? '/assets/fotos/foto_muestra.jpg'
        : '/assets/firmas/firma1_muestra.jpg';
    }

    const nombreNormalizado = nombreArchivo
      .trim()
      .replace(/^fi/i, 'FI')
      .replace(/^ft/i, 'FT')
      .replace(/\.jpeg$/i, '.jpg');

    const carpeta = tipo === 'foto' ? 'fotos' : 'firmas';
    return `/assets/${carpeta}/${nombreNormalizado}`;
  }

  /** 🧩 Si el archivo no se encuentra, mostrar la imagen de muestra */
  onImageError(event: Event, tipo: 'foto' | 'firma'): void {
    const img = event.target as HTMLImageElement;
    img.src =
      tipo === 'foto'
        ? '/assets/fotos/foto_muestra.jpg'
        : '/assets/firmas/firma1_muestra.jpg';
  }

  /** 🌄 Control del modal de imagen ampliada */
  imagenAmpliada: string | null = null;

  abrirImagen(src: string): void {
    this.imagenAmpliada = src;
  }

  cerrarImagen(): void {
    this.imagenAmpliada = null;
  }
}
