// src/app/features/hoja-vida/captura_global/captura-state.service.ts
import { Injectable, effect, signal } from '@angular/core';
import { FormGroup } from '@angular/forms';

export type CapturaPayload = {
  datosPersonales?: any;
  ubicaciones?: any;
  laborales?: any;
  financieros?: any;
  datosFamiliares?: any;
  referencias?: any;
  sarlaft?: any;
  permisos?: any;
};

const KEY = 'capturaHV_state';

function uuid(): string {
  // UUID v4 simple (sin dependencias)
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = (crypto.getRandomValues(new Uint8Array(1))[0] & 15) >> 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

type SaverFn = () => Promise<boolean>;

@Injectable({ providedIn: 'root' })
export class CapturaState {
  // Índice del paso actual
  step = signal<number>(0);

  // Identificador de sesión de captura (para mostrar en el shell)
  capturaId = signal<string>(uuid());

  // Mapa de validez por paso (ej: {0:true, 1:false, ...})
  stepValid = signal<Record<number, boolean>>({});

  // Payload global (buffer local por paso mientras el usuario escribe)
  payload = signal<CapturaPayload>({});

  // Referencias a formularios por paso (opcional)
  private forms: Record<number, FormGroup | undefined> = {};

  // 👉 NUEVO: registro de “savers” (acciones de guardado por paso)
  private savers = new Map<number, SaverFn>();

  constructor() {
    // Rehidratación desde sessionStorage
    try {
      const raw = sessionStorage.getItem(KEY);
      if (raw) {
        const obj = JSON.parse(raw);
        if (typeof obj?.step === 'number') this.step.set(obj.step);
        if (obj?.capturaId) this.capturaId.set(obj.capturaId);
        if (obj?.payload) this.payload.set(obj.payload);
        if (obj?.stepValid) this.stepValid.set(obj.stepValid);
      }
    } catch {
      // no-op
    }

    // Persistencia automática en sessionStorage
    effect(() => {
      sessionStorage.setItem(
        KEY,
        JSON.stringify({
          step: this.step(),
          capturaId: this.capturaId(),
          payload: this.payload(),
          stepValid: this.stepValid(),
        }),
      );
    });
  }

  setStep(n: number) {
    this.step.set(n);
  }

  markValid(i: number, valid: boolean) {
    const m = { ...this.stepValid() };
    m[i] = !!valid;
    this.stepValid.set(m);
  }

  mergePayload(partial: Partial<CapturaPayload>) {
    this.payload.set({ ...this.payload(), ...partial });
  }

  /**
   * Registra un formulario para un paso y sincroniza su validez.
   * A prueba de nulos: si el form aún no existe, marca el paso como inválido y no se suscribe.
   */
  trackForm(stepIndex: number, form: FormGroup | undefined | null) {
    const hasStatus$ = !!(form as any)?.statusChanges?.subscribe;
    const hasValue$ = !!(form as any)?.valueChanges?.subscribe;
    if (!form || !hasStatus$ || !hasValue$) {
      this.markValid(stepIndex, false);
      return;
    }

    this.forms[stepIndex] = form;

    // Validez inicial
    this.markValid(stepIndex, form.valid);

    // Sincroniza validez al cambiar estado del form
    form.statusChanges.subscribe(() => {
      this.markValid(stepIndex, form.valid);
    });

    // (Opcional) valueChanges si quisieras mergear aquí
    form.valueChanges.subscribe(() => {
      // noop — cada paso decide cuándo mergear al payload
    });
  }

  // 👉 NUEVO: registrar la función de guardado para un paso (p.ej. Paso 6)
  registerSaver(stepIndex: number, saver: SaverFn) {
    this.savers.set(stepIndex, saver);
  }

  // 👉 NUEVO: ejecutar (y esperar) el guardado del paso actual antes de avanzar
  async runSaver(stepIndex: number): Promise<boolean> {
    const saver = this.savers.get(stepIndex);
    if (!saver) return true; // si no hay saver para ese paso, no bloquea
    try {
      const ok = await saver();
      return !!ok;
    } catch {
      return false;
    }
  }

  reset() {
    this.step.set(0);
    this.capturaId.set(uuid());
    this.payload.set({});
    this.stepValid.set({});
    this.forms = {};
    this.savers.clear(); // ← limpia savers registrados
    sessionStorage.removeItem(KEY);
  }
}
