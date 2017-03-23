package pl.allegro.tech.opel;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MethodCallExpressionNode implements OpelNode {
    private final OpelNode subject;
    private final String identifier;
    private final Optional<ArgumentsListExpressionNode> arguments;
    private final ImplicitConversion implicitConversion;
    private final MethodExecutionFilter methodExecutionFilter;
    private final Map<MethodCacheKey, Method> methodsCache = new HashMap<>();

    public MethodCallExpressionNode(OpelNode subject, String identifier, Optional<ArgumentsListExpressionNode> arguments, ImplicitConversion implicitConversion, MethodExecutionFilter methodExecutionFilter) {
        this.subject = subject;
        this.identifier = identifier;
        this.arguments = arguments;
        this.implicitConversion = implicitConversion;
        this.methodExecutionFilter = methodExecutionFilter;
    }

    static MethodCallExpressionNode create(OpelNode subject, OpelNode identifier, OpelNode arguments, ImplicitConversion implicitConversion, MethodExecutionFilter methodExecutionFilter) {
        if (!(identifier instanceof IdentifierExpressionNode)) {
            throw new IllegalArgumentException("Cannot create from OpelNode because identifier is of wrong node type " + identifier.getClass().getSimpleName());
        }
        if (!(arguments instanceof ArgumentsListExpressionNode)) {
            throw new IllegalArgumentException("Cannot create from OpelNode because arguments is of wrong node type " + arguments.getClass().getSimpleName());
        }
        return new MethodCallExpressionNode(subject, ((IdentifierExpressionNode) identifier).getIdentifier(), Optional.of((ArgumentsListExpressionNode) arguments), implicitConversion, methodExecutionFilter);
    }

    static MethodCallExpressionNode create(OpelNode subject, OpelNode identifier, ImplicitConversion implicitConversion, MethodExecutionFilter methodExecutionFilter) {
        if (!(identifier instanceof IdentifierExpressionNode)) {
            throw new IllegalArgumentException("Cannot create from OpelNode because identifier is of wrong node type " + identifier.getClass().getSimpleName());
        }
        return new MethodCallExpressionNode(subject, ((IdentifierExpressionNode) identifier).getIdentifier(), Optional.empty(), implicitConversion, methodExecutionFilter);
    }

    @Override
    public CompletableFuture<?> getValue(EvalContext context) {
        return FutureUtil.sequence(
                arguments
                        .map(ags -> ags.getListOfValues(context))
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(this::javaGenericsFix).collect(Collectors.toList())
        ).thenCombine(subject.getValue(context), (args, sbj) -> methodCall(sbj, identifier, args));
    }

    private Object methodCall(Object subject, String methodName, List<?> args) {
        try {
            Class[] argsTypes = args.stream().map(Object::getClass).toArray(Class[]::new);
            ImmutablePair<Object, Method> chosenMethod = implicitConversion.getAllPossibleConversions(subject)
                    .map(convertedSubject -> ImmutablePair.of(convertedSubject, findMatchingMethod(convertedSubject, methodName, args)))
                    .filter(it -> it.getRight().isPresent())
                    .findFirst()
                    .map(it -> ImmutablePair.of(it.left, it.right.get()))
                    .orElseThrow(() -> new RuntimeException("Can't find method '" + methodName + "' for class '" + subject.getClass().getSimpleName() + "' with arguments: " + Arrays.stream(argsTypes).map(Class::getSimpleName).collect(Collectors.joining(", "))));

            return chosenMethod.right.invoke(chosenMethod.left, convertArgs(args, chosenMethod.right));
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new OpelException(e);
        }
    }

    private Optional<Method> findMatchingMethod(Object subject, String methodName, List<?> args) {
        Method matchingMethod = methodsCache.computeIfAbsent(new MethodCacheKey(subject.getClass(), args),
                cacheKey -> Arrays.stream(subject.getClass().getMethods())
                        .filter(m -> m.getName().equals(methodName))
                        .filter(m -> methodExecutionFilter.filter(subject, m))
                        .filter(m -> areArgsMatchForMethod(m, args))
                        .findFirst()
                        .orElse(null));

        return Optional.ofNullable(matchingMethod);
    }

    private Object[] convertArgs(List<?> args, Method chosenMethod) {
        Parameter[] parameters = chosenMethod.getParameters();
        List<Object> convertedArgs = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            Object arg = args.get(i);
            Class<?> expectedType = parameters[i].getType();
            convertedArgs.add(implicitConversion.convert(arg, expectedType));
        }
        return convertedArgs.toArray(new Object[convertedArgs.size()]);
    }

    private boolean areArgsMatchForMethod(Method method, List<?> args) {
        Class[] expectedArgumentsTypes = method.getParameterTypes();
        if (expectedArgumentsTypes.length != args.size()) {
            return false;
        }
        for (int i = 0; i < expectedArgumentsTypes.length; i++) {
            Class<?> expectedType = expectedArgumentsTypes[i];
            Object arg = args.get(i);
            Class<?> givenType = arg.getClass();
            if (!ClassUtils.isAssignable(givenType, expectedType) && !implicitConversion.hasConverter(arg, expectedType)) {
                return false;
            }
        }
        return true;
    }

    private CompletableFuture<Object> javaGenericsFix(CompletableFuture<?> it) {
        return it.thenApply(Function.identity());
    }

    private class MethodCacheKey {
        private final Class c;
        private final List<?> args;

        MethodCacheKey(Class c, List<?> args) {
            this.c = c;
            this.args = args;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodCacheKey that = (MethodCacheKey) o;
            return Objects.equals(c, that.c) &&
                    Objects.equals(args, that.args);
        }

        @Override
        public int hashCode() {
            return Objects.hash(c, args);
        }
    }
}
