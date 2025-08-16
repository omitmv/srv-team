package com.example.srvteam.util;

import com.example.srvteam.common.EnumTpAcesso;
import com.example.srvteam.dto.request.TimeProfissionalRequest;
import com.example.srvteam.dto.response.TimeProfissionalResponse;
import com.example.srvteam.model.TimeProfissional;

public class TimeProfissionalMapper {

    /**
     * Converte TimeProfissionalRequest para TimeProfissional
     */
    public static TimeProfissional toEntity(TimeProfissionalRequest request) {
        if (request == null) {
            return null;
        }

        TimeProfissional timeProfissional = new TimeProfissional();
        timeProfissional.setCdTime(request.getCdTime());
        timeProfissional.setCdProfissional(request.getCdProfissional());

        return timeProfissional;
    }

    /**
     * Converte TimeProfissional para TimeProfissionalResponse
     */
    public static TimeProfissionalResponse toResponse(TimeProfissional timeProfissional) {
        if (timeProfissional == null) {
            return null;
        }

        TimeProfissionalResponse response = new TimeProfissionalResponse(
                timeProfissional.getCdTime(),
                timeProfissional.getCdProfissional()
        );

        // Se o time estiver carregado, incluir suas informações
        if (timeProfissional.getTime() != null) {
            response.setNomeTime(timeProfissional.getTime().getDsNome());
            response.setLogoTime(timeProfissional.getTime().getLogo());
        }

        // Se o profissional estiver carregado, incluir suas informações
        if (timeProfissional.getProfissional() != null) {
            response.setNomeProfissional(timeProfissional.getProfissional().getNome());
            response.setEmailProfissional(timeProfissional.getProfissional().getEmail());
            response.setCdTpAcesso(timeProfissional.getProfissional().getCdTpAcesso());
            
            // Definir descrição do tipo de acesso
            if (timeProfissional.getProfissional().getCdTpAcesso() != null) {
                for (EnumTpAcesso tipo : EnumTpAcesso.values()) {
                    if (tipo.getCodigo() == timeProfissional.getProfissional().getCdTpAcesso()) {
                        response.setTipoAcesso(tipo.getDescricao());
                        break;
                    }
                }
            }
        }

        return response;
    }

    /**
     * Converte TimeProfissional para TimeProfissionalResponse com dados básicos
     */
    public static TimeProfissionalResponse toBasicResponse(TimeProfissional timeProfissional) {
        if (timeProfissional == null) {
            return null;
        }

        return new TimeProfissionalResponse(
                timeProfissional.getCdTime(),
                timeProfissional.getCdProfissional()
        );
    }
}
