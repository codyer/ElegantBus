/*
 * ************************************************************
 * 文件：EventInterfaceProcessor.java  模块：compiler  项目：CleanFramework
 * 当前修改时间：2019年04月02日 15:13:47
 * 上次修改时间：2019年04月02日 15:13:46
 * 作者：Cody.yi   https://github.com/codyer
 *
 * Copyright (c) 2019
 * ************************************************************
 */

package com.cody.live.event.bus.compiler;

import com.cody.live.event.bus.compiler.bean.EventBean;
import com.cody.live.event.bus.compiler.bean.EventInfoBean;
import com.cody.live.event.bus.compiler.bean.EventScopeBean;
import com.cody.live.event.bus.lib.IEvent;
import com.cody.live.event.bus.lib.annotation.AutoGenerate;
import com.cody.live.event.bus.lib.annotation.Event;
import com.cody.live.event.bus.lib.annotation.EventScope;
import com.cody.live.event.bus.lib.exception.WrongTypeDefineException;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Created by xu.yi. on 2019/4/2.
 * 根据注解自动生成事件定义接口类
 */
@AutoService(Processor.class)
public class EventInterfaceProcessor extends AbstractProcessor {
    private static final String TAG = "[EventInterfaceProcessor]";
    ;
    private static final String PACKAGE_NAME = "com.cody.live.event.bus.core.wrapper";
    private static final String RETURN_CLASS = "LiveEventWrapper";
    private static final String GEN_PKG = ".event";
    private static final String CLN_PREFIX = "Scope$";
    private static final String EVENT_PREFIX = "withEvent$";
    private static final String FILE_DESCRIPTION = "LiveEventBus auto generate. don't modify !!!";
    private Elements mElementUtils;
    private Filer mFiler;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //初始化我们需要的基础工具
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(EventScope.class.getCanonicalName());
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
        for (Element element : roundEnvironment.getElementsAnnotatedWith(EventScope.class)) {
            if (element.getKind() == ElementKind.ENUM) {
                EventInfoBean info = new EventInfoBean();
                TypeElement typeElement = (TypeElement) element;
                PackageElement packageElement = mElementUtils.getPackageOf(element);
                info.setPackageName(packageElement.getQualifiedName().toString());
                info.setClassName(typeElement.getSimpleName().toString());
                info.setScopeBean(getScope(element));
                List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
                for (Element e : enclosedElements) {
                    if (e.getKind() == ElementKind.ENUM_CONSTANT) {
                        VariableElement enumElement = (VariableElement) e;
                        String variableName = enumElement.getSimpleName().toString();
                        EventBean eventBean = getEvent(e);
                        eventBean.setName(variableName);
                        info.addEventBeans(eventBean);
                    }
                }
                generateEventInterfaceClass(info);
            } else {
                throw new WrongTypeDefineException();
            }
        }
    }

    private EventScopeBean getScope(Element element1) {
        EventScopeBean scopeBean = new EventScopeBean();
        List<? extends AnnotationMirror> annotationMirrors = mElementUtils.getAllAnnotationMirrors(element1);
        if (annotationMirrors != null && annotationMirrors.size() > 0) {
            for (AnnotationMirror mirror : annotationMirrors) {
                if (EventScope.class.getName().equals(mirror.getAnnotationType().toString())) {
                    if (mirror.getElementValues() != null) {
                        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
                            String key = entry.getKey().getSimpleName().toString();
                            if (("value").equals(key)) {
                                scopeBean.setName(entry.getValue().getValue().toString());
                            } else if (("name").equals(key)) {
                                scopeBean.setName(entry.getValue().getValue().toString());
                            } else if (("active").equals(key)) {
                                scopeBean.setActive((Boolean) entry.getValue().getValue());
                            }
                        }
                    }
                }
            }
        }
        return scopeBean;
    }

    private EventBean getEvent(Element element1) {
        EventBean eventBean = new EventBean();
        List<? extends AnnotationMirror> annotationMirrors = mElementUtils.getAllAnnotationMirrors(element1);
        if (annotationMirrors != null && annotationMirrors.size() > 0) {
            for (AnnotationMirror mirror : annotationMirrors) {
                if (Event.class.getName().equals(mirror.getAnnotationType().toString())) {
                    if (mirror.getElementValues() != null) {
                        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
                            String key = entry.getKey().getSimpleName().toString();
                            if (("value").equals(key)) {
                                eventBean.setDescription(entry.getValue().getValue().toString());
                            } else if (("data").equals(key)) {
                                eventBean.setType(entry.getValue().getValue().toString());
                            } else if (("description").equals(key)) {
                                eventBean.setDescription(entry.getValue().getValue().toString());
                            }
                        }
                    }
                }
            }
        }
        return eventBean;
    }

    private void generateEventInterfaceClass(EventInfoBean infoBean) {
        String interfaceName = generateClassName(infoBean.getScopeBean().getName());
        AnnotationSpec autoGenerate = AnnotationSpec.builder(AutoGenerate.class)
                .addMember("value", "$S", infoBean.getScopeBean().getName())
                .addMember("active", "$L", infoBean.getScopeBean().isActive()).build();
        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(interfaceName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(IEvent.class)
                .addAnnotation(autoGenerate)
                .addJavadoc(FILE_DESCRIPTION);
        for (EventBean e : infoBean.getEventBeans()) {
            ClassName className = ClassName.get(PACKAGE_NAME, RETURN_CLASS);
            TypeName returnType;
            String eventTypeStr = e.getType();
            if (eventTypeStr == null || eventTypeStr.length() == 0) {
                returnType = ParameterizedTypeName.get(className, ClassName.get(Object.class));
            } else {
                Type eventType = getType(eventTypeStr);
                if (eventType != null) {
                    returnType = ParameterizedTypeName.get(className, ClassName.get(eventType));
                } else {
                    returnType = ParameterizedTypeName.get(className, TypeVariableName.get(eventTypeStr));
                }
            }
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(EVENT_PREFIX + e.getName())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(returnType);
            if (e.getDescription() != null && e.getDescription().length() > 0) {
                methodBuilder.addJavadoc(e.getDescription());
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

    private String generateClassName(String originalClassName) {
        return CLN_PREFIX + originalClassName;
    }

    private Type getType(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
