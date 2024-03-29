/*
 * ************************************************************
 * 文件：EventInterfaceProcessor.java  模块：ElegantBus.compiler.main  项目：ElegantBus
 * 当前修改时间：2022年09月12日 17:58:58
 * 上次修改时间：2022年09月12日 17:47:29
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.compiler.main
 * Copyright (c) 2022
 * ************************************************************
 */

package cody.bus.apt;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import cody.bus.annotation.Event;
import cody.bus.annotation.EventGroup;
import cody.bus.apt.bean.EventBean;
import cody.bus.apt.bean.EventGroupBean;

/**
 * Created by xu.yi. on 2019/4/2.
 * 根据注解自动生成事件定义接口类
 */
@AutoService(Processor.class)
public class EventInterfaceProcessor extends AbstractProcessor {
    private static final String RETURN_CLASS = "cody.bus.LiveDataWrapper";
    private static final String ELEGANT_BUS_CLASS = "cody.bus.ElegantBus";
    private static final String GEN_PKG = ".cody";
    private static final String BUS = "Bus";
    private static final String FILE_DESCRIPTION =
            "Automatically generated by ElegantBus APT. \n" +
                    "Don't modify it yourself !!!\n\n" +
                    "by Cody.yi\n\n" +
                    "https://github.com/codyer/ElegantBus \n";
    private Elements mElementUtils;
    private Filer mFiler;
    private Messager mLog;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //初始化我们需要的基础工具
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        mLog = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(EventGroup.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!roundEnvironment.processingOver()) {
            processAnnotations(roundEnvironment);
        }
        return true;
    }

    private void processAnnotations(RoundEnvironment roundEnvironment) {
        Map<String, String> allBusFile = new HashMap<>();
        for (Element element : roundEnvironment.getElementsAnnotatedWith(EventGroup.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                EventGroupBean info = new EventGroupBean();

                processGroup(element, info);
                processEvent(element, info);
                if (allBusFile.containsKey(info.getGroupName())) {
                    String same = allBusFile.get(info.getGroupName());
                    mLog.printMessage(Diagnostic.Kind.ERROR,
                            "\nThe group(" + info.getGroupName() + ") has been defined, please do not repeat the definition.\n" +
                                    "There is a conflict between \n(" +
                                    info.getClassString() + ")\nand\n(" + same + ")");
                } else {
                    generateEventBusClass(info);
                    mLog.printMessage(Diagnostic.Kind.NOTE, "You get a new bus:\n" + generateClassName(info.getEventClassPrefix()));
                    allBusFile.put(info.getGroupName(), info.getClassString());
                }
            } else {
                mLog.printMessage(Diagnostic.Kind.ERROR, "You can only use EventGroup on a Class.");
            }
        }
    }

    private void processGroup(final Element element, final EventGroupBean info) {
        EventGroup group = element.getAnnotation(EventGroup.class);
        PackageElement packageElement = mElementUtils.getPackageOf(element);
        info.setPackageName(packageElement.getQualifiedName().toString());
        info.setClassName(element.getSimpleName().toString());
        info.setGroupName(group.name() + group.value());
        if (info.getGroupName() == null || info.getGroupName().equals("")) {
            info.setGroupName(element.getSimpleName().toString());
        }
        char start = info.getGroupName().toLowerCase().charAt(0);
        if (start < 'a' || start > 'z') {
            mLog.printMessage(Diagnostic.Kind.ERROR, "Group name must start with a character");
        }
        info.setActive(group.active());
    }

    private void processEvent(final Element element, final EventGroupBean info) {
        List<? extends Element> enclosedElements = element.getEnclosedElements();
        for (Element e : enclosedElements) {
            if (e.getKind() == ElementKind.FIELD) {
                VariableElement enumElement = (VariableElement) e;
                String variableName = enumElement.getSimpleName().toString();
                EventBean eventBean = new EventBean();
                Event event = e.getAnnotation(Event.class);
                if (event == null) continue;
                eventBean.setDescription(event.description() + event.value());
                eventBean.setActive(event.active());
                eventBean.setMultiProcess(event.multiProcess());
                eventBean.setEventType(e.asType().toString());
                eventBean.setEventName(variableName);
                info.addEventBeans(eventBean);
            }
        }
    }

    private void generateEventBusClass(EventGroupBean infoBean) {
        String busClassName = generateClassName(infoBean.getGroupName());
        TypeSpec.Builder builder = TypeSpec.classBuilder(busClassName)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(FILE_DESCRIPTION + "\n@see " + infoBean.getClassString() + "\n");
        for (EventBean e : infoBean.getEventBeans()) {
            ClassName className = ClassName.bestGuess(RETURN_CLASS);
            TypeName returnType;
            TypeName returnInType;
            String eventTypeStr = e.getEventType();
            if (eventTypeStr == null || eventTypeStr.length() == 0) {
                returnInType = ClassName.get(Object.class);
            } else {
                Type eventType = getType(eventTypeStr);
                if (eventType != null) {
                    returnInType = ClassName.get(eventType);
                } else {
                    returnInType = TypeVariableName.get(eventTypeStr);
                }
            }
            returnType = ParameterizedTypeName.get(className, returnInType);
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(e.getEventName())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
            ClassName elegantBus = ClassName.bestGuess(ELEGANT_BUS_CLASS);

            if (infoBean.isActive() && e.isActive()) {
                methodBuilder.addCode("return $T.getDefault($S, $S, " + getClassStr(eventTypeStr) + ", $L);\n",
                        elegantBus, infoBean.getGroupName(), e.getEventName(), e.isMultiProcess());
            } else {
                methodBuilder.addCode("return $T.getStub();\n", elegantBus);
            }
            methodBuilder.returns(returnType);
            if (e.getDescription() != null && e.getDescription().length() > 0) {
                methodBuilder.addJavadoc(e.getDescription() + "\n");
            }
            builder.addMethod(methodBuilder.build());
        }

        TypeSpec typeSpec = builder.build();
        String packageName = infoBean.getPackageName() + GEN_PKG;
        try {
            JavaFile.builder(packageName, typeSpec).build().writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 生成 MyEventBus
    private String generateClassName(String className) {
        if (className != null && className.toLowerCase().endsWith(BUS.toLowerCase())) return className;
        return className + BUS;
    }

    private String getClassStr(String type) {
        if (!type.contains("<")) {
            return type + ".class";
        }
        return "(Class<" + type + ">) ((Class)" + outTypeToString(type) + ".class)";
    }

    /**
     * 外层类
     */
    private String outTypeToString(String result) {
        int end = result.indexOf("<");
        if (end != -1) {
            return result.substring(0, end);
        }
        return result;
    }

    private Type getType(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
