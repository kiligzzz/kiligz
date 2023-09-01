package com.kiligz.classHelper;

import com.kiligz.io.FileUtil;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Class助手类
 * <pre>
 * 1.支持依据不同类加载器创建实例，实例可缓存，默认使用线程上下文类加载器 --- {@link #getInstance()}
 * 2.支持获取类、接口的 整个模块中 或 指定包 中的(直接|非抽象)子类、实现类的Class --- {@link #getSubAndImplClasses(Class, String...)}
 * 3.支持获取 整个模块中 或 指定包 中的所有类的Class --- {@link #loadClasses(String...)}
 * 4.支持获取 整个模块中 或 指定包 中的所有类的全限定名 --- {@link #getClassNames(String...)}
 * 5.支持根据类的全限定名获取类的Class --- {@link #loadClass(String)}
 *  (Tips:2、3、4、5都支持获取项目依赖的jar中的内容，并且都支持缓存Class)
 * 6.支持获取 整个模块中 或 指定包 或 一个类 的所有内部类的Class --- {@link #getInnerClasses(String...)}
 * </pre>
 *
 * @author Ivan
 * @since 2023/5/19
 */
public class ClassHelper {
    private static final String CLASS_SUFFIX = ".class";
    private static final String JAR_ROOT_START = "/BOOT-INF/classes/";
    private static final String FILE_ROOT_START = "/classes/";

    /**
     * 类加载器 -> 该类实例
     */
    private static final Map<ClassLoader, ClassHelper> CLASS_LOADER_TO_INSTANCE_MAP = new ConcurrentHashMap<>();

    /**
     * 类加载器
     */
    private final ClassLoader classLoader;

    /**
     * 类全限定名 -> 类
     */
    private final Map<String, Class<?>> classesMap;

    private ClassHelper(ClassLoader classLoader, Map<String, Class<?>> classesMap) {
        this.classLoader = classLoader;
        this.classesMap = classesMap;
    }

    /**
     * 默认使用线程上下文类加载器获取实例
     */
    public static ClassHelper getInstance() {
        return getInstance(Thread.currentThread().getContextClassLoader());
    }

    /**
     * 依据类加载器获取实例
     */
    public static ClassHelper getInstance(ClassLoader classLoader) {
        return CLASS_LOADER_TO_INSTANCE_MAP.computeIfAbsent(
                classLoader, key -> new ClassHelper(key, new ConcurrentHashMap<>()));
    }

    /**
     * 获取一个类的所有子类以及实现类，可指定包
     */
    public List<Class<?>> getSubAndImplClasses(Class<?> origin, String... packageNames) {
        return mergeList(
                getSubClasses(origin, packageNames),
                getImplClasses(origin, packageNames));
    }

    /**
     * 获取一个类的所有子类，可指定包
     */
    public List<Class<?>> getSubClasses(Class<?> origin, String... packageNames) {
        return filterList(
                loadClasses(packageNames),
                clazz -> isSubClass(clazz, origin));
    }

    /**
     * 获取一个接口的所有实现类，可指定包
     */
    public List<Class<?>> getImplClasses(Class<?> origin, String... packageNames) {
        // 不是接口则直接返回
        if (!origin.isInterface()) {
            return new ArrayList<>();
        }
        return filterList(
                loadClasses(packageNames),
                clazz -> isImplClass(clazz, origin));
    }

    /**
     * 获取一个接口的所有非抽象的实现类，可指定包
     */
    public List<Class<?>> getNonAbstractImplClasses(Class<?> origin, String... packageNames) {
        return filterList(
                getImplClasses(origin, packageNames),
                this::isNotAbstractClass);
    }

    /**
     * 获取一个类的直接子类以及实现类，可指定包
     */
    public List<Class<?>> getDirectSubAndImplClasses(Class<?> origin, String... packageNames) {
        return mergeList(
                getDirectSubClasses(origin, packageNames),
                getDirectImplClasses(origin, packageNames));
    }

    /**
     * 获取一个类的直接子类，可指定包
     */
    public List<Class<?>> getDirectSubClasses(Class<?> origin, String... packageNames) {
        return filterList(
                getSubClasses(origin, packageNames),
                sub -> isDirectSubClass(sub, origin));
    }

    /**
     * 获取一个接口的直接实现类，可指定包
     */
    public List<Class<?>> getDirectImplClasses(Class<?> origin, String... packageNames) {
        return filterList(
                getImplClasses(origin, packageNames),
                impl -> isDirectImplClass(impl, origin));
    }

    /**
     * 加载整个模块中或对应包内的所有类，不传值则为整个模块
     */
    public List<Class<?>> loadClasses(String... packageNames) {
        List<Class<?>> classList = new ArrayList<>();

        // 加载指定包下所有类
        List<String> classNames = getClassNames(packageNames);
        for (String className : classNames) {
            Class<?> clazz = loadClass(className);
            if (clazz != null) {
                classList.add(clazz);
            }
        }
        return classList;
    }

    /**
     * 加载项目中或对应包内的所有类，不传值则为项目根路径下
     */
    public List<String> getClassNames(String... packageNames) {
        // 不传包名时，默认为项目根路径
        if (packageNames.length == 0) {
            packageNames = new String[]{""};
        }

        Set<String> classNames = new LinkedHashSet<>();
        for (String packageName : packageNames) {
            URL packageURL = classLoader.getResource(packageNameToPath(packageName));
            if (packageURL == null) {
                continue;
            }

            boolean isJar = "jar".equals(packageURL.getProtocol());
            List<String> classNameList = isJar ?
                    getClassNamesFromJar(packageURL) : getClassNamesFromFile(packageURL);
            classNames.addAll(classNameList);
        }
        return new ArrayList<>(classNames);

    }

    /**
     * 指定类全限定名加载一个类
     */
    public Class<?> loadClass(String className) {
        return classesMap.computeIfAbsent(className, k -> {
            try {
                return classLoader.loadClass(className);
            } catch (Throwable ignore) {
            }
            return null;
        });
    }

    /**
     * 获取指定包的所有内部类
     */
    public List<Class<?>> getInnerClasses(String... packageNames) {
        List<Class<?>> innerClassList = new ArrayList<>();

        List<Class<?>> classList = loadClasses(packageNames);
        for (Class<?> clazz : classList) {
            innerClassList.addAll(getInnerClasses(clazz));
        }
        return innerClassList.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 获取一个类的所有内部类
     */
    public List<Class<?>> getInnerClasses(Class<?> clazz) {
        List<Class<?>> classList = new ArrayList<>();
        Collections.addAll(classList, clazz.getDeclaredClasses());

        List<Class<?>> innerClassList = classList.stream()
                .map(this::getInnerClasses)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        classList.addAll(innerClassList);
        return classList;
    }



    /**
     * 过滤结果
     */
    private List<Class<?>> filterList(List<Class<?>> classList, Predicate<Class<?>> predicate) {
        return classList.stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * 合并结果
     */
    private List<Class<?>> mergeList(List<Class<?>> list1, List<Class<?>> list2) {
        List<Class<?>> mergeList = new ArrayList<>();
        mergeList.addAll(list1);
        mergeList.addAll(list2);
        return mergeList;
    }

    /**
     * 是对应的子类
     */
    private boolean isSubClass(Class<?> clazz, Class<?> origin) {
        return origin != clazz
                && origin.isAssignableFrom(clazz)
                && origin.isInterface() == clazz.isInterface();
    }

    /**
     * 是对应的实现类
     */
    private boolean isImplClass(Class<?> clazz, Class<?> origin) {
        return origin != clazz && origin.isAssignableFrom(clazz) && !clazz.isInterface();
    }

    /**
     * 是抽象类
     */
    private boolean isNotAbstractClass(Class<?> clazz) {
        return !Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * 是直接子类
     */
    private boolean isDirectSubClass(Class<?> sub, Class<?> origin) {
        return sub.getSuperclass() == origin
                || Arrays.asList(sub.getInterfaces()).contains(origin);
    }

    /**
     * 是直接实现类
     */
    private boolean isDirectImplClass(Class<?> impl, Class<?> origin) {
        return Arrays.asList(impl.getInterfaces()).contains(origin);
    }

    /**
     * 从Jar中获取指定包的类全限定名列表
     */
    private List<String> getClassNamesFromJar(URL packageURL) {
        try {
            // 通过packageURL获取jarFile
            URL jarURL = new URL("jar:" + packageURL.getPath());
            JarURLConnection jarURLConnection = (JarURLConnection) jarURL.openConnection();
            JarFile jarFile = jarURLConnection.getJarFile();
            // 包路径
            String packagePath = jarURLConnection.getEntryName();

            return jarFile.stream()
                    .map(JarEntry::getName)
                    .filter(path -> path.startsWith(packagePath) && path.endsWith(CLASS_SUFFIX))
                    .map(path -> pathToClassName(path, JAR_ROOT_START))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 从文件路径下获取指定包的类全限定名列表
     */
    private List<String> getClassNamesFromFile(URL packageURL) {
        return FileUtil.listFiles(packageURL.getFile())
                .stream()
                .map(File::getAbsolutePath)
                .filter(path -> path.endsWith(CLASS_SUFFIX))
                .map(path -> pathToClassName(path, FILE_ROOT_START))
                .collect(Collectors.toList());
    }

    /**
     * 从路径中获取类全限定名
     */
    private String pathToClassName(String path, String startMark) {
        int start = path.contains(startMark) ?
                path.lastIndexOf(startMark) + startMark.length() : 0;
        int end = path.length() - CLASS_SUFFIX.length();
        return path.substring(start, end).replace(File.separator, ".");
    }

    /**
     * 包名转路径
     */
    private String packageNameToPath(String packageName) {
        if (packageName.isEmpty()) {
            return "";
        }
        return packageName.replace(".", File.separator) + File.separator;
    }
}
