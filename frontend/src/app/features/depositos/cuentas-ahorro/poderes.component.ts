import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PoderDTO } from '../../../shared/models/poder.model';

@Component({
  selector: 'app-poderes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './poderes.component.html',
  styleUrls: ['./poderes.component.scss']
})
export class PoderesComponent {

  @Input() poderes: PoderDTO[] = [];
  @Output() poderesChange = new EventEmitter<PoderDTO[]>();

  nuevo: PoderDTO = this.resetNuevo();

  // ---------------------------------------------------------
  // Crear objeto vacío
  // ---------------------------------------------------------
  private resetNuevo(): PoderDTO {
    return {
      idPoder: undefined,
      idCuenta: undefined,
      documentoPoder: '',
      nombrePoder: '',
      telefonoPoder: '',
      celularPoder: ''
    };
  }

  // ---------------------------------------------------------
  // Agregar poder
  // ---------------------------------------------------------
  agregar() {
    if (!this.nuevo.documentoPoder || !this.nuevo.nombrePoder) {
      alert('Documento y nombre son obligatorios.');
      return;
    }

    this.poderes.push({ ...this.nuevo });
    this.poderesChange.emit(this.poderes);

    this.nuevo = this.resetNuevo();
  }

  // ---------------------------------------------------------
  // Eliminar poder
  // ---------------------------------------------------------
  eliminar(i: number) {
    this.poderes.splice(i, 1);
    this.poderesChange.emit(this.poderes);
  }
}
