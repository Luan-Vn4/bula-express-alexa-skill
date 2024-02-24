package br.upe.bulaexpress.rest.services;

import com.amazon.ask.Skill;
import com.amazon.ask.request.impl.BaseSkillRequest;
import com.amazon.ask.response.SkillResponse;
import org.springframework.stereotype.Service;

@Service
public class AlexaSkillService {

    // Atributos
    private final Skill skill;


    // Métodos de acesso
    public AlexaSkillService(Skill skill) {
        this.skill = skill;
    }


    // Serviços
    public SkillResponse<?> getSkillResponse(byte[] serializedRequestEnvelope) {
        return handleRequest(serializedRequestEnvelope);
    }

    private SkillResponse<?> handleRequest(byte[] input) {
        return this.skill.execute(new BaseSkillRequest(input));
    }

}
