import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  Input
} from '@angular/core';

type TipoBienVista =
  | 'INMUEBLE'
  | 'VEHICULO'
  | 'MAQUINARIA'
  | 'INVERSION';

interface BienVista {
  idBien: number | null;
  tipo: TipoBienVista;
  nombreTipo: string;
  nombreSubtipo: string;
  descripcion: string;

  porcentajePropiedad: number;
  valorParticipacion: number;
  titularPrincipal: boolean;
  propiedadCompartida: boolean;
  cantidadPropietarios: number;

  valorComercial: number;
  valorGravamen: number;
  valorNeto: number;

  tieneGravamen: boolean;
  nombreGravamen: string;

  identificadorPrincipal: string;
  identificadorSecundario: string;

  ubicacion: string;

  fechaReferencia: string | null;
  nombreReferencia: string;

  tieneAvaluo: boolean;
  valorAvaluo: number;
  fechaAvaluo: string | null;
  avaluoVigente: boolean;
  avaluoVencido: boolean;
  avaluoProximoVencer: boolean;

  tieneSeguro: boolean;
  aseguradora: string;
  numeroPoliza: string;
  valorAsegurado: number;
  fechaVencimientoSeguro: string | null;
  seguroVigente: boolean;
  seguroVencido: boolean;
  seguroProximoVencer: boolean;

  requiereRevision: boolean;
  cantidadAlertas: number;
  nivelAlerta: string;

  observaciones: string;
}

@Component({
  selector: 'app-expediente-bienes',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './expediente-bienes.component.html',
  styleUrls: ['./expediente-bienes.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExpedienteBienesComponent {

  @Input()
  bienesInmuebles: unknown[] | null = [];

  @Input()
  bienesVehiculos: unknown[] | null = [];

  @Input()
  bienesMaquinaria: unknown[] | null = [];

  @Input()
  bienesInversiones: unknown[] | null = [];

  get inmuebles(): BienVista[] {
    return this.normalizarLista(
      this.bienesInmuebles,
      'INMUEBLE'
    );
  }

  get vehiculos(): BienVista[] {
    return this.normalizarLista(
      this.bienesVehiculos,
      'VEHICULO'
    );
  }

  get maquinaria(): BienVista[] {
    return this.normalizarLista(
      this.bienesMaquinaria,
      'MAQUINARIA'
    );
  }

  get inversiones(): BienVista[] {
    return this.normalizarLista(
      this.bienesInversiones,
      'INVERSION'
    );
  }

  get lista(): BienVista[] {
    return [
      ...this.inmuebles,
      ...this.vehiculos,
      ...this.maquinaria,
      ...this.inversiones
    ];
  }

  get tieneBienes(): boolean {
    return this.lista.length > 0;
  }

  get cantidadBienes(): number {
    return this.lista.length;
  }

  get cantidadInmuebles(): number {
    return this.inmuebles.length;
  }

  get cantidadVehiculos(): number {
    return this.vehiculos.length;
  }

  get cantidadMaquinaria(): number {
    return this.maquinaria.length;
  }

  get cantidadInversiones(): number {
    return this.inversiones.length;
  }

  get cantidadConGravamen(): number {
    return this.lista.filter(
      bien => bien.tieneGravamen
    ).length;
  }

  get cantidadCompartidos(): number {
    return this.lista.filter(
      bien => bien.propiedadCompartida
    ).length;
  }

  get cantidadConSeguro(): number {
    return this.lista.filter(
      bien => bien.tieneSeguro
    ).length;
  }

  get cantidadSegurosVencidos(): number {
    return this.lista.filter(
      bien => bien.seguroVencido
    ).length;
  }

  get cantidadAvaluosVencidos(): number {
    return this.lista.filter(
      bien => bien.avaluoVencido
    ).length;
  }

  get cantidadConRevision(): number {
    return this.lista.filter(
      bien => bien.requiereRevision
    ).length;
  }

  get valorComercialTotal(): number {
    return this.sumar(
      bien => bien.valorComercial
    );
  }

  get valorParticipacionTotal(): number {
    return this.sumar(
      bien => bien.valorParticipacion
    );
  }

  get valorGravamenTotal(): number {
    return this.sumar(
      bien => bien.valorGravamen
    );
  }

  get valorNetoTotal(): number {
    return this.sumar(
      bien => bien.valorNeto
    );
  }

  get valorAseguradoTotal(): number {
    return this.sumar(
      bien => bien.valorAsegurado
    );
  }

  get valorAvaluosTotal(): number {
    return this.sumar(
      bien => bien.valorAvaluo
    );
  }

  bienesPorTipo(
    tipo: TipoBienVista
  ): BienVista[] {
    return this.lista.filter(
      bien => bien.tipo === tipo
    );
  }

  claseTipo(
    bien: BienVista
  ): string {
    switch (bien.tipo) {
      case 'INMUEBLE':
        return 'tipo--inmueble';

      case 'VEHICULO':
        return 'tipo--vehiculo';

      case 'MAQUINARIA':
        return 'tipo--maquinaria';

      case 'INVERSION':
        return 'tipo--inversion';

      default:
        return '';
    }
  }

  claseAlerta(
    bien: BienVista
  ): string {
    const nivel = this.normalizarTexto(
      bien.nivelAlerta
    );

    if (
      nivel.includes('CRIT') ||
      bien.seguroVencido ||
      bien.avaluoVencido
    ) {
      return 'alerta--critica';
    }

    if (
      nivel.includes('ADVERT') ||
      bien.requiereRevision ||
      bien.seguroProximoVencer ||
      bien.avaluoProximoVencer
    ) {
      return 'alerta--advertencia';
    }

    if (nivel.includes('INFORMAT')) {
      return 'alerta--informativa';
    }

    return 'alerta--normal';
  }

  requiereAtencion(
    bien: BienVista
  ): boolean {
    return (
      bien.requiereRevision ||
      bien.seguroVencido ||
      bien.seguroProximoVencer ||
      bien.avaluoVencido ||
      bien.avaluoProximoVencer ||
      bien.cantidadAlertas > 0
    );
  }

  textoSeguro(
    bien: BienVista
  ): string {
    if (!bien.tieneSeguro) {
      return 'Sin seguro';
    }

    if (bien.seguroVencido) {
      return 'Seguro vencido';
    }

    if (bien.seguroProximoVencer) {
      return 'Próximo a vencer';
    }

    if (bien.seguroVigente) {
      return 'Seguro vigente';
    }

    return 'Seguro registrado';
  }

  textoAvaluo(
    bien: BienVista
  ): string {
    if (!bien.tieneAvaluo) {
      return 'Sin avalúo';
    }

    if (bien.avaluoVencido) {
      return 'Avalúo vencido';
    }

    if (bien.avaluoProximoVencer) {
      return 'Próximo a vencer';
    }

    if (bien.avaluoVigente) {
      return 'Avalúo vigente';
    }

    return 'Avalúo registrado';
  }

  private normalizarLista(
    entrada: unknown[] | null | undefined,
    tipo: TipoBienVista
  ): BienVista[] {
    if (!Array.isArray(entrada)) {
      return [];
    }

    return entrada.map(
      item => this.normalizarBien(item, tipo)
    );
  }

  private normalizarBien(
    origen: unknown,
    tipo: TipoBienVista
  ): BienVista {
    const item = this.comoRegistro(origen);

    const valorComercial = this.numero(
      this.obtener(
        item,
        'valorComercial',
        'valorMercado',
        'valorNominal'
      )
    );

    const valorGravamen = this.numero(
      this.obtener(
        item,
        'valorGravamen',
        'valorGarantia',
        'saldoGravamen'
      )
    );

    const porcentajePropiedad = this.numero(
      this.obtener(
        item,
        'porcentajePropiedad',
        'porcentajeParticipacion'
      ),
      100
    );

    const valorParticipacionInformado =
      this.numero(
        this.obtener(
          item,
          'valorParticipacion'
        )
      );

    const valorParticipacion =
      valorParticipacionInformado !== 0
        ? valorParticipacionInformado
        : valorComercial *
          porcentajePropiedad /
          100;

    const valorNetoInformado = this.numero(
      this.obtener(
        item,
        'valorNeto'
      )
    );

    const valorNeto =
      valorNetoInformado !== 0
        ? valorNetoInformado
        : valorComercial - valorGravamen;

    return {
      idBien: this.numeroNullable(
        this.obtener(
          item,
          'idBien'
        )
      ),

      tipo,

      nombreTipo: this.texto(
        this.obtener(
          item,
          'nombreTipoBien'
        ),
        this.nombreTipoPredeterminado(tipo)
      ),

      nombreSubtipo: this.obtenerSubtipo(
        item,
        tipo
      ),

      descripcion: this.texto(
        this.obtener(
          item,
          'descripcionGeneral',
          'descripcionTecnica',
          'descripcion'
        ),
        'Sin descripción'
      ),

      porcentajePropiedad,
      valorParticipacion,

      titularPrincipal: this.booleano(
        this.obtener(
          item,
          'titularPrincipal'
        )
      ),

      propiedadCompartida:
        this.booleano(
          this.obtener(
            item,
            'propiedadCompartida'
          )
        ) ||
        this.numero(
          this.obtener(
            item,
            'cantidadPropietarios'
          )
        ) > 1,

      cantidadPropietarios: this.numero(
        this.obtener(
          item,
          'cantidadPropietarios'
        ),
        1
      ),

      valorComercial,
      valorGravamen,
      valorNeto,

      tieneGravamen:
        this.booleano(
          this.obtener(
            item,
            'tieneGravamen'
          )
        ) ||
        valorGravamen > 0,

      nombreGravamen: this.texto(
        this.obtener(
          item,
          'nombreTipoGravamen',
          'nombreGravamen'
        ),
        valorGravamen > 0
          ? 'Gravamen registrado'
          : 'Sin gravamen'
      ),

      identificadorPrincipal:
        this.obtenerIdentificadorPrincipal(
          item,
          tipo
        ),

      identificadorSecundario:
        this.obtenerIdentificadorSecundario(
          item,
          tipo
        ),

      ubicacion: this.obtenerUbicacion(
        item,
        tipo
      ),

      fechaReferencia: this.obtenerFechaReferencia(
        item,
        tipo
      ),

      nombreReferencia:
        this.obtenerNombreReferencia(tipo),

      tieneAvaluo: this.booleano(
        this.obtener(
          item,
          'tieneAvaluo'
        )
      ),

      valorAvaluo: this.numero(
        this.obtener(
          item,
          'valorAvaluoComercial',
          'valorAvaluo'
        )
      ),

      fechaAvaluo: this.fecha(
        this.obtener(
          item,
          'fechaAvaluo'
        )
      ),

      avaluoVigente: this.booleano(
        this.obtener(
          item,
          'avaluoVigente'
        )
      ),

      avaluoVencido: this.booleano(
        this.obtener(
          item,
          'avaluoVencido'
        )
      ),

      avaluoProximoVencer: this.booleano(
        this.obtener(
          item,
          'avaluoProximoVencer'
        )
      ),

      tieneSeguro:
        this.booleano(
          this.obtener(
            item,
            'tieneSeguro'
          )
        ) ||
        this.texto(
          this.obtener(
            item,
            'numeroPoliza'
          )
        ).length > 0,

      aseguradora: this.texto(
        this.obtener(
          item,
          'aseguradora'
        ),
        'Sin información'
      ),

      numeroPoliza: this.texto(
        this.obtener(
          item,
          'numeroPoliza'
        )
      ),

      valorAsegurado: this.numero(
        this.obtener(
          item,
          'valorAsegurado'
        )
      ),

      fechaVencimientoSeguro: this.fecha(
        this.obtener(
          item,
          'fechaVencimientoSeguro'
        )
      ),

      seguroVigente: this.booleano(
        this.obtener(
          item,
          'seguroVigente'
        )
      ),

      seguroVencido: this.booleano(
        this.obtener(
          item,
          'seguroVencido'
        )
      ),

      seguroProximoVencer: this.booleano(
        this.obtener(
          item,
          'seguroProximoVencer'
        )
      ),

      requiereRevision: this.booleano(
        this.obtener(
          item,
          'requiereRevision'
        )
      ),

      cantidadAlertas: this.numero(
        this.obtener(
          item,
          'cantidadAlertas'
        )
      ),

      nivelAlerta: this.texto(
        this.obtener(
          item,
          'nivelAlerta'
        ),
        'NORMAL'
      ),

      observaciones: this.texto(
        this.obtener(
          item,
          'observaciones',
          'observacionesAvaluo',
          'comentario'
        )
      )
    };
  }

  private obtenerSubtipo(
    item: Record<string, unknown>,
    tipo: TipoBienVista
  ): string {
    const campos: Record<
      TipoBienVista,
      string[]
    > = {
      INMUEBLE: [
        'nombreTipoInmueble',
        'codigoTipoInmueble'
      ],
      VEHICULO: [
        'nombreTipoVehiculo',
        'codigoTipoVehiculo'
      ],
      MAQUINARIA: [
        'nombreTipoMaquinaria',
        'codigoTipoMaquinaria'
      ],
      INVERSION: [
        'nombreTipoInversion',
        'codigoTipoInversion'
      ]
    };

    return this.texto(
      this.obtener(
        item,
        ...campos[tipo]
      ),
      this.nombreTipoPredeterminado(tipo)
    );
  }

  private obtenerIdentificadorPrincipal(
    item: Record<string, unknown>,
    tipo: TipoBienVista
  ): string {
    switch (tipo) {
      case 'INMUEBLE':
        return this.texto(
          this.obtener(
            item,
            'numeroMatriculaInmobiliaria'
          ),
          'Sin matrícula'
        );

      case 'VEHICULO':
        return this.texto(
          this.obtener(
            item,
            'placa'
          ),
          'Sin placa'
        );

      case 'MAQUINARIA':
        return this.texto(
          this.obtener(
            item,
            'serie',
            'numeroSerie'
          ),
          'Sin serie'
        );

      case 'INVERSION':
        return this.texto(
          this.obtener(
            item,
            'numeroTitulo'
          ),
          'Sin número de título'
        );
    }
  }

  private obtenerIdentificadorSecundario(
    item: Record<string, unknown>,
    tipo: TipoBienVista
  ): string {
    switch (tipo) {
      case 'INMUEBLE':
        return this.texto(
          this.obtener(
            item,
            'cedulaCatastral'
          )
        );

      case 'VEHICULO':
        return this.unirTextos(
          this.obtener(item, 'marca'),
          this.obtener(item, 'linea'),
          this.obtener(item, 'modelo')
        );

      case 'MAQUINARIA':
        return this.unirTextos(
          this.obtener(item, 'marca'),
          this.obtener(item, 'modelo')
        );

      case 'INVERSION':
        return this.texto(
          this.obtener(
            item,
            'entidad'
          )
        );
    }
  }

  private obtenerUbicacion(
    item: Record<string, unknown>,
    tipo: TipoBienVista
  ): string {
    if (tipo !== 'INMUEBLE') {
      return '';
    }

    const ubicacionCompleta = this.texto(
      this.obtener(
        item,
        'ubicacionCompleta'
      )
    );

    if (ubicacionCompleta) {
      return ubicacionCompleta;
    }

    return this.unirTextos(
      this.obtener(item, 'direccion'),
      this.obtener(item, 'barrioVereda'),
      this.obtener(item, 'nombreCiudad'),
      this.obtener(item, 'nombreDepartamento'),
      this.obtener(item, 'nombrePais')
    );
  }

  private obtenerFechaReferencia(
    item: Record<string, unknown>,
    tipo: TipoBienVista
  ): string | null {
    switch (tipo) {
      case 'INMUEBLE':
        return this.fecha(
          this.obtener(
            item,
            'fechaEscritura'
          )
        );

      case 'VEHICULO':
        return this.fecha(
          this.obtener(
            item,
            'fechaMatricula',
            'fechaAdquisicion'
          )
        );

      case 'MAQUINARIA':
        return this.fecha(
          this.obtener(
            item,
            'fechaAdquisicion'
          )
        );

      case 'INVERSION':
        return this.fecha(
          this.obtener(
            item,
            'fechaEmision'
          )
        );
    }
  }

  private obtenerNombreReferencia(
    tipo: TipoBienVista
  ): string {
    switch (tipo) {
      case 'INMUEBLE':
        return 'Fecha de escritura';

      case 'VEHICULO':
        return 'Fecha de matrícula';

      case 'MAQUINARIA':
        return 'Fecha de adquisición';

      case 'INVERSION':
        return 'Fecha de emisión';
    }
  }

  private nombreTipoPredeterminado(
    tipo: TipoBienVista
  ): string {
    const nombres: Record<
      TipoBienVista,
      string
    > = {
      INMUEBLE: 'Inmueble',
      VEHICULO: 'Vehículo',
      MAQUINARIA: 'Maquinaria',
      INVERSION: 'Inversión'
    };

    return nombres[tipo];
  }

  private sumar(
    selector: (bien: BienVista) => number
  ): number {
    return this.lista.reduce(
      (total, bien) =>
        total + selector(bien),
      0
    );
  }

  private comoRegistro(
    valor: unknown
  ): Record<string, unknown> {
    if (
      valor !== null &&
      typeof valor === 'object' &&
      !Array.isArray(valor)
    ) {
      return valor as Record<string, unknown>;
    }

    return {};
  }

  private obtener(
    origen: Record<string, unknown>,
    ...claves: string[]
  ): unknown {
    for (const clave of claves) {
      const valor = origen[clave];

      if (
        valor !== null &&
        valor !== undefined &&
        String(valor).trim() !== ''
      ) {
        return valor;
      }
    }

    return null;
  }

  private texto(
    valor: unknown,
    predeterminado = ''
  ): string {
    const resultado = String(
      valor ?? ''
    ).trim();

    return resultado || predeterminado;
  }

  private normalizarTexto(
    valor: unknown
  ): string {
    return this.texto(valor).toUpperCase();
  }

  private numero(
    valor: unknown,
    predeterminado = 0
  ): number {
    if (
      valor === null ||
      valor === undefined ||
      String(valor).trim() === ''
    ) {
      return predeterminado;
    }

    const resultado = Number(valor);

    return Number.isFinite(resultado)
      ? resultado
      : predeterminado;
  }

  private numeroNullable(
    valor: unknown
  ): number | null {
    if (
      valor === null ||
      valor === undefined ||
      String(valor).trim() === ''
    ) {
      return null;
    }

    const resultado = Number(valor);

    return Number.isFinite(resultado)
      ? resultado
      : null;
  }

  private booleano(
    valor: unknown
  ): boolean {
    if (typeof valor === 'boolean') {
      return valor;
    }

    if (typeof valor === 'number') {
      return valor === 1;
    }

    return [
      'S',
      'SI',
      'SÍ',
      'TRUE',
      '1',
      'A',
      'ACTIVO',
      'VIGENTE'
    ].includes(
      this.normalizarTexto(valor)
    );
  }

  private fecha(
    valor: unknown
  ): string | null {
    const resultado = this.texto(valor);

    return resultado || null;
  }

  private unirTextos(
    ...valores: unknown[]
  ): string {
    return valores
      .map(valor => this.texto(valor))
      .filter(valor => valor.length > 0)
      .join(' · ');
  }
}
