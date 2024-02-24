package br.upe.bulaexpress.ask.requestverifier;

import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.servlet.ServletConstants;
import com.amazon.ask.servlet.verifiers.AlexaHttpRequest;
import org.springframework.http.HttpHeaders;

public class AlexaHttpRequestHolder implements AlexaHttpRequest {

    private final byte[] serializedRequestEnvelope;

    private final RequestEnvelope deserializedRequestEnvelope;

    private final String baseEncoded64Signature;

    private final String signingCertificateChainUrl;

    public AlexaHttpRequestHolder(HttpHeaders httpHeaders,
                                  RequestEnvelope requestEnvelope, byte[] serializedRequestEnvelope) {
        this.serializedRequestEnvelope = serializedRequestEnvelope;
        this.deserializedRequestEnvelope = requestEnvelope;
        this.baseEncoded64Signature = httpHeaders.getFirst(ServletConstants.SIGNATURE_REQUEST_HEADER);
        this.signingCertificateChainUrl = httpHeaders.getFirst(ServletConstants.SIGNATURE_CERTIFICATE_CHAIN_URL_REQUEST_HEADER);
    }

    @Override
    public String getBaseEncoded64Signature() {
        return baseEncoded64Signature;
    }

    @Override
    public String getSigningCertificateChainUrl() {
        return signingCertificateChainUrl;
    }

    @Override
    public byte[] getSerializedRequestEnvelope() {
        return serializedRequestEnvelope;
    }

    @Override
    public RequestEnvelope getDeserializedRequestEnvelope() {
        return deserializedRequestEnvelope;
    }

}
