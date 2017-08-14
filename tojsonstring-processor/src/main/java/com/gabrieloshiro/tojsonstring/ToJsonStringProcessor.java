package com.gabrieloshiro.tojsonstring;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.gabrieloshiro.tojsonstring.ToJsonString")
public final class ToJsonStringProcessor extends AbstractProcessor {

    private List<MethodSpec> newIntentMethodSpecs = new ArrayList<>();
    private static final boolean HALT = false;
    private static final boolean CONTINUE = true;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        note("Supported source version is " + SourceVersion.RELEASE_7);
        return SourceVersion.RELEASE_7;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        note("Entering ToJsonString annotation processing...");
        for (Element element : roundEnvironment.getElementsAnnotatedWith(ToJsonString.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                error("ToJsonString can only be used in classes!");
                return HALT;
            }

            generateNewIntentMethod((TypeElement) element);
        }

        if (roundEnvironment.processingOver()) {
            try {
                generateNavigator();
                return CONTINUE;
            } catch (IOException exception) {
                error(exception);
            }
        }

        return HALT;
    }

    private void generateNewIntentMethod(TypeElement element) {
        note("Generating method... for class [" + element.getQualifiedName() + "]");

        Name className = element.getSimpleName();
        String variableName = getVariableName(className);

        final MethodSpec navigationMethodSpec = MethodSpec
                .methodBuilder("toJsonString")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//                .addParameter(TypeName.get(element.asType()), element.getSimpleName().toString().toLowerCase())
                .addParameter(TypeName.get(element.asType()), variableName)
                .returns(ClassName.get(String.class))
                .addStatement(generateToJsonString(element))
                .build();


        newIntentMethodSpecs.add(navigationMethodSpec);
    }

    /**
     * When the code of ToJsonString file is being generated there are two formats that we need to take into consideration. One is the format of the JSON output. It can be
     * minified or it can be beatified. The other is the format
     */

    private String generateToJsonString(TypeElement element) {
        StringBuilder toJsonString = new StringBuilder();
        toJsonString.append("return \"{\\n\" + \n");
        for (Element enclosedElement : element.getEnclosedElements()) {

            if (enclosedElement.getKind() == ElementKind.FIELD) {


                toJsonString.append("      \"  \\\"" + enclosedElement.getSimpleName() + "\\\": \" + " + getVariableName(element.getSimpleName()) + "." + enclosedElement
                        .getSimpleName
                                () +
                                    " + \",\\n\" + \n");

                Set<Modifier> modifiers = enclosedElement.getModifiers();
                String log = "";
                if (modifiers.contains(Modifier.PRIVATE)) {
                    log = "private ";
                } else if (modifiers.contains(Modifier.PROTECTED)) {
                    log = "protected ";
                } else if (modifiers.contains(Modifier.PUBLIC)) {
                    log = "public ";
                }

                if (modifiers.contains(Modifier.STATIC))
                    log = log + "static ";
                if (modifiers.contains(Modifier.FINAL))
                    log = log + "final ";
                note(log + enclosedElement.asType() + " " + enclosedElement.getSimpleName());
            }
        }
        toJsonString.append("   \"}\"");
        return toJsonString.toString();
    }

    private void generateNavigator() throws IOException {
        note("Generating file ToJson...");

        final TypeSpec.Builder builder = TypeSpec.classBuilder("ToJsonString")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        for (MethodSpec methodSpec : newIntentMethodSpecs) {
            builder.addMethod(methodSpec);
        }

        final TypeSpec toJsonStringSpec = builder.build();
        JavaFile.builder("com.gabrieloshiro.tojsonstring.sample", toJsonStringSpec)
                .build()
                .writeTo(processingEnv.getFiler());
    }

    private void note(Object text) {
        print(Diagnostic.Kind.NOTE, text);
    }

    private void error(Object text) {
        print(Diagnostic.Kind.ERROR, text);
    }

    private void print(Diagnostic.Kind logLevel, Object text) {
        processingEnv.getMessager().printMessage(logLevel, text.toString());
    }

    public String getVariableName(Name className) {

        String classFullQualified = className.toString();

        int lastIndexOfDot = classFullQualified.lastIndexOf(".");
        int indexAfterLastDot = lastIndexOfDot + 1;

        return classFullQualified.substring(indexAfterLastDot, indexAfterLastDot + 1).toLowerCase() + classFullQualified.substring(indexAfterLastDot + 1);
    }
}
