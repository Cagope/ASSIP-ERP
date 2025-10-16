// src/app/features/hoja-vida/captura_global/pasos/paso-resumen.component.ts
import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CapturaState } from '../captura-state.service';

@Component({
  standalone: true,
  selector: 'app-paso-resumen',
  imports: [CommonModule],
  template: `
    <h3>Resumen</h3>
    <pre>{{ payload() | json }}</pre>
  `
})
export class PasoResumenComponent {
  private state = inject(CapturaState);
  payload = computed(() => this.state.payload());
}
