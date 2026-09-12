-- Script SQL para criação da tabela tbTimeProfissional

CREATE TABLE tbTimeProfissional (
    cdTime INT NOT NULL,
    cdProfissional INT NOT NULL,
    
    -- Chave primária composta
    PRIMARY KEY (cdTime, cdProfissional),
    
    -- Chaves estrangeiras
    CONSTRAINT FK_TimeProfissional_Time 
        FOREIGN KEY (cdTime) 
        REFERENCES tbTime(cdTime)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    
    CONSTRAINT FK_TimeProfissional_Profissional 
        FOREIGN KEY (cdProfissional) 
        REFERENCES tbUsuario(cdUsuario)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    
    -- Índices para performance
    INDEX idx_tbTimeProfissional_cdTime (cdTime),
    INDEX idx_tbTimeProfissional_cdProfissional (cdProfissional)
);

-- Comentários sobre a tabela
ALTER TABLE tbTimeProfissional 
COMMENT = 'Tabela de associação N:N entre Times e Profissionais (Usuários)';

-- Comentários sobre as colunas específicas
ALTER TABLE tbTimeProfissional MODIFY COLUMN cdTime INT NOT NULL COMMENT 'Código do time (FK para tbTime)';
ALTER TABLE tbTimeProfissional MODIFY COLUMN cdProfissional INT NOT NULL COMMENT 'Código do profissional (FK para tbUsuario)';

-- Trigger para validar se o usuário não é atleta (opcional)
DELIMITER //
CREATE TRIGGER trg_validate_profissional_tipo
    BEFORE INSERT ON tbTimeProfissional
    FOR EACH ROW
BEGIN
    DECLARE tipo_acesso INT;
    
    SELECT cdTpAcesso INTO tipo_acesso 
    FROM tbUsuario 
    WHERE cdUsuario = NEW.cdProfissional;
    
    IF tipo_acesso = 6 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Atletas não podem fazer parte da equipe técnica de times';
    END IF;
END //
DELIMITER ;
