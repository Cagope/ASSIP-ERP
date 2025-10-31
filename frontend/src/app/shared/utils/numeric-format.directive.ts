import { Directive, ElementRef, HostListener, Renderer2, Optional } from '@angular/core';
import { NgControl } from '@angular/forms';

/* ============================================================================
   💡 Directiva Global — numericFormat
   ---------------------------------------------------------------------------
   ERP ASSIP — Formato numérico con separador de miles (en-US)
   ---------------------------------------------------------------------------
   ✅ Características:
     • Muestra formato de miles al perder el foco (1,234,567.89)
     • Mantiene el valor numérico puro en el FormControl (1234567.89)
     • Selecciona todo el texto al enfocar (sin borrar)
     • Soporta decimales y negativos
     • Sin conflictos con Angular ReactiveForms
   ============================================================================ */

@Directive({
  selector: '[numericFormat]',
  standalone: true,
})
export class NumericFormatDirective {
  private readonly locale = 'en-US'; // miles = ',', decimal = '.'

  constructor(
    private el: ElementRef<HTMLInputElement>,
    private renderer: Renderer2,
    @Optional() private control?: NgControl
  ) {}

  // 🎯 Al enfocar → selecciona todo (sin limpiar)
  @HostListener('focus')
  onFocus(): void {
    const input = this.el.nativeElement;
    setTimeout(() => input.select(), 0);
  }

  // ⌨️ Al escribir → acepta solo caracteres válidos
  @HostListener('input', ['$event'])
  onInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const clean = input.value.replace(/[^0-9.,-]/g, '');
    if (clean !== input.value) this.renderer.setProperty(input, 'value', clean);

    const num = this.parseToNumber(clean);
    if (this.control?.control) this.control.control.setValue(num, { emitEvent: false });
  }

  // ✅ Al perder el foco → aplica formato visual persistente
  @HostListener('blur')
  onBlur(): void {
    const input = this.el.nativeElement;
    const raw = input.value.trim();
    if (!raw) return;

    const num = this.parseToNumber(raw);
    if (isNaN(num)) return;

    const formatted = new Intl.NumberFormat(this.locale, {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    }).format(num);

    // Aplica formato visible (mantiene lo que ve el usuario)
    this.renderer.setProperty(input, 'value', formatted);

    // Forzar a que Angular no reescriba inmediatamente
    setTimeout(() => {
      if (this.control?.control) {
        this.control.control.markAsPristine();
        this.control.control.markAsUntouched();
      }
    });
  }

  // 🔢 Conversión texto → número
  private parseToNumber(value: string): number {
    if (!value) return NaN;
    const normalized = value.replace(/,/g, ''); // elimina separadores
    return parseFloat(normalized);
  }
}
