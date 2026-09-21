import type {
  OriginacionExpedientePrintData,
  OriginacionExpedientePersona
} from './originacion-expediente-print.models';


// =========================================================
// ASSIP ERP
// ORIGINACIÓN DE CARTERA
// DOCUMENTO INTEGRAL PARA IMPRESIÓN
// =========================================================

type Registro = Record<string, unknown>;

type TipoValor =
  | 'texto'
  | 'numero'
  | 'dinero'
  | 'porcentaje'
  | 'fecha'
  | 'estado';

interface CampoDocumento {
  titulo: string;
  clave: string;
  tipo?: TipoValor;
}

export class OriginacionExpedientePrintDocument {

  private readonly monedaFormatter =
    new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      maximumFractionDigits: 0
    });

  private readonly numeroFormatter =
    new Intl.NumberFormat('es-CO', {
      maximumFractionDigits: 4
    });


  // =========================================================
  // CONSTRUCCIÓN GENERAL
  // =========================================================

  construir(data: OriginacionExpedientePrintData): string {
    const documento = data.opciones.documento;
    const partes: string[] = ['<main class="expediente">', this.renderEncabezado(data)];

    if (documento === 'COMPLETO') {
      const deudores = data.fotos.fotoDeudores || [];
      const principal = deudores.find(d =>
        String(d['tipo_deudor'] ?? '').toUpperCase() === 'PRINCIPAL'
      ) || null;

      // La solicitud es el primer documento; el titular aparece en esta hoja.
      partes.push('<section class="expediente-primera-hoja">');
      partes.push(this.renderSolicitud(data));
      if (principal) {
        partes.push(this.renderIdentificacionParticipante(data, principal, true));
      } else {
        partes.push(this.nota('No se identificó un solicitante principal en la fotografía de deudores.'));
      }
      partes.push('</section>');

      // La continuación del titular y cada codeudor comienzan en hoja nueva.
      if (principal) {
        partes.push(this.renderExpedienteParticipante(data, principal, true));
      }
      for (const deudor of deudores) {
        if (deudor !== principal) {
          partes.push(this.renderExpedienteParticipante(data, deudor, false));
        }
      }

      // Nunca perder registros cuya relación con un participante esté ausente.
      partes.push(this.renderRegistrosSinParticipante(data, deudores));

      if (data.opciones.incluirHistorialAprobaciones) {
        partes.push(this.renderAprobaciones(data));
      }
    } else {
      // Conservar la impresión independiente de cada sección.
      switch (documento) {
        case 'SOLICITUD':
          partes.push(this.renderSolicitud(data));
          break;
        case 'CODEUDORES':
          partes.push(this.renderDeudores(data));
          break;
        case 'FINANCIERO':
          partes.push(this.renderFinanciero(data));
          break;
        case 'BIENES':
          partes.push(this.renderBienes(data));
          break;
        case 'CENTRAL_RIESGO':
          partes.push(this.renderCentralRiesgo(data));
          break;
        case 'ANALISIS':
          partes.push(this.renderAnalisis(data));
          break;
        case 'APROBACIONES':
          if (data.opciones.incluirHistorialAprobaciones) {
            partes.push(this.renderAprobaciones(data));
          }
          break;
      }
    }

    partes.push(this.renderPie(data), '</main>');
    return partes.join('\n');
  }

  // =========================================================
  // EXPEDIENTE INDIVIDUAL: TITULAR Y CADA CODEUDOR
  // =========================================================

  private renderExpedienteParticipante(
    data: OriginacionExpedientePrintData,
    deudor: Registro,
    esPrincipal: boolean
  ): string {
   const individual = this.fotosDeParticipante(data, deudor);

   const titulo = esPrincipal
     ? ''
     : `Expediente del codeudor — ${this.valor(deudor, 'nombre_completo')}`;
    return `
      <section class="expediente-participante" style="break-before: page; page-break-before: always;">
        ${esPrincipal ? '' : this.titulo(titulo)}
        ${esPrincipal ? '' : this.renderIdentificacionParticipante(data, deudor, false)}
        ${this.renderFinanciero(individual, false)}
        ${this.renderBienes(individual)}
        ${this.renderCentralRiesgo(individual, false)}
        ${this.renderComportamientoCrediticio(deudor)}
        ${this.renderAnalisis(individual, false)}
      </section>
    `;
  }

  private renderIdentificacionParticipante(
    data: OriginacionExpedientePrintData,
    deudor: Registro,
    primeraHoja: boolean
  ): string {

    const persona = this.buscarPersona(data, deudor);

    const documentoCompleto = [
      deudor['tipo_documento'] ?? persona?.tipoDocumento,
      deudor['documento'] ?? persona?.documento
    ]
      .filter(valor =>
        valor !== null &&
        valor !== undefined &&
        String(valor).trim() !== ''
      )
      .join(' ');

    const registro: Registro = {
      ...persona,
      ...deudor,

      nombre_completo:
        deudor['nombre_completo']
        ?? persona?.nombreCompleto,

      documento_completo: documentoCompleto,

      fecha_afiliacion:
        persona?.fechaAfiliacion,

      fecha_nacimiento:
        persona?.fechaNacimiento,

      estado_civil:
        persona?.estadoCivil,

      escolaridad:
        persona?.escolaridad,

      numero_hijos:
        persona?.numeroHijos,

      direccion:
        persona?.direccion,

      telefono:
        persona?.telefono,

      celular:
        persona?.celular,

      correo_electronico:
        persona?.correoElectronico,

      ciudad:
        persona?.ciudad,

      departamento:
        persona?.departamento
    };

    return `
      ${this.titulo(
        primeraHoja
          ? '2. Datos del solicitante'
          : '2. Datos del codeudor'
      )}

      ${this.campos(registro, [

        {
          titulo: 'Nombre completo',
          clave: 'nombre_completo'
        },

        {
          titulo: 'Documento',
          clave: 'documento_completo'
        },

        {
          titulo: 'Fecha de ingreso',
          clave: 'fecha_afiliacion',
          tipo: 'fecha'
        },

        {
          titulo: 'Fecha de nacimiento',
          clave: 'fecha_nacimiento',
          tipo: 'fecha'
        },

        {
          titulo: 'Estado civil',
          clave: 'estado_civil'
        },

        {
          titulo: 'Nivel de estudios',
          clave: 'escolaridad'
        },

        {
          titulo: 'Número de hijos',
          clave: 'numero_hijos'
        },

        {
          titulo: 'Dirección',
          clave: 'direccion'
        },

        {
          titulo: 'Teléfono',
          clave: 'telefono'
        },

        {
          titulo: 'Celular',
          clave: 'celular'
        },

        {
          titulo: 'Correo electrónico',
          clave: 'correo_electronico'
        },

        {
          titulo: 'Ciudad',
          clave: 'ciudad'
        },

        {
          titulo: 'Departamento',
          clave: 'departamento'
        }

      ])}

      ${
        data.opciones.incluirDatosComplementarios
          ? this.renderActividadEconomicaParticipante(persona)
          : ''
      }
    `;
  }

  // =========================================================
  // ACTIVIDAD ECONÓMICA Y LABORAL DEL PARTICIPANTE
  //
  // Información complementaria de Hoja de Vida.
  // Aplica al solicitante y a cada codeudor.
  // =========================================================

  private renderActividadEconomicaParticipante(
    persona: OriginacionExpedientePersona | null
  ): string {

    if (!persona) {
      return '';
    }

    const registro: Registro = {
      ...persona
    };

    return `
      ${this.subtitulo('Actividad económica y laboral')}

      ${this.campos(registro, [

        {
          titulo: 'Actividad económica',
          clave: 'actividadEconomica'
        },

        {
          titulo: 'Sector económico',
          clave: 'sectorEconomico'
        },

        {
          titulo: 'Ocupación',
          clave: 'ocupacion'
        },

        {
          titulo: 'Profesión',
          clave: 'profesion'
        },

        {
          titulo: 'Empresa',
          clave: 'empresa'
        },

        {
          titulo: 'Cargo',
          clave: 'cargo'
        },

        {
          titulo: 'Dirección laboral',
          clave: 'direccionLaboral'
        },

        {
          titulo: 'Teléfono laboral',
          clave: 'telefonoLaboral'
        },

        {
          titulo: 'Celular laboral',
          clave: 'celularLaboral'
        },

        {
          titulo: 'Fecha de vinculación laboral',
          clave: 'fechaVinculacionLaboral',
          tipo: 'fecha'
        }

      ])}
    `;
  }

  // =========================================================
  // COMPORTAMIENTO CREDITICIO Y VALIDACIONES
  //
  // Los datos provienen de la fotografía del deudor.
  // No se recalculan saldos, mora ni resultados.
  // =========================================================

  private renderComportamientoCrediticio(
    deudor: Registro
  ): string {

    return `
      ${this.subtitulo('Comportamiento crediticio y validaciones')}

      ${this.campos(deudor, [

        {
          titulo: 'Saldo de cartera al inicio',
          clave: 'saldo_cartera_inicio',
          tipo: 'dinero'
        },

        {
          titulo: 'Días de mora al inicio',
          clave: 'dias_mora_inicio'
        },

        {
          titulo: 'Validación de mora inicial',
          clave: 'cumple_mora_inicio',
          tipo: 'estado'
        },

        {
          titulo: 'Saldo en validación',
          clave: 'saldo_cartera_validacion',
          tipo: 'dinero'
        },

        {
          titulo: 'Días de mora en validación',
          clave: 'dias_mora_validacion'
        },

        {
          titulo: 'Validación de mora posterior',
          clave: 'cumple_mora_validacion',
          tipo: 'estado'
        }

      ])}
    `;
  }

  private fotosDeParticipante(
    data: OriginacionExpedientePrintData,
    deudor: Registro
  ): OriginacionExpedientePrintData {
    const filtrar = (items: Registro[] | null): Registro[] =>
      (items || []).filter(item => this.mismoParticipante(item, deudor));

    return {
      ...data,
      fotos: {
        ...data.fotos,
        fotoDeudores: [deudor],
        fotoFinanciero: filtrar(data.fotos.fotoFinanciero),
        fotoBienes: filtrar(data.fotos.fotoBienes),
        fotoCentralRiesgo: filtrar(data.fotos.fotoCentralRiesgo),
        fotoAnalisis: filtrar(data.fotos.fotoAnalisis)
      }
    };
  }

  private mismoParticipante(item: Registro, deudor: Registro): boolean {
    const idDeudor = deudor['id_solicitud_deudor'];
    const idItem = item['id_solicitud_deudor'];
    if (idDeudor !== null && idDeudor !== undefined &&
        idItem !== null && idItem !== undefined) {
      return String(idDeudor) === String(idItem);
    }
    const idPersona = deudor['id_datos_personal'];
    const idPersonaItem = item['id_datos_personal'];
    return idPersona !== null && idPersona !== undefined &&
      idPersonaItem !== null && idPersonaItem !== undefined &&
      String(idPersona) === String(idPersonaItem);
  }

  private renderRegistrosSinParticipante(
    data: OriginacionExpedientePrintData,
    deudores: Registro[]
  ): string {
    const sinAsignar = (items: Registro[] | null): Registro[] =>
      (items || []).filter(item =>
        !deudores.some(deudor => this.mismoParticipante(item, deudor))
      );
    const fotos = {
      ...data.fotos,
      fotoFinanciero: sinAsignar(data.fotos.fotoFinanciero),
      fotoBienes: sinAsignar(data.fotos.fotoBienes),
      fotoCentralRiesgo: sinAsignar(data.fotos.fotoCentralRiesgo),
      fotoAnalisis: sinAsignar(data.fotos.fotoAnalisis)
    };
    if (!fotos.fotoFinanciero.length && !fotos.fotoBienes.length &&
        !fotos.fotoCentralRiesgo.length && !fotos.fotoAnalisis.length) {
      return '';
    }
    const individual = { ...data, fotos };
    return `
      <section style="break-before: page; page-break-before: always;">
        ${this.titulo('Registros pendientes de asociar a un participante')}
        ${this.nota('Estos registros no incluyen un identificador que permita relacionarlos de forma segura con el solicitante o un codeudor. Se conservan para revisión.')}
        ${fotos.fotoFinanciero.length ? this.renderFinanciero(individual) : ''}
        ${fotos.fotoBienes.length ? this.renderBienes(individual) : ''}
        ${fotos.fotoCentralRiesgo.length ? this.renderCentralRiesgo(individual) : ''}
        ${fotos.fotoAnalisis.length ? this.renderAnalisis(individual) : ''}
      </section>
    `;
  }

  // =========================================================
  // ENCABEZADO INSTITUCIONAL COOPVALLE
  // =========================================================

  private renderEncabezado(
    data: OriginacionExpedientePrintData
  ): string {

    const id = data.identificacion;

    const referencia =
      data.modo === 'ACTUACION'
        ? `Actuación N.º ${id.idSolicitudAprobacion ?? '—'}`
        : 'Información de originación';

    return `
      <header class="expediente-header">

        <div class="expediente-header__logo-contenedor">

          <img
            class="expediente-header__logo"
            src="/assets/LOGO_EMPRESA.png"
            alt="COOPVALLE"
          >

        </div>

        <div class="expediente-header__empresa">

          <div class="expediente-header__nombre">
            COOPERATIVA DE AHORRO Y CRÉDITO DEL VALLE DE SAN JOSÉ
          </div>

          <div class="expediente-header__nit">
            NIT: 890201545
          </div>

          <h1 class="expediente-header__documento">
            EXPEDIENTE DE SOLICITUD Y OTORGAMIENTO DE CRÉDITO
          </h1>

          <div class="expediente-header__referencia">
            Solicitud N.º ${id.idSolicitudCredito}
            · ${this.esc(referencia)}
          </div>

        </div>

      </header>
    `;
  }


  // =========================================================
  // 1. SOLICITUD DE CRÉDITO
  // =========================================================

  private renderSolicitud(
    data: OriginacionExpedientePrintData
  ): string {

    const solicitud =
      data.fotos.fotoSolicitud;

    if (!solicitud) {
      return this.sinDatos(
        'Solicitud de crédito'
      );
    }

    return `
      ${this.titulo('1. Datos de la Solicitud de crédito')}

      ${this.campos(solicitud, [

        {
          titulo: 'Número de solicitud',
          clave: 'numero_solicitud'
        },

        {
          titulo: 'Fecha de solicitud',
          clave: 'fecha_inicio_solicitud',
          tipo: 'fecha'
        },

        {
          titulo: 'Línea de crédito',
          clave: 'nombre_linea_credito'
        },

        {
          titulo: 'Clasificación',
          clave: 'nombre_clasificacion_credito'
        },

        {
          titulo: 'Destino económico',
          clave: 'nombre_destino_economico'
        },

        {
          titulo: 'Estado del trámite',
          clave: 'nombre_resultado'
        }

      ])}

      ${this.campos(solicitud, [

        {
          titulo: 'Valor solicitado',
          clave: 'valor_solicitado',
          tipo: 'dinero'
        },

        {
          titulo: 'Plazo solicitado (meses)',
          clave: 'plazo_solicitado'
        },

        {
          titulo: 'Tipo de cuota',
          clave: 'nombre_tipo_cuota'
        },

        {
          titulo: 'Cuota proyectada',
          clave: 'valor_cuota_proyectada',
          tipo: 'dinero'
        },

        {
          titulo: 'Tasa de colocación aplicada',
          clave: 'tasa_colocacion_aplicada',
          tipo: 'porcentaje'
        },

        {
          titulo: 'Tasa efectiva anual',
          clave: 'tasa_efectiva_anual',
          tipo: 'porcentaje'
        },

        {
          titulo: 'Modalidad de interés',
          clave: 'nombre_modalidad_interes'
        },

        {
          titulo: 'Periodicidad (meses)',
          clave: 'periodo_meses'
        },

      ])}

      ${this.campos(solicitud, [

        {
          titulo: 'Garantía',
          clave: 'nombre_garantia_credito'
        },

        {
          titulo: 'Subgarantía',
          clave: 'nombre_subgarantia'
        },

        {
          titulo: 'Fondo de garantías',
          clave: 'nombre_fondo_garantia'
        },

        {
          titulo: 'Valor del fondo',
          clave: 'valor_fondo_garantia',
          tipo: 'dinero'
        }

      ])}

      ${this.campos(solicitud, [

        {
          titulo: 'Aportes al inicio',
          clave: 'valor_aportes_inicio',
          tipo: 'dinero'
        },

        {
          titulo: 'Aportes requeridos',
          clave: 'valor_aportes_requerido',
          tipo: 'dinero'
        },

        {
          titulo: 'Cupo máximo por aportes',
          clave: 'cupo_maximo_por_aportes',
          tipo: 'dinero'
        },

        {
          titulo: 'Validación inicial',
          clave: 'cumple_aportes_inicio',
          tipo: 'estado'
        },

      ])}

      ${this.subtitulo('Observaciones del asesor')}

      <div class="expediente-observaciones">
        ${this.valor(solicitud, 'observacion_asesor')}
      </div>
    `;
  }


  // =========================================================
  // 2. SOLICITANTE Y CODEUDORES
  //
  // Presentación individual de identificación, información
  // personal y actividad económica y laboral.
  //
  // Los indicadores de cartera y mora se presentan en el
  // apartado de comportamiento crediticio.
  // =========================================================

  private renderDeudores(
    data: OriginacionExpedientePrintData
  ): string {

    const deudores =
      data.fotos.fotoDeudores || [];

    if (!deudores.length) {
      return this.sinDatos(
        'Solicitante y codeudores'
      );
    }

    const contenido =
      deudores.map((deudor, index) => {

        const esPrincipal =
          String(
            deudor['tipo_deudor'] ?? ''
          ).toUpperCase() === 'PRINCIPAL';

        return `
          <section class="expediente-persona">

            <div class="expediente-persona__encabezado">

              ${index + 1}.

              ${this.rol(deudor)}

              —

              ${this.valor(
                deudor,
                'nombre_completo'
              )}

            </div>

            ${this.renderComportamientoCrediticio(deudor)}

          </section>
        `;

      }).join('');

    return `
      ${this.titulo(
        '2. Solicitante y codeudores'
      )}

      ${contenido}
    `;
  }

  // =========================================================
  // DATOS COMPLEMENTARIOS DE HOJA DE VIDA
  // =========================================================

  private renderPersonaComplementaria(
    persona: OriginacionExpedientePersona | null
  ): string {

    if (!persona) {
      return '';
    }

    const registro: Registro = {
      ...persona
    };

    return `
      ${this.subtitulo('Información personal y de contacto')}

      ${this.campos(registro, [

        {
          titulo: 'Fecha de nacimiento',
          clave: 'fechaNacimiento',
          tipo: 'fecha'
        },

        {
          titulo: 'Estado civil',
          clave: 'estadoCivil'
        },

        {
          titulo: 'Dirección',
          clave: 'direccion'
        },

        {
          titulo: 'Teléfono',
          clave: 'telefono'
        },

        {
          titulo: 'Celular',
          clave: 'celular'
        },

        {
          titulo: 'Correo electrónico',
          clave: 'correoElectronico'
        },

        {
          titulo: 'Ciudad',
          clave: 'ciudad'
        },

        {
          titulo: 'Departamento',
          clave: 'departamento'
        }

      ])}

      ${this.subtitulo('Actividad económica y laboral')}

      ${this.campos(registro, [

        {
          titulo: 'Actividad económica',
          clave: 'actividadEconomica'
        },

        {
          titulo: 'Ocupación',
          clave: 'ocupacion'
        },

        {
          titulo: 'Empresa',
          clave: 'empresa'
        },

        {
          titulo: 'Cargo',
          clave: 'cargo'
        },

        {
          titulo: 'Dirección laboral',
          clave: 'direccionLaboral'
        },

        {
          titulo: 'Teléfono laboral',
          clave: 'telefonoLaboral'
        }

      ])}
    `;
  }


  // =========================================================
  // 3. INFORMACIÓN FINANCIERA
  // =========================================================

  private renderFinanciero(
    data: OriginacionExpedientePrintData,
    mostrarEncabezadoPersona = true
  ): string {

    const registros =
      data.fotos.fotoFinanciero || [];

    if (!registros.length) {
      return this.sinDatos(
        '3. Información financiera'
      );
    }

    const contenido = registros.map(item => {

      const financiero =
        this.objeto(item['financiero']);

      // -----------------------------------------------------
      // TABLA DE INGRESOS
      // -----------------------------------------------------

      const tablaIngresos = this.tablaFinanciera(
        financiero,
        [
          {
            titulo: 'Salario',
            clave: 'valor_salario'
          },
          {
            titulo: 'Pensión',
            clave: 'valor_pension'
          },
          {
            titulo: 'Ingresos independientes',
            clave: 'ingreso_independiente'
          },
          {
            titulo: 'Otros ingresos',
            clave: 'otros_ingresos'
          }
        ],
        'Ingresos mensuales totales',
        'ingresos_totales_natural'
      );

      // -----------------------------------------------------
      // TABLA DE EGRESOS
      //
      // Se agrega una fila visual vacía antes del total.
      // No se modifica la fotografía financiera.
      // -----------------------------------------------------

      const tablaEgresos = this.tablaFinanciera(
        financiero,
        [
          {
            titulo: 'Cuotas de créditos',
            clave: 'egresos_credito'
          },
          {
            titulo: 'Gastos familiares',
            clave: 'egresos_familiares'
          },
          {
            titulo: 'Otros egresos',
            clave: 'otros_egresos'
          }
        ],
        'Egresos mensuales totales',
        'egresos_totales_natural'
      ).replace(
        /(<tr\b[^>]*class="[^"]*expediente-tabla__total[^"]*"[^>]*>)/,
        `
          <tr class="expediente-financiero-fila-vacia">
            <td>&nbsp;</td>
            <td>&nbsp;</td>
          </tr>
          $1
        `
      );

      return `
        <section class="expediente-persona">

          ${mostrarEncabezadoPersona
            ? this.encabezadoPersona(data, item)
            : ''}

          <div class="expediente-financiero-columnas">

            <div class="expediente-financiero-columna">

              ${this.subtitulo(
                'Ingresos mensuales declarados'
              )}

              ${tablaIngresos}

            </div>

            <div class="expediente-financiero-columna">

              ${this.subtitulo(
                'Egresos mensuales declarados'
              )}

              ${tablaEgresos}

            </div>

          </div>

          <div class="expediente-financiero-descripciones">

            <div class="expediente-financiero-descripcion">

              <span class="expediente-campo__label">
                Origen de fondos
              </span>

              <span class="expediente-campo__valor">
                ${this.valor(
                  financiero,
                  'origen_fondos'
                )}
              </span>

            </div>

            <div class="expediente-financiero-descripcion">

              <span class="expediente-campo__label">
                Descripción de otros ingresos
              </span>

              <span class="expediente-campo__valor">
                ${this.valor(
                  financiero,
                  'comentario_otros_ingresos'
                )}
              </span>

            </div>

            <div class="expediente-financiero-descripcion">

              <span class="expediente-campo__label">
                Descripción de otros egresos
              </span>

              <span class="expediente-campo__valor">
                ${this.valor(
                  financiero,
                  'comentario_otros_egresos'
                )}
              </span>

            </div>

          </div>

          ${this.subtitulo('Patrimonio declarado')}

          <div class="expediente-patrimonio-columnas">

            <div class="expediente-patrimonio-columna">

              <table class="expediente-tabla expediente-tabla--patrimonio">

                <thead>
                  <tr>
                    <th>Concepto</th>
                    <th class="expediente-tabla__numero">Valor</th>
                  </tr>
                </thead>

                <tbody>

                  <tr>
                    <td>Total activos</td>
                    <td class="expediente-tabla__numero">
                      ${this.formato(financiero['total_activos'], 'dinero')}
                    </td>
                  </tr>

                  <tr>
                    <td>Total pasivos</td>
                    <td class="expediente-tabla__numero">
                     ${this.formato(financiero['total_pasivos'], 'dinero')}
                    </td>
                  </tr>

                  <tr class="expediente-tabla__total">
                    <td>Patrimonio</td>
                    <td class="expediente-tabla__numero">
                     ${this.formato(financiero['patrimonio_total'], 'dinero')}
                    </td>
                  </tr>

                </tbody>

              </table>

            </div>

            <div class="expediente-patrimonio-columna">

              <table class="expediente-tabla expediente-tabla--patrimonio">

                <thead>
                  <tr>
                    <th>Concepto</th>
                    <th>Información</th>
                  </tr>
                </thead>

                <tbody>

                  <tr>
                    <td>Relación financiera</td>
                    <td>
                      ${this.valor(financiero, 'relacion_financiera')}
                    </td>
                  </tr>

                  <tr>
                    <td>Deuda en relación financiera</td>
                    <td class="expediente-tabla__numero">
                      ${this.formato(financiero['deuda_relacion_financiera'], 'dinero')}
                    </td>
                  </tr>

                </tbody>

              </table>

            </div>

          </div>

        </section>
      `;

    }).join('');

    return `
      ${this.titulo('3. Información financiera')}

      ${contenido}

      ${this.nota(
        'Los valores corresponden a la información registrada ' +
        'para cada persona. Los totales se presentan como fueron ' +
        'conservados en la fotografía financiera.'
      )}
    `;
  }


  // =========================================================
  // TABLA DETALLADA DE INGRESOS Y EGRESOS
  // =========================================================

  private tablaFinanciera(
    financiero: Registro,
    conceptos: CampoDocumento[],
    tituloTotal: string,
    claveTotal: string
  ): string {

    const filas =
      conceptos.map(concepto => `
        <tr>

          <td>
            ${this.esc(concepto.titulo)}
          </td>

          <td class="expediente-tabla__numero">
            ${this.formato(
              financiero[concepto.clave],
              'dinero'
            )}
          </td>

        </tr>
      `).join('');

    return `
      <table class="expediente-tabla">

        <thead>
          <tr>
            <th>Concepto</th>
            <th class="expediente-tabla__numero">
              Valor mensual
            </th>
          </tr>
        </thead>

        <tbody>
          ${filas}
        </tbody>

        <tfoot>
          <tr class="expediente-tabla__total">

            <td>
              ${this.esc(tituloTotal)}
            </td>

            <td class="expediente-tabla__numero">
              ${this.formato(
                financiero[claveTotal],
                'dinero'
              )}
            </td>

          </tr>
        </tfoot>

      </table>
    `;
  }


  // =========================================================
  // 4. BIENES Y GARANTÍAS
  // =========================================================

  private renderBienes(
    data: OriginacionExpedientePrintData
  ): string {

    const bienes =
      data.fotos.fotoBienes || [];

   if (!bienes.length) {
     return this.sinDatos(
       '4. Bienes y garantías'
     );
   }

    const contenido =
      bienes.map((bien, index) => `
        <section class="expediente-bien">

          <div class="expediente-bien__titulo">
            Bien ${index + 1}
            —
            ${this.valor(bien, 'descripcion_bien')}
          </div>

          <div class="expediente-bien__contenido">

            ${this.encabezadoPersona(data, bien)}

            ${this.campos(bien, [

              {
                titulo: 'Valor comercial',
                clave: 'valor_comercial',
                tipo: 'dinero'
              },

              {
                titulo: 'Porcentaje de propiedad',
                clave: 'porcentaje_propiedad',
                tipo: 'porcentaje'
              },

              {
                titulo: 'Gravámenes',
                clave: 'valor_gravamen',
                tipo: 'dinero'
              },

              {
                titulo: 'Comprometido en créditos',
                clave: 'valor_comprometido_creditos',
                tipo: 'dinero'
              },

              {
                titulo: 'Valor asignado a la solicitud',
                clave: 'valor_asignado_solicitud',
                tipo: 'dinero'
              },

              {
                titulo: 'Garantía admisible',
                clave: 'valor_garantia_admisible',
                tipo: 'dinero'
              },

              {
                titulo: 'Garantía disponible',
                clave: 'valor_garantia_disponible',
                tipo: 'dinero'
              },

              {
                titulo: 'Observaciones',
                clave: 'observacion'
              }

            ])}

            <div class="expediente-nota">
              Garantía real de esta solicitud:
              ${this.siNo(bien['es_garantia_real'])}
            </div>

          </div>

        </section>
      `).join('');

    return `
      ${this.titulo('4. Bienes y garantías')}

      ${contenido}

      ${this.nota(
        'El registro de un bien no acredita por sí solo ' +
        'la constitución de una garantía.'
      )}
    `;
  }


  // =========================================================
  // 5. CENTRALES DE RIESGO
  // =========================================================

  private renderCentralRiesgo(
    data: OriginacionExpedientePrintData,
    mostrarEncabezadoPersona = true
  ): string {

    const registros =
      data.fotos.fotoCentralRiesgo || [];

    if (!registros.length) {
      return this.sinDatos(
        '5. Centrales de riesgo'
      );
    }

    const contenido =
      registros.map(item => {

        const central =
          this.objeto(
            item['central_riesgo']
          );

        return `
          <section class="expediente-persona">

            ${mostrarEncabezadoPersona
              ? this.encabezadoPersona(data, item)
              : ''}

            ${this.campos(central, [

              {
                titulo: 'Fecha de consulta',
                clave: 'fecha_consulta',
                tipo: 'fecha'
              },

              {
                titulo: 'Puntaje',
                clave: 'puntaje_central'
              },

              {
                titulo: 'Calificación cualitativa',
                clave: 'calificacion_cualitativa'
              },

              {
                titulo: 'Saldo actual de obligaciones',
                clave: 'saldo_actual_obligaciones',
                tipo: 'dinero'
              },

              {
                titulo: 'Cuotas mensuales',
                clave: 'valor_cuotas_mensuales',
                tipo: 'dinero'
              },

              {
                titulo: 'Refinanciaciones',
                clave: 'cantidad_refinanciaciones'
              },

              {
                titulo: 'Reestructuraciones',
                clave: 'cantidad_reestructuraciones'
              },

              {
                titulo: 'Cuentas embargadas',
                clave: 'cantidad_cuentas_embargadas'
              }

            ])}

            ${this.subtitulo('Calificaciones reportadas')}

            ${this.campos(central, [

              {
                titulo: 'Calificación A',
                clave: 'cantidad_calificacion_a'
              },

              {
                titulo: 'Calificación B',
                clave: 'cantidad_calificacion_b'
              },

              {
                titulo: 'Calificación C',
                clave: 'cantidad_calificacion_c'
              },

              {
                titulo: 'Calificación D',
                clave: 'cantidad_calificacion_d'
              },

              {
                titulo: 'Calificación E',
                clave: 'cantidad_calificacion_e'
              },

              {
                titulo: 'Calificación K',
                clave: 'cantidad_calificacion_k'
              }

            ])}

            ${this.subtitulo('Observaciones')}

            <div class="expediente-observaciones">
              ${this.valor(central, 'observacion')}
            </div>

          </section>
        `;
      }).join('');

    return `
      ${this.titulo('5. Centrales de riesgo')}

      ${contenido}
    `;
  }


  // =========================================================
  // 6. ANÁLISIS DE OTORGAMIENTO
  // =========================================================

  private renderAnalisis(
    data: OriginacionExpedientePrintData,
    mostrarEncabezadoPersona = true
  ): string {

    const analisis =
      data.fotos.fotoAnalisis || [];

    if (!analisis.length) {
      return this.sinDatos(
        '6. Análisis de otorgamiento'
      );
    }

    const contenido =
      analisis.map(item => `
        <section class="expediente-persona">

          ${mostrarEncabezadoPersona
            ? this.encabezadoPersona(data, item)
            : ''}

          ${this.subtitulo('Capacidad de pago')}

          ${this.campos(item, [

            {
              titulo: 'Ingresos considerados',
              clave: 'total_ingresos',
              tipo: 'dinero'
            },

            {
              titulo: 'Egresos considerados',
              clave: 'total_egresos',
              tipo: 'dinero'
            },

            {
              titulo: 'Ingreso disponible',
              clave: 'ingreso_disponible',
              tipo: 'dinero'
            },

            {
              titulo: 'Cuota proyectada',
              clave: 'valor_cuota_proyectada',
              tipo: 'dinero'
            },

            {
              titulo: 'Indicador de capacidad de pago',
              clave: 'indicador_capacidad_pago',
              tipo: 'porcentaje'
            },

            {
              titulo: 'Cumple capacidad de pago',
              clave: 'cumple_capacidad_pago',
              tipo: 'estado'
            },

            {
              titulo: 'Indicador de endeudamiento',
              clave: 'indicador_endeudamiento'
            }

          ])}

          ${this.subtitulo('Comportamiento y garantías')}

          ${this.campos(item, [

            {
              titulo: 'Calificación de centrales',
              clave: 'calificacion_central'
            },

            {
              titulo: 'Puntaje de central',
              clave: 'puntaje_central'
            },

            {
              titulo: 'Eventos de mora',
              clave: 'cantidad_eventos_mora'
            },

            {
              titulo: 'Mora máxima últimos 24 meses',
              clave: 'mora_maxima_24_meses'
            },

            {
              titulo: 'Edad de riesgo máxima',
              clave: 'edad_riesgo_maxima'
            },

            {
              titulo: 'Garantías asignadas',
              clave: 'valor_garantias_asignado',
              tipo: 'dinero'
            },

            {
              titulo: 'Cumple garantías',
              clave: 'cumple_garantias',
              tipo: 'estado'
            }

          ])}

          ${this.subtitulo('Resultado del modelo')}

          ${this.campos(item, [

            {
              titulo: 'Perfil de riesgo',
              clave: 'perfil_riesgo'
            },

            {
              titulo: 'Puntaje total',
              clave: 'puntaje_total'
            },

            {
              titulo: 'Recomendación',
              clave: 'recomendacion'
            },

            {
              titulo: 'Cumple otorgamiento',
              clave: 'cumple_otorgamiento',
              tipo: 'estado'
            },

            {
              titulo: 'Fecha del análisis',
              clave: 'fecha_analisis',
              tipo: 'fecha'
            },

            {
              titulo: 'Motivo del resultado',
              clave: 'motivo_resultado'
            }

          ])}

        </section>
      `).join('');

    return `
      ${this.titulo('6. Análisis de otorgamiento')}

      ${contenido}

      ${this.nota(
        'Los indicadores y resultados corresponden al ' +
        'modelo registrado. La recomendación no sustituye ' +
        'la decisión del ente de aprobación.'
      )}
    `;
  }


  // =========================================================
  // 7. HISTORIAL DE APROBACIONES
  // =========================================================

  private renderAprobaciones(
    data: OriginacionExpedientePrintData
  ): string {

    const actuaciones =
      data.actuaciones || [];

    if (!actuaciones.length) {
      return this.sinDatos(
        '7. Historial de aprobación'
      );
    }

    const contenido =
      actuaciones.map((actuacion, index) => {

        const registro: Registro = {
          ...actuacion
        };

        return `
          <section class="expediente-aprobacion">

            <div class="expediente-aprobacion__titulo">
              Actuación ${index + 1}
              —
              ${this.esc(actuacion.nombreEnteAprobacion)}
            </div>

            <div class="expediente-aprobacion__contenido">

              ${this.campos(registro, [

                {
                  titulo: 'Decisión',
                  clave: 'nombreDecision'
                },

                {
                  titulo: 'Fecha de decisión',
                  clave: 'fechaDecision',
                  tipo: 'fecha'
                },

                {
                  titulo: 'Número de acta',
                  clave: 'numeroActa'
                },

                {
                  titulo: 'Fecha de acta',
                  clave: 'fechaActa',
                  tipo: 'fecha'
                },

                {
                  titulo: 'Responsable',
                  clave: 'nombreUsuarioDecision'
                }

              ])}

              ${this.subtitulo('Concepto registrado')}

              <div class="expediente-aprobacion__concepto">
                ${this.esc(actuacion.concepto)}
              </div>

            </div>

          </section>
        `;
      }).join('');

    return `
      ${this.titulo('7. Historial de aprobación')}

      ${contenido}
    `;
  }


  // =========================================================
  // PIE DEL DOCUMENTO
  // =========================================================

  private renderPie(
    data: OriginacionExpedientePrintData
  ): string {

    return `
      <footer class="expediente-footer">

        COOPVALLE
        —
        Expediente de solicitud de crédito

        <div class="expediente-footer__referencia">

          Solicitud:
          ${this.esc(
            data.identificacion.numeroSolicitud
            || String(data.identificacion.idSolicitudCredito)
          )}

          ${
            data.identificacion.idSolicitudAprobacion
              ? `
                —
                Actuación:
                ${data.identificacion.idSolicitudAprobacion}
              `
              : ''
          }

        </div>

      </footer>
    `;
  }


  // =========================================================
  // COMPONENTES DOCUMENTALES
  // =========================================================

  private titulo(
    texto: string
  ): string {

    return `
      <h2 class="expediente-seccion__titulo">
        ${this.esc(texto)}
      </h2>
    `;
  }


  private subtitulo(
    texto: string
  ): string {

    return `
      <h3 class="expediente-seccion__subtitulo">
        ${this.esc(texto)}
      </h3>
    `;
  }


  private nota(
    texto: string
  ): string {

    return `
      <div class="expediente-nota">
        ${this.esc(texto)}
      </div>
    `;
  }


  private sinDatos(
    titulo: string
  ): string {

    return `
      ${this.titulo(titulo)}

      <div class="expediente-sin-datos">
        No hay información disponible para esta sección.
      </div>
    `;
  }


  private campos(
    registro: Registro,
    campos: CampoDocumento[]
  ): string {

    const contenido = campos.map(campo => {

      const esNumerico =
        campo.tipo === 'dinero'
        || campo.tipo === 'porcentaje'
        || campo.tipo === 'numero';

      return `
        <div class="expediente-campo
          ${esNumerico ? 'expediente-campo--numerico' : ''}">

          <span class="expediente-campo__label">
            ${this.esc(campo.titulo)}
          </span>

          <span class="expediente-campo__valor">
            ${this.formato(
              registro[campo.clave],
              campo.tipo || 'texto'
            )}
          </span>

        </div>
      `;

    }).join('');

    return `
      <div class="expediente-grid">
        ${contenido}
      </div>
    `;
  }


  // =========================================================
  // RELACIÓN ENTRE FOTOGRAFÍAS Y DEUDORES
  // =========================================================

  private encabezadoPersona(
    data: OriginacionExpedientePrintData,
    registro: Registro
  ): string {

    const deudor =
      this.buscarDeudor(
        data,
        registro['id_solicitud_deudor']
      );

    const nombre =
      deudor
        ? this.valor(
            deudor,
            'nombre_completo'
          )
        : 'Persona vinculada';

    const rol =
      deudor
        ? this.rol(deudor)
        : 'Deudor';

    return `
      <div class="expediente-persona__encabezado">

        <span class="expediente-persona__tipo">
          ${rol}
        </span>

        ${nombre}

      </div>
    `;
  }


  private buscarDeudor(
    data: OriginacionExpedientePrintData,
    idSolicitudDeudor: unknown
  ): Registro | null {

    return (
      data.fotos.fotoDeudores || []
    ).find(
      deudor =>
        String(deudor['id_solicitud_deudor'])
        === String(idSolicitudDeudor)
    ) || null;
  }


  private buscarPersona(
    data: OriginacionExpedientePrintData,
    deudor: Registro
  ): OriginacionExpedientePersona | null {

    const id =
      deudor['id_datos_personal'];

    return data.personas.find(
      persona =>
        persona.idDatosPersonal
        === Number(id)
    ) || null;
  }


  private rol(
    deudor: Registro
  ): string {

    const tipo =
      String(
        deudor['tipo_deudor'] || ''
      ).toUpperCase();

    if (tipo === 'PRINCIPAL') {
      return 'Solicitante';
    }

    if (tipo === 'CODEUDOR') {
      return 'Codeudor';
    }

    return this.esc(
      tipo || 'Deudor'
    );
  }


  // =========================================================
  // FORMATO DE VALORES
  // =========================================================

  private valor(
    registro: Registro,
    clave: string
  ): string {

    return this.formato(
      registro[clave],
      'texto'
    );
  }


  private formato(
    valor: unknown,
    tipo: TipoValor
  ): string {

    if (
      valor === null
      || valor === undefined
      || valor === ''
    ) {
      return 'No registrado';
    }

    if (tipo === 'estado') {

      if (valor === true) {
        return 'Cumple';
      }

      if (valor === false) {
        return 'No cumple';
      }

      return 'No evaluado';
    }

    if (tipo === 'fecha') {
      return this.fechaTexto(valor);
    }

    if (tipo === 'numero') {

      const numero = Number(valor);

      if (!Number.isFinite(numero)) {
        return 'No registrado';
      }

      return this.numeroFormatter.format(numero);
    }

    if (
      tipo === 'dinero'
      || tipo === 'porcentaje'
    ) {

      const numero =
        Number(valor);

      if (!Number.isFinite(numero)) {
        return 'No registrado';
      }

      if (tipo === 'dinero') {
        return this.monedaFormatter.format(
          numero
        );
      }

      return (
        this.numeroFormatter.format(numero)
        + ' %'
      );
    }

    if (typeof valor === 'boolean') {
      return valor ? 'Sí' : 'No';
    }

    return this.esc(
      String(valor).replaceAll('_', ' ')
    );
  }


  private fechaTexto(
    valor: unknown
  ): string {

    if (
      typeof valor !== 'string'
      || !valor
    ) {
      return 'No registrada';
    }

    const fecha =
      valor.slice(0, 10);

    const partes =
      fecha.split('-');

    if (partes.length !== 3) {
      return this.esc(fecha);
    }

    return this.esc(
      `${partes[2]}/${partes[1]}/${partes[0]}`
    );
  }


  private siNo(
    valor: unknown
  ): string {

    if (valor === true) {
      return 'Sí';
    }

    if (valor === false) {
      return 'No';
    }

    return 'No registrado';
  }


  private objeto(
    valor: unknown
  ): Registro {

    if (
      valor
      && typeof valor === 'object'
      && !Array.isArray(valor)
    ) {
      return valor as Registro;
    }

    return {};
  }


  // =========================================================
  // SEGURIDAD DE CONTENIDO HTML
  // =========================================================

  private esc(
    valor: unknown
  ): string {

    return String(
      valor ?? ''
    )
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');
  }

}
