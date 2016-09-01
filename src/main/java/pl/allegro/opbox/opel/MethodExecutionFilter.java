package pl.allegro.opbox.opel;

import java.lang.reflect.Method;

public interface MethodExecutionFilter {
    boolean filter(Object subject, Method method);
}
