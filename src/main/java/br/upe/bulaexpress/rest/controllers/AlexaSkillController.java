package br.upe.bulaexpress.rest.controllers;

import br.upe.bulaexpress.ask.requestverifier.RequestVerifier;
import br.upe.bulaexpress.rest.services.AlexaSkillService;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.response.SkillResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alexaskill")
public class AlexaSkillController {

    // Atributos
    private final RequestVerifier requestVerifier;

    private final AlexaSkillService alexaSkillService;

    // Métodos de acesso
    public AlexaSkillController(RequestVerifier requestVerifier, AlexaSkillService alexaSkillService) {
        this.requestVerifier = requestVerifier;
        this.alexaSkillService = alexaSkillService;
    }

    // Métodos HTTP
    @PostMapping("/")
    public String handleSkillIntent(@RequestHeader HttpHeaders httpHeader, @RequestBody RequestEnvelope requestEnvelope)
            throws JsonProcessingException {
        byte[] serializedRequestEnvelope = serialize(requestEnvelope);

        this.requestVerifier.verify(httpHeader, requestEnvelope, serializedRequestEnvelope);
        SkillResponse<?> skillResponse = this.alexaSkillService.getSkillResponse(serializedRequestEnvelope);

        return new String(skillResponse.getRawResponse());
    }

    private static byte[] serialize(Object object) throws JsonProcessingException{
        ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        return objectMapper.writeValueAsBytes(object);
    }

}
