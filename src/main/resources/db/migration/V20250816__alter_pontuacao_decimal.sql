-- Migração para garantir que a coluna pontuacao está como DECIMAL(10,3)
ALTER TABLE tbPontuacao 
MODIFY COLUMN pontuacao DECIMAL(10,3) NOT NULL;
