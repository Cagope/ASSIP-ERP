import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import {
  LocalizacionesApi,
  LocalizacionFormDTO,
  LocalizacionSaveDTO
} from './localizaciones.api';

import { GeneralApi } from '../../../shared/general/general.api';

@Component({
  selector: 'app-localizaciones-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './localizaciones-upsert.component.html',
  styleUrls: ['./localizaciones-upsert.component.scss']
})
export class LocalizacionesUpsertComponent implements OnInit {

  private api = inject(LocalizacionesApi);
  private generalApi = inject(GeneralApi);
  private fb = inject(FormBuilder);

  route = inject(ActivatedRoute);
  router = inject(Router);

  agencias: any[] = [];

  form = this.fb.group({
    nombre: ['', Validators.required],
    telefono: [null as string | null],
    idAgencia: [null as number | null, Validators.required]
  });

  ngOnInit(): void {

    // 🔽 cargar agencias
    this.generalApi.listarAgencias().subscribe(data => {
      this.agencias = data;
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;

    this.api.obtener(+id).subscribe((data: LocalizacionFormDTO) => {
      this.form.patchValue(data);
    });
  }

  guardar(): void {
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
