-- Script SQL para adicionar a coluna cdTpAcesso na tabela tbUsuario

-- Adicionar a nova coluna
ALTER TABLE tbUsuario 
ADD COLUMN cdTpAcesso INT NOT NULL DEFAULT 6;

-- Atualizar usuários existentes para ter um tipo de acesso (6 = ATLETA por padrão)
UPDATE tbUsuario 
SET cdTpAcesso = 6 
WHERE cdTpAcesso IS NULL;

-- Opcional: Criar índice para melhor performance em consultas por tipo de acesso
CREATE INDEX idx_tbUsuario_cdTpAcesso ON tbUsuario(cdTpAcesso);

-- Comentários sobre os códigos dos tipos de acesso:
-- 1 = Administrador
-- 2 = Nutricionista  
-- 3 = Treinador
-- 4 = Coach
-- 5 = Funcionário
-- 6 = Atleta
