import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import {
  BloquesApi,
  BloqueFormDTO,
  BloqueSaveDTO
} from './bloques.api';

import { SessionService } from '../../../core/auth/session.service';

@Component({
  selector: 'app-bloques-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './bloques-upsert.component.html',
  styleUrls: ['./bloques-upsert.component.scss']
})
export class BloquesUpsertComponent implements OnInit {

  // =========================================================
  // INYECCIONES
  // =========================================================
  private readonly api = inject(BloquesApi);
  private readonly fb = inject(FormBuilder);
  private readonly session = inject(SessionService);

  readonly route = inject(ActivatedRoute);
  readonly router = inject(Router);

  // =========================================================
  // PERMISOS
  // =========================================================
  readonly puedeEditar =
    this.session.tienePermiso('ACTIVOS_FIJOS_EDIT');

  // =========================================================
  // FORMULARIO
  // =========================================================
  form = this.fb.group({
    codigoBloque: ['', Validators.required],
    nombreBloque: ['', Validators.required],
    mesesDepreciacionDefecto: [0, Validators.required]
  });

  // =========================================================
  // INIT
  // =========================================================
  ngOnInit(): void {

    const id = this.route.snapshot.paramMap.get('id');

    if (!id) return;

    this.api.obtener(+id).subscribe((data: BloqueFormDTO) => {
      this.form.patchValue(data);
    });
  }

  // =========================================================
  // ACCIONES
  // =========================================================
  guardar(): void {

    if (!this.puedeEditar) return;

    if (this.form.invalid) return;

    const payload: BloqueSaveDTO =
      this.form.value as BloqueSaveDTO;

    const id = this.route.snapshot.paramMap.get('id');

    const req = id
      ? this.api.actualizar(+id, payload)
      : this.api.crear(payload);

    req.subscribe(() => {
      this.router.navigate(['/activos-fijos/bloques']);
    });
  }

  cancelar(): void {
    this.router.navigate(['/activos-fijos/bloques']);
  }
}
