package com.demo.arouter.compiler.utils;

public class Constants {

    // Generate
    public static final String METHOD_LOAD_PATH = "loadPath";
    public static final String METHOD_LOAD_GROUP = "loadGroup";
    public static final String VARIABLE_PATH_MAP = "pathMap";
    public static final String VARIABLE_GROUP_MAP = "groupMap";
    public static final String SEPARATOR = "$$";
    public static final String PROJECT = "ARouter";
    public static final String PREFIX_OF_GROUP_NAME = PROJECT + SEPARATOR + "Group" + SEPARATOR;
    public static final String PREFIX_OF_PATH_NAME = PROJECT + SEPARATOR + "Path" + SEPARATOR;
    public static final String PACKAGE_OF_GENERATE_FILE = "com.demo.arouter.routes";


    // Options of processor
    public static final String KEY_MODULE_NAME = "AROUTER_MODULE_NAME";


    // Log
    public static final CharSequence NO_MODULE_NAME_TIPS = "These no module name, at 'build.gradle', like :\n" +
            "android {\n" +
            "    defaultConfig {\n" +
            "        ...\n" +
            "        javaCompileOptions {\n" +
            "            annotationProcessorOptions {\n" +
            "                arguments = [AROUTER_MODULE_NAME: project.getName()]\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    // Custom interface
    private static final String AROUTER_PACKAGE = "com.demo.arouter";
    private static final String API_PACKAGE = ".api.core";
    public static final String IROUTE_PATH = AROUTER_PACKAGE + API_PACKAGE + ".ILoadPath";
    public static final String IROUTE_GROUP = AROUTER_PACKAGE + API_PACKAGE + ".ILoadGroup";

    // Annotation type
    public static final String ANNOTATION_TYPE_ROUTE = AROUTER_PACKAGE + ".annotation.Route";
    public static final String ANNOTATION_TYPE_AUTOWIRED = AROUTER_PACKAGE + ".annotation.Autowired";


}
