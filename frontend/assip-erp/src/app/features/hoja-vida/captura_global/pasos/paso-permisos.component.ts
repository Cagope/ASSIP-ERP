import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CapturaState } from '../captura-state.service';
import { PermisosEspecialesUpsertComponent } from '../../permisos-especiales/permisos-especiales-upsert.component';

@Component({
  standalone: true,
  selector: 'app-paso-permisos',
  imports: [CommonModule, PermisosEspecialesUpsertComponent],
  template: `
    <app-permisos-especiales-upsert
      [captureMode]="true"
      [idPersonaOverride]="state.payload().datosPersonales?.idDatosPersonal || null"
      (valueChange)="onValue($event)"
      (validChange)="onValid($event)">
    </app-permisos-especiales-upsert>
  `
})
export class PasoPermisosComponent {
  constructor(public state: CapturaState) {}
  private readonly STEP = 7; // índice de “Permisos”

  onValue(val: any) { this.state.mergePayload({ permisos: val }); }
  onValid(ok: boolean) { this.state.markValid(this.STEP, ok); }
}
