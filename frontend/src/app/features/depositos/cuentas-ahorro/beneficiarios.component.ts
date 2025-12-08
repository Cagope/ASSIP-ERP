import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BeneficiarioDTO } from '../../../shared/models/beneficiario.model';

@Component({
  selector: 'app-beneficiarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './beneficiarios.component.html',
  styleUrls: ['./beneficiarios.component.scss']
})
export class BeneficiariosComponent {

  @Input() beneficiarios: BeneficiarioDTO[] = [];
  @Output() beneficiariosChange = new EventEmitter<BeneficiarioDTO[]>();

  nuevo: BeneficiarioDTO = this.resetNuevo();

  // ---------------------------------------------------------
  // Crear objeto vacío
  // ---------------------------------------------------------
  private resetNuevo(): BeneficiarioDTO {
    return {
      idBeneficiario: undefined,
      idCuentaAhorro: undefined,
      documentoBeneficiario: '',
      nombreBeneficiario: '',
      telefonoBeneficiario: '',
      celularBeneficiario: '',
      tipoParentesco: ''
    };
  }

  // ---------------------------------------------------------
  // Agregar beneficiario
  // ---------------------------------------------------------
  agregar() {
    if (!this.nuevo.documentoBeneficiario || !this.nuevo.nombreBeneficiario) {
      alert('Documento y nombre son obligatorios.');
      return;
    }

    this.beneficiarios.push({ ...this.nuevo });
    this.beneficiariosChange.emit(this.beneficiarios);

    this.nuevo = this.resetNuevo();
  }

  // ---------------------------------------------------------
  // Eliminar beneficiario
  // ---------------------------------------------------------
  eliminar(i: number) {
    this.beneficiarios.splice(i, 1);
    this.beneficiariosChange.emit(this.beneficiarios);
  }
}
