package com.demo.arouter.api.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.LruCache;

import com.demo.arouter.api.core.ILoadGroup;
import com.demo.arouter.api.core.ILoadParameter;
import com.demo.arouter.api.core.ILoadPath;
import com.demo.arouter.model.RouteMeta;

import java.util.Map;

public class ARouter {

    public static final String SEPARATOR = "$$";
    public static final String PROJECT = "ARouter";
    public static final String PREFIX_OF_GROUP_NAME = PROJECT + SEPARATOR + "Group" + SEPARATOR;
    public static final String PACKAGE_OF_GENERATE_FILE = "com.demo.arouter.routes";
    public static final String SUFFIX_OF_PARAMETER_FILE = "$$Parameter";

    private static volatile ARouter sInstance;
    private String mGroup;
    private String mPath;
    private LruCache<String, ILoadGroup> mGroupCache;
    private LruCache<String, ILoadPath> mPathCache;
    private LruCache<String, ILoadParameter> mParameterCache;

    private ARouter() {
        mGroupCache = new LruCache<>(100);
        mPathCache = new LruCache<>(100);
        mParameterCache = new LruCache<>(100);
    }

    public static ARouter getInstance() {
        if (sInstance == null) {
            synchronized (ARouter.class) {
                if (sInstance == null) {
                    sInstance = new ARouter();
                }
            }
        }
        return sInstance;
    }

    public BundleManager build(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new IllegalArgumentException("Path should not be null!");
        }

        mPath = path;
        mGroup = extractGroup(path);

        return new BundleManager();
    }

    /**
     * 从 path 中提取出默认的 group，如果 @Route 中没填 group()，
     * 那么默认 group 就作为最终的路由组。
     */
    private String extractGroup(String path) {
        if (!path.startsWith("/") || path.lastIndexOf("/") == 0) {
            throw new IllegalArgumentException("Extract the default group failed, the path must be start with '/' and contain more than 2 '/'!");
        }

        String defaultGroup = path.substring(1, path.indexOf("/", 1));
        if (TextUtils.isEmpty(defaultGroup)) {
            throw new IllegalArgumentException("Extract the default group failed! There's nothing between 2 '/'!");
        }
        return defaultGroup;
    }

    /**
     * 根据组名去 Group Map 里找到对应的 Path 的 Class 对象，通过反射拿到该 Class
     * 对应的实例，再从 Map 中根据路径找到对应的 RouteMeta，拿出 Class 做跳转
     */
    public Object navigation(Context context, BundleManager bundleManager, int requestCode) {
        try {
            // Group 路由文件的全类名
            String groupFileName = PACKAGE_OF_GENERATE_FILE + "." + PREFIX_OF_GROUP_NAME + mGroup;
            // 先去缓存中找 Group 路由文件，缓存没有则通过反射获取
            ILoadGroup loadGroup = mGroupCache.get(groupFileName);
            if (loadGroup == null) {
                loadGroup = (ILoadGroup) Class.forName(groupFileName).newInstance();
                mGroupCache.put(groupFileName, loadGroup);
            }

            if (loadGroup.loadGroup() == null) {
                throw new RuntimeException("Group route table loads failed.");
            }

            // 类似的过程，去获取 Path 路由文件
            ILoadPath loadPath = mPathCache.get(mPath);
            if (loadPath == null) {
                loadPath = loadGroup.loadGroup().get(mGroup).newInstance();
                mPathCache.put(mPath, loadPath);
            }

            Map<String, RouteMeta> pathMap = loadPath.loadPath();
            if (pathMap == null) {
                throw new RuntimeException("Path route table loads failed.");
            }

            RouteMeta routeMeta = pathMap.get(mPath);
            if (routeMeta != null) {
                switch (routeMeta.getRouteType()) {
                    case ACTIVITY:
                        Intent intent = new Intent(context, routeMeta.getTargetClass());
                        intent.putExtras(bundleManager.getBundle());
                        if (requestCode > 0) {
                            ((Activity) context).startActivityForResult(intent, requestCode);
                        } else {
                            context.startActivity(intent);
                        }
                        break;
                    // todo 其它类型处理待添加...
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void inject(Activity activity) {
        String activityName = activity.getClass().getName();
        ILoadParameter iLoadParameter = mParameterCache.get(activityName);
        try {
            if (iLoadParameter == null) {
                // 缓存中没有，就要通过反射拿到接口实现类
                String parameterFileName = activityName + SUFFIX_OF_PARAMETER_FILE;
                iLoadParameter = (ILoadParameter) Class.forName(parameterFileName).newInstance();
                mParameterCache.put(activityName, iLoadParameter);
            }

            iLoadParameter.loadParameter(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
