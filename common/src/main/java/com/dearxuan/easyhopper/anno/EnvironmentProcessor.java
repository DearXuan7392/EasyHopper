package com.dearxuan.easyhopper.anno;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compile-time annotation processor that validates {@link Environment} constraints.
 * <p>
 * For every element annotated with {@code @Environment(CLIENT)} or
 * {@code @Environment(SERVER)}, the processor checks:
 * <ul>
 *   <li>Superclass and implemented interfaces are not from a conflicting environment</li>
 *   <li>Field types are not from a conflicting environment</li>
 *   <li>Method return types and parameter types are not from a conflicting environment</li>
 *   <li>Method overrides respect the parent's environment</li>
 *   <li>References to known client-only packages (e.g. {@code net.minecraft.client.*})
 *       from server-annotated code, and vice versa</li>
 * </ul>
 * <p>
 * Elements annotated with {@code @Environment(BOTH)} or without any annotation
 * are treated as universally compatible and are not checked.
 * <p>
 * This processor runs entirely at compile time and has no effect at runtime.
 */
@SupportedAnnotationTypes("com.dearxuan.easyhopper.anno.Environment")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class EnvironmentProcessor extends AbstractProcessor {

    private static final String ANNOTATION_NAME = "com.dearxuan.easyhopper.anno.Environment";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        TypeElement envAnnotation = processingEnv.getElementUtils()
                .getTypeElement(ANNOTATION_NAME);
        if (envAnnotation == null) {
            return false;
        }

        // Collect all elements with @Environment into a map: element -> environment value
        Map<Element, EnvType> envMap = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(envAnnotation)) {
            Environment env = element.getAnnotation(Environment.class);
            if (env != null) {
                envMap.put(element, env.value());
            }
        }

        if (envMap.isEmpty()) {
            return false;
        }

        // Validate each annotated type element
        for (Map.Entry<Element, EnvType> entry : envMap.entrySet()) {
            Element element = entry.getKey();
            EnvType env = entry.getValue();
            if (env == EnvType.BOTH) {
                continue;
            }
            if (element.getKind() == ElementKind.CLASS
                    || element.getKind() == ElementKind.INTERFACE
                    || element.getKind() == ElementKind.ENUM) {
                checkTypeElement((TypeElement) element, env, envMap);
            }
        }

        return false;
    }

    /**
     * Check all members of a type element for environment compatibility.
     */
    private void checkTypeElement(TypeElement type, EnvType env, Map<Element, EnvType> envMap) {
        // 1. Check superclass
        TypeMirror superclass = type.getSuperclass();
        if (superclass instanceof DeclaredType) {
            Element superElement = ((DeclaredType) superclass).asElement();
            checkCompatibility(type, superElement, env, envMap, "extends");
        }

        // 2. Check implemented interfaces
        for (TypeMirror iface : type.getInterfaces()) {
            if (iface instanceof DeclaredType) {
                Element ifaceElement = ((DeclaredType) iface).asElement();
                checkCompatibility(type, ifaceElement, env, envMap, "implements");
            }
        }

        // 3. Check field types
        for (VariableElement field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
            TypeMirror fieldType = field.asType();
            Element fieldTypeElement = getTypeElement(fieldType);
            if (fieldTypeElement != null) {
                checkCompatibility(type, fieldTypeElement, env, envMap,
                        "field \"" + field.getSimpleName() + "\" of type");
            }
        }

        // 4. Check method return types and parameter types
        for (ExecutableElement method : ElementFilter.methodsIn(type.getEnclosedElements())) {
            // Return type
            TypeMirror returnType = method.getReturnType();
            Element returnTypeElement = getTypeElement(returnType);
            if (returnTypeElement != null) {
                checkCompatibility(type, returnTypeElement, env, envMap,
                        "return type of method \"" + method.getSimpleName() + "\"");
            }

            // Parameter types
            for (VariableElement param : method.getParameters()) {
                TypeMirror paramType = param.asType();
                Element paramTypeElement = getTypeElement(paramType);
                if (paramTypeElement != null) {
                    checkCompatibility(type, paramTypeElement, env, envMap,
                            "parameter type of method \"" + method.getSimpleName() + "\"");
                }
            }
        }

        // 5. Check override methods: if a method overrides a method from a parent
        //    with a conflicting environment, report an error
        checkMethodOverrides(type, env, envMap);
    }

    /**
     * Check that no method in this type overrides a method from a parent type
     * that has a conflicting environment.
     */
    private void checkMethodOverrides(TypeElement type, EnvType env, Map<Element, EnvType> envMap) {
        TypeMirror superclass = type.getSuperclass();
        if (superclass instanceof DeclaredType) {
            Element superElement = ((DeclaredType) superclass).asElement();
            if (superElement instanceof TypeElement) {
                EnvType superEnv = envMap.get(superElement);
                if (superEnv != null && superEnv != EnvType.BOTH && superEnv != env) {
                    // Check each method in the current type
                    List<ExecutableElement> methods = ElementFilter.methodsIn(type.getEnclosedElements());
                    for (ExecutableElement method : methods) {
                        if (overridesMethod(method, (TypeElement) superElement)) {
                            processingEnv.getMessager().printMessage(
                                    Diagnostic.Kind.ERROR,
                                    String.format(
                                            "@Environment(%s) type \"%s\" overrides method \"%s\" "
                                                    + "from @Environment(%s) superclass \"%s\". "
                                                    + "This is not allowed.",
                                            env, type.getQualifiedName(),
                                            method.getSimpleName(),
                                            superEnv,
                                            ((TypeElement) superElement).getQualifiedName()
                                    ),
                                    method
                            );
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if a method overrides a method in the given parent type by matching
     * name and parameter types.
     */
    private boolean overridesMethod(ExecutableElement method, TypeElement parentType) {
        String methodName = method.getSimpleName().toString();
        List<? extends VariableElement> params = method.getParameters();

        for (ExecutableElement parentMethod : ElementFilter.methodsIn(parentType.getEnclosedElements())) {
            if (!parentMethod.getSimpleName().toString().equals(methodName)) {
                continue;
            }
            List<? extends VariableElement> parentParams = parentMethod.getParameters();
            if (params.size() != parentParams.size()) {
                continue;
            }
            boolean match = true;
            for (int i = 0; i < params.size(); i++) {
                if (!params.get(i).asType().toString().equals(parentParams.get(i).asType().toString())) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the target element has a conflicting environment annotation.
     * If so, report a compile error.
     */
    private void checkCompatibility(
            TypeElement sourceType, Element targetElement,
            EnvType sourceEnv, Map<Element, EnvType> envMap,
            String relation) {
        if (targetElement == null) {
            return;
        }

        // If the target element itself has an @Environment annotation, check it
        EnvType targetEnv = envMap.get(targetElement);
        if (targetEnv != null && targetEnv != EnvType.BOTH && targetEnv != sourceEnv) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    String.format(
                            "@Environment(%s) type \"%s\" %s @Environment(%s) \"%s\". "
                                    + "Cross-environment reference is not allowed.",
                            sourceEnv, sourceType.getQualifiedName(),
                            relation, targetEnv,
                            getElementQualifiedName(targetElement)
                    ),
                    sourceType
            );
        }

        // Also check if the package of the target element is restricted
        checkPackageHeuristic(sourceType, targetElement, sourceEnv, relation);
    }

    /**
     * Heuristic check: flag references to known client-only or server-only packages
     * even when the target class does not have an explicit {@code @Environment} annotation.
     * <p>
     * This catches cases like referencing Minecraft's {@code net.minecraft.client.*}
     * packages from server-annotated code.
     */
    private void checkPackageHeuristic(
            TypeElement sourceType, Element targetElement,
            EnvType sourceEnv, String relation) {
        String qualifiedName = getElementQualifiedName(targetElement);
        if (qualifiedName == null) {
            return;
        }

        boolean isClientOnly = qualifiedName.startsWith("net.minecraft.client.");
        boolean isServerOnly = qualifiedName.startsWith("net.minecraft.server.");

        if (sourceEnv == EnvType.SERVER && isClientOnly) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    String.format(
                            "@Environment(SERVER) type \"%s\" %s client-only class \"%s\". "
                                    + "Cross-environment reference is not allowed.",
                            sourceType.getQualifiedName(), relation, qualifiedName
                    ),
                    sourceType
            );
        }

        if (sourceEnv == EnvType.CLIENT && isServerOnly) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    String.format(
                            "@Environment(CLIENT) type \"%s\" %s server-only class \"%s\". "
                                    + "Cross-environment reference is not allowed.",
                            sourceType.getQualifiedName(), relation, qualifiedName
                    ),
                    sourceType
            );
        }
    }

    /**
     * Extract the {@link Element} (type declaration) from a {@link TypeMirror},
     * handling type parameters, arrays, and primitives.
     */
    private Element getTypeElement(TypeMirror typeMirror) {
        if (typeMirror instanceof DeclaredType) {
            return ((DeclaredType) typeMirror).asElement();
        }
        return null;
    }

    /**
     * Get the qualified name of an element, or null if it does not have one.
     */
    private String getElementQualifiedName(Element element) {
        if (element instanceof TypeElement) {
            return ((TypeElement) element).getQualifiedName().toString();
        }
        return element.getSimpleName().toString();
    }
}