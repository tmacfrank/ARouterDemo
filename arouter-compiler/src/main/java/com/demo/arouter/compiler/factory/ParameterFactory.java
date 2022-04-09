package com.demo.arouter.compiler.factory;

import com.demo.arouter.annotation.Autowired;
import com.demo.arouter.compiler.utils.Constants;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class ParameterFactory {

    private static final String CONTENT = "$T t = ($T) target";

    private MethodSpec.Builder methodBuilder;
    private ClassName className;
    private Messager messager;
    private Types typeUtils;
    private TypeMirror providerType;

    private ParameterFactory(Builder builder) {
        this.className = builder.className;
        this.messager = builder.messager;
        this.typeUtils = builder.typeUtils;

        methodBuilder = MethodSpec.methodBuilder(Constants.METHOD_LOAD_PARAMETER)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(builder.parameterSpec)
                .addAnnotation(Override.class);

        providerType = builder.elementUtils.getTypeElement(Constants.IPROVIDER).asType();
    }

    /**
     * 添加方法体内容的第一行：MainActivity t = (MainActivity) target;
     */
    public void addFirstStatement() {
        methodBuilder.addStatement(CONTENT, className, className);
    }

    /**
     * 构建方体内容，如：t.s = t.getIntent.getStringExtra("s");
     *
     * @param element 被注解的属性元素
     */
    public void buildStatement(Element element) {
        TypeMirror typeMirror = element.asType();
        // 获取 TypeKind 枚举类型的序列号
        int type = typeMirror.getKind().ordinal();
        // 获取属性名
        String fieldName = element.getSimpleName().toString();
        // 获取属性值，如果 @Autowired 的 name() 为空，则采用属性名
        Autowired autowired = element.getAnnotation(Autowired.class);
        String annotationValue = StringUtils.isEmpty(autowired.name()) ?
                fieldName : autowired.name();
        // 赋值语句拼接的前缀
        String finalValue = "t." + fieldName;
        String methodContent = finalValue + " = t.getIntent().";

        // 根据成员类型拼接 methodContent 后面的内容
        if (TypeKind.INT.ordinal() == type) {
            // finalValue 字符串作为 getIntExtra() 中的默认值部分，因为被
            // 注解的属性可能有初始值。
            methodContent += "getIntExtra($S," + finalValue + ")";
        } else if (TypeKind.BOOLEAN == typeMirror.getKind()) {
            // t.fieldName = t.getIntent.getStringExtra("s",t.fieldName);
            methodContent += "getBooleanExtra($S," + finalValue + ")";
        } else {
            // TypeKind 中没有定义字符串类型，需要做特殊处理
            if (typeMirror.toString().equalsIgnoreCase(Constants.STRING)) {
                // t.s = t.getIntent.getStringExtra("s");
                methodContent += "getStringExtra($S)";
            }

            if (typeUtils.isSubtype(typeMirror, providerType)) {
                // t.orderService = (IOrderService) ARouter.getInstance().build("/order/OrderServiceImpl").navigation(t);
                methodContent = finalValue+" = ($T) $T.getInstance().build($S).navigation(t)";
                methodBuilder.addStatement(methodContent,
                        ClassName.get(typeMirror),
                        ClassName.get(Constants.MANAGER_PACKAGE,Constants.PROJECT),
                        annotationValue);
                return;
            }
        }

        if (methodContent.endsWith(")")) {
            methodBuilder.addStatement(methodContent, annotationValue);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "Just support String、int、boolean type temporarily.");
        }
    }

    public MethodSpec build() {
        return methodBuilder.build();
    }

    public static class Builder {

        private ClassName className;
        private Messager messager;
        private ParameterSpec parameterSpec;
        private Types typeUtils;
        private Elements elementUtils;

        public Builder(ParameterSpec parameterSpec) {
            this.parameterSpec = parameterSpec;
        }

        public Builder setClassName(ClassName className) {
            this.className = className;
            return this;
        }

        public Builder setMessager(Messager messager) {
            this.messager = messager;
            return this;
        }

        public Builder setTypeUtils(Types typeUtils) {
            this.typeUtils = typeUtils;
            return this;
        }

        public Builder setElementUtils(Elements elementUtils) {
            this.elementUtils = elementUtils;
            return this;
        }

        public ParameterFactory build() {
            if (parameterSpec == null) {
                throw new IllegalArgumentException("MethodSpec.Builder is null!");
            }

            if (className == null) {
                throw new IllegalArgumentException("ClassName is null!");
            }

            if (messager == null) {
                throw new IllegalArgumentException("Messager is null!");
            }

            return new ParameterFactory(this);
        }
    }
}
