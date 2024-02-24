package br.upe.bulaexpress.ask.handlers;

import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory responsável pela instanciação das implementações de RequestHandlers utilizadas pela Skill da Alexa que
 * estiverem registrados como beans Spring para que sejam detectados. Esta classe permite obter tais instâncias por
 * meio dos nomes das requests da Alexa suportadas pela Skill, sendo esse nome definido de acordo com o tipo da
 * request:<br><br>
 * <b>Requests Padrões</b>: têm o nome definido por tudo aquilo que vem antes do "Request". Por exemplo, o LaunchRequest
 * teria o nome: "Launch"<br><br>
 * <b>Intent Requests</b>: são requests que lidam com diferentes tipos de ação, portanto têm o seu nome definido a partir
 * do nome do intent. Por exemplo, um IntentRequest que tivesse o IntentName como "HelloWorldIntent" teria o nome:
 * "HelloWorldIntent"
 */
@Component
public final class BeanRequestHandlerFactory implements RequestHandlerFactory {

    //Atributos

    private static final Map<String, RequestHandler> handlersMap = new HashMap<>();

    private final ApplicationContext applicationContext;

    public BeanRequestHandlerFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        // Carrega os componentes uma única vez durante a inicialização
        if (handlersMap.isEmpty()) {
            getHandlersMap();
        }
    }

    // Métodos de acesso
    private void getHandlersMap() {
        String[] requestHandlersBeanNames = this.applicationContext.getBeanNamesForType(RequestHandler.class);
        Arrays.stream(requestHandlersBeanNames).forEach(this::putFoundInstanceToMap);
    }

    private void putFoundInstanceToMap(String beansNames) {
        RequestHandler requestHandler = (RequestHandler) this.applicationContext.getBean(beansNames);
        // É necessário normalizar o nome do bean, pois, por padrão, o primeiro caractere é minúsculo
        handlersMap.put(normalizeBeanNameToClassName(beansNames), requestHandler);
    }

    private String normalizeBeanNameToClassName(String beanName) {
       return beanName.substring(0, 1).toUpperCase() + beanName.substring(1);
    }

    // Métodos Factory

    /**
     * Retorna o {@link RequestHandler} correspondente ao nome especificado, desde que esteja registrado como bean no
     * contexto da aplicação Spring
     * @param requestName nome do request do respectivo {@link RequestHandler} a ser obtido. Para mais detalhes
     * sobre os nomes veja a documentação de: {@link BeanRequestHandlerFactory}
     * @return {@link RequestHandler} correspondente
     */
    @Override
    public RequestHandler getRequestHandlerInstance(String requestName) {
        return handlersMap.get(requestName + "Handler");
    }

    /**
     * Retorna todos os {@link RequestHandler} registrados como beans no contexto Spring
     * @param requestNames nomes dos requests dos respectivos {@link RequestHandler} a serem obtidos. Para mais detalhes
     *                    sobre os nomes veja a documentação de: {@link BeanRequestHandlerFactory}
     * @return array de {@link RequestHandler} encontrados
     */
    @Override
    public RequestHandler[] getRequestHandlers(String... requestNames) {
        return Arrays.stream(requestNames)
                .filter(requestName -> handlersMap.get(requestName + "Handler") != null)
                .map(requestName -> handlersMap.get(requestName + "Handler")).toArray(RequestHandler[]::new);
    }

    /**
     * Fornece todas os {@link RequestHandler} registrados como beans no contexto Spring
     * @return array de {@link RequestHandler} encontrados
     */
    @Override
    public RequestHandler[] getRequestHandlers() {
        return handlersMap.values().toArray(RequestHandler[]::new);
    }

}
