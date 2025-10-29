import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header-actions',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header-actions.component.html',
  styleUrls: ['./header-actions.component.scss']
})
export class HeaderActionsComponent {
  @Input() titulo = ''; // Ejemplo: "Agencias"
  @Input() mostrarNuevo = true;
  @Input() mostrarImprimir = true;
  @Input() mostrarExportar = true;

  @Output() nuevo = new EventEmitter<void>();
  @Output() imprimir = new EventEmitter<void>();
  @Output() exportar = new EventEmitter<void>();

  onNuevo() { this.nuevo.emit(); }
  onImprimir() { this.imprimir.emit(); }
  onExportar() { this.exportar.emit(); }
}
