import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ParametrosApi } from './parametros.api';
import { AgenciasApi, Agencia } from '../agencias/agencia.api';

@Component({
  selector: 'app-parametro-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './parametro-upsert.component.html',
  styleUrls: ['./parametro-upsert.component.scss']
})
export class ParametroUpsertComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ParametrosApi);
  private readonly agenciasApi = inject(AgenciasApi);
  readonly route = inject(ActivatedRoute);
  readonly router = inject(Router);

  agencias: Agencia[] = [];

  form = this.fb.group({
    idAgencia: [null as number | null, Validators.required],
    codigoParametro: [
      null as number | null,
      [
        Validators.required,
        Validators.min(1),
        Validators.max(2147483647) // 🔹 evita error de rango en backend (int)
      ]
    ],
    nombreParametro: ['', [Validators.required, Validators.maxLength(100)]],
    valorParametro: [
      0,
      [
        Validators.required,
        Validators.min(0),
        Validators.max(999999999.99) // 🔹 tope razonable para valores numéricos
      ]
    ],
    tipoValor: [false, Validators.required]
  });

  ngOnInit(): void {
    this.cargarAgencias();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.api.obtener(+id).subscribe((data) => {
        if (data) {
          this.form.patchValue({
            idAgencia: data.idAgencia,
            codigoParametro: data.codigoParametro,
            nombreParametro: data.nombreParametro,
            valorParametro: data.valorParametro,
            tipoValor: data.tipoValor
          });
        }
      });
    }
  }

  cargarAgencias(): void {
    this.agenciasApi.listar().subscribe({
      next: (data) => (this.agencias = data),
      error: () => alert('Error al cargar agencias.')
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      console.warn('⚠️ Formulario inválido:', this.form.value);
      return;
    }

    const value = this.form.value;
    const payload = {
      idAgencia: Number(value.idAgencia),
      codigoParametro: Number(value.codigoParametro),
      nombreParametro: value.nombreParametro!,
      valorParametro: Number(value.valorParametro),
      tipoValor: value.tipoValor ?? false
    };

    console.log('📦 Payload enviado:', payload);

    const id = this.route.snapshot.paramMap.get('id');
    const request = id
      ? this.api.actualizar(+id, payload)
      : this.api.crear(payload);

    request.subscribe(() => this.router.navigate(['/general/parametros']));
  }
}
