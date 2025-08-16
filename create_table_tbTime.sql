-- Script SQL para criação da tabela tbTime

CREATE TABLE tbTime (
    cdTime INT PRIMARY KEY AUTO_INCREMENT,
    dsNome VARCHAR(250) NOT NULL,
    logo VARCHAR(500),
    cdProfissionalResponsavel INT NOT NULL,
    
    -- Chave estrangeira
    CONSTRAINT FK_Time_ProfissionalResponsavel 
        FOREIGN KEY (cdProfissionalResponsavel) 
        REFERENCES tbUsuario(cdUsuario)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    
    -- Índices para performance
    INDEX idx_tbTime_dsNome (dsNome),
    INDEX idx_tbTime_cdProfissionalResponsavel (cdProfissionalResponsavel)
);

-- Comentários sobre as colunas
ALTER TABLE tbTime 
COMMENT = 'Tabela de times gerenciados por profissionais';

-- Comentários sobre as colunas específicas
ALTER TABLE tbTime MODIFY COLUMN cdTime INT AUTO_INCREMENT COMMENT 'Código único do time';
ALTER TABLE tbTime MODIFY COLUMN dsNome VARCHAR(250) NOT NULL COMMENT 'Nome do time';
ALTER TABLE tbTime MODIFY COLUMN logo VARCHAR(500) COMMENT 'URL ou caminho do logo do time';
ALTER TABLE tbTime MODIFY COLUMN cdProfissionalResponsavel INT NOT NULL COMMENT 'Código do usuário responsável pelo time';
