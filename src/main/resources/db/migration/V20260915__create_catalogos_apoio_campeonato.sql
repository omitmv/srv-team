CREATE TABLE tbOrganizador (
    cdOrganizador INT NOT NULL AUTO_INCREMENT,
    nmOrganizador VARCHAR(250) NOT NULL,
    nmOrganizadorNormalizado VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    flAtivo BIT NOT NULL DEFAULT b'1',
    dtCadastro DATETIME(6) NOT NULL,
    cdUsuarioCadastro INT NOT NULL,
    dtAlteracao DATETIME(6) NULL,
    cdUsuarioAlteracao INT NULL,
    nmOrganizadorAtivoKey VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin
        GENERATED ALWAYS AS (CASE WHEN flAtivo = b'1' THEN nmOrganizadorNormalizado ELSE NULL END) STORED,
    PRIMARY KEY (cdOrganizador),
    CONSTRAINT fk_tbOrganizador_cdUsuarioCadastro FOREIGN KEY (cdUsuarioCadastro)
        REFERENCES tbUsuario (cdUsuario),
    CONSTRAINT fk_tbOrganizador_cdUsuarioAlteracao FOREIGN KEY (cdUsuarioAlteracao)
        REFERENCES tbUsuario (cdUsuario),
    CONSTRAINT uk_tbOrganizador_nmOrganizadorAtiva UNIQUE (nmOrganizadorAtivoKey),
    KEY idx_tbOrganizador_flAtivo (flAtivo),
    KEY idx_tbOrganizador_nmOrganizadorNormalizado (nmOrganizadorNormalizado)
);

CREATE TABLE tbPais (
    cdPais INT NOT NULL AUTO_INCREMENT,
    codigoIso2 CHAR(2) CHARACTER SET ascii NOT NULL,
    codigoIso3 CHAR(3) CHARACTER SET ascii NOT NULL,
    nmPais VARCHAR(150) NOT NULL,
    flAtivo BIT NOT NULL DEFAULT b'1',
    dtCadastro DATETIME(6) NOT NULL,
    PRIMARY KEY (cdPais),
    CONSTRAINT uk_tbPais_codigoIso2 UNIQUE (codigoIso2),
    CONSTRAINT uk_tbPais_codigoIso3 UNIQUE (codigoIso3)
);

CREATE TABLE tbSubdivisao (
    cdSubdivisao INT NOT NULL AUTO_INCREMENT,
    cdPais INT NOT NULL,
    codigoIso VARCHAR(10) CHARACTER SET ascii NOT NULL,
    nmSubdivisao VARCHAR(150) NOT NULL,
    flAtivo BIT NOT NULL DEFAULT b'1',
    dtCadastro DATETIME(6) NOT NULL,
    PRIMARY KEY (cdSubdivisao),
    CONSTRAINT fk_tbSubdivisao_cdPais FOREIGN KEY (cdPais)
        REFERENCES tbPais (cdPais),
    CONSTRAINT uk_tbSubdivisao_paisCodigoIso UNIQUE (cdPais, codigoIso),
    KEY idx_tbSubdivisao_cdPais (cdPais)
);
