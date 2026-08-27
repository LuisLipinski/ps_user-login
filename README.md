# PS_User — My Pet Admin

Microsserviço responsável pela identidade de negócio e gestão de usuários do My Pet Admin.

## Escopo

O PS_User é responsável por:

- cadastro e manutenção do perfil do usuário;
- vínculo lógico com `empresaId`;
- status do usuário;
- roles/perfis de autorização;
- criação idempotente do primeiro `MASTER` durante onboarding;
- gestão administrativa respeitando a hierarquia MASTER/ADMIN;
- consulta interna de identidade para serviços autorizados.

## Fora do escopo

Não pertencem ao PS_User:

- login;
- emissão/validação de JWT;
- armazenamento e validação de senha;
- recuperação de senha;
- refresh token e sessão.

Essas responsabilidades pertencem ao PS_Login.

## Arquitetura

- Java 21
- Spring Boot 3.5.x
- Spring Data JPA
- Spring Security
- OpenFeign
- Flyway
- PostgreSQL
- Swagger/OpenAPI
- JaCoCo

## Roles oficiais

- `MASTER`
- `ADMIN`
- `LOJA`
- `VETERINARIO`
- `BANHO`
- `HOTEL`
- `CRECHE`

O primeiro MASTER da empresa é identificado por `primaryMaster=true` e possui proteções adicionais de domínio.

## Integração com Empresa

`empresaId` é uma referência lógica externa. Não existe FK cross-service. A existência da empresa é validada pela API interna do PS_Empresa usando `X-Internal-Key`.

## Contrato para PS_Login

O PS_User expõe um lookup interno mínimo para o futuro PS_Login:

`GET /internal/usuarios/identity?email={email}`

Proteção: `X-Internal-Key`.

Resposta de identidade:

- `userId`
- `empresaId`
- `email`
- `status`
- `roles`

O endpoint não autentica, não valida senha e não emite JWT. Usuários inativos são retornados com `status=INATIVO`; cabe ao PS_Login impedir a autenticação.

## Banco

O serviço utiliza banco lógico próprio do PS_User e Flyway para versionamento de schema.

## Estado atual

A reconstrução V1, a hierarquia de usuários e o CRUD administrativo estão implementados. O PS_User permanece separado do PS_Login por responsabilidade de domínio.
