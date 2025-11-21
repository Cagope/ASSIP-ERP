import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { FormasAhorroApi, FormaAhorro } from './formas-ahorro.api';

@Component({
  selector: 'app-formas-ahorro-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './formas-ahorro-upsert.component.html',
  styleUrls: ['./formas-ahorro-upsert.component.scss']
})
export class FormasAhorroUpsertComponent implements OnInit {

  private readonly api = inject(FormasAhorroApi);
  private readonly fb = inject(FormBuilder);
  readonly route = inject(ActivatedRoute);
  readonly router = inject(Router);

  form = this.fb.group({
    codigoForma: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(2)]],
    nombreForma: ['', [Validators.required]],

    consecutivoForma: [null as number | null],
    tipoCaptacion: [null as string | null],
    tiempoLiquidacion: [null as number | null],

    cuentaFormaCorto: [null as number | null],
    cuentaFormaLargo: [null as number | null],
    cuentaGasto: [null as number | null],
    cuentaCxpForma: [null as number | null],
    cuentaGmfForma: [null as number | null],

    tipoInteresForma: [null as number | null],
    fechaUltimaLiquidacion: [null as string | null],

    autorizadoForma: [false as boolean | null],

    documentoForma: [null as string | null],
    periodoGracia: [null as number | null],
    valorMinimo: [null as number | null],
    tasaInteresForma: [null as number | null]
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.api.obtener(+id).subscribe((data: FormaAhorro) => {
        this.form.patchValue({
          codigoForma: data.codigoForma,
          nombreForma: data.nombreForma,

          consecutivoForma: data.consecutivoForma ?? null,
          tipoCaptacion: data.tipoCaptacion ?? null,
          tiempoLiquidacion: data.tiempoLiquidacion ?? null,

          cuentaFormaCorto: data.cuentaFormaCorto ?? null,
          cuentaFormaLargo: data.cuentaFormaLargo ?? null,
          cuentaGasto: data.cuentaGasto ?? null,
          cuentaCxpForma: data.cuentaCxpForma ?? null,
          cuentaGmfForma: data.cuentaGmfForma ?? null,

          tipoInteresForma: data.tipoInteresForma ?? null,
          fechaUltimaLiquidacion: data.fechaUltimaLiquidacion ?? null,
          autorizadoForma: data.autorizadoForma ?? false,

          documentoForma: data.documentoForma ?? null,
          periodoGracia: data.periodoGracia ?? null,
          valorMinimo: data.valorMinimo ?? null,
          tasaInteresForma: data.tasaInteresForma ?? null
        });
      });
    }
  }

  guardar(): void {
    if (this.form.invalid) return;

    const payload = this.form.value as FormaAhorro;
    const id = this.route.snapshot.paramMap.get('id');

    const req = id
      ? this.api.actualizar(+id, payload)
      : this.api.crear(payload);

    req.subscribe(() => {
      this.router.navigate(['/depositos/formas-ahorro']);
    });
  }

  cancelar(): void {
    this.router.navigate(['/depositos/formas-ahorro']);
  }
}
