import type {
  OriginacionCompromisosPrintData
} from './originacion-compromisos-print.models';

import type {
  OriginacionCartera,
  OriginacionVectorResumen
} from '../contexto/originacion-contexto.models';


type Registro = Record<string, unknown>;


interface FilaInformeCartera {

  linea: string;

  valorInicial: number | null;

  valorActual: number | null;

  diasMora: number | null;

}


// =========================================================
// COMPROMISOS, OBLIGACIONES Y AUTORIZACIONES ESPECIALES
// UNA HOJA TAMAÑO CARTA
// =========================================================

export class OriginacionCompromisosPrintDocument {

  private readonly moneda = new Intl.NumberFormat(
    'es-CO',
    {
      style: 'currency',
      currency: 'COP',
      maximumFractionDigits: 0
    }
  );

  private readonly numero = new Intl.NumberFormat(
    'es-CO',
    {
      maximumFractionDigits: 2
    }
  );


  // =========================================================
  // CONSTRUIR DOCUMENTO
  // =========================================================

  construir(
    data: OriginacionCompromisosPrintData
  ): string {

    const fotos = data.fotos;

    const deudores =
      fotos.fotoDeudores ?? [];

    const principal = deudores.find(item =>

      String(
        item['tipo_deudor'] ?? ''
      ).trim().toUpperCase() === 'PRINCIPAL'

    );

    if (!principal) {

      throw new Error(
        'No se encontró el deudor principal en la fotografía de la solicitud.'
      );

    }


    const financiero = this.asociado(
      fotos.fotoFinanciero ?? [],
      principal
    );

    const detalleFinanciero = this.objeto(
      financiero?.['financiero']
    );

    const analisis = this.asociado(
      fotos.fotoAnalisis ?? [],
      principal
    );

    const central = this.objeto(

      this.asociado(
        fotos.fotoCentralRiesgo ?? [],
        principal
      )?.['central_riesgo']

    );

    const codeudores = deudores.filter(
      item => item !== principal
    );


    // =====================================================
    // INDICADORES ECONÓMICOS DEL DEUDOR PRINCIPAL
    // =====================================================

    const ingresos = this.primero(
      detalleFinanciero,
      [
        'ingresos_totales_natural'
      ]
    );

    const egresos = this.primero(
      detalleFinanciero,
      [
        'egresos_totales_natural'
      ]
    );

    const cuotas = this.primero(
      central,
      [
        'valor_cuotas_mensuales'
      ]
    );

    const disponible = this.primero(
      analisis ?? {},
      [
        'indicador_capacidad_pago',
        'porcentaje_disponible'
      ]
    );


    // =====================================================
    // INFORME DE CARTERA DEL DEUDOR PRINCIPAL
    // =====================================================

    const filasCartera = this.construirInformeCartera(
      data.carteraActual ?? [],
      data.vectorResumen ?? []
    );


    // =====================================================
    // DOCUMENTO HTML
    // =====================================================

    return `

      <main class="compromisos">

        <header class="cabecera">

          <div class="cabecera__logo-contenedor">

            <img
              class="cabecera__logo"
              src="/assets/LOGO_EMPRESA.png"
              alt="COOPVALLE"
            >

          </div>


          <div class="cabecera__contenido">

            <div class="cabecera__empresa">
              COOPERATIVA DE AHORRO Y CRÉDITO DEL VALLE DE SAN JOSÉ LTDA.
            </div>

            <div class="cabecera__nombre">
              COOPVALLE
            </div>

            <div class="cabecera__nit">
              NIT: 890201545-4
            </div>

            <div class="cabecera__titulo">
              Compromisos, obligaciones y autorizaciones especiales
            </div>

            <div class="referencia">

              Solicitud ${this.esc(
                data.numeroSolicitud ??
                String(data.idSolicitudCredito)
              )}

              · Fecha de impresión:
              ${this.esc(data.fechaImpresion)}

            </div>

          </div>

        </header>


        <p class="declaracion">

          De acuerdo con lo establecido por la ley y las
          entidades de control y vigilancia que ejercen
          control sobre COOPVALLE, declaro que:

        </p>


        <ol class="clausulas">

          <li>

            La información aquí suministrada es auténtica
            y veraz; por lo tanto, autorizo a COOPVALLE
            para verificarla a través de los medios que
            considere convenientes.

          </li>


          <li>

            Autorizo a COOPVALLE, a quien represente sus
            derechos y ostente la calidad de acreedor,
            a reportar, actualizar, solicitar y divulgar
            a las centrales de información del sector y
            a las entidades que administren bases de datos
            con los mismos fines, la información referente
            a mi comportamiento comercial y al cumplimiento
            de mis obligaciones, incluidas las actuales,
            pasadas y futuras.

          </li>


          <li>

            El incumplimiento de cualquiera de estos
            compromisos y obligaciones, así como de
            cualquier otro adquirido con COOPVALLE,
            será causa justa y suficiente para la
            terminación unilateral de los contratos
            con esta entidad, entre ellos los de
            cuentas de ahorro y certificados de
            depósito a término.

          </li>


          <li>

            Acepto que COOPVALLE no adquiere compromiso
            alguno con la presentación de esta solicitud
            de crédito.

          </li>


          <li>

            Yo, solicitante de este crédito, autorizo
            a COOPVALLE para abonar el neto de este
            préstamo a mis cuentas de ahorros.

          </li>


          <li>

            Autorizo a COOPVALLE para que debite de mi
            cuenta de depósitos los valores correspondientes
            a las cuotas de créditos otorgados por la
            Cooperativa y los costos de consultas con
            tarjeta débito en cajeros automáticos para
            conocer mi saldo de ahorros o cupo rotativo
            de crédito.

          </li>

        </ol>


        <!-- ============================================= -->
        <!-- FIRMAS DE LOS PARTICIPANTES                  -->
        <!-- ============================================= -->

        <div class="seccion">
          Firmas de los participantes
        </div>


        <div class="firmas">

          ${[principal, ...codeudores].map(
            (deudor, indice) => `

              <div class="firma">

                <div class="firma__linea"></div>

                <div class="firma__rol">

                  ${
                    indice === 0
                      ? 'DEUDOR PRINCIPAL'
                      : `CODEUDOR N.º ${indice}`
                  }

                </div>

                <div class="firma__nombre">

                  ${this.esc(
                    this.texto(
                      deudor['nombre_completo']
                    )
                  )}

                </div>

                <div class="firma__documento">

                  ${this.esc(
                    this.texto(
                      deudor['tipo_documento']
                    )
                  )}

                  ${this.esc(
                    this.texto(
                      deudor['documento']
                    )
                  )}

                </div>

              </div>

            `
          ).join('')}

        </div>


        <!-- ============================================= -->
        <!-- INFORME DE CARTERA                           -->
        <!-- ============================================= -->

        <div class="seccion">

          Informe de cartera — deudor principal

        </div>


        <table class="tabla">

          <thead>

            <tr>

              <th>Línea</th>

              <th>Valor inicial</th>

              <th>Valor actual</th>

              <th>Mora (días)</th>

            </tr>

          </thead>


          <tbody>

            ${
              filasCartera.length > 0

                ? filasCartera.map(fila => `

                    <tr>

                      <td>

                        ${this.esc(
                          fila.linea
                        )}

                      </td>


                      <td class="numero">

                        ${this.dinero(
                          fila.valorInicial
                        )}

                      </td>


                      <td class="numero">

                        ${this.dinero(
                          fila.valorActual
                        )}

                      </td>


                      <td class="numero">

                        ${this.esc(
                          fila.diasMora ?? '—'
                        )}

                      </td>

                    </tr>

                  `).join('')

                : `

                    <tr>

                      <td colspan="4">

                        El deudor principal no registra
                        créditos vigentes en COOPVALLE.

                      </td>

                    </tr>

                  `
            }

          </tbody>

        </table>


        <!-- ============================================= -->
        <!-- INDICADORES ECONÓMICOS                       -->
        <!-- ============================================= -->

        <div class="indicadores">

          ${this.indicador(
            'Valor ingresos',
            this.dinero(ingresos)
          )}

          ${this.indicador(
            'Valor egresos',
            this.dinero(egresos)
          )}

          ${this.indicador(
            'Valor cuotas',
            this.dinero(cuotas)
          )}

          ${this.indicador(
            '% disponible',
            this.porcentaje(disponible)
          )}

        </div>


        <!-- ============================================= -->
        <!-- OBSERVACIONES                                -->
        <!-- ============================================= -->

        <div class="observaciones">

          <div class="observaciones__titulo">

            OBSERVACIONES:

          </div>

          ${
            Array.from(
              { length: 7 },
              () => '<div class="observaciones__linea"></div>'
            ).join('')
          }

        </div>


        <!-- ============================================= -->
        <!-- JEFE DE CARTERA                              -->
        <!-- ============================================= -->

        <div class="jefe">

          <div class="jefe__linea"></div>

          <div class="jefe__texto">

            FIRMA JEFE DE CARTERA

          </div>

        </div>

      </main>

    `;

  }


  // =========================================================
  // CONSTRUIR INFORME DE CARTERA
  // =========================================================

  private construirInformeCartera(
    carteraActual: OriginacionCartera[],
    vectorResumen: OriginacionVectorResumen[]
  ): FilaInformeCartera[] {

    const filas: FilaInformeCartera[] = [];


    // =====================================================
    // INDEXAR VECTOR POR ID DE CRÉDITO
    // =====================================================

    const vectorPorCredito = new Map<
      number,
      OriginacionVectorResumen
    >();

    for (const vector of vectorResumen) {

      vectorPorCredito.set(
        vector.idCarteraCredito,
        vector
      );

    }


    // =====================================================
    // ACUMULADOR LÍNEA 010
    // =====================================================

    const avances: FilaInformeCartera[] = [];


    // =====================================================
    // RECORRER CARTERA ACTUAL
    // =====================================================

    for (const credito of carteraActual) {

      const saldoActual =
        this.valorNumerico(
          credito.saldoActual
        );


      // Solo créditos con saldo vigente.

      if (
        saldoActual === null ||
        saldoActual <= 0
      ) {

        continue;

      }


      const vector =
        vectorPorCredito.get(
          credito.idCarteraCredito
        );


      // ===================================================
      // VALOR INICIAL
      // ===================================================

      const valorInicial =
        this.valorNumerico(
          vector?.valorInicialCredito ??
          vector?.valorDesembolsado
        );


      // ===================================================
      // LÍNEA
      // ===================================================

      const codigoLinea =
        String(
          credito.codigoLineaCredito ??
          credito.idLineaCredito ??
          ''
        ).trim().padStart(3, '0');


      const nombreLinea =
        credito.nombreLineaCredito ??
        vector?.nombreLineaCredito ??
        codigoLinea;


      const fila: FilaInformeCartera = {

        linea:
          nombreLinea,

        valorInicial,

        valorActual:
          saldoActual,

        diasMora:
          this.valorNumerico(
            credito.diasMora
          )

      };


      // ===================================================
      // CONSOLIDAR EXCLUSIVAMENTE LÍNEA 010
      // ===================================================

      if (codigoLinea === '010') {

        avances.push(
          fila
        );

      } else {

        filas.push(
          fila
        );

      }

    }


    // =====================================================
    // CONSOLIDACIÓN DE AVANCES DE TARJETA
    // =====================================================

    if (avances.length > 0) {

      const valoresIniciales =
        avances
          .map(item => item.valorInicial)
          .filter(
            (valor): valor is number =>
              valor !== null
          );

      const valoresActuales =
        avances
          .map(item => item.valorActual)
          .filter(
            (valor): valor is number =>
              valor !== null
          );

      const moras =
        avances
          .map(item => item.diasMora)
          .filter(
            (valor): valor is number =>
              valor !== null
          );


      const valorInicial =
        valoresIniciales.length === avances.length

          ? valoresIniciales.reduce(
              (total, valor) =>
                total + valor,
              0
            )

          : null;


      const valorActual =
        valoresActuales.reduce(
          (total, valor) =>
            total + valor,
          0
        );


      const diasMora =
        moras.length > 0

          ? Math.max(
              ...moras
            )

          : null;


      filas.push({

        linea:
          'CUPO ROTATIVO',

        valorInicial,

        valorActual,

        diasMora

      });

    }


    return filas;

  }


  // =========================================================
  // CONVERSIÓN NUMÉRICA SEGURA
  // =========================================================

  private valorNumerico(
    valor: unknown
  ): number | null {

    if (
      valor === null ||
      valor === undefined ||
      valor === ''
    ) {

      return null;

    }

    const numero =
      Number(valor);

    return Number.isFinite(numero)
      ? numero
      : null;

  }


  // =========================================================
  // INDICADORES
  // =========================================================

  private indicador(
    titulo: string,
    valor: string
  ): string {

    return `

      <div class="indicador">

        <span class="indicador__titulo">

          ${this.esc(titulo)}

        </span>

        <span class="indicador__valor">

          ${valor}

        </span>

      </div>

    `;

  }


  // =========================================================
  // BUSCAR INFORMACIÓN DEL DEUDOR PRINCIPAL
  // =========================================================

  private asociado(
    registros: Registro[],
    principal: Registro
  ): Registro | null {

    const idDeudor =
      principal['id_solicitud_deudor'];

    const idPersona =
      principal['id_datos_personal'];


    return registros.find(item =>

      idDeudor != null &&

      String(
        item['id_solicitud_deudor'] ?? ''
      ) === String(idDeudor)

    ) ?? registros.find(item =>

      idPersona != null &&

      String(
        item['id_datos_personal'] ?? ''
      ) === String(idPersona)

    ) ?? null;

  }


  // =========================================================
  // CONVERTIR A OBJETO
  // =========================================================

  private objeto(
    valor: unknown
  ): Registro {

    return valor &&
      typeof valor === 'object' &&
      !Array.isArray(valor)

        ? valor as Registro

        : {};

  }


  // =========================================================
  // PRIMER VALOR DISPONIBLE
  // =========================================================

  private primero(
    registro: Registro,
    claves: string[]
  ): unknown {

    for (const clave of claves) {

      const valor =
        registro[clave];

      if (
        valor !== null &&
        valor !== undefined &&
        valor !== ''
      ) {

        return valor;

      }

    }

    return null;

  }


  // =========================================================
  // TEXTO
  // =========================================================

  private texto(
    valor: unknown
  ): string {

    return valor === null ||
      valor === undefined ||
      valor === ''

        ? '________________'

        : String(valor);

  }


  // =========================================================
  // DINERO
  // =========================================================

  private dinero(
    valor: unknown
  ): string {

    if (
      valor === null ||
      valor === undefined ||
      valor === ''
    ) {

      return '—';

    }

    const numero =
      Number(valor);

    return Number.isFinite(numero)

      ? this.esc(
          this.moneda.format(numero)
        )

      : '—';

  }


  // =========================================================
  // PORCENTAJE
  // =========================================================

  private porcentaje(
    valor: unknown
  ): string {

    if (
      valor === null ||
      valor === undefined ||
      valor === ''
    ) {

      return '—';

    }

    const numero =
      Number(valor);

    return Number.isFinite(numero)

      ? `${this.numero.format(numero)} %`

      : '—';

  }


  // =========================================================
  // ESCAPAR HTML
  // =========================================================

  private esc(
    valor: unknown
  ): string {

    return String(
      valor ?? ''
    )

      .replace(/&/g, '&amp;')

      .replace(/</g, '&lt;')

      .replace(/>/g, '&gt;')

      .replace(/"/g, '&quot;')

      .replace(/'/g, '&#39;');

  }

}
