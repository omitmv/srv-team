-- Legacy schema fixture representing the pre-Flyway baseline of the application.
-- This file exists only to validate Flyway baseline behavior against a real,
-- independent legacy database structure and is not itself a Flyway migration.

CREATE TABLE tbUsuario (
    cdUsuario INT NOT NULL AUTO_INCREMENT,
    login VARCHAR(250) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    nome VARCHAR(250) NOT NULL,
    email VARCHAR(250) NOT NULL,
    dataCadastro DATETIME(6) NOT NULL,
    flAtivo BIT NOT NULL,
    dtExpiracao DATETIME(6) NULL,
    cdTpAcesso INT NOT NULL,
    PRIMARY KEY (cdUsuario),
    CONSTRAINT uk_tbUsuario_login UNIQUE (login),
    CONSTRAINT uk_tbUsuario_email UNIQUE (email),
    KEY idx_tbUsuario_cdTpAcesso (cdTpAcesso)
);

CREATE TABLE tbCompeticao (
    cdCompeticao INT NOT NULL AUTO_INCREMENT,
    nmCompeticao VARCHAR(250) NOT NULL,
    dtInicio DATE NOT NULL,
    dtFim DATE NOT NULL,
    local VARCHAR(1000) NULL,
    federacao VARCHAR(250) NULL,
    PRIMARY KEY (cdCompeticao)
);

CREATE TABLE tbSistema (
    cdSistema INT NOT NULL AUTO_INCREMENT,
    dsSistema VARCHAR(250) NOT NULL,
    flAtivo BIT NOT NULL,
    PRIMARY KEY (cdSistema),
    CONSTRAINT uk_tbSistema_dsSistema UNIQUE (dsSistema)
);

CREATE TABLE tbGrupoMuscular (
    cdGrupoMuscular INT NOT NULL AUTO_INCREMENT,
    dsGrupoMuscular VARCHAR(250) NOT NULL,
    PRIMARY KEY (cdGrupoMuscular),
    CONSTRAINT uk_tbGrupoMuscular_dsGrupoMuscular UNIQUE (dsGrupoMuscular)
);

CREATE TABLE tbEstimulo (
    cdEstimulo INT NOT NULL AUTO_INCREMENT,
    dsEstimulo VARCHAR(250) NOT NULL,
    obs VARCHAR(5000) NULL,
    PRIMARY KEY (cdEstimulo),
    CONSTRAINT uk_tbEstimulo_dsEstimulo UNIQUE (dsEstimulo)
);

CREATE TABLE tbTecnica (
    cdTecnica INT NOT NULL AUTO_INCREMENT,
    dsTecnica VARCHAR(250) NOT NULL,
    obs VARCHAR(500) NULL,
    PRIMARY KEY (cdTecnica),
    CONSTRAINT uk_tbTecnica_dsTecnica UNIQUE (dsTecnica)
);

CREATE TABLE tbExercicio (
    cdExercicio INT NOT NULL AUTO_INCREMENT,
    dsExercicio VARCHAR(250) NOT NULL,
    cdGrupoMuscular INT NOT NULL,
    video VARCHAR(500) NULL,
    PRIMARY KEY (cdExercicio),
    CONSTRAINT uk_tbExercicio_dsExercicio UNIQUE (dsExercicio),
    KEY idx_tbExercicio_cdGrupoMuscular (cdGrupoMuscular),
    CONSTRAINT fk_tbExercicio_cdGrupoMuscular FOREIGN KEY (cdGrupoMuscular)
        REFERENCES tbGrupoMuscular (cdGrupoMuscular)
);

CREATE TABLE tbMenu (
    cdMenu INT NOT NULL AUTO_INCREMENT,
    dsMenu VARCHAR(250) NOT NULL,
    cdSistema INT NOT NULL,
    PRIMARY KEY (cdMenu),
    CONSTRAINT uk_tbMenu_dsMenu UNIQUE (dsMenu),
    KEY idx_tbMenu_cdSistema (cdSistema),
    CONSTRAINT fk_tbMenu_cdSistema FOREIGN KEY (cdSistema)
        REFERENCES tbSistema (cdSistema)
);

CREATE TABLE tbPontuacao (
    cdPontuacao INT NOT NULL AUTO_INCREMENT,
    cdCompeticao INT NOT NULL,
    posicao INT NOT NULL,
    pontuacao DECIMAL(10,3) NOT NULL,
    PRIMARY KEY (cdPontuacao),
    KEY idx_tbPontuacao_cdCompeticao (cdCompeticao),
    CONSTRAINT fk_tbPontuacao_cdCompeticao FOREIGN KEY (cdCompeticao)
        REFERENCES tbCompeticao (cdCompeticao)
);

CREATE TABLE tbCompetidores (
    cdCompetidor INT NOT NULL,
    cdCompeticao INT NOT NULL,
    dtCadastro DATETIME(6) NOT NULL,
    PRIMARY KEY (cdCompetidor, cdCompeticao),
    KEY idx_tbCompetidores_cdCompeticao (cdCompeticao),
    CONSTRAINT fk_tbCompetidores_cdCompetidor FOREIGN KEY (cdCompetidor)
        REFERENCES tbUsuario (cdUsuario),
    CONSTRAINT fk_tbCompetidores_cdCompeticao FOREIGN KEY (cdCompeticao)
        REFERENCES tbCompeticao (cdCompeticao)
);

CREATE TABLE tbPontuacaoHist (
    cdPontuacaoHist INT NOT NULL AUTO_INCREMENT,
    cdCompetidor INT NOT NULL,
    cdCompeticao INT NOT NULL,
    cdPontuacao INT NOT NULL,
    dtCadastro DATETIME(6) NOT NULL,
    PRIMARY KEY (cdPontuacaoHist),
    KEY idx_tbPontuacaoHist_competidor_competicao (cdCompetidor, cdCompeticao),
    KEY idx_tbPontuacaoHist_cdPontuacao (cdPontuacao),
    CONSTRAINT fk_tbPontuacaoHist_competidor_competicao FOREIGN KEY (cdCompetidor, cdCompeticao)
        REFERENCES tbCompetidores (cdCompetidor, cdCompeticao),
    CONSTRAINT fk_tbPontuacaoHist_cdPontuacao FOREIGN KEY (cdPontuacao)
        REFERENCES tbPontuacao (cdPontuacao)
);

CREATE TABLE tbTime (
    cdTime INT NOT NULL AUTO_INCREMENT,
    dsNome VARCHAR(250) NOT NULL,
    logo VARCHAR(500) NULL,
    cdProfissionalResponsavel INT NOT NULL,
    PRIMARY KEY (cdTime),
    KEY idx_tbTime_dsNome (dsNome),
    KEY idx_tbTime_cdProfissionalResponsavel (cdProfissionalResponsavel),
    CONSTRAINT fk_tbTime_cdProfissionalResponsavel FOREIGN KEY (cdProfissionalResponsavel)
        REFERENCES tbUsuario (cdUsuario)
);

CREATE TABLE tbTimeProfissional (
    cdTime INT NOT NULL,
    cdProfissional INT NOT NULL,
    PRIMARY KEY (cdTime, cdProfissional),
    KEY idx_tbTimeProfissional_cdProfissional (cdProfissional),
    CONSTRAINT fk_tbTimeProfissional_cdTime FOREIGN KEY (cdTime)
        REFERENCES tbTime (cdTime),
    CONSTRAINT fk_tbTimeProfissional_cdProfissional FOREIGN KEY (cdProfissional)
        REFERENCES tbUsuario (cdUsuario)
);

CREATE TABLE tbTreino (
    cdTreino INT NOT NULL AUTO_INCREMENT,
    dsTreino VARCHAR(250) NOT NULL,
    dtCadastro DATETIME(6) NOT NULL,
    dtInicio DATE NOT NULL,
    dtFinal DATE NOT NULL,
    cdProfissional INT NOT NULL,
    cdAtleta INT NOT NULL,
    obs VARCHAR(2500) NULL,
    PRIMARY KEY (cdTreino),
    CONSTRAINT uk_tbTreino_dsTreino UNIQUE (dsTreino),
    KEY idx_tbTreino_cdProfissional (cdProfissional),
    KEY idx_tbTreino_cdAtleta (cdAtleta),
    CONSTRAINT fk_tbTreino_cdProfissional FOREIGN KEY (cdProfissional)
        REFERENCES tbUsuario (cdUsuario),
    CONSTRAINT fk_tbTreino_cdAtleta FOREIGN KEY (cdAtleta)
        REFERENCES tbUsuario (cdUsuario)
);

CREATE TABLE tbTreinoEstimulo (
    cdTreino INT NOT NULL,
    cdEstimulo INT NOT NULL,
    PRIMARY KEY (cdTreino, cdEstimulo),
    KEY idx_tbTreinoEstimulo_cdEstimulo (cdEstimulo),
    CONSTRAINT fk_tbTreinoEstimulo_cdTreino FOREIGN KEY (cdTreino)
        REFERENCES tbTreino (cdTreino),
    CONSTRAINT fk_tbTreinoEstimulo_cdEstimulo FOREIGN KEY (cdEstimulo)
        REFERENCES tbEstimulo (cdEstimulo)
);

CREATE TABLE tbEstimuloExercicio (
    cdEstimulo INT NOT NULL,
    cdExercicio INT NOT NULL,
    serie VARCHAR(250) NULL,
    intervalo VARCHAR(50) NULL,
    cdTecnica INT NOT NULL,
    obs VARCHAR(5000) NULL,
    PRIMARY KEY (cdEstimulo, cdExercicio),
    KEY idx_tbEstimuloExercicio_cdExercicio (cdExercicio),
    KEY idx_tbEstimuloExercicio_cdTecnica (cdTecnica),
    CONSTRAINT fk_tbEstimuloExercicio_cdEstimulo FOREIGN KEY (cdEstimulo)
        REFERENCES tbEstimulo (cdEstimulo),
    CONSTRAINT fk_tbEstimuloExercicio_cdExercicio FOREIGN KEY (cdExercicio)
        REFERENCES tbExercicio (cdExercicio),
    CONSTRAINT fk_tbEstimuloExercicio_cdTecnica FOREIGN KEY (cdTecnica)
        REFERENCES tbTecnica (cdTecnica)
);
