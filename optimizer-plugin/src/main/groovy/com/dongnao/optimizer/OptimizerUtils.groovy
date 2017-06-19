package com.dongnao.optimizer

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.GradleException
import org.gradle.api.Project

import javax.imageio.ImageIO
import java.awt.image.BufferedImage;

class OptimizerUtils {

    def static final DRAWABLE = "drawable"
    def static final MIPMAP = "mipmap"
    def static final PNG9 = ".9.png"
    def static final PNG = ".png"
    def static final JPG = ".jpg"


    def static isImgFolder(File file) {
        return file.name.startsWith(DRAWABLE) || file.name.startsWith(MIPMAP)
    }

    def static isPreOptimizePng(File file) {
        return file.name.endsWith(PNG) && !file.name.endsWith(PNG9)
    }

    def static isPreOptimizeJpg(File file) {
        return file.name.endsWith(JPG)
    }


    def static isTransparent(File file) {
        BufferedImage img = ImageIO.read(file);
        return img.colorModel.hasAlpha()
    }


    def static getTool(Project project, String name) {
        def toolName
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            toolName = "${name}_win.exe"
        } else if (Os.isFamily(Os.FAMILY_MAC)) {
            toolName = "${name}_darwin"
        } else {
            toolName = "${name}_linux"
        }
        def path = "${project.buildDir.absolutePath}/tools/$name/$toolName"
        println path
        def file = new File(path)
        if (!file.exists()) {
            file.parentFile.mkdirs()
            new FileOutputStream(file).withStream {
                def inputStream = OptimizerUtils.class.getResourceAsStream("/$name/${toolName}")
                it.write(inputStream.getBytes())
            }
        }
        if (file.exists() && file.setExecutable(true)) {
            return file.absolutePath
        }
        throw GradleException("$toolName 工具不存在或者无法执行")
    }


}