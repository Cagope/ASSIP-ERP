import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormArray } from '@angular/forms';
import { RangosApi, RangosRequest } from './rangos.api';
import { RangosExporterService } from './rangos-exporter.service';

@Component({
  selector: 'app-rangos-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './rangos-list.component.html',
  styleUrls: ['./rangos-list.component.scss']
})
export class RangosListComponent {

  private fb = inject(FormBuilder);
  private api = inject(RangosApi);
  private exporter = inject(RangosExporterService);

  cargando = false;
  resultados: any[] = [];
  resumen: any[] = [];

  agencias = [
    { id: '0', nombre: 'Todas' },
    { id: '1', nombre: 'Agencia 1' },
    { id: '2', nombre: 'Agencia 2' },
    { id: '3', nombre: 'Agencia 3' }
  ];

  form!: FormGroup;

  ngOnInit() {

    this.form = this.fb.group({
      tipo: ['EDAD', Validators.required],
      agencia: ['0', Validators.required],
      fechaCorte: [null, Validators.required],
      rangos: this.fb.array([])
    });

    // Crear los 4 rangos
    for (let i = 0; i < 4; i++) {
      const grupo = this.fb.group({
        desde: [null, Validators.required],
        hasta: [null, Validators.required]
      });

      // Detecta cambios en HASTA y encadena
      grupo.get('hasta')!.valueChanges.subscribe(valor => {
        if (valor != null && i < 3) {
          const siguiente = this.rangos.at(i + 1) as FormGroup;

          const nuevoDesde = Number(valor) + 1;

          // Solo se cambia si no ha sido modificado por el usuario
          if (!siguiente.get('desde')!.dirty) {
            siguiente.get('desde')!.setValue(nuevoDesde, { emitEvent: false });
          }
        }
      });

      this.rangos.push(grupo);
    }
  }


  get rangos(): FormArray {
    return this.form.get('rangos') as FormArray;
  }

  consultar() {

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const body: RangosRequest = this.form.value;

    this.cargando = true;

    this.api.consultar(body).subscribe({
      next: (res) => {

        this.resultados = res || [];
        this.resumen = this.calcularResumen();
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error en informe de rangos', err);
        this.cargando = false;
      }
    });
  }

  calcularResumen() {

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
        saldo: cuentas.reduce((a, b) => a + b.saldo, 0)
      });
    }

    return resumen;
  }

  // 🔥🔥🔥 MÉTODO QUE FALTABA
  exportar() {
    const request = this.form.value;   // ← trae tipo, agencia, fechaCorte, rangos
    this.exporter.exportar(this.resumen, this.resultados, request);
  }
}
