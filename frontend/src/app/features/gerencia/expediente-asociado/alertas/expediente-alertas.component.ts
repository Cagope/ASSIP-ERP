import { CommonModule } from '@angular/common';

import {
  ChangeDetectionStrategy,
  Component,
  Input
} from '@angular/core';

import {
  ExpedienteAsociado
} from '../expediente-asociado.dto';

type FiltroAlerta =
  | 'TODAS'
  | 'CRITICA'
  | 'ADVERTENCIA'
  | 'INFORMATIVA'
  | 'RESUELTA';

type NivelAlerta =
  | 'CRITICA'
  | 'ADVERTENCIA'
  | 'INFORMATIVA';

interface AlertaVista {
  idAlerta: number | null;

  codigoAlerta: string;
  titulo: string;
  descripcion: string;

  modulo: string;
  submodulo: string;
  categoria: string;
  tipoAlerta: string;

  nivel: NivelAlerta;
  prioridad: string;

  codigoEstado: string;
  nombreEstado: string;

  activa: boolean;
  abierta: boolean;
  enGestion: boolean;
  atendida: boolean;
  descartada: boolean;
  vencida: boolean;
  resuelta: boolean;

  bloqueante: boolean;
  requiereGestion: boolean;
  requiereSeguimiento: boolean;
  requiereEscalamiento: boolean;

  reincidente: boolean;
  cantidadReincidencias: number;

  fechaEvento: string | null;
  fechaVencimiento: string | null;
  fechaGeneracion: string | null;
  fechaPrimeraDeteccion: string | null;
  fechaUltimaDeteccion: string | null;
  fechaInicioGestion: string | null;
  fechaAtencion: string | null;
  fechaCierre: string | null;

  diasDesdeDeteccion: number;
  diasParaVencimiento: number;
  diasVencida: number;
  cantidadDiasAbierta: number;

  nombreUsuarioResponsable: string;
  nombreAgencia: string;

  accionRequerida: string;
  gestionRealizada: string;
  resultadoGestion: string;
  observacionesGestion: string;

  referenciaRegistro: string;
  descripcionRegistro: string;

  rutaFrontend: string;
  parametroRuta: string;
  etiquetaAccion: string;

  permiteGestionar: boolean;
  permiteDescartar: boolean;
  permiteCerrar: boolean;

  ordenVisual: number;
  origenCalculado: boolean;
}

@Component({
  selector: 'app-expediente-alertas',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './expediente-alertas.component.html',
  styleUrls: ['./expediente-alertas.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExpedienteAlertasComponent {

  private alertasEntrada: unknown[] = [];
  private expedienteEntrada: Record<string, unknown> = {};

  filtroActivo: FiltroAlerta = 'TODAS';

  @Input()
  set alertas(valor: unknown[] | null | undefined) {
    this.alertasEntrada = Array.isArray(valor)
      ? valor
      : [];
  }

  get alertas(): unknown[] {
    return this.alertasEntrada;
  }

  @Input()
  set expediente(
    valor: ExpedienteAsociado | null | undefined
  ) {
    this.expedienteEntrada = this.registro(valor);
  }

  get expediente(): Record<string, unknown> {
    return this.expedienteEntrada;
  }

  get listaBackend(): AlertaVista[] {
    return this.alertasEntrada.map(
      item => this.normalizarAlerta(item)
    );
  }

  get listaCalculada(): AlertaVista[] {
    return this.generarAlertasCalculadas();
  }

  get listaCompleta(): AlertaVista[] {
    const combinadas = [
      ...this.listaBackend,
      ...this.listaCalculada
    ];

    const unicas = new Map<string, AlertaVista>();

    for (const alerta of combinadas) {
      const clave = [
        alerta.codigoAlerta,
        alerta.modulo,
        alerta.submodulo
      ].join('|');

      if (!unicas.has(clave)) {
        unicas.set(clave, alerta);
      }
    }

    return Array
      .from(unicas.values())
      .sort((a, b) =>
        this.ordenNivel(a.nivel) -
        this.ordenNivel(b.nivel) ||
        a.ordenVisual -
        b.ordenVisual ||
        a.titulo.localeCompare(b.titulo)
      );
  }

  get listaFiltrada(): AlertaVista[] {
    switch (this.filtroActivo) {
      case 'CRITICA':
        return this.listaCompleta.filter(
          alerta =>
            alerta.nivel === 'CRITICA' &&
            !alerta.resuelta
        );

      case 'ADVERTENCIA':
        return this.listaCompleta.filter(
          alerta =>
            alerta.nivel === 'ADVERTENCIA' &&
            !alerta.resuelta
        );

      case 'INFORMATIVA':
        return this.listaCompleta.filter(
          alerta =>
            alerta.nivel === 'INFORMATIVA' &&
            !alerta.resuelta
        );

      case 'RESUELTA':
        return this.listaCompleta.filter(
          alerta => alerta.resuelta
        );

      default:
        return this.listaCompleta;
    }
  }

  get tieneAlertas(): boolean {
    return this.listaCompleta.length > 0;
  }

  get cantidadTotal(): number {
    return this.listaCompleta.length;
  }

  get cantidadCriticas(): number {
    return this.listaCompleta.filter(
      alerta =>
        alerta.nivel === 'CRITICA' &&
        !alerta.resuelta
    ).length;
  }

  get cantidadAdvertencias(): number {
    return this.listaCompleta.filter(
      alerta =>
        alerta.nivel === 'ADVERTENCIA' &&
        !alerta.resuelta
    ).length;
  }

  get cantidadInformativas(): number {
    return this.listaCompleta.filter(
      alerta =>
        alerta.nivel === 'INFORMATIVA' &&
        !alerta.resuelta
    ).length;
  }

  get cantidadResueltas(): number {
    return this.listaCompleta.filter(
      alerta => alerta.resuelta
    ).length;
  }

  get cantidadBloqueantes(): number {
    return this.listaCompleta.filter(
      alerta =>
        alerta.bloqueante &&
        !alerta.resuelta
    ).length;
  }

  get cantidadEnGestion(): number {
    return this.listaCompleta.filter(
      alerta =>
        alerta.enGestion &&
        !alerta.resuelta
    ).length;
  }

  get cantidadVencidas(): number {
    return this.listaCompleta.filter(
      alerta =>
        alerta.vencida &&
        !alerta.resuelta
    ).length;
  }

  get cantidadReincidentes(): number {
    return this.listaCompleta.filter(
      alerta =>
        alerta.reincidente &&
        !alerta.resuelta
    ).length;
  }

  get nivelGeneral(): string {
    if (
      this.cantidadBloqueantes > 0 ||
      this.cantidadCriticas > 0
    ) {
      return 'CRÍTICO';
    }

    if (this.cantidadAdvertencias >= 3) {
      return 'ALTO';
    }

    if (this.cantidadAdvertencias > 0) {
      return 'MEDIO';
    }

    return 'BAJO';
  }

  get mensajeGeneral(): string {
    switch (this.nivelGeneral) {
      case 'CRÍTICO':
        return 'Existen situaciones que requieren atención inmediata.';

      case 'ALTO':
        return 'El expediente presenta varias novedades pendientes.';

      case 'MEDIO':
        return 'El expediente requiere algunas actualizaciones.';

      default:
        return 'El expediente no presenta novedades relevantes.';
    }
  }

  cambiarFiltro(
    filtro: FiltroAlerta
  ): void {
    this.filtroActivo = filtro;
  }

  esFiltroActivo(
    filtro: FiltroAlerta
  ): boolean {
    return this.filtroActivo === filtro;
  }

  claseNivel(
    alerta: AlertaVista
  ): string {
    switch (alerta.nivel) {
      case 'CRITICA':
        return 'nivel--critico';

      case 'ADVERTENCIA':
        return 'nivel--advertencia';

      default:
        return 'nivel--informativo';
    }
  }

  claseEstado(
    alerta: AlertaVista
  ): string {
    if (alerta.resuelta) {
      return 'estado--resuelta';
    }

    if (alerta.vencida) {
      return 'estado--vencida';
    }

    if (alerta.enGestion) {
      return 'estado--gestion';
    }

    if (alerta.abierta) {
      return 'estado--abierta';
    }

    return 'estado--neutral';
  }

  claseRiesgoGeneral(): string {
    switch (this.nivelGeneral) {
      case 'CRÍTICO':
        return 'riesgo--critico';

      case 'ALTO':
        return 'riesgo--alto';

      case 'MEDIO':
        return 'riesgo--medio';

      default:
        return 'riesgo--bajo';
    }
  }

  iconoNivel(
    alerta: AlertaVista
  ): string {
    if (alerta.bloqueante) {
      return '!';
    }

    switch (alerta.nivel) {
      case 'CRITICA':
        return '×';

      case 'ADVERTENCIA':
        return '!';

      default:
        return 'i';
    }
  }

  textoEstado(
    alerta: AlertaVista
  ): string {
    if (alerta.resuelta) {
      return 'Resuelta';
    }

    if (alerta.vencida) {
      return 'Vencida';
    }

    if (alerta.enGestion) {
      return 'En gestión';
    }

    if (alerta.abierta) {
      return 'Abierta';
    }

    return alerta.nombreEstado || 'Sin estado';
  }

  textoModulo(
    alerta: AlertaVista
  ): string {
    return [
      alerta.modulo,
      alerta.submodulo
    ]
      .filter(valor => valor)
      .join(' · ') || 'EXPEDIENTE';
  }

  trackByAlerta(
    index: number,
    alerta: AlertaVista
  ): string {
    return [
      alerta.codigoAlerta,
      alerta.modulo,
      alerta.submodulo,
      index
    ].join('|');
  }

  private generarAlertasCalculadas(): AlertaVista[] {
    const resultado: AlertaVista[] = [];

    const contacto = this.registro(
      this.expedienteEntrada['contacto']
    );

    const financiero = this.registro(
      this.expedienteEntrada['informacionFinanciera']
    );

    const sarlaft = this.registro(
      this.expedienteEntrada['sarlaft']
    );

    const creditos = this.arreglo(
      this.expedienteEntrada['creditos']
    );

    const inmuebles = this.arreglo(
      this.expedienteEntrada['bienesInmuebles']
    );

    const vehiculos = this.arreglo(
      this.expedienteEntrada['bienesVehiculos']
    );

    const maquinaria = this.arreglo(
      this.expedienteEntrada['bienesMaquinaria']
    );

    const inversiones = this.arreglo(
      this.expedienteEntrada['bienesInversiones']
    );

    const garantias = this.arreglo(
      this.expedienteEntrada['garantias']
    );

    const cuentas = this.arreglo(
      this.expedienteEntrada['cuentasAhorro']
    );

    const cdats = this.arreglo(
      this.expedienteEntrada['cdats']
    );

    if (!this.contactoCompleto(contacto)) {
      resultado.push(
        this.alertaCalculada(
          'CONTACTO_INCOMPLETO',
          'Información de contacto incompleta',
          'El asociado no tiene dirección, celular o correo electrónico completo.',
          'HOJA_VIDA',
          'CONTACTO',
          'ADVERTENCIA',
          'Actualizar la información de contacto.'
        )
      );
    }

    if (Object.keys(financiero).length === 0) {
      resultado.push(
        this.alertaCalculada(
          'FINANCIERO_INEXISTENTE',
          'Información financiera no registrada',
          'No se encontró información financiera del asociado.',
          'HOJA_VIDA',
          'FINANCIERO',
          'ADVERTENCIA',
          'Registrar la información financiera.'
        )
      );
    }

    if (
      Object.keys(sarlaft).length > 0 &&
      !this.booleano(
        this.obtener(
          sarlaft,
          'vigente',
          'sarlaftVigente',
          'informacionVigente'
        )
      )
    ) {
      resultado.push(
        this.alertaCalculada(
          'SARLAFT_NO_VIGENTE',
          'Información SARLAFT pendiente',
          'La información SARLAFT del asociado no se encuentra vigente.',
          'SARLAFT',
          'ACTUALIZACION',
          'CRITICA',
          'Actualizar la información SARLAFT.',
          true
        )
      );
    }

    if (
      creditos.some(credito =>
        this.numero(
          this.obtener(
            this.registro(credito),
            'diasMora',
            'diasDeMora'
          )
        ) > 0
      )
    ) {
      resultado.push(
        this.alertaCalculada(
          'CREDITOS_EN_MORA',
          'Créditos en mora',
          'El asociado presenta una o más obligaciones con días de mora.',
          'CARTERA',
          'CREDITOS',
          'CRITICA',
          'Revisar el estado de las obligaciones.'
        )
      );
    }

    if (
      garantias.some(garantia =>
        !this.booleano(
          this.obtener(
            this.registro(garantia),
            'garantiaSuficiente',
            'garantiasSuficientes'
          )
        )
      ) &&
      garantias.length > 0
    ) {
      resultado.push(
        this.alertaCalculada(
          'GARANTIA_INSUFICIENTE',
          'Garantía insuficiente',
          'Una o más garantías no cubren completamente la obligación.',
          'CARTERA',
          'GARANTIAS',
          'ADVERTENCIA',
          'Revisar la cobertura de las garantías.'
        )
      );
    }

    const bienes = [
      ...inmuebles,
      ...vehiculos,
      ...maquinaria,
      ...inversiones
    ];

    if (
      bienes.some(bien =>
        this.booleano(
          this.obtener(
            this.registro(bien),
            'seguroVencido'
          )
        )
      )
    ) {
      resultado.push(
        this.alertaCalculada(
          'SEGURO_BIEN_VENCIDO',
          'Seguro vencido',
          'Uno o más bienes tienen el seguro vencido.',
          'HOJA_VIDA',
          'BIENES',
          'CRITICA',
          'Solicitar la renovación del seguro.'
        )
      );
    }

    if (
      bienes.some(bien =>
        this.booleano(
          this.obtener(
            this.registro(bien),
            'avaluoVencido'
          )
        )
      )
    ) {
      resultado.push(
        this.alertaCalculada(
          'AVALUO_BIEN_VENCIDO',
          'Avalúo vencido',
          'Uno o más bienes tienen el avalúo vencido.',
          'HOJA_VIDA',
          'BIENES',
          'ADVERTENCIA',
          'Solicitar un avalúo actualizado.'
        )
      );
    }

    if (
      cuentas.some(cuenta =>
        !this.booleano(
          this.obtener(
            this.registro(cuenta),
            'activo',
            'cuentaActiva',
            'estadoOperativo'
          )
        )
      ) &&
      cuentas.length > 0
    ) {
      resultado.push(
        this.alertaCalculada(
          'CUENTA_AHORRO_INACTIVA',
          'Cuenta de ahorro inactiva',
          'El asociado tiene una o más cuentas de ahorro no operativas.',
          'DEPOSITOS',
          'CUENTAS_AHORRO',
          'INFORMATIVA',
          'Verificar el estado de las cuentas.'
        )
      );
    }

    if (
      cdats.some(cdat =>
        this.booleano(
          this.obtener(
            this.registro(cdat),
            'vencido'
          )
        )
      )
    ) {
      resultado.push(
        this.alertaCalculada(
          'CDAT_VENCIDO',
          'CDAT vencido',
          'El asociado tiene uno o más CDAT vencidos.',
          'CDAT',
          'CUENTAS_CDAT',
          'ADVERTENCIA',
          'Revisar la liquidación o renovación del CDAT.'
        )
      );
    }

    return resultado;
  }

  private contactoCompleto(
    contacto: Record<string, unknown>
  ): boolean {
    if (Object.keys(contacto).length === 0) {
      return false;
    }

    const direccion = this.texto(
      this.obtener(
        contacto,
        'direccionResidencia',
        'direccion'
      )
    );

    const celular = this.texto(
      this.obtener(
        contacto,
        'celular',
        'numeroCelular'
      )
    );

    const correo = this.texto(
      this.obtener(
        contacto,
        'correoElectronico',
        'email',
        'correo'
      )
    );

    return Boolean(
      direccion &&
      celular &&
      correo
    );
  }

  private normalizarAlerta(
    origen: unknown
  ): AlertaVista {
    const item = this.registro(origen);

    const nivel = this.normalizarNivel(
      this.obtener(
        item,
        'nivel',
        'prioridad'
      )
    );

    const codigoEstado = this.normalizarTexto(
      this.obtener(
        item,
        'codigoEstado',
        'estado'
      )
    );

    const atendida = this.booleano(
      this.obtener(
        item,
        'atendida'
      )
    );

    const descartada = this.booleano(
      this.obtener(
        item,
        'descartada'
      )
    );

    const fechaCierre = this.fecha(
      this.obtener(
        item,
        'fechaCierre'
      )
    );

    const resuelta =
      atendida ||
      descartada ||
      Boolean(fechaCierre) ||
      [
        'ATENDIDA',
        'CERRADA',
        'RESUELTA',
        'DESCARTADA'
      ].includes(codigoEstado);

    return {
      idAlerta: this.numeroNullable(
        this.obtener(
          item,
          'idAlerta'
        )
      ),

      codigoAlerta: this.texto(
        this.obtener(
          item,
          'codigoAlerta'
        ),
        'ALERTA_EXPEDIENTE'
      ),

      titulo: this.texto(
        this.obtener(
          item,
          'titulo'
        ),
        'Alerta del expediente'
      ),

      descripcion: this.texto(
        this.obtener(
          item,
          'descripcion'
        ),
        'No se proporcionó una descripción.'
      ),

      modulo: this.texto(
        this.obtener(
          item,
          'modulo'
        ),
        'EXPEDIENTE'
      ),

      submodulo: this.texto(
        this.obtener(
          item,
          'submodulo'
        )
      ),

      categoria: this.texto(
        this.obtener(
          item,
          'categoria'
        )
      ),

      tipoAlerta: this.texto(
        this.obtener(
          item,
          'tipoAlerta'
        ),
        'AUTOMATICA'
      ),

      nivel,

      prioridad: this.texto(
        this.obtener(
          item,
          'prioridad'
        ),
        nivel
      ),

      codigoEstado,

      nombreEstado: this.texto(
        this.obtener(
          item,
          'nombreEstado'
        ),
        codigoEstado || 'Sin estado'
      ),

      activa: this.booleano(
        this.obtener(
          item,
          'activa'
        )
      ),

      abierta: this.booleano(
        this.obtener(
          item,
          'abierta'
        )
      ),

      enGestion: this.booleano(
        this.obtener(
          item,
          'enGestion'
        )
      ),

      atendida,
      descartada,

      vencida: this.booleano(
        this.obtener(
          item,
          'vencida'
        )
      ),

      resuelta,

      bloqueante: this.booleano(
        this.obtener(
          item,
          'bloqueante'
        )
      ),

      requiereGestion: this.booleano(
        this.obtener(
          item,
          'requiereGestion'
        )
      ),

      requiereSeguimiento: this.booleano(
        this.obtener(
          item,
          'requiereSeguimiento'
        )
      ),

      requiereEscalamiento: this.booleano(
        this.obtener(
          item,
          'requiereEscalamiento'
        )
      ),

      reincidente: this.booleano(
        this.obtener(
          item,
          'reincidente'
        )
      ),

      cantidadReincidencias: this.numero(
        this.obtener(
          item,
          'cantidadReincidencias'
        )
      ),

      fechaEvento: this.fecha(
        this.obtener(item, 'fechaEvento')
      ),

      fechaVencimiento: this.fecha(
        this.obtener(item, 'fechaVencimiento')
      ),

      fechaGeneracion: this.fecha(
        this.obtener(item, 'fechaGeneracion')
      ),

      fechaPrimeraDeteccion: this.fecha(
        this.obtener(item, 'fechaPrimeraDeteccion')
      ),

      fechaUltimaDeteccion: this.fecha(
        this.obtener(item, 'fechaUltimaDeteccion')
      ),

      fechaInicioGestion: this.fecha(
        this.obtener(item, 'fechaInicioGestion')
      ),

      fechaAtencion: this.fecha(
        this.obtener(item, 'fechaAtencion')
      ),

      fechaCierre,

      diasDesdeDeteccion: this.numero(
        this.obtener(item, 'diasDesdeDeteccion')
      ),

      diasParaVencimiento: this.numero(
        this.obtener(item, 'diasParaVencimiento')
      ),

      diasVencida: this.numero(
        this.obtener(item, 'diasVencida')
      ),

      cantidadDiasAbierta: this.numero(
        this.obtener(item, 'cantidadDiasAbierta')
      ),

      nombreUsuarioResponsable: this.texto(
        this.obtener(
          item,
          'nombreUsuarioResponsable'
        )
      ),

      nombreAgencia: this.texto(
        this.obtener(
          item,
          'nombreAgencia'
        )
      ),

      accionRequerida: this.texto(
        this.obtener(
          item,
          'accionRequerida'
        )
      ),

      gestionRealizada: this.texto(
        this.obtener(
          item,
          'gestionRealizada'
        )
      ),

      resultadoGestion: this.texto(
        this.obtener(
          item,
          'resultadoGestion'
        )
      ),

      observacionesGestion: this.texto(
        this.obtener(
          item,
          'observacionesGestion'
        )
      ),

      referenciaRegistro: this.texto(
        this.obtener(
          item,
          'referenciaRegistro'
        )
      ),

      descripcionRegistro: this.texto(
        this.obtener(
          item,
          'descripcionRegistro'
        )
      ),

      rutaFrontend: this.texto(
        this.obtener(
          item,
          'rutaFrontend'
        )
      ),

      parametroRuta: this.texto(
        this.obtener(
          item,
          'parametroRuta'
        )
      ),

      etiquetaAccion: this.texto(
        this.obtener(
          item,
          'etiquetaAccion'
        )
      ),

      permiteGestionar: this.booleano(
        this.obtener(
          item,
          'permiteGestionar'
        )
      ),

      permiteDescartar: this.booleano(
        this.obtener(
          item,
          'permiteDescartar'
        )
      ),

      permiteCerrar: this.booleano(
        this.obtener(
          item,
          'permiteCerrar'
        )
      ),

      ordenVisual: this.numero(
        this.obtener(
          item,
          'ordenVisual'
        ),
        50
      ),

      origenCalculado: false
    };
  }

  private alertaCalculada(
    codigoAlerta: string,
    titulo: string,
    descripcion: string,
    modulo: string,
    submodulo: string,
    nivel: NivelAlerta,
    accionRequerida: string,
    bloqueante = false
  ): AlertaVista {
    return {
      idAlerta: null,
      codigoAlerta,
      titulo,
      descripcion,
      modulo,
      submodulo,
      categoria: 'EXPEDIENTE_ASOCIADO',
      tipoAlerta: 'CALCULADA_FRONTEND',
      nivel,
      prioridad: nivel,
      codigoEstado: 'ABIERTA',
      nombreEstado: 'Abierta',
      activa: true,
      abierta: true,
      enGestion: false,
      atendida: false,
      descartada: false,
      vencida: false,
      resuelta: false,
      bloqueante,
      requiereGestion: true,
      requiereSeguimiento: false,
      requiereEscalamiento: bloqueante,
      reincidente: false,
      cantidadReincidencias: 0,
      fechaEvento: null,
      fechaVencimiento: null,
      fechaGeneracion: null,
      fechaPrimeraDeteccion: null,
      fechaUltimaDeteccion: null,
      fechaInicioGestion: null,
      fechaAtencion: null,
      fechaCierre: null,
      diasDesdeDeteccion: 0,
      diasParaVencimiento: 0,
      diasVencida: 0,
      cantidadDiasAbierta: 0,
      nombreUsuarioResponsable: '',
      nombreAgencia: '',
      accionRequerida,
      gestionRealizada: '',
      resultadoGestion: '',
      observacionesGestion: '',
      referenciaRegistro: '',
      descripcionRegistro: '',
      rutaFrontend: '',
      parametroRuta: '',
      etiquetaAccion: '',
      permiteGestionar: false,
      permiteDescartar: false,
      permiteCerrar: false,
      ordenVisual: 40,
      origenCalculado: true
    };
  }

  private normalizarNivel(
    valor: unknown
  ): NivelAlerta {
    const nivel = this.normalizarTexto(valor);

    if (
      nivel.includes('CRIT') ||
      nivel.includes('ALTO') ||
      nivel.includes('BLOQUE')
    ) {
      return 'CRITICA';
    }

    if (
      nivel.includes('ADVERT') ||
      nivel.includes('MEDIO')
    ) {
      return 'ADVERTENCIA';
    }

    return 'INFORMATIVA';
  }

  private ordenNivel(
    nivel: NivelAlerta
  ): number {
    switch (nivel) {
      case 'CRITICA':
        return 1;

      case 'ADVERTENCIA':
        return 2;

      default:
        return 3;
    }
  }

  private arreglo(
    valor: unknown
  ): unknown[] {
    return Array.isArray(valor)
      ? valor
      : [];
  }

  private registro(
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
    return this.texto(valor)
      .toUpperCase();
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
      'ABIERTA',
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
}
