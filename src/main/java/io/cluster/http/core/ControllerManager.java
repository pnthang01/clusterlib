/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.http.core;

import io.cluster.http.annotation.HttpMethod;
import io.cluster.http.annotation.RequestMapping;
import io.cluster.http.annotation.RequestMethod;
import io.cluster.http.annotation.RequestParam;
import io.cluster.util.ClassLoaderUtil;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class ControllerManager {

    private static final Logger LOGGER = LogManager.getLogger(ControllerManager.class.getName());

    private Map<String, MethodMapping> controllerMap;

    private static ControllerManager _instance;

    public static void initialize(String... packages) throws ClassNotFoundException, IOException {
        List<Class<?>> allClasses = new ArrayList();
        for (String pkg : packages) {
            allClasses.addAll(ClassLoaderUtil.getClassesForPackage(pkg));
        }
        _instance = new ControllerManager(allClasses);
    }

    public static void initialize(List<String> packages) throws ClassNotFoundException, IOException {
        List<Class<?>> allClasses = new ArrayList();
        for (String pkg : packages) {
            allClasses.addAll(ClassLoaderUtil.getClassesForPackage(pkg));
        }
        _instance = new ControllerManager(allClasses);
    }

    public ControllerManager(List<Class<?>> classes) {
        controllerMap = new ConcurrentHashMap();
        LOGGER.info("Initialize total " + classes.size() + " controllers. Start to mapping...");
        for (Class clazz : classes) {
            try {
                RequestMapping anno = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
                if (null != anno) {
                    String moduleMapping = anno.uri();
                    AbstractController newInstance = (AbstractController) clazz.newInstance();
                    for (Method method : clazz.getDeclaredMethods()) {
                        RequestMapping methodAnno = method.getAnnotation(RequestMapping.class);
                        RequestMethod requestMethodAnno = method.getAnnotation(RequestMethod.class);
                        HttpMethod requestMethod = requestMethodAnno == null ? HttpMethod.GET : requestMethodAnno.method();
                        if (null != methodAnno) {
                            String finalMapping = moduleMapping + methodAnno.uri();
                            LOGGER.info("Follwing uri is mapped: " + finalMapping + " method=" + method.getName()
                                    + " parameters.size=" + method.getParameterTypes().length, true);
                            MethodMapping methodMapping = new MethodMapping(newInstance, method, requestMethod);
                            controllerMap.put(finalMapping, methodMapping);
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("Cannot auto mapping to class: " + clazz.getSimpleName() + " with error", ex);
            }
        }
    }

    public static Object invokeUri(String uri, HttpMethod requestMethod, Map<String, List<String>> params) {
        try {
            MethodMapping get = _instance.controllerMap.get(uri);
            if (null == get) {
                LOGGER.error("Uri " + uri + " is 404, cannot invoke."); // URI is not found
                return 404;
            } else if (requestMethod.compareTo(get.getRequestMethod()) != 0) {
                LOGGER.error("Uri " + uri + " is 405, cannot invoke."); // Wrong Request method
                return 405;
            } else {
                Method method = get.getMethod();
                LOGGER.info("Method name: " + method.getName());
                Class<?>[] paramClasses = method.getParameterTypes();
                Object[] paramValue = new Object[paramClasses.length];
                for (int i = 0; i < paramClasses.length; ++i) {
                    Class<?> type = paramClasses[i];
                    RequestParam annotation = (RequestParam) method.getParameterAnnotations()[i][0];
                    List<String> paramList = params.get(annotation.name());
                    String value = paramList == null ? null : paramList.get(0);
                    if (null == value || value.isEmpty()) {
                        if (annotation.required()) {
                            LOGGER.error("Param is missing with index: " + i);
                            return 504;
                        }
                        paramValue[i] = (annotation.defaultValue() != null && !annotation.defaultValue().isEmpty())
                                ? StaticFileHandler.castParamValue(type, annotation.defaultValue()) : null;
                    } else {
                        paramValue[i] = StaticFileHandler.castParamValue(type, value);
                    }
                }
                return method.invoke(get.getController(), paramValue);
            }
        } catch (Exception ex) {
            LOGGER.error("Error when invokeUri: %s", ex);
            return null;
        }
    }

    private class MethodMapping {

        private final AbstractController controller;
        private final Method method;
        private final HttpMethod requestMethod;

        public MethodMapping(AbstractController controller, Method method, HttpMethod requestMethod) {
            this.controller = controller;
            this.method = method;
            this.requestMethod = requestMethod;
        }

        public AbstractController getController() {
            return controller;
        }

        public Method getMethod() {
            return method;
        }

        public HttpMethod getRequestMethod() {
            return requestMethod;
        }

    }

}
