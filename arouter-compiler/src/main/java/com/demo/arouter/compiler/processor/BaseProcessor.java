package com.demo.arouter.compiler.processor;

import com.demo.arouter.compiler.utils.Constants;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public abstract class BaseProcessor extends AbstractProcessor {

    // 操作Element工具类(类、函数、属性都是Element)
    Elements elementUtils;

    // type(信息类)工具类 包含用于操作TypeMirror的工具方法
    Types typeUtils;

    // 用来输出警告、错误等日志
    Messager messager;

    // 文件生成器 类/资源，Filter用来创建新的类文件，class文件以及辅助文件
    Filer filer;

    // 模块名，由模块在 build.gradle 中通过配置 annotationProcessorOptions 传进来
    String moduleName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();

        // 获取模块名
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            moduleName = options.get(Constants.KEY_MODULE_NAME);
        }

        if (StringUtils.isNotEmpty(moduleName)) {
            // 将模块名中的字符清理掉，并输出编译信息
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");
            messager.printMessage(Diagnostic.Kind.NOTE,
                    "The user has configuration the module name, it was [\" + moduleName + \"]");
        } else {
            // 如果没有配置模块名，就输出错误信息并抛异常。messager 输出类型为 ERROR 的话编译会直接停掉。
            messager.printMessage(Diagnostic.Kind.ERROR, Constants.NO_MODULE_NAME_TIPS);
            throw new RuntimeException("\"ARouter::Compiler >>> No module name, for more information, look at gradle log.\"");
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        // 返回支持的注解参数的 key
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add(Constants.KEY_MODULE_NAME);
        return hashSet;

        // 或者直接像下面这样：
        /*return new HashSet<String>() {{
            this.add(Constants.KEY_MODULE_NAME);
        }};*/
    }
}
