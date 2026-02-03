import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import {
  TiposComprobantesApi,
  TipoComprobante
} from './tipos-comprobantes.api';
import { AgenciasApi, Agencia } from '../../general/agencias/agencia.api';

import { SessionService } from '../../../core/auth/session.service';


@Component({
  standalone: true,
  selector: 'app-tipos-comprobantes-upsert',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './tipos-comprobantes-upsert.component.html'
})
export class TiposComprobantesUpsertComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly api = inject(TiposComprobantesApi);
  private readonly agenciasApi = inject(AgenciasApi);
  readonly route = inject(ActivatedRoute);
  readonly router = inject(Router);
  private readonly session = inject(SessionService);


  agencias: Agencia[] = [];

  form = this.fb.group({
    tipoComprobante: ['', [Validators.required, Validators.maxLength(5)]],
    idAgencia: [null as number | null, Validators.required],
    nombreTipoComprobante: ['', [Validators.required, Validators.maxLength(100)]],
    cscComprobante: [0, [Validators.required, Validators.min(0)]],
    comprobanteActivo: [true]
  });

  ngOnInit(): void {
    this.cargarAgencias();

    const tipo = this.route.snapshot.paramMap.get('tipo');
    const idAgencia = this.route.snapshot.paramMap.get('idAgencia');

    if (tipo && idAgencia) {
      this.api.obtener(tipo, +idAgencia).subscribe({
        next: (data) => {
          this.form.patchValue(data);
          this.form.get('tipoComprobante')?.disable();
          this.form.get('idAgencia')?.disable();
        }
      });
    }
  }

  cargarAgencias(): void {
    this.agenciasApi.listar().subscribe({
      next: (data) => (this.agencias = data)
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      return;
    }

    const raw = this.form.getRawValue();

    const payload: TipoComprobante = {
      tipoComprobante: raw.tipoComprobante!,
      idAgencia: Number(raw.idAgencia),
      nombreTipoComprobante: raw.nombreTipoComprobante!,
      cscComprobante: raw.cscComprobante!,
      comprobanteActivo: raw.comprobanteActivo ?? true,
    };

    const tipo = this.route.snapshot.paramMap.get('tipo');

    const request = tipo
      ? this.api.actualizar(payload)
      : this.api.crear(payload);

    request.subscribe({
      next: () => this.router.navigate(['/contabilidad/tipos-comprobantes'])
    });
  }

  cancelar(): void {
    this.router.navigate(['/contabilidad/tipos-comprobantes']);
  }
}
