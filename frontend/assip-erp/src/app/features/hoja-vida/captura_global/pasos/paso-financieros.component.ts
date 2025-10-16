import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CapturaState } from '../captura-state.service';
import { FinancierosUpsertComponent } from '../../financieros/financieros-upsert.component';

@Component({
  standalone: true,
  selector: 'app-paso-financieros',
  imports: [CommonModule, FinancierosUpsertComponent],
  template: `
    <app-financieros-upsert
      [captureMode]="true"
      [idPersonaOverride]="state.payload().datosPersonales?.idDatosPersonal || null"
      (valueChange)="onValue($event)"
      (validChange)="onValid($event)">
    </app-financieros-upsert>
  `
})
export class PasoFinancierosComponent {
  constructor(public state: CapturaState) {}
  private readonly STEP = 3; // ojo con el índice correcto

  onValue(val: any) { this.state.mergePayload({ financieros: val }); }
  onValid(ok: boolean) { this.state.markValid(this.STEP, ok); }
}
