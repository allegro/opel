import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.allegro.tech.opel.ExpressionValidationResult;
import pl.allegro.tech.opel.OpelEngine;
import pl.allegro.tech.opel.OpelEngineBuilder;
import spark.ModelAndView;
import spark.Spark;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static spark.Spark.get;
import static spark.Spark.staticFileLocation;

public class Main {

    private static OpelEngine opelEngine = registerSimpleConversions(OpelEngineBuilder.create()).build();
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    interface ModelVariables {
        String RESULT = "result";
        String ERROR = "error";
        String EXPRESSION = "expression";
    }

    public static void main(String[] args) {
        determineAndSetPort();
        staticFileLocation("/static");

        get("/", (request, response) -> {
            Map<String, Object> model = Optional.ofNullable(request.queryParams("expression"))
                    .map(Main::sanitizeExpression)
                    .map(Main::processExpression)
                    .orElse(new HashMap<>());

            return new ModelAndView(model, "index.ftl");
        }, new FreeMarkerTemplateEngine());
    }

    private static Map<String, Object> processExpression(String expr) {
        Map<String, Object> model = new HashMap<>();
        ExpressionValidationResult validationResult = opelEngine.validate(expr);
        model.put(ModelVariables.EXPRESSION, expr);

        if (validationResult.isSucceed()) {
            try {
                String result = opelEngine.eval(expr).join().toString();
                model.put(ModelVariables.RESULT, result);
            } catch (Exception e) {
                model.put(ModelVariables.ERROR, e.getMessage());
                log.error("Error during expression calculation", e);
            }
        } else {
            model.put(ModelVariables.ERROR, validationResult.getErrorMessage());
        }

        return model;
    }

    private static String sanitizeExpression(String expression) {
        return expression.replaceAll("\r", "");
    }

    private static void determineAndSetPort() {
        Optional.ofNullable(new ProcessBuilder().environment().get("PORT"))
                .ifPresent(port -> Spark.port(Integer.parseInt(port)));
    }

    private static OpelEngineBuilder registerSimpleConversions(OpelEngineBuilder opelEngineBuilder) {
        opelEngineBuilder.withImplicitConversion(String.class, BigDecimal.class, BigDecimal::new);
        opelEngineBuilder.withImplicitConversion(String.class, Integer.class, Integer::valueOf);
        opelEngineBuilder.withImplicitConversion(Integer.class, String.class, Object::toString);
        opelEngineBuilder.withImplicitConversion(BigDecimal.class, String.class, BigDecimal::toPlainString);
        return opelEngineBuilder;
    }

}
