package br.upe.bulaexpress.configuration;

import br.upe.bulaexpress.exceptions.requests.RequestException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;

@Configuration
@EnableRetry
public class AppConfig {

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplateBuilder retryTemplateBuilder = new RetryTemplateBuilder();

        retryTemplateBuilder.exponentialBackoff(2000, 2, 60000);
        retryTemplateBuilder.retryOn(RequestException.class);
        retryTemplateBuilder.maxAttempts(4);

        RetryTemplate retryTemplate = retryTemplateBuilder.build();
        retryTemplate.registerListener(new RetryListener() {
            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T,
                    E> callback, Throwable throwable) {
                System.out.println("Tentando");
            }
        });

        return retryTemplate;
    }

}
