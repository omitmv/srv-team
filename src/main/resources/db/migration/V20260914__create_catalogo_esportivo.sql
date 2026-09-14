CREATE TABLE tbCategoria (
    cdCategoria INT NOT NULL AUTO_INCREMENT,
    nmCategoria VARCHAR(150) NOT NULL,
    nmCategoriaNormalizado VARCHAR(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    flAtivo BIT NOT NULL DEFAULT b'1',
    dtCadastro DATETIME(6) NOT NULL,
    cdUsuarioCadastro INT NOT NULL,
    dtAlteracao DATETIME(6) NULL,
    cdUsuarioAlteracao INT NULL,
    nmCategoriaAtivaKey VARCHAR(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin
        GENERATED ALWAYS AS (CASE WHEN flAtivo = b'1' THEN nmCategoriaNormalizado ELSE NULL END) STORED,
    PRIMARY KEY (cdCategoria),
    CONSTRAINT fk_tbCategoria_cdUsuarioCadastro FOREIGN KEY (cdUsuarioCadastro)
        REFERENCES tbUsuario (cdUsuario),
    CONSTRAINT fk_tbCategoria_cdUsuarioAlteracao FOREIGN KEY (cdUsuarioAlteracao)
        REFERENCES tbUsuario (cdUsuario),
    CONSTRAINT uk_tbCategoria_nmCategoriaAtiva UNIQUE (nmCategoriaAtivaKey),
    KEY idx_tbCategoria_flAtivo (flAtivo),
    KEY idx_tbCategoria_nmCategoriaNormalizado (nmCategoriaNormalizado)
);

CREATE TABLE tbClasse (
    cdClasse INT NOT NULL AUTO_INCREMENT,
    cdCategoria INT NOT NULL,
    nmClasse VARCHAR(150) NOT NULL,
    nmClasseNormalizado VARCHAR(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    tipoClasse VARCHAR(20) CHARACTER SET ascii NOT NULL,
    flAtivo BIT NOT NULL DEFAULT b'1',
    dtCadastro DATETIME(6) NOT NULL,
    cdUsuarioCadastro INT NOT NULL,
    dtAlteracao DATETIME(6) NULL,
    cdUsuarioAlteracao INT NULL,
    nmClasseAtivaKey VARCHAR(220) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin
        GENERATED ALWAYS AS (
            CASE WHEN flAtivo = b'1'
                THEN CONCAT(CAST(cdCategoria AS CHAR), ':', nmClasseNormalizado)
                ELSE NULL
            END
        ) STORED,
    cdCategoriaOverallAtiva INT
        GENERATED ALWAYS AS (
            CASE WHEN flAtivo = b'1' AND tipoClasse = 'OVERALL' THEN cdCategoria ELSE NULL END
        ) STORED,
    PRIMARY KEY (cdClasse),
    CONSTRAINT fk_tbClasse_cdCategoria FOREIGN KEY (cdCategoria)
        REFERENCES tbCategoria (cdCategoria),
    CONSTRAINT fk_tbClasse_cdUsuarioCadastro FOREIGN KEY (cdUsuarioCadastro)
        REFERENCES tbUsuario (cdUsuario),
    CONSTRAINT fk_tbClasse_cdUsuarioAlteracao FOREIGN KEY (cdUsuarioAlteracao)
        REFERENCES tbUsuario (cdUsuario),
    CONSTRAINT ck_tbClasse_tipoClasse CHECK (tipoClasse IN ('COMUM', 'OVERALL')),
    CONSTRAINT uk_tbClasse_categoriaNomeAtiva UNIQUE (nmClasseAtivaKey),
    CONSTRAINT uk_tbClasse_categoriaOverallAtiva UNIQUE (cdCategoriaOverallAtiva),
    KEY idx_tbClasse_cdCategoria (cdCategoria),
    KEY idx_tbClasse_flAtivo (flAtivo),
    KEY idx_tbClasse_tipoClasse (tipoClasse)
);
