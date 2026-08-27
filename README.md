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
- refresh token e sessão;
- envio de convite de ativação.

Essas responsabilidades pertencem ao PS_Login.

## Arquitetura

- Java 25 LTS
- Spring Boot 4.1.1
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

### Regras de criação

- o primeiro usuário do onboarding é `MASTER` e `primaryMaster=true`;
- `MASTER` pode criar MASTER, ADMIN e perfis operacionais;
- `ADMIN` pode criar apenas `LOJA`, `VETERINARIO`, `BANHO`, `HOTEL` e `CRECHE`;
- `ADMIN` não pode criar MASTER nem ADMIN;
- perfis operacionais não podem criar usuários.

### Regras de exclusão

- o primeiro/primary MASTER nunca pode ser excluído;
- MASTER pode excluir qualquer outro usuário;
- ADMIN pode excluir somente perfis operacionais;
- ADMIN não pode excluir MASTER nem ADMIN;
- perfis operacionais não podem excluir usuários.

## Integração com Empresa

`empresaId` é uma referência lógica externa. Não existe FK cross-service. A existência da empresa é validada pela API interna do PS_Empresa usando `X-Internal-Key`.

## Contratos internos usados pelo PS_Login

### Identidade por e-mail

`GET /internal/usuarios/identity?email={email}`

Proteção: `X-Internal-Key`.

Resposta mínima:

- `userId`
- `empresaId`
- `email`
- `status`
- `roles`

O PS_Login usa este contrato durante autenticação e recuperação/ativação quando precisa resolver a identidade por e-mail.

### Contexto por usuário

`GET /internal/usuarios/{usuarioId}` com `X-Actor-User-Id` do próprio usuário é reutilizado pelo PS_Login para revalidar contexto atual durante refresh e operações sensíveis.

O PS_Login não reaproveita cegamente status/roles de tokens antigos: ele consulta o PS_User antes de renovar sessões ou executar fluxos que exigem identidade ativa.

## Provisionamento de senha e convite

O PS_User **não recebe nem armazena senha**.

Fluxo oficial:

```text
Orchestrator
   |
   | cria identidade
   v
PS_User
   |
   | user criado
   v
Orchestrator
   |
   | solicita convite
   v
PS_Login
   |
   `--> e-mail para o próprio usuário definir a senha
```

MASTER/ADMIN nunca define nem visualiza a senha de outro usuário.

O PS_User também não deve chamar diretamente o PS_Login dentro da transação de criação do usuário. A coordenação pertence ao componente confiável de onboarding/gestão, evitando acoplamento de domínio e problemas de consistência distribuída entre criação de identidade e envio de convite.

## Banco

O serviço utiliza banco lógico próprio do PS_User e Flyway para versionamento de schema.

## Estado atual

A reconstrução V1, a hierarquia de usuários, o CRUD administrativo e os contratos internos necessários ao PS_Login estão implementados.

O PS_Login já existe como microsserviço separado e é responsável por:

- convite/ativação;
- login;
- access JWT;
- refresh/logout;
- troca de senha;
- recuperação/reset de senha.

A próxima integração de produto é conectar a criação de usuário ao convite por meio do Orchestrator/API Gateway, sem mover responsabilidade de credencial para o PS_User.
