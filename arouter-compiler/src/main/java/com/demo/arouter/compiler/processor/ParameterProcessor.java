package com.demo.arouter.compiler.processor;

import com.demo.arouter.annotation.Autowired;
import com.demo.arouter.compiler.factory.ParameterFactory;
import com.demo.arouter.compiler.utils.Constants;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes(Constants.ANNOTATION_TYPE_AUTOWIRED)
public class ParameterProcessor extends BaseProcessor {

    // key:类节点，value:被 @Autowired 注解的属性集合
    private Map<TypeElement, List<Element>> mTempParameterMap = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (CollectionUtils.isEmpty(annotations)) return false;

        // 获取所有被 @Autowired 注解的属性集合
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Autowired.class);
        if (CollectionUtils.isNotEmpty(elements)) {
            try {
                // 解析元素，存入 mTempParameterMap 这个临时 Map 中
                parseElements(elements);
                // 根据 mTempParameterMap 的信息生成文件
                createParameterFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return true;
    }

    /**
     * 把被 @Autowired 注解的成员信息存入临时 Map
     */
    private void parseElements(Set<? extends Element> elements) {
        for (Element element : elements) {
            // element 是被 @Autowired 修饰的属性，获取属性的上级元素，
            // 即类， TypeElement 作为 Map 的 key
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            if (mTempParameterMap.containsKey(typeElement)) {
                mTempParameterMap.get(typeElement).add(element);
            } else {
                List<Element> fieldList = new ArrayList<>();
                fieldList.add(element);
                mTempParameterMap.put(typeElement, fieldList);
            }
        }
    }

    private void createParameterFile() throws IOException {
        if (MapUtils.isEmpty(mTempParameterMap)) return;

        TypeElement activityElement = elementUtils.getTypeElement(Constants.ACTIVITY);
        TypeElement parameterElement = elementUtils.getTypeElement(Constants.IPARAMETER);
        // 方法参数
        ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, Constants.PARAMETER_NAME).build();

        for (Map.Entry<TypeElement, List<Element>> entry : mTempParameterMap.entrySet()) {
            TypeElement typeElement = entry.getKey();

            // 限制 @Autowired 当前只能用在 Activity 内的属性之上
            if (!typeUtils.isSubtype(typeElement.asType(), activityElement.asType())) {
                throw new RuntimeException("@Autowired only works on Activity now!");
            }

            ClassName className = ClassName.get(typeElement);
            // 通过 ParameterFactory 生成方法体内容
            ParameterFactory parameterFactory = new ParameterFactory.Builder(parameterSpec)
                    .setClassName(className)
                    .setMessager(messager)
                    .setTypeUtils(typeUtils)
                    .setElementUtils(elementUtils)
                    .build();

            // 添加方法体内容第一行
            parameterFactory.addFirstStatement();

            // 遍历所有属性，生成对应的语句
            for (Element element : entry.getValue()) {
                parameterFactory.buildStatement(element);
            }

            // 生成类
            String fileName = typeElement.getSimpleName() + Constants.SUFFIX_OF_PARAMETER_FILE;
            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成获取参数类文件：" +
                    className.packageName() + "." + fileName);

            TypeSpec typeSpec = TypeSpec.classBuilder(fileName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ClassName.get(parameterElement))
                    .addMethod(parameterFactory.build())
                    .build();

            JavaFile.builder(typeElement.getEnclosingElement().toString(), typeSpec)
                    .build()
                    .writeTo(filer);
        }
    }
}
