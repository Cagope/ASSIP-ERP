import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import {
  BloquesApi,
  BloqueFormDTO,
  BloqueSaveDTO
} from './bloques.api';

@Component({
  selector: 'app-bloques-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './bloques-upsert.component.html',
  styleUrls: ['./bloques-upsert.component.scss']
})
export class BloquesUpsertComponent implements OnInit {

  private api = inject(BloquesApi);
  private fb = inject(FormBuilder);

  route = inject(ActivatedRoute);
  router = inject(Router);

  form = this.fb.group({
    codigoBloque: ['', Validators.required],
    nombreBloque: ['', Validators.required],
    mesesDepreciacionDefecto: [0, Validators.required]
  });

  ngOnInit(): void {

    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;

    this.api.obtener(+id).subscribe((data: BloqueFormDTO) => {
      this.form.patchValue(data);
    });
  }

  guardar(): void {
    if (this.form.invalid) return;

    const payload: BloqueSaveDTO = this.form.value as BloqueSaveDTO;
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
