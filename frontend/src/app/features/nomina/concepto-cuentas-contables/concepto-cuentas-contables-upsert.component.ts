import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';

import {
  ConceptoCuentasContablesApi,
  ConceptoCuentaContableFormDTO
} from './concepto-cuentas-contables.api';

// 👉 Catálogo de conceptos nómina
import { ConceptosNominaApi } from '../conceptos-nomina/conceptos-nomina.api';

// 👉 Sesión (agencia activa)
import { SessionService } from '../../../core/auth/session.service';

// 👉 Cuentas contables (autocomplete)
import { CuentasApi, CuentaAutocompleteDTO } from '../../../shared/cuentas/cuentas.api';

import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  standalone: true,
  selector: 'app-concepto-cuentas-contables-upsert',
  templateUrl: './concepto-cuentas-contables-upsert.component.html',
  styleUrls: ['./concepto-cuentas-contables-upsert.component.scss'],
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule, // ✅ OBLIGATORIO para [formControl]
    HeaderActionsComponent
  ],
})
export class ConceptoCuentasContablesUpsertComponent implements OnInit {

  // =========================================================
  // INYECCIONES
  // =========================================================
  private readonly api = inject(ConceptoCuentasContablesApi);
  private readonly conceptosApi = inject(ConceptosNominaApi);
  private readonly cuentasApi = inject(CuentasApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  public session = inject(SessionService);

  // =========================================================
  // STATE
  // =========================================================
  idMapeo?: number;
  loading = false;
  guardando = false;

  // =========================================================
  // FORM (modelo simple)
  // =========================================================
  form: ConceptoCuentaContableFormDTO = {
    codigoConcepto: '',
    idAgencia: 0,
    idCuentaDebito: 0,
    idCuentaCredito: 0,
    activo: true
  };

  // =========================================================
  // FUENTES DE DATOS
  // =========================================================
  agencias: any[] = [];
  conceptos: any[] = [];

  // =========================================================
  // AUTOCOMPLETE CUENTAS
  // =========================================================
  ctaDebitoCtrl = new FormControl('');
  ctaCreditoCtrl = new FormControl('');

  cuentasDebito: CuentaAutocompleteDTO[] = [];
  cuentasCredito: CuentaAutocompleteDTO[] = [];

  // =========================================================
  // INIT
  // =========================================================
  ngOnInit(): void {

    // -------------------------------
    // AGENCIAS DESDE SESIÓN
    // -------------------------------
    this.agencias = this.session.getAgencias?.() ?? [];

    const agActiva = this.session.getAgenciaActiva?.();
    if (agActiva?.idAgencia) {
      this.form.idAgencia = agActiva.idAgencia;
      this.onChangeAgencia(agActiva.idAgencia);
    }

    // -------------------------------
    // CONCEPTOS NÓMINA
    // -------------------------------
    this.conceptosApi.listar().subscribe({
      next: d => this.conceptos = d ?? [],
      error: err => {
        console.error('Error cargando conceptos nómina', err);
        this.conceptos = [];
      }
    });

    // -------------------------------
    // AUTOCOMPLETE CUENTA DÉBITO
    // -------------------------------
    this.ctaDebitoCtrl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(texto => {

        const idAgencia = this.form.idAgencia;

        if (
          !idAgencia ||
          typeof texto !== 'string' ||
          texto.length < 2
        ) {
          return of([]);
        }

        return this.cuentasApi.buscar(idAgencia, texto);
      })
    ).subscribe(data => {
      this.cuentasDebito = data ?? [];
    });

    // -------------------------------
    // AUTOCOMPLETE CUENTA CRÉDITO
    // -------------------------------
    this.ctaCreditoCtrl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(texto => {

        const idAgencia = this.form.idAgencia;

        if (
          !idAgencia ||
          typeof texto !== 'string' ||
          texto.length < 2
        ) {
          return of([]);
        }

        return this.cuentasApi.buscar(idAgencia, texto);
      })
    ).subscribe(data => {
      this.cuentasCredito = data ?? [];
    });

    // -------------------------------
    // EDITAR
    // -------------------------------
    const id = this.route.snapshot.paramMap.get('idMapeo');
    if (id) {
      this.idMapeo = Number(id);
      this.cargar();
    }
  }

  // =========================================================
  // CARGAR (editar)
  // =========================================================
  cargar(): void {

    if (!this.idMapeo) return;

    this.loading = true;

    this.api.obtener(this.idMapeo).subscribe({
      next: (data) => {

        this.form = data;

        // =========================================
        // 👉 PRECARGAR CUENTA DÉBITO (EDICIÓN)
        // =========================================
        if (data.idCuentaDebito && (data as any).cuentaDebito) {
          this.ctaDebitoCtrl.setValue(
            (data as any).cuentaDebito,
            { emitEvent: false }
          );
        }

        // =========================================
        // 👉 PRECARGAR CUENTA CRÉDITO (EDICIÓN)
        // =========================================
        if (data.idCuentaCredito && (data as any).cuentaCredito) {
          this.ctaCreditoCtrl.setValue(
            (data as any).cuentaCredito,
            { emitEvent: false }
          );
        }

        this.loading = false;
      },

      error: (err) => {
        console.error('Error cargando mapeo contable', err);
        alert('No se pudo cargar el registro.');
        this.loading = false;
      }
    });
  }

  // =========================================================
  // CAMBIO DE AGENCIA
  // =========================================================
  onChangeAgencia(idAgencia: number): void {

    const id = Number(idAgencia);

    if (!id) {
      this.form.idCuentaDebito = 0;
      this.form.idCuentaCredito = 0;
      this.ctaDebitoCtrl.reset('', { emitEvent: false });
      this.ctaCreditoCtrl.reset('', { emitEvent: false });
      this.cuentasDebito = [];
      this.cuentasCredito = [];
      return;
    }

    this.form.idAgencia = id;

    // limpiar cuentas seleccionadas
    this.form.idCuentaDebito = 0;
    this.form.idCuentaCredito = 0;
    this.ctaDebitoCtrl.reset('', { emitEvent: false });
    this.ctaCreditoCtrl.reset('', { emitEvent: false });
  }

  // =========================================================
  // SELECCIONAR CUENTAS
  // =========================================================
  seleccionarCuentaDebito(c: CuentaAutocompleteDTO): void {

    const label = `${c.codigoCuenta} — ${c.nombreCuenta}`;

    this.form.idCuentaDebito = c.idCuenta;
    this.ctaDebitoCtrl.setValue(label, { emitEvent: false });
    this.cuentasDebito = [];
  }

  seleccionarCuentaCredito(c: CuentaAutocompleteDTO): void {

    const label = `${c.codigoCuenta} — ${c.nombreCuenta}`;

    this.form.idCuentaCredito = c.idCuenta;
    this.ctaCreditoCtrl.setValue(label, { emitEvent: false });
    this.cuentasCredito = [];
  }

  // =========================================================
  // GUARDAR
  // =========================================================
  guardar(): void {

    if (
      !this.form.idAgencia ||
      !this.form.codigoConcepto ||
      !this.form.idCuentaDebito ||
      !this.form.idCuentaCredito
    ) {
      alert('Debe seleccionar agencia, concepto y cuentas.');
      return;
    }

    this.guardando = true;

    const req$ = this.idMapeo
      ? this.api.actualizar(this.idMapeo, this.form)
      : this.api.crear(this.form);

    req$.subscribe({
      next: () => {
        this.guardando = false;
        this.volver();
      },
      error: (err) => {
        console.error('Error guardando mapeo contable', err);
        alert('No se pudo guardar.');
        this.guardando = false;
      }
    });
  }

  // =========================================================
  // VOLVER
  // =========================================================
  volver(): void {
    this.router.navigate(['/nomina/concepto-cuentas-contables']);
  }
}
