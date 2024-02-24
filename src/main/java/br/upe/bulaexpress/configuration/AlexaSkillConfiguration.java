package br.upe.bulaexpress.configuration;

import br.upe.bulaexpress.ask.handlers.HelloWorldHandler;
import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.amazon.ask.servlet.ServletConstants;
import com.amazon.ask.servlet.util.ServletUtils;
import com.amazon.ask.servlet.verifiers.SkillRequestSignatureVerifier;
import com.amazon.ask.servlet.verifiers.SkillRequestTimestampVerifier;
import com.amazon.ask.servlet.verifiers.SkillServletVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AlexaSkillConfiguration {

    @Value("${com.amazon.ask.skill.id}")
    private String skillId;

    @Value("${com.amazon.ask.verification.millis-time-stamp-tolerance}")
    private Long millisTimeStampTolerance;

    @Value("${com.amazon.ask.verification.request-signature-check}")
    private Boolean checkRequestSignature;

    @Bean
    public List<SkillServletVerifier> getAlexaRequestVerifiers() {
        List<SkillServletVerifier> alexaRequestVerifiers = new ArrayList<>();

        if (checkRequestSignature) {
            alexaRequestVerifiers.add(new SkillRequestSignatureVerifier());
        }

        if (ServletUtils.getTimeStampToleranceSystemProperty() == null) {
            alexaRequestVerifiers.add(new SkillRequestTimestampVerifier(ServletConstants.DEFAULT_TOLERANCE_MILLIS));
        } else {
            alexaRequestVerifiers.add(new SkillRequestTimestampVerifier(millisTimeStampTolerance));
        }

        return alexaRequestVerifiers;
    }

    @Bean
    public Skill getSkill() {
        return Skills.custom()
                .addRequestHandlers(new HelloWorldHandler())
                .withSkillId(skillId)
                .build();
    }

}
