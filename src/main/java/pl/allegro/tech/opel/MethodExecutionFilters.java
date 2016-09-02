package pl.allegro.tech.opel;

import java.lang.reflect.Method;

public enum MethodExecutionFilters implements MethodExecutionFilter {
    ALLOW_ALL() {
        @Override
        public boolean filter(Object subject, Method method) {
            return true;
        }
    },
    DENY_ALL() {
        @Override
        public boolean filter(Object subject, Method method) {
            return false;
        }
    }
}
