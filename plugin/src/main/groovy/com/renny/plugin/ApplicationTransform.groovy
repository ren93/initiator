package com.renny.plugin

import com.alibaba.fastjson.JSON
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import javassist.CtNewMethod
import javassist.Modifier
import org.gradle.api.Project

class ApplicationTransform {


    CtClass appCtClass
    ClassPool classPool
    Project mProject
    ArrayList<InitItem> pathList
    String contentMethod = "INIT_CONTENT"

    ApplicationTransform(Project project, CtClass ctClass, ArrayList<InitItem> pathList, ClassPool classPool) {
        this.appCtClass = ctClass
        this.classPool = classPool
        this.mProject = project
        this.pathList = pathList
    }

    void handleActivitySaveState() {

        //mProject.logger.error("ApplicationTransform ${appCtClass.name} ")

        CtMethod createCtMethod = appCtClass.declaredMethods.find {
            it.name == "onCreate" && it.parameterTypes == [] as CtClass[]
        }
      //  mProject.logger.error("ApplicationTransform ${createCtMethod} ")
        String content = ""

        try {
            content = JSON.toJSON(pathList)
        } catch (Exception e) {
            e.printStackTrace()
        }
        generateEnabledField(appCtClass, content)

        if (createCtMethod == null) {//application 没有 onCreate 方法
            createCtMethod = CtNewMethod.make(generateActivityRestoreMethod(), appCtClass)
            appCtClass.addMethod(createCtMethod)
        }
        createCtMethod.insertBefore("com.renny.mylibrary.InitManager.addPath(${contentMethod});\n" +
                "com.renny.mylibrary.InitManager.doInit(this);\n")
    }

    void generateEnabledField(CtClass ctClass, String path) {
        CtField pathCtField = ctClass.declaredFields.find {
            it.name == contentMethod && it.getType().name == "java.lang.String"
        }
        if (pathCtField != null) {
            ctClass.removeField(pathCtField)
        }
        pathCtField = new CtField(classPool.get("java.lang.String"), contentMethod, ctClass)
        pathCtField.setModifiers(Modifier.PRIVATE | Modifier.STATIC)
        ctClass.addField(pathCtField, CtField.Initializer.constant(path))
    }


    static String generateActivityRestoreMethod() {
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("public void onCreate() {\n ")
        stringBuilder.append("super.onCreate();\n")
        stringBuilder.append("}")
        return stringBuilder.toString()
    }
}
