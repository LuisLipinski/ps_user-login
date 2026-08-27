# PS_User — My Pet Admin

Reconstrução do microsserviço de usuários do My Pet Admin.

## Escopo

O PS_User é responsável por identidade de negócio do usuário dentro do SaaS:

- cadastro e manutenção do perfil do usuário;
- vínculo lógico com `empresaId`;
- status do usuário;
- roles/perfis de autorização;
- criação idempotente do usuário `MASTER` durante onboarding;
- consulta interna de dados de usuário para outros serviços, quando necessário.

## Fora do escopo

Não pertencem ao PS_User:

- login;
- emissão/validação de JWT;
- armazenamento e validação de senha;
- recuperação de senha;
- refresh token e sessão.

Essas responsabilidades pertencem ao futuro PS_Login.

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

## Integração com Empresa

`empresaId` é uma referência lógica externa. Não existe FK cross-service. A existência da empresa é validada pela API interna do PS_Empresa usando `X-Internal-Key`.

## Banco

O serviço deverá usar um banco lógico próprio, recomendado: `ps_user_db`.

## Estado da reconstrução

Branch inicial: `rebuild/ps-user-v1`.

A implementação antiga de User/Login é considerada legado e não é fonte para a nova arquitetura.
