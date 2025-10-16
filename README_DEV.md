# ERP ASSIP — Manifiesto Técnico (Base Estable Inicial)

**Tag:** v0.1.0-base-estable-2025-10-16  
**Rama de referencia:** release/base-estable-inicial

## Alcance funcional asegurado
- Seguridad: login, JWT, roles, cambio de contraseña.
- General: zonas, sub-zonas, parámetros, directivos, agencias (CRUD + impresión + export).
- Hoja de Vida: datos personales, familiares, financieros, laborales, referencias, ubicaciones, SARLAFT (CRUD + impresión + export).
- Catálogos: países, departamentos, ciudades, tipos de documento.

## Lineamientos técnicos congelados
- Backend (Spring Boot): capas por dominio (domain/dto/mapper/repository/service/web), Flyway por schema, DTOs camelCase hacia FE.
- Frontend (Angular): features por dominio (standalone + lazy), helpers/pipes en shared, exportadores por módulo.
- Contratos: OpenAPI (Springdoc) por dominio (agrupado por paths o paquetes).
- Estilos: tokens SCSS (botones, layout, tipografía).

## Compilación verificada
- Backend: mvn clean verify ??
- Frontend: 
pm ci && npm run build ??

## Notas de seguridad
- Autenticación JWT activa.
- Rutas segmentadas por dominio: /api/hoja-vida/**, /api/general/**, etc.
- Auditoría en tablas de negocio.

## Próximos pasos (sin romper esta base)
1. Crear capa shared en backend (infra común) sin modificar APIs existentes.
2. Pipes/Utils compartidos en frontend para formateos y decodificadores.
3. Normalización consistente de DTOs de “listado vs detalle” por dominio.
4. Documentar y versionar contratos OpenAPI por grupo.

> Esta base NO se edita salvo fixes críticos. La evolución se hará en nuevas ramas/PRs siguiendo CONTRIBUTING.
