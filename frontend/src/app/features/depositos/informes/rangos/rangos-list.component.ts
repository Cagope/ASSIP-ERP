import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { GeneralApi } from '../../../../shared/general/general.api';
import { FormasAhorroApi } from '../../formas-ahorro/formas-ahorro.api';

import { RangosApi, RangosRequest } from './rangos.api';
import { RangosExporterService } from './rangos-exporter.service';
import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { NumericFormatDirective } from '../../../../shared/utils/numeric-format.directive';

@Component({
  selector: 'app-rangos-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HeaderActionsComponent,
    NumericFormatDirective
  ],
  templateUrl: './rangos-list.component.html',
  styleUrls: ['./rangos-list.component.scss']
})
export class RangosListComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly api = inject(RangosApi);
  private readonly generalApi = inject(GeneralApi);
  private readonly formasApi = inject(FormasAhorroApi);
  private readonly exporter = inject(RangosExporterService);

  cargando = false;
  resultados: any[] = [];
  resumen: any[] = [];
  error = '';

  agencias: any[] = [];
  formas: any[] = [];

  form!: FormGroup;

  async ngOnInit(): Promise<void> {
    this.crearFormulario();
    await this.cargarCatalogos();
  }

  private crearFormulario(): void {

    this.form = this.fb.group({
      tipo: ['EDAD', Validators.required],
      idAgencia: [0, Validators.required],
      codigoForma: ['0', Validators.required],
      fechaCorte: [this.fechaHoy(), Validators.required],
      rangos: this.fb.array([])
    });

    for (let i = 0; i < 4; i++) {

      const grupo = this.fb.group({
        desde: [null, Validators.required],
        hasta: [null, Validators.required]
      });

      grupo.get('hasta')!.valueChanges.subscribe(valor => {

        if (valor != null && i < 3) {

          const siguiente =
            this.rangos.at(i + 1) as FormGroup;

          const nuevoDesde =
            Number(valor) + 1;

          if (!siguiente.get('desde')!.dirty) {
            siguiente
              .get('desde')!
              .setValue(nuevoDesde, { emitEvent: false });
          }

        }

      });

      this.rangos.push(grupo);
    }
  }

  async cargarCatalogos(): Promise<void> {

    try {

      const agencias =
        await this.generalApi.listarAgencias().toPromise();

      this.agencias =
        agencias || [];

    } catch (e) {

      console.error('Error cargando agencias:', e);
      this.agencias = [];

    }

    try {

      const formas =
        await this.formasApi.listar().toPromise();

      this.formas = Array.from(
        new Map(
          (formas || []).map((f: any) => [
            f.codigoForma,
            f
          ])
        ).values()
      ).sort((a: any, b: any) =>
        String(a.codigoForma)
          .localeCompare(String(b.codigoForma))
      );

    } catch (e) {

      console.error('Error cargando formas:', e);
      this.formas = [];

    }
  }

  get rangos(): FormArray {
    return this.form.get('rangos') as FormArray;
  }

  consultar(): void {

    this.error = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const body: RangosRequest = {
      ...this.form.value,
      idAgencia: Number(this.form.value.idAgencia || 0),
      codigoForma: this.form.value.codigoForma || '0'
    };

    this.cargando = true;

    this.api.consultar(body).subscribe({
      next: (res) => {

        this.resultados = res || [];
        this.resumen = this.calcularResumen();
        this.cargando = false;

      },
      error: (err) => {

        console.error('Error en informe de rangos', err);

        this.error =
          err?.error?.message ||
          'No fue posible consultar el informe.';

        this.cargando = false;

      }
    });
  }

  calcularResumen(): any[] {

    const resumen = [];

    for (let i = 0; i < 4; i++) {

      const r = this.form.value.rangos[i];

      const cuentas = this.resultados.filter(x => {

        if (this.form.value.tipo === 'EDAD') {
          return x.edadAnios >= r.desde && x.edadAnios <= r.hasta;
        }

        if (this.form.value.tipo === 'SALDO') {
          return x.saldo >= r.desde && x.saldo <= r.hasta;
        }

        if (this.form.value.tipo === 'ANTIGUEDAD') {
          return x.antiguedadAnios >= r.desde && x.antiguedadAnios <= r.hasta;
        }

        return false;
      });

      resumen.push({
        cuentas: cuentas.length,
        saldo: cuentas.reduce(
          (total, item) => total + Number(item.saldo || 0),
          0
        )
      });
    }

    return resumen;
  }

  limpiar(): void {

    this.form.reset({
      tipo: 'EDAD',
      idAgencia: 0,
      codigoForma: '0',
      fechaCorte: this.fechaHoy()
    });

    this.resultados = [];
    this.resumen = [];
    this.error = '';
  }

  exportar(): void {

    if (!this.resultados.length) {
      alert('No hay datos para exportar.');
      return;
    }

    const request = {
      ...this.form.value,
      idAgencia: Number(this.form.value.idAgencia || 0),
      codigoForma: this.form.value.codigoForma || '0'
    };

    this.exporter.exportar(
      this.resumen,
      this.resultados,
      request
    );
  }

  private fechaHoy(): string {
    return new Date()
      .toISOString()
      .substring(0, 10);
  }

}
