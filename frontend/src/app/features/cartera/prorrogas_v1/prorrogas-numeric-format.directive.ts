import { Directive, ElementRef, HostListener, Optional, Renderer2 } from '@angular/core';
import { NgControl } from '@angular/forms';

/** Formato monetario colombiano exclusivo de Prórrogas V1. */
@Directive({ selector: '[prorrogasNumericFormat]', standalone: true })
export class ProrrogasNumericFormatDirective {
  constructor(
    private readonly element: ElementRef<HTMLInputElement>,
    private readonly renderer: Renderer2,
    @Optional() private readonly ngControl?: NgControl
  ) {}

  /** Selecciona el contenido al ingresar, como en Pago de Cuotas. */
  @HostListener('focus')
  onFocus(): void {
    setTimeout(() => this.element.nativeElement.select(), 0);
  }

  /** Acepta dígitos y separadores colombianos sin reformatear mientras se digita. */
  @HostListener('input', ['$event'])
  onInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const limpio = input.value.replace(/[^0-9.,-]/g, '');
    if (limpio !== input.value) this.renderer.setProperty(input, 'value', limpio);
    const valor = this.convertir(limpio);
    if (this.ngControl?.control) {
      this.ngControl.control.setValue(valor, { emitEvent: false });
    }
  }

  /** Presenta puntos de miles al salir del campo. */
  @HostListener('blur')
  onBlur(): void {
    const input = this.element.nativeElement;
    if (!input.value.trim()) return;
    const valor = this.convertir(input.value);
    if (!Number.isFinite(valor)) return;
    this.renderer.setProperty(input, 'value', new Intl.NumberFormat('es-CO', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    }).format(valor));
  }

  /** Los puntos son miles; la coma representa los decimales. */
  private convertir(texto: string): number {
    if (!texto.trim()) return 0;
    const normalizado = texto.replace(/\./g, '').replace(',', '.');
    const numero = Number(normalizado);
    return Number.isFinite(numero) ? numero : NaN;
  }
}
