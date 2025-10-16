# ERP ASSIP � Manifiesto T�cnico (Base Estable Inicial)

**Tag:** v0.1.0-base-estable-2025-10-16  
**Rama de referencia:** release/base-estable-inicial

## Alcance funcional asegurado
- Seguridad: login, JWT, roles, cambio de contrase�a.
- General: zonas, sub-zonas, par�metros, directivos, agencias (CRUD + impresi�n + export).
- Hoja de Vida: datos personales, familiares, financieros, laborales, referencias, ubicaciones, SARLAFT (CRUD + impresi�n + export).
- Cat�logos: pa�ses, departamentos, ciudades, tipos de documento.

## Lineamientos t�cnicos congelados
- Backend (Spring Boot): capas por dominio (domain/dto/mapper/repository/service/web), Flyway por schema, DTOs camelCase hacia FE.
- Frontend (Angular): features por dominio (standalone + lazy), helpers/pipes en shared, exportadores por m�dulo.
- Contratos: OpenAPI (Springdoc) por dominio (agrupado por paths o paquetes).
- Estilos: tokens SCSS (botones, layout, tipograf�a).

## Compilaci�n verificada
- Backend: mvn clean verify ??
- Frontend: 
pm ci && npm run build ??

## Notas de seguridad
- Autenticaci�n JWT activa.
- Rutas segmentadas por dominio: /api/hoja-vida/**, /api/general/**, etc.
- Auditor�a en tablas de negocio.

## Pr�ximos pasos (sin romper esta base)
1. Crear capa shared en backend (infra com�n) sin modificar APIs existentes.
2. Pipes/Utils compartidos en frontend para formateos y decodificadores.
3. Normalizaci�n consistente de DTOs de �listado vs detalle� por dominio.
4. Documentar y versionar contratos OpenAPI por grupo.

> Esta base NO se edita salvo fixes cr�ticos. La evoluci�n se har� en nuevas ramas/PRs siguiendo CONTRIBUTING.
