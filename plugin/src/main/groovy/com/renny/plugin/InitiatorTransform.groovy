package com.renny.plugin

import com.android.build.api.transform.*
import com.google.common.collect.Sets
import com.renny.libcore.AppInit
import com.renny.libcore.InitContext
import groovy.io.FileType
import javassist.ClassPath
import javassist.ClassPool
import javassist.CtClass
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import java.lang.reflect.Constructor

class InitiatorTransform extends Transform {

    Project mProject
    CtClass appCtClass
    File appDest
    def initPath = new ArrayList<InitItem>()


    InitiatorTransform(Project project) {
        mProject = project
    }

    @Override
    String getName() {
        return "initiator"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return Collections.singleton(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        if (mProject.plugins.hasPlugin("com.android.application")) {
            return Sets.immutableEnumSet(
                    QualifiedContent.Scope.PROJECT,
                    QualifiedContent.Scope.SUB_PROJECTS,
                    QualifiedContent.Scope.EXTERNAL_LIBRARIES)
        } else if (mProject.plugins.hasPlugin("com.android.library") || mProject.plugins.hasPlugin("java-library")) {
            return Sets.immutableEnumSet(
                    QualifiedContent.Scope.PROJECT)
        } else {
            return Collections.emptySet()
        }
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        ClassPool classPool = ClassPool.getDefault()

        def classPath = []

        classPool.appendClassPath(mProject.android.bootClasspath[0].toString())

        try {
            Class jarClassPathClazz = Class.forName("javassist.JarClassPath")
            Constructor constructor = jarClassPathClazz.getDeclaredConstructor(String.class)
            constructor.setAccessible(true)

            transformInvocation.inputs.each { input ->


                def subProjectInputs = []

                input.jarInputs.each { jarInput ->
                    // mProject.logger.error("jar input=   " + jarInput.file.getAbsolutePath())
                    ClassPath clazzPath = (ClassPath) constructor.newInstance(jarInput.file.absolutePath)
                    classPath.add(clazzPath)
                    classPool.appendClassPath(clazzPath)

                    def jarName = jarInput.name
                    if (jarName.endsWith(".jar")) {
                        jarName = jarName.substring(0, jarName.length() - 4)
                    }
                    //mProject.logger.error("jar name   " + jarName)
                    if (jarName.startsWith(":")) {
                       // mProject.logger.error("jar name startsWith冒号   " + jarName)
                        //handle it later, after classpath set
                        subProjectInputs.add(jarInput)
                    } else {
                        def dest = transformInvocation.outputProvider.getContentLocation(jarName,
                                jarInput.contentTypes, jarInput.scopes, Format.JAR)
                        // mProject.logger.error("jar output path:" + dest.getAbsolutePath())
                        FileUtils.copyFile(jarInput.file, dest)
                    }
                }

                // Handle library project jar here
                subProjectInputs.each { jarInput ->

                    def jarName = jarInput.name
                    if (jarName.endsWith(".jar")) {
                        jarName = jarName.substring(0, jarName.length() - 4)
                    }

                    if (jarName.startsWith(":")) {
                        // sub project
                        File unzipDir = new File(
                                jarInput.file.getParent(),
                                jarName.replace(":", "") + "_unzip")
                        if (unzipDir.exists()) {
                            unzipDir.delete()
                        }
                        unzipDir.mkdirs()
                        Decompression.uncompress(jarInput.file, unzipDir)

                        File repackageFolder = new File(
                                jarInput.file.getParent(),
                                jarName.replace(":", "") + "_repackage"
                        )

                        FileUtils.copyDirectory(unzipDir, repackageFolder)

                        unzipDir.eachFileRecurse(FileType.FILES) { File it ->
                            checkAndTransformClass(classPool, it, repackageFolder)
                        }

                        // re-package the folder to jar
                        def dest = transformInvocation.outputProvider.getContentLocation(
                                jarName, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                        Compressor zc = new Compressor(dest.getAbsolutePath())
                        zc.compress(repackageFolder.getAbsolutePath())
                    }
                }

                input.directoryInputs.each { dirInput ->
                    def outDir = transformInvocation.outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
                    classPool.appendClassPath(dirInput.file.absolutePath)
                    // dirInput.file is like "build/intermediates/classes/debug"
                    int pathBitLen = dirInput.file.toString().length()

                    def callback = { File it ->
                        def path = "${it.toString().substring(pathBitLen)}"
                        if (it.isDirectory()) {
                            new File(outDir, path).mkdirs()
                        } else {
                            boolean handled = checkAndTransformClass(classPool, it, outDir)
                            if (!handled) {
                                // copy the file to output location
                                new File(outDir, path).bytes = it.bytes
                            }
                        }
                    }
                    if (dirInput.changedFiles == null || dirInput.changedFiles.isEmpty()) {
                        dirInput.file.traverse(callback)
                    } else {
                        dirInput.changedFiles.keySet().each(callback)
                    }
                }
            }

            if (appCtClass != null) {
                mProject.logger.error("appCtClass==  ${appCtClass.name}    size" + initPath.size())
                if (initPath.size() > 0) {
                    ApplicationTransform applicationTransform = new ApplicationTransform(mProject,
                            appCtClass, initPath, classPool)
                    applicationTransform.handleActivitySaveState()
                }
                appCtClass.writeFile(appDest.absolutePath)
                appCtClass.detach()
            }
        } finally {
            classPath.each { it ->
                classPool.removeClassPath(it)
            }
        }

    }


    boolean checkAndTransformClass(ClassPool classPool, File file, File dest) {


        CtClass androidAppCtClass = classPool.get("android.app.Application")
        CtClass initCtClass = classPool.get("com.renny.mylibrary.IAppInit")

        mProject.logger.error("androidAppCtClass  ${androidAppCtClass.name}")
        mProject.logger.error("initCtClass  ${initCtClass.name}")


        classPool.importPackage("android.os")
        classPool.importPackage("android.util")

        if (!file.name.endsWith("class")) {
            return false
        }

        CtClass ctClass
        try {
            ctClass = classPool.makeClass(new FileInputStream(file))
        } catch (Throwable throwable) {
            mProject.logger.error("Parsing class file ${file.getAbsolutePath()} fail.", throwable)
            return false
        }

        mProject.logger.error("checkAndTransformClass  ${ctClass.name}")
        boolean handled = false
        if (androidAppCtClass != null && ctClass.subclassOf(androidAppCtClass)) {
            mProject.logger.error("FindApplication ${ctClass.getAnnotation(InitContext.class)} ")
            appCtClass = ctClass
            appDest = dest
            handled = true
        }
        if (initCtClass != null && ctClass.getInterfaces().contains(initCtClass)) {
            def initItem=new InitItem()
            initItem.setPath(ctClass.name)

            Object annotation =  ctClass.getAnnotation(AppInit.class)
            mProject.logger.error("FindApplication ${ctClass.getAnnotation(AppInit.class)} ")
            if (annotation != null) {
                AppInit appInit = (AppInit)annotation
                initItem.setBackground(appInit.background())
                initItem.setDelay(appInit.delay())
                initItem.setOnlyInDebug(appInit.onlyInDebug())
                initItem.setInChildProcess(appInit.inChildProcess())
                initItem.setPriority(appInit.priority())
            }
            initPath.add(initItem)
            mProject.logger.error("FindAppInit  ${ctClass.name} ")
        }

        return handled
    }


}