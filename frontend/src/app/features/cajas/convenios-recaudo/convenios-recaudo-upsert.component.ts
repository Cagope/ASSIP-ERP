import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  ReactiveFormsModule,
  FormBuilder,
  Validators
} from '@angular/forms';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  ConvenioRecaudo,
  ConvenioRecaudoCuenta,
  ConvenioRecaudoBusqueda,
  ConveniosRecaudoApi
} from './convenios-recaudo.api';

import {
  VincularCajaApi,
  CajaProvisionActivaDTO
} from '../provisiones/vincular-caja/vincular-caja.api';

@Component({
  selector: 'app-convenios-recaudo-upsert',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
  ],
  templateUrl: './convenios-recaudo-upsert.component.html',
  styleUrls: ['./convenios-recaudo-upsert.component.scss']
})
export class ConveniosRecaudoUpsertComponent implements OnInit {

  private readonly fb =
    inject(FormBuilder);

  private readonly api =
    inject(ConveniosRecaudoApi);

  private readonly cajaApi = inject(VincularCajaApi);

  readonly route =
    inject(ActivatedRoute);

  readonly router =
    inject(Router);

  editando = false;
  cargando = false;
  buscando = false;

  cuentas: ConvenioRecaudoCuenta[] = [];
  convenioActual: ConvenioRecaudo | null = null;

  provisionActiva: CajaProvisionActivaDTO | null = null;
  busqueda: ConvenioRecaudoBusqueda | null = null;

  error = '';
  mensaje = '';

  form = this.fb.group({
    idAgencia: [null as number | null, Validators.required],
    documento: ['', [Validators.required, Validators.maxLength(20)]],
    codigoConvenio: ['', [Validators.required, Validators.maxLength(20)]],
    nombreConvenio: ['', [Validators.required, Validators.maxLength(150)]],
    idCuentaAhorro: [null as number | null, Validators.required],
    estado: ['A', Validators.required]
  });

  ngOnInit(): void {

    this.cargarCajaActiva();

    const id =
      this.route.snapshot.paramMap.get('id');

    this.editando = !!id;

    if (id) {
      this.cargarConvenio(+id);
    }
  }

  cargarConvenio(
    idConvenio: number
  ): void {

    this.cargando = true;
    this.error = '';

    this.api.obtener(idConvenio)
      .subscribe({
        next: data => {

          this.convenioActual = data;

          this.form.patchValue({
            idAgencia: data.idAgencia ?? null,
            documento: data.documento ?? '',
            codigoConvenio: data.codigoConvenio ?? '',
            nombreConvenio: data.nombreConvenio ?? '',
            idCuentaAhorro: data.idCuentaAhorro ?? null,
            estado: data.estado ?? 'A'
          });

          if (data.idCuentaAhorro) {
            this.cuentas = [
              {
                idCuentaAhorro: data.idCuentaAhorro,
                idAgencia: data.idAgencia,
                codigoAgencia: data.codigoAgencia,
                nombreAgencia: data.nombreAgencia,
                idDatosPersonal: data.idDatosPersonal,
                documento: data.documento,
                nombreTitular: data.nombreTitular,
                codigoCuenta: data.codigoCuenta,
                codigoForma: data.codigoForma,
                nombreForma: data.nombreForma,
                saldoActual: data.saldoActual,
                estadoOperativo: true,
                mensajeOperativo: 'Cuenta asociada al convenio'
              }
            ];
          }

          this.cargando = false;
        },
        error: err => {

          console.error(err);

          this.error =
            err?.error?.message
            || 'No fue posible cargar el convenio.';

          this.cargando = false;
        }
      });
  }

  buscarCuentas(): void {

    this.error = '';
    this.mensaje = '';
    this.cuentas = [];

    const idAgencia =
      Number(this.form.get('idAgencia')?.value || 0);

    const documento =
      String(this.form.get('documento')?.value || '').trim();

    if (!idAgencia) {
      this.error = 'Digite la agencia.';
      return;
    }

    if (!documento) {
      this.error = 'Digite el documento.';
      return;
    }

    this.buscando = true;

    this.api.buscarCuentas(
      idAgencia,
      documento
    ).subscribe({
      next: data => {

        this.busqueda = data;
        this.cuentas = data.cuentas || [];

        if (this.cuentas.length === 1) {
          this.form.patchValue({
            idCuentaAhorro: this.cuentas[0].idCuentaAhorro ?? null
          });
        }

        this.mensaje =
          data.mensaje ||
          'Persona validada correctamente.';

        this.buscando = false;
      },
      error: err => {

        console.error(err);

        this.error =
          err?.error?.message
          || 'No fue posible buscar cuentas.';

        this.buscando = false;
      }
    });
  }

  guardar(): void {

    if (this.form.invalid) {
      this.error = 'Complete los campos obligatorios.';
      return;
    }

    const value =
      this.form.value;

    const payload = {
      idAgencia: Number(value.idAgencia),
      codigoConvenio: String(value.codigoConvenio || '').trim(),
      nombreConvenio: String(value.nombreConvenio || '').trim(),
      idCuentaAhorro: Number(value.idCuentaAhorro),
      estado: String(value.estado || 'A')
    };

    const id =
      this.route.snapshot.paramMap.get('id');

    const request = id
      ? this.api.actualizar(+id, payload)
      : this.api.crear(payload);

    request.subscribe({
      next: () => {
        this.router.navigate([
          '/cajas/convenios-recaudo'
        ]);
      },
      error: err => {

        console.error(err);

        this.error =
          err?.error?.message
          || 'No fue posible guardar el convenio.';
      }
    });
  }

  cargarCajaActiva(): void {
    this.cajaApi.obtenerProvisionActivaUsuario().subscribe({
      next: data => {
        this.provisionActiva = data;

        this.form.patchValue({
          idAgencia: data.idAgencia
        });
      },
      error: err => {
        this.error =
          err?.error?.message ||
          'El usuario no tiene una caja/provisión activa.';
      }
    });
  }

}
