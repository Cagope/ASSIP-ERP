// src/app/features/hoja-vida/captura_global/pasos/paso-referencias.component.ts
import { Component, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup } from '@angular/forms';
import { Subscription } from 'rxjs';

import { CapturaState } from '../captura-state.service';
import { ActivatedRoute, Router } from '@angular/router';

// Ajusta el import si tu upsert está en otra ruta
import { ReferenciasPersonalesUpsertComponent } from '../../referencias-personales/referencias-personales-upsert.component';

@Component({
  standalone: true,
  selector: 'app-paso-referencias',
  imports: [CommonModule, ReferenciasPersonalesUpsertComponent],
  template: `
    <app-referencias-personales-upsert
      #child
      [idDatosPersonalesInput]="idDatos">
    </app-referencias-personales-upsert>
  `,
  styles: [`
    :host ::ng-deep app-referencias-personales-upsert .acciones,
    :host ::ng-deep app-referencias-personales-upsert .btn-guardar,
    :host ::ng-deep app-referencias-personales-upsert .btn-cancelar {
      display: none !important;
    }
  `]
})
export class PasoReferenciasComponent implements OnInit, OnDestroy {
  @ViewChild('child', { static: true }) child!: ReferenciasPersonalesUpsertComponent;

  private state = inject(CapturaState);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  // Índice 0-based de Referencias
  private STEP = 5;
  private subs: Subscription[] = [];

  // Se pasa al hijo por @Input para inicializar su FK
  idDatos = 0;

  // ————— Helpers —————
  private n(x: any): number { const v = Number(x ?? 0); return Number.isFinite(v) && v > 0 ? v : 0; }

  /** ID canónico: primero query param raíz, luego payload.datosPersonales.id_datos_personal */
  private getPersonaIdStrict(): number | null {
    const rootQ = this.router.routerState.snapshot.root.queryParams;
    const q = this.n(rootQ?.['idDatosPersonales']);
    if (q) return q;

    const p: any = this.state.payload?.() ?? {};
    const id = this.n(p?.datosPersonales?.id_datos_personal);
    return id || null;
  }

  /** Fija ?idDatosPersonales=ID si no está aún en la URL del paso */
  private async ensureIdInUrl(id: number): Promise<void> {
    const already = this.n(this.route.snapshot.queryParamMap.get('idDatosPersonales'));
    if (!already) {
      await this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { idDatosPersonales: id },
        queryParamsHandling: 'merge',
        replaceUrl: true,
      });
    }
  }

  constructor() {
    // Calcula id antes de renderizar el hijo
    this.idDatos = this.getPersonaIdStrict() ?? 0;
  }

  ngOnInit(): void {
    // Si ya hay id, asegúralo en la URL del paso
    if (this.idDatos > 0) {
      this.ensureIdInUrl(this.idDatos).catch(() => {});
    }

    // Inicialización diferida para tener child.form listo
    queueMicrotask(() => {
      const form: FormGroup | undefined = (this.child as any)?.form;
      if (!form) {
        console.warn('[PasoReferencias] child.form no disponible');
        this.state.markValid?.(this.STEP, false);
        return;
      }

      // 1) Rehidratar snapshot previo (para no redigitar al volver)
      const prev = (this.state.payload()?.referencias ?? null) as any;
      if (prev) {
        try {
          form.patchValue(prev, { emitEvent: false });
          // Disparar el ciclo depto->ciudad del hijo (importante para listar ciudades correctas)
          const dep = prev?.id_departamento ?? null;
          const ciu = prev?.id_ciudad ?? null;
          (this.child.depCtrl as any)?.setValue?.(dep, { emitEvent: true });
          setTimeout(() => (this.child.ciuCtrl as any)?.setValue?.(ciu, { emitEvent: true }), 0);
        } catch (e) {
          console.warn('[PasoReferencias] Rehidratación parcial:', e);
        }
      }

      // 2) Track form + validez
      this.state.trackForm?.(this.STEP, form);
      this.state.markValid?.(this.STEP, form.valid);

      // 3) Snapshot continuo al payload
      this.subs.push(
        form.valueChanges.subscribe(() => {
          const snap = (form as any).getRawValue ? (form as any).getRawValue() : form.value;
          this.state.payload.update(curr => ({ ...curr, referencias: snap }));
          this.state.markValid?.(this.STEP, form.status === 'VALID');
        })
      );

      // 4) Saver del paso:
      //    - Si hay ID → persiste en API (inyecta FK al hijo) y retorna true/false según éxito.
      //    - Si NO hay ID → NO llama API; solo snapshot y retorna true (flujo no se bloquea).
      (this.state as any).registerSaver?.(this.STEP, async () => {
        const fk = this.getPersonaIdStrict();
        const snap = (form as any).getRawValue ? (form as any).getRawValue() : form.value;

        // Asegura snapshot antes de cualquier cosa
        this.state.payload.update(curr => ({ ...curr, referencias: snap }));

        if (!fk) {
          // No hay ID aún: no intentamos guardar; no bloqueamos la navegación.
          return true;
        }

        // Hay ID: inyectarlo al hijo y fijarlo en URL por consistencia
        this.child.idDatosPersonales.set(fk);
        await this.ensureIdInUrl(fk);

        // Llamar guardado real del hijo
        if (typeof this.child.saveFromParent === 'function') {
          return await this.child.saveFromParent(fk);
        }

        console.warn('[PasoReferencias] saveFromParent() no está implementado en el upsert.');
        return false;
      });

      // 5) Snapshot inicial
      const initial = (form as any).getRawValue ? (form as any).getRawValue() : form.value;
      this.state.payload.update(curr => ({ ...curr, referencias: initial }));
      this.state.markValid?.(this.STEP, form.valid);
    });
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
    const form: FormGroup | undefined = (this.child as any)?.form;
    if (form) {
      const snap = (form as any).getRawValue ? (form as any).getRawValue() : form.value;
      this.state.payload.update(curr => ({ ...curr, referencias: snap }));
    }
  }
}
