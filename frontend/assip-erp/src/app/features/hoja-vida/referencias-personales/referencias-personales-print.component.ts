import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';
import { ReferenciasPersonalesApi, ReferenciaPersonal } from './referencias-personales.api';
import { EmpresasApi, EmpresaDTO } from '../../../shared/general/empresas.api';

type PersonaItem = {
  id?: number;
  idDatosPersonales?: number;
  id_datos_personales?: number;
  idDatosPersonal?: number;
  documento?: string;
  nombres?: string;
  apellidos?: string;
  primerNombre?: string;
  segundoNombre?: string;
  primerApellido?: string;
  segundoApellido?: string;
  [k: string]: any;
};

type Corte = {
  id: number;
  documento: string;
  nombreCompleto: string;
  ref: ReferenciaPersonal;
};

@Component({
  selector: 'app-referencias-personales-print',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './referencias-personales-print.component.html',
  styleUrls: ['./referencias-personales-print.component.scss'],
})
export class ReferenciasPersonalesPrintComponent implements OnInit {
  private personasApi = inject(DatosPersonalesApi);
  private refApi = inject(ReferenciasPersonalesApi);
  private empresasApi = inject(EmpresasApi);
  private route = inject(ActivatedRoute);

  q = signal<string>('');
  cortes = signal<Corte[]>([]);
  nowStr = signal<string>('');

  // Empresa
  empresaSigla = signal<string>('—');
  empresaNit = signal<string>('');

  ngOnInit(): void {
    // Query param opcional
    this.q.set(this.route.snapshot.queryParamMap.get('q') ?? '');
    this.nowStr.set(new Date().toLocaleString());

    // Empresa principal (sigla y NIT)
    this.empresasApi.getEmpresaPrincipal().subscribe({
      next: (e: EmpresaDTO | null) => {
        if (e?.siglaEmpresa) this.empresaSigla.set(String(e.siglaEmpresa).trim() || '—');
        const doc = (e?.documentoEmpresa ?? '').toString().trim();
        const dv  = (e?.digitoVerificacion ?? '').toString().trim();
        const nit = doc ? (dv ? `${doc}-${dv}` : doc) : '';
        if (nit) this.empresaNit.set(nit);
      },
      error: () => {}
    });

    // Traer personas y armar cortes con su única referencia
    this.personasApi.list({ q: this.q() || undefined, page: 0, size: 1000, sort: 'idDatosPersonal,asc' })
      .subscribe({
        next: async (resp: any) => {
          const personas: PersonaItem[] = Array.isArray(resp) ? resp : (resp?.content ?? resp?.items ?? []);
          const arr: Corte[] = [];
          for (const p of personas) {
            const id = this.personaId(p);
            if (!id) continue;
            const ref = await this.refApi.getByPersona(id).toPromise().catch(() => null);
            if (!ref) continue;
            arr.push({
              id,
              documento: String(p.documento ?? '').trim(),
              nombreCompleto: this.nombreCompleto(p),
              ref,
            });
          }
          this.cortes.set(arr);
        },
        error: () => this.cortes.set([]),
      });
  }

  personaId(p: PersonaItem): number {
    const candidates = [
      (p as any)?.id,
      (p as any)?.idDatosPersonales,
      (p as any)?.id_datos_personales,
      (p as any)?.idDatosPersonal,
    ];
    const found = candidates.find(v => typeof v === 'number' && !Number.isNaN(v) && v > 0);
    return (found ?? 0) as number;
  }

  nombreCompleto(p: PersonaItem): string {
    const nombres = (p.nombres ?? `${p.primerNombre ?? ''} ${p.segundoNombre ?? ''}`).toString().trim();
    const apellidos = (p.apellidos ?? `${p.primerApellido ?? ''} ${p.segundoApellido ?? ''}`).toString().trim();
    return `${nombres} ${apellidos}`.replace(/\s+/g, ' ').trim();
  }

  imprimir(): void { window.print(); }
}
