package br.upe.bulaexpress.ask.requestverifier;

import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.servlet.verifiers.AlexaHttpRequest;
import com.amazon.ask.servlet.verifiers.SkillServletVerifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class RequestVerifier {

    List<SkillServletVerifier> alexaRequestVerifiers;

    public RequestVerifier(List<SkillServletVerifier> alexaRequestVerifiers) {
        this.alexaRequestVerifiers = alexaRequestVerifiers;
    }

    public void verify(HttpHeaders httpHeader, RequestEnvelope requestEnvelope, byte[] serializedRequestEnvelope) {
        AlexaHttpRequest alexaHttpRequest = new AlexaHttpRequestHolder(
                httpHeader, requestEnvelope, serializedRequestEnvelope);

        for (SkillServletVerifier verifier : alexaRequestVerifiers) {
            verifier.verify(alexaHttpRequest);
        }
    }

}
