ALTER TABLE tbSubdivisao
    ADD CONSTRAINT uk_tbSubdivisao_cdPaisCdSubdivisao UNIQUE (cdPais, cdSubdivisao);

ALTER TABLE tbCompeticao
    DROP COLUMN federacao,
    ADD COLUMN nmCompeticaoNormalizado VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ADD COLUMN cdOrganizador INT NOT NULL,
    ADD COLUMN cdPais INT NOT NULL,
    ADD COLUMN cdSubdivisao INT NULL,
    ADD COLUMN status VARCHAR(20) CHARACTER SET ascii NOT NULL DEFAULT 'ATIVO',
    ADD COLUMN cdCriador INT NOT NULL,
    ADD COLUMN lockVersion BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN dtCadastro DATETIME(6) NOT NULL,
    ADD COLUMN cdUsuarioCadastro INT NOT NULL,
    ADD COLUMN dtAlteracao DATETIME(6) NULL,
    ADD COLUMN cdUsuarioAlteracao INT NULL,
    ADD COLUMN nmCompeticaoAtivaKey VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin
        GENERATED ALWAYS AS (CASE WHEN status = 'ATIVO' THEN nmCompeticaoNormalizado ELSE NULL END) STORED,
    ADD COLUMN cdOrganizadorAtivaKey INT
        GENERATED ALWAYS AS (CASE WHEN status = 'ATIVO' THEN cdOrganizador ELSE NULL END) STORED,
    ADD COLUMN cdPaisAtivaKey INT
        GENERATED ALWAYS AS (CASE WHEN status = 'ATIVO' THEN cdPais ELSE NULL END) STORED,
    ADD COLUMN cdSubdivisaoAtivaKey INT
        GENERATED ALWAYS AS (CASE WHEN status = 'ATIVO' THEN COALESCE(cdSubdivisao, 0) ELSE NULL END) STORED,
    ADD COLUMN dtInicioAtivaKey DATE
        GENERATED ALWAYS AS (CASE WHEN status = 'ATIVO' THEN dtInicio ELSE NULL END) STORED,
    ADD CONSTRAINT fk_tbCompeticao_cdOrganizador FOREIGN KEY (cdOrganizador)
        REFERENCES tbOrganizador (cdOrganizador),
    ADD CONSTRAINT fk_tbCompeticao_cdPais FOREIGN KEY (cdPais)
        REFERENCES tbPais (cdPais),
    ADD CONSTRAINT fk_tbCompeticao_cdPaisCdSubdivisao FOREIGN KEY (cdPais, cdSubdivisao)
        REFERENCES tbSubdivisao (cdPais, cdSubdivisao),
    ADD CONSTRAINT fk_tbCompeticao_cdCriador FOREIGN KEY (cdCriador)
        REFERENCES tbUsuario (cdUsuario),
    ADD CONSTRAINT fk_tbCompeticao_cdUsuarioCadastro FOREIGN KEY (cdUsuarioCadastro)
        REFERENCES tbUsuario (cdUsuario),
    ADD CONSTRAINT fk_tbCompeticao_cdUsuarioAlteracao FOREIGN KEY (cdUsuarioAlteracao)
        REFERENCES tbUsuario (cdUsuario),
    ADD CONSTRAINT ck_tbCompeticao_status CHECK (status IN ('ATIVO', 'CANCELADO')),
    ADD CONSTRAINT ck_tbCompeticao_periodo CHECK (dtFim >= dtInicio),
    ADD CONSTRAINT uk_tbCompeticao_identidadeAtiva UNIQUE (
        nmCompeticaoAtivaKey,
        cdOrganizadorAtivaKey,
        cdPaisAtivaKey,
        cdSubdivisaoAtivaKey,
        dtInicioAtivaKey
    ),
    ADD KEY idx_tbCompeticao_status (status),
    ADD KEY idx_tbCompeticao_cdOrganizador (cdOrganizador),
    ADD KEY idx_tbCompeticao_cdPais (cdPais),
    ADD KEY idx_tbCompeticao_cdSubdivisao (cdSubdivisao),
    ADD KEY idx_tbCompeticao_cdCriador (cdCriador),
    ADD KEY idx_tbCompeticao_nmCompeticaoNormalizado (nmCompeticaoNormalizado);
