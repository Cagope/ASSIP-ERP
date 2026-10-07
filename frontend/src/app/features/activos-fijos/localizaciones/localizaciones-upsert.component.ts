import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import {
  LocalizacionesApi,
  LocalizacionFormDTO,
  LocalizacionSaveDTO
} from './localizaciones.api';

import { GeneralApi } from '../../../shared/general/general.api';
import { SessionService } from '../../../core/auth/session.service';

@Component({
  selector: 'app-localizaciones-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './localizaciones-upsert.component.html',
  styleUrls: ['./localizaciones-upsert.component.scss']
})
export class LocalizacionesUpsertComponent implements OnInit {

  // =========================================================
  // INYECCIONES
  // =========================================================
  private readonly api = inject(LocalizacionesApi);
  private readonly generalApi = inject(GeneralApi);
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
  // DATOS
  // =========================================================
  agencias: any[] = [];

  // =========================================================
  // FORMULARIO
  // =========================================================
  form = this.fb.group({
    nombre: ['', Validators.required],
    telefono: [null as string | null],
    idAgencia: [null as number | null, Validators.required]
  });

  // =========================================================
  // INIT
  // =========================================================
  ngOnInit(): void {

    this.generalApi.listarAgencias().subscribe(data => {
      this.agencias = data;
    });

    const id = this.route.snapshot.paramMap.get('id');

    if (!id) return;

    this.api.obtener(+id).subscribe((data: LocalizacionFormDTO) => {
      this.form.patchValue(data);
    });
  }

  // =========================================================
  // ACCIONES
  // =========================================================
  guardar(): void {

    if (!this.puedeEditar) return;

    if (this.form.invalid) return;

    const payload = this.form.value as LocalizacionSaveDTO;
    const id = this.route.snapshot.paramMap.get('id');

    const req = id
      ? this.api.actualizar(+id, payload)
      : this.api.crear(payload);

    req.subscribe(() => {
      this.router.navigate(['/activos-fijos/localizaciones']);
    });
  }

  cancelar(): void {
    this.router.navigate(['/activos-fijos/localizaciones']);
  }
}
