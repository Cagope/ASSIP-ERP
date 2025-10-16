import { Component, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup } from '@angular/forms';
import { Subscription } from 'rxjs';

// State del wizard
import { CapturaState } from '../captura-state.service';

// ⬇️ Tu upsert real de Ubicaciones
import { UbicacionesUpsertComponent } from '../../ubicaciones/ubicaciones-upsert.component';

@Component({
  standalone: true,
  selector: 'app-paso-ubicaciones',
  imports: [CommonModule, UbicacionesUpsertComponent],
  template: `
    <app-ubicaciones-upsert #child></app-ubicaciones-upsert>
  `,
  styles: [`
    :host ::ng-deep app-ubicaciones-upsert .acciones,
    :host ::ng-deep app-ubicaciones-upsert .btn-guardar,
    :host ::ng-deep app-ubicaciones-upsert .btn-cancelar {
      display: none !important;
    }
  `]
})
export class PasoUbicacionesComponent implements OnInit, OnDestroy {
  @ViewChild('child', { static: true }) child!: UbicacionesUpsertComponent;

  private state = inject(CapturaState);
  private STEP = 1; // índice del paso "Ubicaciones" en el STEPS del shell
  private subs: Subscription[] = [];

  ngOnInit(): void {
    // Espera a que el hijo cree su form
    queueMicrotask(() => this.initWithChild());
  }

  private async initWithChild(): Promise<void> {
    const form: FormGroup | undefined = (this.child as any)?.form;
    if (!form) {
      console.warn('[Captura] UbicacionesUpsertComponent no expone form; si usas outputs, ajusta este wrapper.');
      return;
    }

    // Registra form para validez reactiva
    this.state.trackForm(this.STEP, form);

    // Precarga datos si hay payload previo
    const prev = this.state.payload().ubicaciones ?? null;

    if (prev) {
      // 1) Parchea TODO menos sub-zona (la fijamos después de cargar lista)
      form.patchValue(
        {
          ...prev,
          id_sub_zona: null,
        },
        { emitEvent: false }
      );

      // 2) Si había zona previa, dispara la carga de sub-zonas del hijo
      const prevZona = Number(prev.id_zona ?? 0) || null;
      const prevSub  = Number(prev.id_sub_zona ?? 0) || null;

      if (prevZona) {
        // Llama al método del hijo que ya usas internamente
        if (typeof (this.child as any).onZonaChange === 'function') {
          (this.child as any).onZonaChange(prevZona);
        } else {
          // Fallback: setea el control; el hijo está suscrito a valueChanges
          form.get('id_zona')?.setValue(prevZona, { emitEvent: true });
        }

        // 3) Espera a que el hijo cargue subZonas (usa su signal)
        await this.waitForSubZonas(() => (this.child as any)?.subZonas?.() ?? [], 1200);

        // 4) Re-setea la sub-zona previa si existe en la lista
        if (prevSub) {
          const list: any[] = (this.child as any)?.subZonas?.() ?? [];
          const exists = list.some(s => Number(s.idSubZona) === prevSub);
          if (exists) {
            form.get('id_sub_zona')?.setValue(prevSub, { emitEvent: false });
          }
        }
      }
    }

    // Sincroniza validez y payload cuando el form cambie
    this.subs.push(
      form.statusChanges.subscribe(() => {
        if (form.valid) {
          this.state.mergePayload({ ubicaciones: form.getRawValue() });
          this.state.markValid(this.STEP, true);
        } else {
          this.state.markValid(this.STEP, false);
        }
      })
    );

    // Disparo inicial
    if (form.valid) {
      this.state.mergePayload({ ubicaciones: form.getRawValue() });
      this.state.markValid(this.STEP, true);
    }
  }

  private waitForSubZonas(getList: () => any[], timeoutMs = 1200): Promise<void> {
    return new Promise<void>(resolve => {
      const start = Date.now();
      const tick = () => {
        const list = getList();
        if ((Array.isArray(list) && list.length > 0) || Date.now() - start > timeoutMs) {
          resolve();
        } else {
          setTimeout(tick, 80);
        }
      };
      tick();
    });
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
    const form: FormGroup | undefined = (this.child as any)?.form;
    if (form) this.state.mergePayload({ ubicaciones: form.getRawValue() });
  }
}
