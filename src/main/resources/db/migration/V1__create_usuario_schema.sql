CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL,
    onboarding_id UUID,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(320) NOT NULL,
    status VARCHAR(20) NOT NULL,
    data_criacao TIMESTAMPTZ NOT NULL,
    data_atualizacao TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT uk_usuarios_onboarding UNIQUE (onboarding_id),
    CONSTRAINT ck_usuarios_status CHECK (status IN ('ATIVO', 'INATIVO'))
);

CREATE INDEX idx_usuarios_empresa_id ON usuarios (empresa_id);
CREATE INDEX idx_usuarios_status ON usuarios (status);

CREATE TABLE usuario_roles (
    usuario_id UUID NOT NULL,
    role VARCHAR(30) NOT NULL,
    CONSTRAINT pk_usuario_roles PRIMARY KEY (usuario_id, role),
    CONSTRAINT fk_usuario_roles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT ck_usuario_roles_role CHECK (role IN ('MASTER', 'ADMIN', 'USER'))
);
