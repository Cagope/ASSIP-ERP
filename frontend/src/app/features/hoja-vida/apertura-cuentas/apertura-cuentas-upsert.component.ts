import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';

import { AperturaCuentasApi } from './apertura-cuentas.api';
import { SessionService } from '../../../core/auth/session.service';

@Component({
  selector: 'app-apertura-cuentas-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './apertura-cuentas-upsert.component.html',
  styleUrls: ['./apertura-cuentas-upsert.component.scss']
})
export class AperturaCuentasUpsertComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly api = inject(AperturaCuentasApi);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  editando = false;
  mensajeBloqueo = '';

  idDatosPersonal!: number;
  idFormaAhorroInicial: number | null = null;

  formas: any[] = [];
  formaAportes: any | null = null;
  formasOpcionales: any[] = [];
  codigoAportes: string | null = null;

  get esAportesSeleccionado(): boolean {
    const id = this.form.get('idFormaAhorro')?.value;
    const f = this.formas.find(x => x.idFormaAhorro === id);
    return !!f && f.codigoForma === '01';
  }

  form: FormGroup = this.fb.group({
    idFormaAhorro: [null as number | null, Validators.required],
    gmf: ['S', Validators.required],
    retencion: [false],

    documentoApoderadoAportes: [''],
    nombreApoderadoAportes: [''],
    telefonoApoderadoAportes: [''],
    celularApoderadoAportes: [''],

    documentoApoderadoAhorro: [''],
    nombreApoderadoAhorro: [''],
    telefonoApoderadoAhorro: [''],
    celularApoderadoAhorro: ['']
  });

  ngOnInit(): void {

    this.route.queryParams.subscribe(params => {
      this.idDatosPersonal = +params['idDatosPersonal'];
      this.idFormaAhorroInicial = params['idFormaAhorro']
        ? +params['idFormaAhorro']
        : null;

      this.cargarFormas();
    });

    this.form.get('idFormaAhorro')?.valueChanges.subscribe(v => {
      this.aplicarReglasForma(v);
    });
  }

  aplicarReglasForma(idForma: number | null): void {
    if (!idForma) return;

    const forma = this.formas.find(f => f.idFormaAhorro === idForma);
    if (!forma) return;

    const esAportes = forma.codigoForma === '01';

    if (esAportes) {

      this.form.patchValue({ gmf: 'N', retencion: false });
      this.form.get('gmf')?.disable({ emitEvent: false });
      this.form.get('retencion')?.disable({ emitEvent: false });

      this.form.get('documentoApoderadoAportes')?.setValidators([Validators.required]);
      this.form.get('nombreApoderadoAportes')?.setValidators([Validators.required]);

    } else {
      this.form.get('gmf')?.enable({ emitEvent: false });
      this.form.get('retencion')?.enable({ emitEvent: false });

      this.form.get('documentoApoderadoAportes')?.clearValidators();
      this.form.get('nombreApoderadoAportes')?.clearValidators();
    }

    this.form.get('documentoApoderadoAportes')?.updateValueAndValidity({ emitEvent:false });
    this.form.get('nombreApoderadoAportes')?.updateValueAndValidity({ emitEvent:false });
  }

  cargarFormas(): void {

    const agenciaActiva = this.session.getAgenciaActiva()?.idAgencia;

    this.api.listarFormas(this.idDatosPersonal).subscribe({
      next: lista => {

        // ⚠ NO bloquear si hay lista
        if (!lista) {
          this.mensajeBloqueo =
            '⚠ Error cargando formas de apertura.';
          return;
        }

        this.formas = lista ?? [];

        // Aportes SIEMPRE existe visualmente
        this.formaAportes = lista.find(f => f.codigoForma === '01') || null;

        // Opcionales SÍ se filtran por agencia
        this.formasOpcionales = lista.filter(f =>
          ['02','03','05','06'].includes(f.codigoForma)
          && Number(f.idAgencia) === Number(agenciaActiva)
        );

        // Mostrar consecutivo de aportes aunque no pertenezca a la agencia
        if (this.formaAportes) {
          this.codigoAportes = String(this.formaAportes.consecutivo).padStart(10, '0');
        }

        // AUTOCARGA solo si pertenece a agencia
        if (this.formaAportes &&
            Number(this.formaAportes.idAgencia) === Number(agenciaActiva) &&
            this.idFormaAhorroInicial == null) {
          this.form.patchValue({ idFormaAhorro: this.formaAportes.idFormaAhorro });
          this.aplicarReglasForma(this.formaAportes.idFormaAhorro);
        }

        // Caso edición
        if (this.idFormaAhorroInicial !== null) {
          this.form.patchValue({ idFormaAhorro: this.idFormaAhorroInicial });
          this.aplicarReglasForma(this.idFormaAhorroInicial);
          this.editando = true;
        }
      },
      error: () => console.error('❌ Error cargando formas')
    });
  }

  guardar(): void {
    if (!this.formas.length) {
      alert('No se puede procesar: reglas impiden apertura.');
      return;
    }

    if (this.form.invalid) return;

    const agenciaActiva = this.session.getAgenciaActiva();

    const dto = {
      idDatosPersonal: this.idDatosPersonal,
      idAgenciaUsuario: agenciaActiva?.idAgencia ?? null,
      ...this.form.getRawValue()
    };

    this.api.crear(dto).subscribe({
      next: () => {
        alert('✔ Cuenta procesada correctamente');
        this.router.navigate(['/hoja-vida/apertura-cuentas']);
      },
      error: err => {
        console.error('❌ Error guardando cuenta', err);
        alert('Ocurrió un error al guardar la cuenta.');
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/hoja-vida/apertura-cuentas']);
  }
}
