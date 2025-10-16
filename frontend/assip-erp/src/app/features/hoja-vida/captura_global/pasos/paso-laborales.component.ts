import { Component, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup } from '@angular/forms';
import { Subscription } from 'rxjs';

// State del wizard
import { CapturaState } from '../captura-state.service';

// ⬇️ Tu upsert real de Laborales
import { LaboralesUpsertComponent } from '../../laborales/laborales-upsert.component';

@Component({
  standalone: true,
  selector: 'app-paso-laborales',
  imports: [CommonModule, LaboralesUpsertComponent],
  template: `
    <!-- OPCIÓN A: si tu upsert tiene @Input() inWizard -->
    <!-- <app-laborales-upsert #child [inWizard]="true"></app-laborales-upsert> -->

    <!-- OPCIÓN B: si NO tiene Input, úsalo tal cual -->
    <app-laborales-upsert #child></app-laborales-upsert>
  `,
  styles: [`
    /* OPCIÓN B: ocultar acciones del upsert SOLO en el wizard */
    :host ::ng-deep app-laborales-upsert .acciones,
    :host ::ng-deep app-laborales-upsert .btn-guardar,
    :host ::ng-deep app-laborales-upsert .btn-cancelar {
      display: none !important;
    }
  `]
})
export class PasoLaboralesComponent implements OnInit, OnDestroy {
  @ViewChild('child', { static: true }) child!: LaboralesUpsertComponent;

  private state = inject(CapturaState);
  private STEP = 2; // índice del paso "Laborales" en el STEPS del shell
  private subs: Subscription[] = [];

  ngOnInit(): void {
    // Espera a que el hijo cree su form
    queueMicrotask(() => {
      const form: FormGroup | undefined = (this.child as any)?.form;
      if (!form) {
        console.warn('[Captura] LaboralesUpsertComponent no expone form; si usas outputs, ajusta este wrapper.');
        return;
      }

      // Registrar form para validez reactiva
      this.state.trackForm(this.STEP, form);

      // Precargar datos si hay payload previo
      const prev = this.state.payload().laborales ?? null;
      if (prev) form.patchValue(prev, { emitEvent: false });

      // Sincronizar validez y payload cuando cambie el form
      this.subs.push(
        form.statusChanges.subscribe(() => {
          if (form.valid) {
            this.state.mergePayload({ laborales: form.getRawValue() });
            this.state.markValid(this.STEP, true);
          } else {
            this.state.markValid(this.STEP, false);
          }
        })
      );

      // Disparo inicial
      if (form.valid) {
        this.state.mergePayload({ laborales: form.getRawValue() });
        this.state.markValid(this.STEP, true);
      }
    });
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
    const form: FormGroup | undefined = (this.child as any)?.form;
    if (form) this.state.mergePayload({ laborales: form.getRawValue() });
  }
}
