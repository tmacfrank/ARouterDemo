package com.demo.arouter.compiler.processor;

import com.demo.arouter.annotation.Route;
import com.demo.arouter.compiler.utils.Constants;
import com.demo.arouter.enums.RouteType;
import com.demo.arouter.model.RouteMeta;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

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
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes(Constants.ANNOTATION_TYPE_ROUTE)
public class RouteProcessor extends BaseProcessor {

    // 扫描注解后临时保存 RouteMeta 的 Map，key 是组名
    private Map<String, List<RouteMeta>> mTempPathMap = new HashMap<>();

    // 存放生成 Group 文件所需信息的临时 Map，<组名，Path 文件名>
    private Map<String, String> mTempGroupMap = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 如果被扫描的模块没有使用本处理器支持的注解，就无需处理了。
        if (CollectionUtils.isEmpty(annotations)) return false;

        // 拿到所有被 Route 注解修饰的程序元素，由于 @Route 只作用在
        // 类上，所以 Set 中其实都是类元素。
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Route.class);
        if (CollectionUtils.isNotEmpty(elements)) {
            parseElement(elements);
        }
        return true;
    }

    /**
     * 解析 elements 集合，先将每个 element 解析成 RouteMeta 存入临时
     * Map 中，然后再遍历临时 Map，生成路由信息。
     */
    private void parseElement(Set<? extends Element> elements) {
        // 获取 Activity 类对应的 Element 和 TypeMirror 对象
        TypeElement activityType = elementUtils.getTypeElement(RouteType.ACTIVITY.getClassName());
        TypeMirror activityMirror = activityType.asType();

        for (Element element : elements) {
            TypeMirror elementMirror = element.asType();
            Route route = element.getAnnotation(Route.class);
            RouteMeta.Builder builder = new RouteMeta.Builder();

            // 判断 element 的类型，如 RouteType.ACTIVITY 等
            if (typeUtils.isSubtype(elementMirror, activityMirror)) {
                messager.printMessage(Diagnostic.Kind.NOTE, "Found activity route:" + elementMirror.toString());
                builder.setRouteType(RouteType.ACTIVITY);
            } else {
                throw new RuntimeException("The @Route is marked on unsupported class, look at [" + elementMirror.toString() + "].");
            }

            RouteMeta routeMeta = builder.setElement(element)
                    .setPath(route.path())
                    .setGroup(route.group())
                    .build();

            addToTempMap(routeMeta);
        }

        // 扫描结束后，从临时 Map 里取出 RouteMeta 创建路由文件。
        // 注意，这里需要 app、order 等功能模块依赖 arouter-api，否则就找不到这些接口。
        TypeElement pathLoadType = elementUtils.getTypeElement(Constants.IROUTE_PATH);
        TypeElement groupLoadType = elementUtils.getTypeElement(Constants.IROUTE_GROUP);

        try {
            createPathFile(pathLoadType);
            createGroupFile(groupLoadType, pathLoadType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建 Path 路由文件
     */
    private void createPathFile(TypeElement pathLoadType) throws IOException {
        if (MapUtils.isEmpty(mTempPathMap)) return;

        for (Map.Entry<String, List<RouteMeta>> entry : mTempPathMap.entrySet()) {
            // 方法返回值类型 Map<String, RouterBean>
            TypeName methodReturns = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouteMeta.class));

            // 配置方法以及第一个语句 Map<String, RouteMeta> pathMap = new HashMap<>();
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.METHOD_LOAD_PATH)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .returns(methodReturns)
                    .addStatement("$T<$T,$T> $N = new $T<>()",
                            ClassName.get(Map.class),
                            ClassName.get(String.class),
                            ClassName.get(RouteMeta.class),
                            Constants.VARIABLE_PATH_MAP,
                            ClassName.get(HashMap.class));

            // 遍历 List<RouteMeta> 生成 pathMap.put("/app/MainActivity",
            // RouteMeta.create(RouteType.ACTIVITY,"app","/app/MainActivity",MainActivity.class));
            for (RouteMeta routeMeta : entry.getValue()) {
                methodBuilder.addStatement("$N.put($S,$T.create($T.$L,$S,$S,$T.class))",
                        Constants.VARIABLE_PATH_MAP,
                        routeMeta.getPath(),
                        ClassName.get(RouteMeta.class),
                        ClassName.get(RouteType.class),
                        routeMeta.getRouteType(),
                        routeMeta.getGroup(),
                        routeMeta.getPath(),
                        ClassName.get((TypeElement) routeMeta.getElement()));
            }

            methodBuilder.addStatement("return $N", Constants.VARIABLE_PATH_MAP);

            // 生成类节点
            String pathFileName = Constants.PREFIX_OF_PATH_NAME + entry.getKey();
            TypeSpec typeSpec = TypeSpec.classBuilder(pathFileName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ClassName.get(pathLoadType))
                    .addMethod(methodBuilder.build())
                    .build();

            // 生成文件，包名由一个常量指定
            JavaFile.builder(Constants.PACKAGE_OF_GENERATE_FILE, typeSpec)
                    .build()
                    .writeTo(filer);

            // 非常重要一步！！！路径文件生成后，才能赋值路由组 mTempGroupMap
            mTempGroupMap.put(entry.getKey(), pathFileName);
        }
    }

    /**
     * 创建 Group 路由文件
     */
    private void createGroupFile(TypeElement groupLoadType, TypeElement pathLoadType) throws IOException {
        // 返回值类型 Map<String, Class<? extends ILoadPath>>
        TypeName returnType = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathLoadType))));

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.METHOD_LOAD_GROUP)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType);

        // Map<String,Class<? extends ILoadPath>> pathMap = new HashMap<>();
        methodBuilder.addStatement("$T<$T,$T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathLoadType))),
                Constants.VARIABLE_PATH_MAP,
                ClassName.get(HashMap.class));

        // pathMap.put("app",ARouter$$Path$$app.class);
        for (Map.Entry<String, String> entry : mTempGroupMap.entrySet()) {
            methodBuilder.addStatement("$N.put($S,$T.class)",
                    Constants.VARIABLE_PATH_MAP,
                    entry.getKey(),
                    ClassName.get(Constants.PACKAGE_OF_GENERATE_FILE,entry.getValue()));
        }

        methodBuilder.addStatement("return $N", Constants.VARIABLE_PATH_MAP);

        // 创建类对象
        String groupFileName = Constants.PREFIX_OF_GROUP_NAME + moduleName;
        TypeSpec typeSpec = TypeSpec.classBuilder(groupFileName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodBuilder.build())
                .addSuperinterface(ClassName.get(groupLoadType))
                .build();

        // 创建文件
        JavaFile.builder(Constants.PACKAGE_OF_GENERATE_FILE, typeSpec)
                .build()
                .writeTo(filer);
    }

    private void addToTempMap(RouteMeta routeMeta) {
        if (checkRouterPath(routeMeta)) {
            String group = routeMeta.getGroup();
            List<RouteMeta> metaList = mTempPathMap.get(group);
            if (metaList == null) {
                metaList = new ArrayList<>();
                metaList.add(routeMeta);
                mTempPathMap.put(group, metaList);
            } else {
                metaList.add(routeMeta);
            }
        }
    }

    /**
     * 判断路径的合法性，并确定组名（因为 @Route 中组名可以不填，但是到这步要确定）
     */
    private boolean checkRouterPath(RouteMeta routeMeta) {
        String path = routeMeta.getPath();
        String group = routeMeta.getGroup();
        if (StringUtils.isEmpty(path) || !path.startsWith("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "The value of path in @Route is empty or not start with /");
            return false;
        }

        if (path.lastIndexOf("/") == 0) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "The value of path in @Route is Illegal,such as /app/MainActivity.");
            return false;
        }

        String finalGroup = path.substring(1, path.indexOf("/", 1));
        if (finalGroup.contains("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "The value of path in @Route is Illegal,such as /app/MainActivity.");
            return false;
        }

        // 如果 @Route 通过 group() 设置了组名，该组名必须与 path() 中截取的组名相同。
        if (StringUtils.isNotEmpty(group) && !group.equals(finalGroup)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Group name in group() and path() should be same!");
            return false;
        }

        routeMeta.setGroup(finalGroup);
        return true;
    }
}