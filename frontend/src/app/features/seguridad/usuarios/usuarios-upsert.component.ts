// src/app/features/seguridad/usuarios/usuarios-upsert.component.ts

import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  forkJoin,
  Observable,
  of,
  switchMap
} from 'rxjs';

import {
  UsuariosApi,
  UsuarioCreacionCompleta
} from './usuarios.api';

import {
  Usuario
} from './usuarios.models';

import {
  AgenciasApi,
  Agencia
} from '../../general/agencias/agencia.api';

import { RolesApi } from '../roles/roles.api';
import { Rol } from '../roles/roles.models';

import {
  DatosPersonales,
  DatosPersonalesApi
} from '../../hoja-vida/datos-personales/datos-personales.api';


@Component({
  selector: 'app-usuarios-upsert',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './usuarios-upsert.component.html',
  styleUrls: ['./usuarios-upsert.component.scss'],
})
export class UsuariosUpsertComponent implements OnInit {

  private readonly fb =
    inject(FormBuilder);

  private readonly route =
    inject(ActivatedRoute);

  private readonly api =
    inject(UsuariosApi);

  private readonly agenciasApi =
    inject(AgenciasApi);

  private readonly rolesApi =
    inject(RolesApi);

  private readonly datosPersonalesApi =
    inject(DatosPersonalesApi);

  private readonly router =
    inject(Router);


  // =========================================================
  // ESTADO
  // =========================================================

  idUsuario =
    signal<number | null>(null);

  cargando =
    signal<boolean>(false);

  guardando =
    signal<boolean>(false);

  generandoDocumento =
    signal<boolean>(false);


  // =========================================================
  // PERSONA VINCULADA
  // =========================================================

  personaCtrl =
    new FormControl<string>('', {
      nonNullable: true
    });

  personas =
    signal<DatosPersonales[]>([]);

  personasEncontradas =
    signal<DatosPersonales[]>([]);

  personaSeleccionada =
    signal<DatosPersonales | null>(null);

  cargandoPersonas =
    signal<boolean>(false);


  // =========================================================
  // AGENCIAS
  // =========================================================

  agenciasDisponibles =
    signal<Agencia[]>([]);

  agenciasUsuario =
    signal<number[]>([]);


  // =========================================================
  // ROLES
  // =========================================================

  rolesDisponibles =
    signal<Rol[]>([]);

  rolesUsuario =
    signal<number[]>([]);

  /**
   * Conserva los roles existentes al abrir la edición.
   *
   * Se utiliza para calcular únicamente qué roles
   * deben agregarse y cuáles deben retirarse.
   */
  rolesOriginales =
    signal<number[]>([]);


  // =========================================================
  // FORMULARIO
  // =========================================================

  form = this.fb.group({

    idDatosPersonal: [
      null as number | null,
      Validators.required
    ],

    username: [
      '',
      [
        Validators.required,
        Validators.maxLength(100),
        Validators.pattern(/^[A-Za-z0-9._-]+$/)
      ]
    ],

    nombreCompleto: [
      '',
      Validators.required
    ],

    email: [
      '',
      [
        Validators.email,
        Validators.maxLength(150)
      ]
    ],

    activo: [
      true
    ],

    fechaInicioVigencia: [
      this.fechaActual(),
      Validators.required
    ],

    fechaFinVigencia: [
      null as string | null
    ],

    idAgenciaPrincipal: [
      null as number | null,
      Validators.required
    ]

  });


  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    const param =
      this.route.snapshot.paramMap.get('id');

    if (param && param !== 'nuevo') {

      const id =
        Number(param);

      if (Number.isFinite(id) && id > 0) {
        this.idUsuario.set(id);
      }
    }

    this.cargarPersonas();
    this.cargarRoles();
    this.cargarAgenciasDisponibles();

    const idUsuario =
      this.idUsuario();

    if (idUsuario != null) {
      this.cargarUsuario(idUsuario);
    }
  }


  // =========================================================
  // PERSONAS
  // =========================================================

  /**
   * Carga las personas disponibles desde Hoja de Vida.
   *
   * La búsqueda posterior se realiza localmente siguiendo
   * el mismo patrón utilizado en otros procesos del ERP.
   */
  private cargarPersonas(): void {

    if (this.cargandoPersonas()) {
      return;
    }

    this.cargandoPersonas.set(true);

    this.datosPersonalesApi
      .listar()
      .subscribe({

        next: lista => {

          const personas =
            [...(lista ?? [])]
              .sort((a, b) =>
                this.nombreCompletoPersona(a)
                  .localeCompare(
                    this.nombreCompletoPersona(b),
                    'es',
                    {
                      sensitivity: 'base'
                    }
                  )
              );

          this.personas.set(personas);

          this.personasEncontradas.set([]);

          this.resolverPersonaUsuario();
        },

        error: err => {

          console.error(
            'Error cargando personas:',
            err
          );

          this.personas.set([]);
          this.personasEncontradas.set([]);
        },

        complete: () => {

          this.cargandoPersonas.set(false);
        }
      });
  }


  /**
   * Busca personas por documento o nombre.
   *
   * Se muestran únicamente coincidencias cuando existen
   * al menos tres caracteres de búsqueda.
   */
  buscarPersona(): void {

    const texto =
      this.normalizarTexto(
        this.personaCtrl.value
      );

    if (texto.length < 3) {

      this.personasEncontradas.set([]);

      return;
    }

    /*
     * Si el usuario modifica manualmente el texto después
     * de seleccionar una persona, se elimina la selección
     * anterior para evitar guardar un ID que ya no corresponda
     * con lo mostrado en pantalla.
     */
    const seleccionada =
      this.personaSeleccionada();

    if (
      seleccionada != null
      && texto !== this.normalizarTexto(
        this.textoPersona(seleccionada)
      )
    ) {

      this.personaSeleccionada.set(null);

      this.form.patchValue({
        idDatosPersonal: null
      });
    }

    const encontrados =
      this.personas()
        .filter(persona => {

          const documento =
            this.normalizarTexto(
              persona.documento
            );

          const nombre =
            this.normalizarTexto(
              this.nombreCompletoPersona(persona)
            );

          return (
            documento.includes(texto)
            || nombre.includes(texto)
          );
        })
        .slice(0, 10);

    this.personasEncontradas.set(
      encontrados
    );
  }


  /**
   * Vincula la persona seleccionada con el usuario.
   */
  seleccionarPersona(
    persona: DatosPersonales
  ): void {

    const idDatosPersonal =
      Number(
        persona.idDatosPersonal
      );

    if (
      !Number.isFinite(idDatosPersonal)
      || idDatosPersonal <= 0
    ) {

      return;
    }

    const nombreCompleto =
      this.nombreCompletoPersona(
        persona
      );

    this.personaSeleccionada.set(
      persona
    );

    this.personaCtrl.setValue(
      this.textoPersona(persona),
      {
        emitEvent: false
      }
    );

    this.personasEncontradas.set([]);

    this.form.patchValue({

      idDatosPersonal,

      nombreCompleto
    });
  }


  /**
   * Permite retirar la persona seleccionada para realizar
   * una nueva búsqueda.
   */
  cambiarPersona(): void {

    this.personaSeleccionada.set(null);

    this.personasEncontradas.set([]);

    this.personaCtrl.setValue(
      '',
      {
        emitEvent: false
      }
    );

    this.form.patchValue({

      idDatosPersonal: null,

      nombreCompleto: ''
    });
  }


  /**
   * Resuelve la persona vinculada cuando se abre un usuario
   * existente para edición.
   *
   * Puede ejecutarse después de cargar el usuario o después
   * de cargar las personas; por eso valida ambas condiciones.
   */
  private resolverPersonaUsuario(): void {

    const idDatosPersonal =
      this.form.controls.idDatosPersonal.value;

    if (
      idDatosPersonal == null
      || this.personas().length === 0
    ) {

      return;
    }

    const persona =
      this.personas()
        .find(item =>
          Number(item.idDatosPersonal)
          === Number(idDatosPersonal)
        );

    if (!persona) {
      return;
    }

    this.personaSeleccionada.set(
      persona
    );

    this.personaCtrl.setValue(
      this.textoPersona(persona),
      {
        emitEvent: false
      }
    );
  }


  /**
   * Construye el nombre completo de una persona utilizando
   * los datos oficiales de Hoja de Vida.
   */
  nombreCompletoPersona(
    persona: DatosPersonales
  ): string {

    return [
      persona.nombres,
      persona.primerApellido,
      persona.segundoApellido
    ]
      .map(valor =>
        String(valor ?? '').trim()
      )
      .filter(valor =>
        valor.length > 0
      )
      .join(' ');
  }


  /**
   * Texto mostrado en el autocomplete.
   */
  textoPersona(
    persona: DatosPersonales
  ): string {

    const nombre =
      this.nombreCompletoPersona(
        persona
      );

    const documento =
      String(
        persona.documento ?? ''
      ).trim();

    if (nombre && documento) {
      return `${nombre} — ${documento}`;
    }

    return nombre || documento;
  }


  /**
   * Normaliza textos para realizar búsquedas sin depender
   * de mayúsculas, minúsculas o tildes.
   */
  private normalizarTexto(
    valor: unknown
  ): string {

    return String(
      valor ?? ''
    )
      .trim()
      .toLocaleLowerCase('es')
      .normalize('NFD')
      .replace(
        /[\u0300-\u036f]/g,
        ''
      );
  }


  // =========================================================
  // CATÁLOGOS
  // =========================================================

  /**
   * Carga el catálogo de roles disponibles.
   */
  private cargarRoles(): void {

    this.rolesApi.listar().subscribe({

      next: roles => {

        this.rolesDisponibles.set(
          roles.filter(
            rol => rol.activo !== false
          )
        );
      },

      error: err => {

        console.error(
          'Error cargando roles:',
          err
        );
      }
    });
  }


  /**
   * Carga el catálogo de agencias.
   */
  private cargarAgenciasDisponibles(): void {

    this.agenciasApi.listar().subscribe({

      next: agencias => {

        this.agenciasDisponibles.set(
          agencias
        );
      },

      error: err => {

        console.error(
          'Error cargando agencias:',
          err
        );
      }
    });
  }


  // =========================================================
  // USUARIO
  // =========================================================

  /**
   * Carga los datos generales del usuario
   * y posteriormente sus roles y agencias.
   */
  private cargarUsuario(
    idUsuario: number
  ): void {

    this.cargando.set(true);

    this.api.buscarPorId(idUsuario).subscribe({

      next: usuario => {

        this.form.patchValue({

          idDatosPersonal:
            usuario.idDatosPersonal ?? null,

          username:
            usuario.username ?? '',

          nombreCompleto:
            usuario.nombreCompleto ?? '',

          email:
            usuario.email ?? '',

          activo:
            usuario.activo ?? true,

          fechaInicioVigencia:
            this.aFechaInput(
              usuario.fechaInicioVigencia
            ),

          fechaFinVigencia:
            this.aFechaInput(
              usuario.fechaFinVigencia
            ),

          idAgenciaPrincipal:
            usuario.idAgenciaPrincipal ?? null
        });

        this.resolverPersonaUsuario();

        this.cargarRolesUsuario(
          idUsuario
        );

        this.cargarAgenciasUsuario(
          idUsuario
        );
      },

      error: err => {

        console.error(
          'Error cargando usuario:',
          err
        );

        this.cargando.set(false);
      },

      complete: () => {

        this.cargando.set(false);
      }
    });
  }


  /**
   * Carga los roles vigentes actualmente
   * asignados al usuario.
   */
  private cargarRolesUsuario(
    idUsuario: number
  ): void {

    this.api.listarRoles(idUsuario).subscribe({

      next: roles => {

        const ids =
          roles.map(
            rol => rol.idRol
          );

        this.rolesUsuario.set(
          [...ids]
        );

        this.rolesOriginales.set(
          [...ids]
        );
      },

      error: err => {

        console.error(
          'Error cargando roles del usuario:',
          err
        );
      }
    });
  }


  /**
   * Carga las agencias actualmente
   * asignadas al usuario.
   */
  private cargarAgenciasUsuario(
    idUsuario: number
  ): void {

    this.api.listarAgencias(idUsuario).subscribe({

      next: agencias => {

        const ids =
          agencias
            .map(agencia =>
              Number(
                agencia['id_agencia']
                ?? agencia['idAgencia']
              )
            )
            .filter(id =>
              Number.isFinite(id)
            );

        this.agenciasUsuario.set(
          ids
        );
      },

      error: err => {

        console.error(
          'Error cargando agencias del usuario:',
          err
        );
      }
    });
  }


  // =========================================================
  // AGENCIAS
  // =========================================================

  /**
   * Selecciona o retira una agencia.
   */
  toggleAgencia(
    idAgencia: number,
    checked: boolean
  ): void {

    const actuales =
      new Set(
        this.agenciasUsuario()
      );

    if (checked) {
      actuales.add(idAgencia);
    } else {
      actuales.delete(idAgencia);
    }

    this.agenciasUsuario.set(
      Array.from(actuales)
    );
  }


  /**
   * Indica si una agencia se encuentra seleccionada.
   */
  agenciaSeleccionada(
    idAgencia: number
  ): boolean {

    return this.agenciasUsuario()
      .includes(idAgencia);
  }

  /**
   * Indica si la agencia principal se encuentra
   * dentro de las agencias autorizadas del usuario.
   */
  agenciaPrincipalAutorizada(): boolean {

    const idAgenciaPrincipal =
      this.form.controls.idAgenciaPrincipal.value;

    if (idAgenciaPrincipal == null) {
      return false;
    }

    return this.agenciasUsuario()
      .includes(
        Number(idAgenciaPrincipal)
      );
  }


  // =========================================================
  // ROLES
  // =========================================================

  /**
   * Selecciona o retira un rol.
   */
  toggleRol(
    idRol: number,
    checked: boolean
  ): void {

    const actuales =
      new Set(
        this.rolesUsuario()
      );

    if (checked) {
      actuales.add(idRol);
    } else {
      actuales.delete(idRol);
    }

    this.rolesUsuario.set(
      Array.from(actuales)
    );
  }


  /**
   * Indica si un rol se encuentra seleccionado.
   */
  rolSeleccionado(
    idRol: number
  ): boolean {

    return this.rolesUsuario()
      .includes(idRol);
  }


  // =========================================================
  // GUARDAR
  // =========================================================

  /**
   * Guarda los datos generales y posteriormente
   * sincroniza roles y agencias.
   */
  guardar(): void {

    if (this.form.invalid) {

      this.form.markAllAsTouched();

      alert(
        'Debe completar los campos obligatorios.'
      );

      return;
    }

    if (!this.vigenciaValida()) {

      alert(
        'La fecha fin de vigencia no puede ser anterior a la fecha de inicio.'
      );

      return;
    }

    if (
      this.form.controls.idDatosPersonal.value == null
    ) {

      alert(
        'Debe seleccionar una persona.'
      );

      return;
    }

    if (this.agenciasUsuario().length === 0) {

      alert(
        'Debe seleccionar al menos una agencia autorizada.'
      );

      return;
    }

    if (!this.agenciaPrincipalAutorizada()) {

      alert(
        'La agencia principal debe estar incluida entre las agencias autorizadas.'
      );

      return;
    }

    if (this.rolesUsuario().length === 0) {

      alert(
        'Debe seleccionar al menos un rol.'
      );

      return;
    }

    if (this.guardando()) {
      return;
    }

    const valores =
      this.form.getRawValue();

    const data: Usuario = {

      idDatosPersonal:
        valores.idDatosPersonal ?? null,

      username:
        valores.username?.trim() || null,

      nombreCompleto:
        valores.nombreCompleto?.trim() || null,

      email:
        valores.email?.trim() || null,

      activo:
        valores.activo ?? true,

      fechaInicioVigencia:
        this.aFechaBackend(
          valores.fechaInicioVigencia
        ),

      fechaFinVigencia:
        this.aFechaBackend(
          valores.fechaFinVigencia
        ),

      idAgenciaPrincipal:
        valores.idAgenciaPrincipal ?? null
    };

    const idActual = this.idUsuario();

    this.guardando.set(true);

    // =========================================================
    // CREACIÓN COMPLETA — UNA SOLA PETICIÓN
    // =========================================================

    if (idActual == null) {

      const datos: UsuarioCreacionCompleta = {
        ...data,
        roles: [...this.rolesUsuario()],
        agencias: [...this.agenciasUsuario()]
      };

      this.api.crearCompleto(datos).subscribe({

        next: usuarioCreado => {

          const idUsuario = usuarioCreado.idUsuario;

          if (idUsuario == null) {
            this.guardando.set(false);

            alert(
              'El usuario fue creado correctamente, pero no fue posible ' +
              'obtener su identificador para generar el PDF de activación.'
            );

            this.volver();
            return;
          }

          this.descargarDocumentoPasswordInicial(idUsuario);
        },

        error: err => {
          this.mostrarErrorGuardado(err);
        }
      });

      return;
    }

    // =========================================================
    // ACTUALIZACIÓN — CONSERVA EL FLUJO EXISTENTE
    // =========================================================

    data.idUsuario = idActual;

    this.api.guardar(data)
      .pipe(
        switchMap(usuarioGuardado => {

          const idUsuario = usuarioGuardado.idUsuario;

          if (idUsuario == null) {
            throw new Error(
              'El backend no devolvió el identificador del usuario.'
            );
          }

          return forkJoin({
            roles: this.sincronizarRoles(idUsuario),
            agencias: this.api.asignarAgencias(
              idUsuario,
              [...this.agenciasUsuario()]
            )
          });
        })
      )
      .subscribe({

        next: () => {
          this.guardando.set(false);
          this.volver();
        },

        error: err => {
          this.mostrarErrorGuardado(err);
        }
      });
  }

    // =========================================================
    // GESTIÓN DE CONTRASEÑA
    // =========================================================

    /**
     * Genera el documento de gestión de contraseña
     * inmediatamente después de crear un usuario.
     *
     * En este escenario el backend determinará que corresponde
     * a una ACTIVACION porque el usuario todavía no posee
     * contraseña.
     */
    private descargarDocumentoPasswordInicial(
      idUsuario: number
    ): void {

      this.api.generarDocumentoPassword(idUsuario)
        .subscribe({

          next: pdf => {

            if (!pdf || pdf.size === 0) {

              this.guardando.set(false);

              alert(
                'El usuario fue creado correctamente, ' +
                'pero el documento PDF llegó vacío.'
              );

              this.volver();
              return;
            }

            this.descargarPdfPassword(
              pdf,
              idUsuario
            );

            this.guardando.set(false);

            alert(
              'Usuario creado correctamente.\n\n' +
              'Se solicitó la descarga del documento de gestión de contraseña.\n\n' +
              'Contraseña para abrir el PDF: número de documento ' +
              'de identidad del usuario.'
            );

            this.volver();
          },

          error: err => {

            console.error(
              'Error generando documento de contraseña:',
              err
            );

            this.guardando.set(false);

            alert(
              'El usuario fue creado correctamente, ' +
              'pero no fue posible generar o descargar ' +
              'el documento de gestión de contraseña.\n\n' +
              'No vuelva a crear el usuario.'
            );

            this.volver();
          }
        });
    }


    /**
     * Genera manualmente un nuevo documento de gestión
     * de contraseña para un usuario existente.
     *
     * Si el usuario ya posee contraseña, el backend generará
     * un token de RECUPERACION.
     */
    generarDocumentoPassword(): void {

      const idUsuario =
        this.idUsuario();

      if (
        idUsuario == null
        || this.generandoDocumento()
        || this.guardando()
      ) {
        return;
      }

      const username =
        this.form.controls.username.value?.trim()
        || `ID ${idUsuario}`;

      const confirmado =
        window.confirm(
          `Se generará un nuevo documento de gestión de contraseña ` +
          `para el usuario "${username}".\n\n` +
          `Si existe un código de recuperación anterior, dejará de ser válido.\n\n` +
          `¿Desea continuar?`
        );

      if (!confirmado) {
        return;
      }

      this.generandoDocumento.set(true);

      this.api.generarDocumentoPassword(idUsuario)
        .subscribe({

          next: pdf => {

            if (!pdf || pdf.size === 0) {

              this.generandoDocumento.set(false);

              alert(
                'El documento PDF generado está vacío.'
              );

              return;
            }

            this.descargarPdfPassword(
              pdf,
              idUsuario
            );

            this.generandoDocumento.set(false);

            alert(
              'Documento generado correctamente.\n\n' +
              'Contraseña para abrir el PDF: número de documento ' +
              'de identidad del usuario.\n\n' +
              'El usuario podrá utilizar el código incluido en el documento ' +
              'para establecer o restablecer su contraseña.'
            );
          },

          error: err => {

            console.error(
              'Error generando documento de contraseña:',
              err
            );

            this.generandoDocumento.set(false);

            alert(
              err?.error?.error
              ?? err?.error?.message
              ?? err?.message
              ?? 'No fue posible generar el documento de contraseña.'
            );
          }
        });
    }


    /**
     * Descarga localmente el PDF recibido desde backend.
     */
    private descargarPdfPassword(
      pdf: Blob,
      idUsuario: number
    ): void {

      const url =
        URL.createObjectURL(pdf);

      const enlace =
        document.createElement('a');

      enlace.href = url;

      enlace.download =
        `gestion_password_usuario_${idUsuario}.pdf`;

      document.body.appendChild(
        enlace
      );

      enlace.click();

      enlace.remove();

      setTimeout(() => {
        URL.revokeObjectURL(url);
      }, 1000);
    }

  /**
   * Presenta los errores ocurridos durante
   * la creación o actualización del usuario.
   */
  private mostrarErrorGuardado(err: any): void {

    this.guardando.set(false);

    console.error(
      'Error guardando usuario:',
      err
    );

    alert(
      err?.error?.error
      ?? err?.error?.message
      ?? err?.message
      ?? 'No fue posible guardar el usuario.'
    );
  }

  /**
   * Sincroniza únicamente las diferencias entre
   * los roles originales y los seleccionados.
   *
   * De esta manera no se retiran y recrean roles
   * que permanecen asignados, preservando correctamente
   * el histórico de seguridad.
   */
  private sincronizarRoles(
    idUsuario: number
  ): Observable<unknown> {

    const originales =
      new Set(
        this.rolesOriginales()
      );

    const seleccionados =
      new Set(
        this.rolesUsuario()
      );

    const agregar =
      Array.from(seleccionados)
        .filter(
          idRol => !originales.has(idRol)
        );

    const retirar =
      Array.from(originales)
        .filter(
          idRol => !seleccionados.has(idRol)
        );

    const operaciones: Observable<unknown>[] = [];

    for (const idRol of agregar) {

      operaciones.push(
        this.api.asignarRol(
          idUsuario,
          idRol
        )
      );
    }

    for (const idRol of retirar) {

      operaciones.push(
        this.api.retirarRol(
          idUsuario,
          idRol
        )
      );
    }

    if (operaciones.length === 0) {
      return of(null);
    }

    return forkJoin(
      operaciones
    );
  }


  // =========================================================
  // FECHAS
  // =========================================================

  /**
   * Obtiene la fecha actual local en formato yyyy-MM-dd.
   */
  private fechaActual(): string {

    const ahora = new Date();

    const anio = ahora.getFullYear();

    const mes = String(
      ahora.getMonth() + 1
    ).padStart(2, '0');

    const dia = String(
      ahora.getDate()
    ).padStart(2, '0');

    return `${anio}-${mes}-${dia}`;
  }

  /**
   * Convierte un LocalDateTime recibido desde backend
   * al formato utilizado por un input HTML de tipo date.
   */
  private aFechaInput(
    fecha: string | null | undefined
  ): string | null {

    if (!fecha) {
      return null;
    }

    return fecha.substring(0, 10);
  }


  /**
   * Convierte una fecha capturada por el formulario
   * al formato LocalDateTime esperado por backend.
   */
  private aFechaBackend(
    fecha: string | null | undefined
  ): string | null {

    if (!fecha) {
      return null;
    }

    return `${fecha}T00:00:00`;
  }


  /**
   * Regresa al listado de usuarios.
   */
  volver(): void {

    this.router.navigate([
      '/seguridad/usuarios'
    ]);
  }

  /**
   * Indica si el rango de vigencia ingresado es válido.
   *
   * La fecha final es opcional, pero cuando existe
   * no puede ser anterior a la fecha inicial.
   */
  vigenciaValida(): boolean {

    const inicio =
      this.form.controls.fechaInicioVigencia.value;

    const fin =
      this.form.controls.fechaFinVigencia.value;

    if (!inicio || !fin) {
      return true;
    }

    return fin >= inicio;
  }

}
