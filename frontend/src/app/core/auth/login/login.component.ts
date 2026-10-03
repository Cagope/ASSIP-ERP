import {
  Component,
  inject,
  signal
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
  Router
} from '@angular/router';

import {
  AuthApi
} from '../auth.api';

import {
  SessionService
} from '../session.service';

type EtapaAcceso =
  | 'USUARIO'
  | 'PASSWORD'
  | 'ACTIVACION'
  | 'RECUPERACION';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  private readonly fb = inject(FormBuilder);
  private readonly authApi = inject(AuthApi);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  loading = signal(false);
  errorMsg = signal<string | null>(null);
  successMsg = signal<string | null>(null);

  avisoVencimientoPassword = signal<string | null>(null);
  pendienteContinuarPorVencimiento = signal(false);

  etapa = signal<EtapaAcceso>('USUARIO');

  showPassword = signal(false);
  showPasswordNueva = signal(false);

  longitudMinimaPassword = signal<number | null>(null);

  remember = signal(false);

  form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: [''],
    codigo: [''],
    passwordNueva: [''],
    confirmacionPassword: ['']
  });

  get f() {
    return this.form.controls;
  }

  constructor() {

    // =========================================================
    // DESARROLLO TEMPORAL - RETIRAR ANTES DE PRODUCCIÓN
    // =========================================================
    //
    // Durante el desarrollo del módulo de seguridad se permite
    // recordar temporalmente usuario y contraseña para agilizar
    // las pruebas repetitivas de login.
    //
    // IMPORTANTE:
    // La contraseña queda almacenada en texto plano dentro de
    // localStorage bajo la clave "dev_login_data".
    //
    // ANTES DE PRODUCCIÓN:
    // 1. Eliminar este bloque completo.
    // 2. Restaurar únicamente la lectura de "auth_username".
    // 3. Eliminar los demás bloques identificados con:
    //    "DESARROLLO TEMPORAL - RETIRAR ANTES DE PRODUCCIÓN".
    // 4. Limpiar "dev_login_data" de localStorage.
    //
    // En producción NUNCA se debe almacenar una contraseña
    // de usuario en localStorage.
    // =========================================================

    const datosDesarrollo =
      localStorage.getItem('dev_login_data');

    if (datosDesarrollo) {

      try {

        const datos =
          JSON.parse(datosDesarrollo);

        const username =
          String(
            datos?.username ?? ''
          ).trim();

        const password =
          String(
            datos?.password ?? ''
          );

        if (username) {

          this.form.controls.username.setValue(
            username
          );

          this.form.controls.password.setValue(
            password
          );

          this.remember.set(true);

          localStorage.setItem(
            'auth_username',
            username
          );
        }

      } catch {

        // Si los datos temporales están dañados,
        // se eliminan para no afectar el login.
        localStorage.removeItem(
          'dev_login_data'
        );
      }

    } else {

      // Comportamiento normal:
      // recordar únicamente el nombre de usuario.
      const username =
        localStorage.getItem(
          'auth_username'
        );

      if (username) {

        this.form.controls.username.setValue(
          username
        );

        this.remember.set(true);
      }
    }

    // Si cambia el usuario, debe verificarse nuevamente su estado.
    this.form.controls.username.valueChanges.subscribe(() => {
      if (this.etapa() !== 'USUARIO') {
        this.reiniciarEtapa();
      }
    });
  }

  // =========================================================
  // ESTADO DE ACCESO
  // =========================================================

  consultarEstado(): void {

    const username = this.f.username.value.trim();

    if (!username) {
      this.f.username.markAsTouched();
      return;
    }

    if (this.loading()) {
      return;
    }

    this.loading.set(true);
    this.errorMsg.set(null);
    this.successMsg.set(null);
    this.avisoVencimientoPassword.set(null);
    this.pendienteContinuarPorVencimiento.set(false);

    this.authApi.consultarEstadoAcceso(username)
      .subscribe({

        next: respuesta => {

          this.loading.set(false);

          // Ignorar respuestas de una búsqueda anterior.
          if (this.f.username.value.trim() !== username) {
            return;
          }

          const longitudMinima =
            Number(respuesta.longitudMinimaPassword);

          if (
            !Number.isInteger(longitudMinima)
            || longitudMinima <= 0
          ) {
            this.longitudMinimaPassword.set(null);

            this.errorMsg.set(
              'No fue posible obtener la política de contraseña.'
            );

            return;
          }

          this.longitudMinimaPassword.set(
            longitudMinima
          );

          if (respuesta.estado === 'PENDIENTE_ACTIVACION') {
            this.etapa.set('ACTIVACION');
            return;
          }

          if (respuesta.estado === 'ACTIVO') {
            this.etapa.set('PASSWORD');
            return;
          }

          this.errorMsg.set(
            'No fue posible determinar el estado de la cuenta.'
          );
        },

        error: err => {
          this.loading.set(false);
          this.mostrarError(
            err,
            'No fue posible consultar el estado de acceso.'
          );
        }
      });
  }

  // =========================================================
  // INICIO DE SESIÓN
  // =========================================================

  submit(): void {

    if (this.etapa() === 'USUARIO') {
      this.consultarEstado();
      return;
    }

    if (this.etapa() === 'ACTIVACION') {
      this.activarCuenta();
      return;
    }

    if (this.etapa() === 'RECUPERACION') {
      this.recuperarPassword();
      return;
    }

    /*
     * El usuario ya fue autenticado correctamente y únicamente
     * está confirmando que desea continuar después de leer
     * el aviso de vencimiento.
     */
    if (this.pendienteContinuarPorVencimiento()) {
      this.pendienteContinuarPorVencimiento.set(false);
      this.cargarContextoSesion();
      return;
    }

    this.iniciarSesion();
  }

  // =========================================================
  // CAMBIO VOLUNTARIO DE CONTRASEÑA
  // =========================================================

  irCambiarPassword(): void {

    if (this.etapa() !== 'PASSWORD' || this.loading()) {
      return;
    }

    /*
     * Si el usuario ya se autenticó y está viendo el aviso
     * de vencimiento, el token ya está disponible y puede
     * ir directamente al cambio de contraseña.
     */
    if (
      this.pendienteContinuarPorVencimiento()
      && this.session.isAuthenticated()
    ) {
      this.f.password.reset();

      this.router.navigate([
        '/cambiar-password'
      ]);

      return;
    }

    const username = this.f.username.value.trim();
    const password = this.f.password.value;

    if (!username || !password) {
      this.errorMsg.set(
        'Ingrese su contraseña actual para continuar.'
      );
      return;
    }

    this.loading.set(true);
    this.errorMsg.set(null);
    this.successMsg.set(null);
    this.avisoVencimientoPassword.set(null);
    this.pendienteContinuarPorVencimiento.set(false);

    this.authApi.login(username, password)
      .subscribe({

        next: result => {

          this.session.setToken(result.token);
          this.session.setUser(result.username || username);

          this.actualizarRecordatorio(username);

          this.f.password.reset();

          this.loading.set(false);

          this.router.navigate([
            '/cambiar-password'
          ]);
        },

        error: err => {

          this.loading.set(false);

          this.mostrarError(
            err,
            'No fue posible verificar sus credenciales.'
          );
        }
      });
  }

  private iniciarSesion(): void {

    const username = this.f.username.value.trim();
    const password = this.f.password.value;

    if (!username || !password) {
      this.errorMsg.set('Debe ingresar su contraseña.');
      return;
    }

    if (this.loading()) {
      return;
    }

    this.loading.set(true);
    this.errorMsg.set(null);
    this.successMsg.set(null);
    this.avisoVencimientoPassword.set(null);
    this.pendienteContinuarPorVencimiento.set(false);

    this.authApi.login(username, password)
      .subscribe({

        next: result => {

          this.session.setToken(result.token);
          this.session.setUser(result.username || username);

          this.actualizarRecordatorio(username);

          if (result.requiereCambioPassword) {

            this.loading.set(false);

            this.f.password.reset();

            this.router.navigate([
              '/cambiar-password'
            ]);

            return;
          }

          /*
           * Si la contraseña está próxima a vencer o vence hoy,
           * detenemos aquí la navegación para que el usuario
           * pueda leer el aviso.
           *
           * La autenticación ya fue correcta y el token ya fue
           * almacenado.
           */
          if (result.mostrarAvisoVencimientoPassword) {

            this.mostrarAvisoVencimiento(result);

            this.pendienteContinuarPorVencimiento.set(true);

            this.loading.set(false);

            return;
          }

          this.cargarContextoSesion();
        },

        error: err => {

          this.loading.set(false);

          this.pendienteContinuarPorVencimiento.set(false);

          this.mostrarError(
            err,
            'Error al iniciar sesión.'
          );
        }
      });
  }

  // =========================================================
  // AVISO DE VENCIMIENTO DE CONTRASEÑA
  // =========================================================

  private mostrarAvisoVencimiento(
    result: any
  ): void {

    const estado =
      String(
        result?.estadoVigenciaPassword ?? ''
      ).trim();

    const diasRestantes =
      Number(
        result?.diasRestantesPassword
      );

    const fechaVencimiento =
      String(
        result?.fechaVencimientoPassword ?? ''
      ).trim();

    if (estado === 'VENCE_HOY') {

      this.avisoVencimientoPassword.set(
        'Su contraseña vence hoy. Puede cambiarla ahora con el botón de la llave o continuar al sistema.'
      );

      return;
    }

    if (
      estado === 'PROXIMA_VENCER'
      && Number.isFinite(diasRestantes)
      && diasRestantes > 0
    ) {

      const textoDias =
        diasRestantes === 1
          ? '1 día'
          : `${diasRestantes} días`;

      const textoFecha =
        fechaVencimiento
          ? ` Fecha de vencimiento: ${fechaVencimiento}.`
          : '';

      this.avisoVencimientoPassword.set(
        `Su contraseña vencerá en ${textoDias}.${textoFecha} Puede cambiarla ahora con el botón de la llave o continuar al sistema.`
      );

      return;
    }

    this.avisoVencimientoPassword.set(
      'Su contraseña está próxima a vencer. Puede cambiarla ahora o continuar al sistema.'
    );
  }

  // =========================================================
  // RECUPERACIÓN DE CONTRASEÑA
  // =========================================================

  irRecuperarPassword(): void {

    if (this.etapa() !== 'PASSWORD' || this.loading()) {
      return;
    }

    /*
     * La recuperación utiliza el mismo usuario
     * previamente consultado.
     */
    this.f.password.reset();
    this.f.codigo.reset();
    this.f.passwordNueva.reset();
    this.f.confirmacionPassword.reset();

    this.showPassword.set(false);
    this.showPasswordNueva.set(false);

    this.errorMsg.set(null);
    this.successMsg.set(null);
    this.avisoVencimientoPassword.set(null);
    this.pendienteContinuarPorVencimiento.set(false);

    this.etapa.set('RECUPERACION');
  }

  // =========================================================
  // ACTIVACIÓN INICIAL
  // =========================================================

  private activarCuenta(): void {

    const username = this.f.username.value.trim();
    const codigo = this.f.codigo.value.trim();
    const passwordNueva = this.f.passwordNueva.value;
    const confirmacion = this.f.confirmacionPassword.value;

    if (!/^\d{6}$/.test(codigo)) {
      this.errorMsg.set(
        'Debe ingresar el código de seis dígitos del PDF.'
      );
      return;
    }

    if (!this.validarNuevaPassword(
      passwordNueva,
      confirmacion
    )) {
      return;
    }

    if (this.loading()) {
      return;
    }

    this.loading.set(true);
    this.errorMsg.set(null);

    this.authApi.activarCuenta({
      username,
      codigo,
      passwordNueva
    }).subscribe({

      next: respuesta => {

        this.loading.set(false);

        this.f.codigo.reset();
        this.f.passwordNueva.reset();
        this.f.confirmacionPassword.reset();
        this.f.password.reset();

        this.etapa.set('PASSWORD');

        this.successMsg.set(
          respuesta.mensaje ||
          'Cuenta activada correctamente. Ingrese con su nueva contraseña.'
        );
      },

      error: err => {
        this.loading.set(false);
        this.mostrarError(
          err,
          'No fue posible activar la cuenta.'
        );
      }
    });
  }

  private recuperarPassword(): void {

    const username = this.f.username.value.trim();
    const codigo = this.f.codigo.value.trim();
    const passwordNueva = this.f.passwordNueva.value;
    const confirmacion = this.f.confirmacionPassword.value;

    if (!/^\d{6}$/.test(codigo)) {
      this.errorMsg.set(
        'Debe ingresar el código de seis dígitos del PDF.'
      );
      return;
    }

    if (!this.validarNuevaPassword(
      passwordNueva,
      confirmacion
    )) {
      return;
    }

    if (this.loading()) {
      return;
    }

    this.loading.set(true);
    this.errorMsg.set(null);
    this.successMsg.set(null);

    this.authApi.recuperarPassword({
      username,
      codigo,
      passwordNueva
    }).subscribe({

      next: respuesta => {

        this.loading.set(false);

        this.f.codigo.reset();
        this.f.passwordNueva.reset();
        this.f.confirmacionPassword.reset();
        this.f.password.reset();

        this.showPasswordNueva.set(false);

        this.etapa.set('PASSWORD');

        this.successMsg.set(
          respuesta.mensaje ||
          'Contraseña restablecida correctamente. Ingrese con su nueva contraseña.'
        );
      },

      error: err => {

        this.loading.set(false);

        this.mostrarError(
          err,
          'No fue posible restablecer la contraseña.'
        );
      }
    });
  }

  private validarNuevaPassword(
    passwordNueva: string,
    confirmacion: string
  ): boolean {

    const longitudMinima =
      this.longitudMinimaPassword();

    if (
      longitudMinima === null
      || !Number.isInteger(longitudMinima)
      || longitudMinima <= 0
    ) {
      this.errorMsg.set(
        'No fue posible obtener la política de contraseña.'
      );
      return false;
    }

    if (!passwordNueva) {
      this.errorMsg.set(
        'Debe ingresar la nueva contraseña.'
      );
      return false;
    }

    if (passwordNueva.length < longitudMinima) {
      this.errorMsg.set(
        `La contraseña debe tener mínimo ${longitudMinima} caracteres.`
      );
      return false;
    }

    if (!/[A-Z]/.test(passwordNueva)) {
      this.errorMsg.set(
        'La contraseña debe contener al menos una letra mayúscula.'
      );
      return false;
    }

    if (!/[a-z]/.test(passwordNueva)) {
      this.errorMsg.set(
        'La contraseña debe contener al menos una letra minúscula.'
      );
      return false;
    }

    if (!/[0-9]/.test(passwordNueva)) {
      this.errorMsg.set(
        'La contraseña debe contener al menos un número.'
      );
      return false;
    }

    if (!/[^A-Za-z0-9\s]/.test(passwordNueva)) {
      this.errorMsg.set(
        'La contraseña debe contener al menos un carácter especial.'
      );
      return false;
    }

    if (/\s/.test(passwordNueva)) {
      this.errorMsg.set(
        'La contraseña no puede contener espacios.'
      );
      return false;
    }

    if (passwordNueva !== confirmacion) {
      this.errorMsg.set(
        'Las contraseñas no coinciden.'
      );
      return false;
    }

    return true;
  }

  // =========================================================
  // CONTEXTO DE SESIÓN
  // =========================================================

  private cargarContextoSesion(): void {

    this.loading.set(true);

    this.authApi.me()
      .subscribe({

        next: info => {

          this.session.setUser(info.username);
          this.session.setRoles(info.roles ?? []);
          this.session.setPermisos(info.permisos ?? []);
          this.session.setAgencias(info.agencias ?? []);

          this.loading.set(false);

          this.router.navigate(['/']);
        },

        error: err => {

          this.session.logout();
          this.loading.set(false);

          this.pendienteContinuarPorVencimiento.set(false);

          this.mostrarError(
            err,
            'No fue posible cargar el contexto de seguridad.'
          );
        }
      });
  }

  // =========================================================
  // CONTROLES
  // =========================================================

  toggleShowPassword(): void {
    this.showPassword.update(valor => !valor);
  }

  toggleShowPasswordNueva(): void {
    this.showPasswordNueva.update(valor => !valor);
  }

  volverUsuario(): void {

    /*
     * Si ya existía un login correcto pendiente de continuar,
     * eliminamos ese token antes de volver a seleccionar usuario.
     */
    if (this.pendienteContinuarPorVencimiento()) {
      this.session.logout();
    }

    this.reiniciarEtapa();
  }

  private reiniciarEtapa(): void {

    this.etapa.set('USUARIO');
    this.longitudMinimaPassword.set(null);

    this.f.password.reset();
    this.f.codigo.reset();
    this.f.passwordNueva.reset();
    this.f.confirmacionPassword.reset();

    this.showPassword.set(false);
    this.showPasswordNueva.set(false);

    this.errorMsg.set(null);
    this.successMsg.set(null);
    this.avisoVencimientoPassword.set(null);
    this.pendienteContinuarPorVencimiento.set(false);

    // =========================================================
    // DESARROLLO TEMPORAL - RETIRAR ANTES DE PRODUCCIÓN
    // =========================================================
    //
    // Al volver a la selección de usuario, restauramos la
    // contraseña temporal para no tener que digitarla de nuevo
    // durante las pruebas.
    //
    // ANTES DE PRODUCCIÓN:
    // Eliminar este bloque completo.
    // =========================================================

    const datosDesarrollo =
      localStorage.getItem('dev_login_data');

    if (datosDesarrollo) {

      try {

        const datos =
          JSON.parse(datosDesarrollo);

        const usernameGuardado =
          String(
            datos?.username ?? ''
          ).trim();

        const passwordGuardado =
          String(
            datos?.password ?? ''
          );

        /*
         * Solo restauramos la contraseña si corresponde
         * exactamente al usuario que continúa en el formulario.
         */
        if (
          usernameGuardado
          && usernameGuardado
            === this.f.username.value.trim()
        ) {
          this.f.password.setValue(
            passwordGuardado
          );
        }

      } catch {
        // No afectar el login por datos temporales inválidos.
      }
    }
  }

  onRememberChange(checked: boolean): void {

    this.remember.set(checked);

    if (!checked) {

      localStorage.removeItem(
        'auth_username'
      );

      // =======================================================
      // DESARROLLO TEMPORAL - RETIRAR ANTES DE PRODUCCIÓN
      // =======================================================
      //
      // Al desmarcar "Recordar usuario" también se elimina
      // inmediatamente la contraseña temporal almacenada.
      //
      // ANTES DE PRODUCCIÓN:
      // Eliminar estas líneas junto con todo el mecanismo
      // "dev_login_data".
      // =======================================================

      localStorage.removeItem(
        'dev_login_data'
      );
    }
  }

  private actualizarRecordatorio(
    username: string
  ): void {

    if (this.remember()) {

      localStorage.setItem(
        'auth_username',
        username
      );

      // =======================================================
      // DESARROLLO TEMPORAL - RETIRAR ANTES DE PRODUCCIÓN
      // =======================================================
      //
      // Guarda la contraseña utilizada en el último login
      // correcto únicamente para facilitar las pruebas.
      //
      // SEGURIDAD:
      // localStorage NO es un almacenamiento seguro para
      // contraseñas. Este comportamiento está permitido
      // exclusivamente durante el desarrollo local.
      //
      // ANTES DE PRODUCCIÓN:
      // 1. Eliminar este localStorage.setItem completo.
      // 2. Buscar en este archivo "dev_login_data".
      // 3. Eliminar todos los bloques marcados como
      //    "DESARROLLO TEMPORAL - RETIRAR ANTES DE PRODUCCIÓN".
      // 4. Limpiar manualmente la clave "dev_login_data"
      //    de los navegadores usados durante desarrollo.
      //
      // El comportamiento definitivo debe conservar únicamente
      // "auth_username"; nunca la contraseña.
      // =======================================================

      localStorage.setItem(
        'dev_login_data',
        JSON.stringify({
          username,
          password: this.f.password.value
        })
      );

    } else {

      localStorage.removeItem(
        'auth_username'
      );

      // DESARROLLO TEMPORAL - RETIRAR ANTES DE PRODUCCIÓN
      localStorage.removeItem(
        'dev_login_data'
      );
    }
  }

  private mostrarError(
    err: any,
    mensaje: string
  ): void {

    this.errorMsg.set(
      err?.error?.error
      ?? err?.error?.message
      ?? err?.message
      ?? mensaje
    );
  }
}
