import {
  Component,
  forwardRef,
  inject
} from '@angular/core';

import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR
} from '@angular/forms';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  NominaBusquedaApi,
  NominaEmpleadoBusquedaDTO
} from './nomina-busqueda.api';

@Component({
  standalone: true,
  selector: 'app-empleado-autocomplete',
  templateUrl: './empleado-autocomplete.component.html',
  styleUrls: ['./empleado-autocomplete.component.scss'],
  imports: [CommonModule, FormsModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => EmpleadoAutocompleteComponent),
      multi: true
    }
  ]
})
export class EmpleadoAutocompleteComponent
  implements ControlValueAccessor {

  private readonly api = inject(NominaBusquedaApi);

  // =====================================================
  // 🔹 Valor interno (idEmpleado)
  // =====================================================

  private value: number | null = null;

  // =====================================================
  // 🔹 Angular callbacks
  // =====================================================

  private onChange: (value: number | null) => void = () => {};
  private onTouched: () => void = () => {};

  disabled = false;

  // =====================================================
  // UI
  // =====================================================

  q = '';
  resultados: NominaEmpleadoBusquedaDTO[] = [];
  loading = false;

  // =====================================================
  // BUSCAR
  // =====================================================

  buscar(): void {

    const texto = this.q.trim();

    if (texto.length < 2) {
      this.resultados = [];
      return;
    }

    this.loading = true;

    this.api.buscarEmpleados(texto)
      .subscribe({
        next: data => {
          this.resultados = data ?? [];
          this.loading = false;
        },
        error: () => {
          this.resultados = [];
          this.loading = false;
        }
      });
  }

  // =====================================================
  // SELECCIONAR
  // =====================================================

  seleccionar(x: NominaEmpleadoBusquedaDTO): void {

    this.value = x.idEmpleado;

    this.q = `${x.documento} — ${x.nombreCompleto}`;
    this.resultados = [];

    this.onChange(this.value);
    this.onTouched();
  }

  limpiar(): void {
    this.value = null;
    this.q = '';
    this.resultados = [];

    this.onChange(null);
  }

  // =====================================================
  // CONTROL VALUE ACCESSOR
  // =====================================================

  writeValue(value: number | null): void {

    this.value = value;

    if (value == null) {
      this.q = '';
      return;
    }

    this.api.buscarEmpleadoPorId(value)
      .subscribe({
        next: x => {
          if (!x) {
            this.q = '';
            return;
          }

          this.q = `${x.documento} — ${x.nombreCompleto}`;
        },
        error: () => {
          this.q = '';
        }
      });
  }
  registerOnChange(fn: (value: number | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }
}
