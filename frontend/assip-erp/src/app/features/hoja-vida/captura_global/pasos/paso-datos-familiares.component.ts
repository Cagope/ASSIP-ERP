import { Component, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormArray, FormControl, FormGroup } from '@angular/forms';
import { Subscription } from 'rxjs';
import { auditTime } from 'rxjs/operators';

import { CapturaState } from '../captura-state.service';
import { DatosFamiliaresUpsertComponent } from '../../datos-familiares/datos-familiares-upsert.component';

@Component({
  standalone: true,
  selector: 'app-paso-datos-familiares',
  imports: [CommonModule, DatosFamiliaresUpsertComponent],
  template: `<app-datos-familiares-upsert #child></app-datos-familiares-upsert>`,
  styles: [`
    :host ::ng-deep app-datos-familiares-upsert .acciones,
    :host ::ng-deep app-datos-familiares-upsert .btn-guardar,
    :host ::ng-deep app-datos-familiares-upsert .btn-cancelar {
      display: none !important;
    }
  `]
})
export class PasoDatosFamiliaresComponent implements OnInit, OnDestroy {
  @ViewChild('child', { static: true }) child!: DatosFamiliaresUpsertComponent;

  private state = inject(CapturaState);
  private STEP = 4;
  private subs: Subscription[] = [];

  async ngOnInit(): Promise<void> {
    // Espera a que el hijo cree su form
    await this.waitFor(() => !!(this.child as any)?.form, 1200);

    const form: FormGroup | undefined = (this.child as any)?.form;
    if (!form) { console.warn('[Captura] DatosFamiliaresUpsertComponent sin form'); return; }

    // Habilita todo para permitir captura en el wizard
    form.enable({ emitEvent: false });

    // Registra para el guard
    this.state.trackForm(this.STEP, form);

    // Rehidratar si había payload previo (profundo)
    const prev = this.state.payload().datosFamiliares ?? null;
    if (prev) {
      // Si el upsert carga catálogos async, espera un instante
      await this.waitFor(() => this.formReadyForPatch(form), 500);
      this.deepRehydrateForm(form, prev, (this.child as any)); // emitEvent interno = true
    }

    // Guardar SIEMPRE el snapshot (válido o no)
    this.subs.push(
      form.valueChanges.pipe(auditTime(60)).subscribe(() => {
        this.state.mergePayload({ datosFamiliares: this.deepSnapshot(form) });
      })
    );

    // Marcar validez por separado
    this.subs.push(
      form.statusChanges.subscribe(() => {
        this.state.markValid(this.STEP, form.valid);
      })
    );

    // Snapshot inicial
    this.state.mergePayload({ datosFamiliares: this.deepSnapshot(form) });
    this.state.markValid(this.STEP, form.valid);
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
    const form: FormGroup | undefined = (this.child as any)?.form;
    if (form) {
      this.state.mergePayload({ datosFamiliares: this.deepSnapshot(form) });
    }
  }

  // ===== Helpers =====

  private deepSnapshot(ctrl: AbstractControl): any {
    if (ctrl instanceof FormControl) return ctrl.value;
    if (ctrl instanceof FormGroup) {
      const out: any = {};
      Object.keys(ctrl.controls).forEach(k => out[k] = this.deepSnapshot(ctrl.controls[k]));
      return out;
    }
    if (ctrl instanceof FormArray) return ctrl.controls.map(c => this.deepSnapshot(c));
    return null;
  }

  private deepRehydrateForm(ctrl: AbstractControl, data: any, child: any): void {
    if (ctrl instanceof FormControl) {
      ctrl.setValue(data ?? null, { emitEvent: true });
      return;
    }
    if (ctrl instanceof FormGroup) {
      Object.keys(ctrl.controls).forEach(key => {
        if (data && Object.prototype.hasOwnProperty.call(data, key)) {
          this.deepRehydrateForm(ctrl.controls[key], data[key], child);
        }
      });
      return;
    }
    if (ctrl instanceof FormArray) {
      const arrData: any[] = Array.isArray(data) ? data : [];
      while (ctrl.length) ctrl.removeAt(0, { emitEvent: false });

      const factory = typeof child?.buildItemGroup === 'function' ? child.buildItemGroup.bind(child) : null;

      arrData.forEach(item => {
        let group: AbstractControl;
        if (factory) {
          group = factory(item); // usa el factory del upsert (mejor)
        } else {
          // fallback plano
          const fg = new FormGroup({});
          Object.keys(item ?? {}).forEach(k => fg.addControl(k, new FormControl(item[k])));
          group = fg;
        }
        // set valores (profundo)
        this.deepRehydrateForm(group, item, child);
        ctrl.push(group, { emitEvent: false });
      });

      ctrl.updateValueAndValidity({ emitEvent: true });
      return;
    }
  }

  private formReadyForPatch(_form: FormGroup): boolean {
    // Ajusta si tu upsert necesita tener listas cargadas antes del patch (ej. ciudades/parentescos)
    return true;
    // p.ej. return !!_form.get('idDepartamento') && !!_form.get('idCiudad');
  }

  private waitFor(cond: () => boolean, timeoutMs = 1000): Promise<void> {
    return new Promise<void>(resolve => {
      const t0 = Date.now();
      const tick = () => {
        if (cond() || Date.now() - t0 > timeoutMs) return resolve();
        setTimeout(tick, 50);
      };
      tick();
    });
  }
}
