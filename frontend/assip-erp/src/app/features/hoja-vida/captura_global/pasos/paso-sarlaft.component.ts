import { Component, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CapturaState } from '../captura-state.service';
import { SarlaftUpsertComponent } from '../../sarlaft/sarlaft-upsert.component';

@Component({
  standalone: true,
  selector: 'app-paso-sarlaft',
  imports: [CommonModule, SarlaftUpsertComponent],
  template: `
    <app-sarlaft-upsert
      #child
      [captureMode]="true"
      [idPersonaOverride]="state.payload().datosPersonales?.idDatosPersonal || null"
      (valueChange)="onValue($event)"
      (validChange)="onValid($event)">
    </app-sarlaft-upsert>
  `,
  styles: [`
    :host ::ng-deep app-sarlaft-upsert .acciones,
    :host ::ng-deep app-sarlaft-upsert .btn-guardar,
    :host ::ng-deep app-sarlaft-upsert .btn-cancelar { display: none !important; }
  `]
})
export class PasoSarlaftComponent implements OnInit, OnDestroy {
  @ViewChild('child', { static: true }) child!: SarlaftUpsertComponent;

  // PUBLIC para el template
  constructor(public state: CapturaState) {}
  private readonly STEP = 6;

  ngOnInit(): void {
    // precarga si había datos en state
    const prev = this.state.payload().sarlaft ?? null;
    if (prev && this.child) {
      // si quieres, puedes setear this.child.view.set(prev) con cuidado
    }
  }

  onValue(val: any) {
    this.state.mergePayload({ sarlaft: val });
  }
  onValid(ok: boolean) {
    this.state.markValid(this.STEP, ok);
  }

  ngOnDestroy(): void {
    // opcional: persistir última vista del hijo si quieres
  }
}
