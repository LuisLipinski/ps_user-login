ALTER TABLE usuarios
    ADD COLUMN primary_master BOOLEAN NOT NULL DEFAULT FALSE;

WITH primeiros_masters AS (
    SELECT DISTINCT ON (u.empresa_id) u.id
    FROM usuarios u
    JOIN usuario_roles ur ON ur.usuario_id = u.id
    WHERE ur.role = 'MASTER'
    ORDER BY u.empresa_id, u.data_criacao, u.id
)
UPDATE usuarios
SET primary_master = TRUE
WHERE id IN (SELECT id FROM primeiros_masters);

CREATE UNIQUE INDEX uk_usuarios_primary_master_empresa
    ON usuarios (empresa_id)
    WHERE primary_master = TRUE;

ALTER TABLE usuario_roles
    DROP CONSTRAINT ck_usuario_roles_role;

ALTER TABLE usuario_roles
    ADD CONSTRAINT ck_usuario_roles_role
    CHECK (role IN ('MASTER', 'ADMIN', 'LOJA', 'VETERINARIO', 'BANHO', 'HOTEL', 'CRECHE'));
