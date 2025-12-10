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
  codigoFormaConsecutivo: string | null = null;
  idFormaSeleccionada: number | null = null;
  nombreFormaSeleccionada: string | null = null;

  // ✔ Catálogo GMF — mismo formato del módulo guía
  tiposGmf = [
    { codigoTipoGmf: 'S', descripcionTiposGmf: 'Sí' },
    { codigoTipoGmf: 'N', descripcionTiposGmf: 'No' },
    { codigoTipoGmf: 'U', descripcionTiposGmf: 'Carta Única' }
  ];

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

    // 1️⃣ Escuchar SIEMPRE cambios del combo
    this.form.get('idFormaAhorro')?.valueChanges.subscribe(id => {

        id = Number(id); // ⭐ CORRECCIÓN: asegurar número

        this.idFormaSeleccionada = id;

        const forma = this.formas.find(f => Number(f.idFormaAhorro) === id);

        this.nombreFormaSeleccionada = forma?.nombreForma ?? null;

        if (forma && forma.codigoForma !== '01') {
          const consecutivo =
            forma.consecutivo ??
            forma.consecutivoForma ??
            forma.siguienteConsecutivo ??
            null;

          this.codigoFormaConsecutivo = consecutivo
            ? consecutivo.toString().padStart(10, '0')
            : null;
        } else {
          this.codigoFormaConsecutivo = null;
        }

        this.aplicarReglasForma(id);
    });

    // 2️⃣ Luego procesamos agencias y cargamos formas
    let agencia = this.session.getAgenciaActiva();

    this.route.queryParams.subscribe(params => {

      if (!agencia || !agencia.idAgencia) {
        const idAg = Number(params['idAgencia'] ?? 0);
        if (idAg > 0 && agencia) {
          agencia = {
            idAgencia: idAg,
            codigoAgencia: agencia.codigoAgencia,
            nombreAgencia: agencia.nombreAgencia
          };
        }
      }

      if (!agencia || !agencia.idAgencia) {
        this.mensajeBloqueo = "No se pudo determinar la agencia activa.";
        return;
      }

      this.idDatosPersonal = Number(params['idDatosPersonal']);
      this.idFormaAhorroInicial = params['idFormaAhorro']
        ? Number(params['idFormaAhorro'])
        : null;

      this.cargarFormas();
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

    // 🔹 Mostrar consecutivo cuando NO es aportes
    if (!esAportes) {

      const consecutivo =
        forma.consecutivo ??
        forma.consecutivoForma ??      // muchos DTO lo traen así
        forma.siguienteConsecutivo ??   // otros así
        null;

      this.codigoFormaConsecutivo = consecutivo
        ? consecutivo.toString().padStart(10, '0')
        : null;

    } else {
      this.codigoFormaConsecutivo = null;
    }

  }

  cargarFormas(): void {

    const agenciaActiva = Number(this.session.getAgenciaActiva()?.idAgencia ?? 0);

    this.api.listarFormas(this.idDatosPersonal, agenciaActiva).subscribe({
      next: lista => {

        if (!lista || !lista.length) {
          this.mensajeBloqueo = '⚠ No hay formas de ahorro configuradas.';
          return;
        }

        // TODAS las formas del backend
        this.formas = lista;

        // Forma aportes
        this.formaAportes = lista.find(f => f.codigoForma === '01') || null;

        // Filtrar opcionales por agencia
        this.formasOpcionales = lista.filter(f =>
          f.codigoForma !== '01' &&
          Number(f.idAgencia) === agenciaActiva
        );

        // Código aportes
        if (this.formaAportes) {
          this.codigoAportes = String(this.formaAportes.consecutivo)
            .padStart(10, '0');
        }

        // Autoselección aportes
        if (this.formaAportes &&
            Number(this.formaAportes.idAgencia) === agenciaActiva &&
            this.idFormaAhorroInicial == null) {

          this.form.patchValue({ idFormaAhorro: this.formaAportes.idFormaAhorro });
          this.aplicarReglasForma(this.formaAportes.idFormaAhorro);
        }

        // Modo edición
        if (this.idFormaAhorroInicial !== null) {
          this.form.patchValue({ idFormaAhorro: this.idFormaAhorroInicial });
          this.aplicarReglasForma(this.idFormaAhorroInicial);
          this.editando = true;
        }
      },

      error: () => {
        console.error('❌ Error cargando formas');
        this.mensajeBloqueo = '⚠ Error cargando formas de apertura.';
      }
    });
  }

  guardar(): void {
    if (!this.formas.length) {
      alert('No se puede procesar: reglas impiden apertura.');
      return;
    }

    if (this.form.invalid) return;

    const agenciaActiva = this.session.getAgenciaActiva();
    const usuarioId = this.session.getUsuarioId();

    if (!usuarioId) {
      alert('No se pudo obtener el usuario. Cierre sesión e ingrese de nuevo.');
      return;
    }

    const dto = {
      idDatosPersonal: this.idDatosPersonal,
      idAgenciaUsuario: agenciaActiva?.idAgencia ?? null,
      usuarioId,
      ...this.form.getRawValue()
    };

    this.api.crear(dto).subscribe({
      next: res => {
        alert(res?.mensaje ?? '✔ Cuenta procesada correctamente');
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
