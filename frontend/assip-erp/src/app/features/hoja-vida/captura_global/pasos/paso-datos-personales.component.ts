// src/app/features/hoja-vida/captura_global/pasos/paso-datos-personales.component.ts
import { Component, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup } from '@angular/forms';
import { Subscription } from 'rxjs';

import { CapturaState } from '../captura-state.service';
import { DatosPersonalesUpsertComponent } from '../../datos-personales/datos-personales-upsert.component';

@Component({
  standalone: true,
  selector: 'app-paso-datos-personales',
  imports: [CommonModule, DatosPersonalesUpsertComponent],
  template: `
    <app-datos-personales-upsert #child></app-datos-personales-upsert>
  `
})
export class PasoDatosPersonalesComponent implements OnInit, OnDestroy {
  @ViewChild('child', { static: true }) child!: DatosPersonalesUpsertComponent;

  private state = inject(CapturaState);
  private STEP = 0;
  private subs: Subscription[] = [];

  ngOnInit(): void {
    queueMicrotask(() => {
      const form: FormGroup | undefined = (this.child as any)?.form;
      if (!form) {
        console.warn('[PasoDatosPersonales] El upsert no expone form; ajusta si usas otra interfaz.');
        this.state.markValid(this.STEP, false);
        return;
      }

      // 1) Validez reactiva
      this.state.trackForm(this.STEP, form);

      // 2) Precarga buffer previo
      const prev = this.state.payload().datosPersonales ?? null;
      if (prev) {
        form.patchValue(prev, { emitEvent: false });
      }

      // 3) Mantener snapshot en el state
      this.subs.push(
        form.statusChanges.subscribe(() => {
          this.state.markValid(this.STEP, form.valid);
          if (form.valid) {
            const snap = (form as any).getRawValue ? (form as any).getRawValue() : form.value;
            this.state.mergePayload({ datosPersonales: { ...snap } });
          }
        })
      );

      // 4) Saver: SOLO snapshot. NO API.
      if (typeof (this.state as any).registerSaver === 'function') {
        (this.state as any).registerSaver(this.STEP, async () => {
          const snap = (form as any).getRawValue ? (form as any).getRawValue() : form.value;
          const curr = this.state.payload().datosPersonales ?? {};
          const id =
            (curr as any).id ??
            (curr as any).idDatosPersonal ??
            (curr as any).id_datos_personal ?? null;

          this.state.mergePayload({
            datosPersonales: {
              ...snap,
              ...(id ? { id, idDatosPersonal: id, id_datos_personal: id } : {})
            }
          });

          // Nunca bloquea el avance
          return true;
        });
      }

      // 5) Validez inicial
      this.state.markValid(this.STEP, form.valid);
    });
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
    const form: FormGroup | undefined = (this.child as any)?.form;
    if (form) {
      const snap = (form as any).getRawValue ? (form as any).getRawValue() : form.value;
      this.state.mergePayload({ datosPersonales: { ...snap } });
    }
  }
}
