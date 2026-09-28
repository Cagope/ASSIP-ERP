import {
  Component,
  OnInit
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  Router
} from '@angular/router';

import {
  finalize
} from 'rxjs/operators';

import {
  OriginacionDesembolsoApi
} from '../originacion-desembolso.api';

import {
  CreditoPendienteDesembolso
} from '../originacion-desembolso.models';


@Component({
  selector: 'app-originacion-desembolso-list',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './originacion-desembolso-list.component.html',
  styleUrls: [
    './originacion-desembolso-list.component.scss'
  ]
})
export class OriginacionDesembolsoListComponent implements OnInit {

  // =========================================================
  // DATOS
  // =========================================================

  creditos: CreditoPendienteDesembolso[] = [];
  creditosFiltrados: CreditoPendienteDesembolso[] = [];

  cargando = false;

  error = '';


  // =========================================================
  // FILTRO
  // =========================================================

  filtro = '';


  // =========================================================
  // FORMATEADORES
  // =========================================================

  private readonly formatoMoneda = new Intl.NumberFormat(
    'es-CO',
    {
      style: 'currency',
      currency: 'COP',
      maximumFractionDigits: 0
    }
  );


  constructor(
    private readonly api: OriginacionDesembolsoApi,
    private readonly router: Router
  ) {
  }


  // =========================================================
  // INICIO
  // =========================================================

  ngOnInit(): void {
    this.cargar();
  }


  // =========================================================
  // CARGAR BANDEJA
  // =========================================================

  cargar(): void {

    if (this.cargando) {
      return;
    }

    this.cargando = true;
    this.error = '';

    this.api
      .listarPendientes()
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({

        next: (creditos) => {

          this.creditos =
            Array.isArray(creditos)
              ? creditos
              : [];

          this.aplicarFiltro();
        },

        error: (error) => {

          console.error(
            'Error cargando créditos pendientes de desembolso',
            error
          );

          this.creditos = [];
          this.creditosFiltrados = [];

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible cargar los créditos pendientes de desembolso.'
            );
        }
      });
  }


  // =========================================================
  // FILTRAR
  // =========================================================

  cambiarFiltro(
    valor: string
  ): void {

    this.filtro = valor ?? '';

    this.aplicarFiltro();
  }


  limpiarFiltro(): void {

    this.filtro = '';

    this.aplicarFiltro();
  }


  private aplicarFiltro(): void {

    const texto =
      this.normalizarTexto(
        this.filtro
      );

    if (!texto) {

      this.creditosFiltrados = [
        ...this.creditos
      ];

      return;
    }

    this.creditosFiltrados =
      this.creditos.filter(
        credito => {

          const contenido =
            [
              credito.documento,
              credito.nombreCompleto,
              credito.pagareCartera,
              credito.codigoLineaCredito,
              credito.nombreLineaCredito
            ]
              .map(valor =>
                this.normalizarTexto(
                  valor
                )
              )
              .join(' ');

          return contenido.includes(texto);
        }
      );
  }


  // =========================================================
  // ABRIR DESEMBOLSO
  // =========================================================

  abrir(
    credito: CreditoPendienteDesembolso
  ): void {

    if (
      !credito?.idCarteraCredito ||
      credito.idCarteraCredito <= 0
    ) {
      return;
    }

    void this.router.navigate(
      [
        '/cartera/originacion/desembolso',
        credito.idCarteraCredito
      ]
    );
  }


  // =========================================================
  // TRACK
  // =========================================================

  trackByCredito(
    _index: number,
    credito: CreditoPendienteDesembolso
  ): number {

    return credito.idCarteraCredito;
  }


  // =========================================================
  // PRESENTACIÓN
  // =========================================================

  moneda(
    valor: number | null | undefined
  ): string {

    return this.formatoMoneda.format(
      this.numero(valor)
    );
  }


  fecha(
    valor: string | null | undefined
  ): string {

    if (!valor) {
      return '—';
    }

    const partes =
      valor
        .substring(0, 10)
        .split('-');

    if (partes.length !== 3) {
      return valor;
    }

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
  }


  // =========================================================
  // TOTALES
  // =========================================================

  get cantidadPendientes(): number {

    return this.creditosFiltrados.length;
  }


  get valorTotalPendiente(): number {

    return this.creditosFiltrados.reduce(
      (total, credito) =>
        total +
        this.numero(
          credito.valorCredito
        ),
      0
    );
  }


  // =========================================================
  // UTILIDADES
  // =========================================================

  private numero(
    valor: number | null | undefined
  ): number {

    const numero =
      Number(valor ?? 0);

    return Number.isFinite(numero)
      ? numero
      : 0;
  }


  private normalizarTexto(
    valor: unknown
  ): string {

    return String(
      valor ?? ''
    )
      .normalize('NFD')
      .replace(
        /[\u0300-\u036f]/g,
        ''
      )
      .trim()
      .toLowerCase();
  }


  private obtenerMensajeError(
    error: any,
    mensajeDefecto: string
  ): string {

    const mensaje =
      error?.error?.message ??
      error?.error?.mensaje ??
      (
        typeof error?.error === 'string'
          ? error.error
          : null
      ) ??
      error?.message;

    return mensaje
      ? String(mensaje)
      : mensajeDefecto;
  }
}
