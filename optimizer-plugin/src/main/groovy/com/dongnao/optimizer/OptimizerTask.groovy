package com.dongnao.optimizer

import groovy.xml.Namespace
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class OptimizerTask extends DefaultTask {

    static def PNG_TOOL = "pngcrush"
    static def JPG_TOOL = "guetzli"
    static def WEBP_TOOL = "cwebp"


    @Input
    File manifestFile

    @Input
    File res

    @Input
    int apiLevel

    @Input
    int quality


    def webpTool
    def jpgTool
    def pngTool
    String launcher
    String round_launcher

    OptimizerTask() {
        group "optimize"
        webpTool = OptimizerUtils.getTool(project, WEBP_TOOL)
        jpgTool = OptimizerUtils.getTool(project, JPG_TOOL)
        pngTool = OptimizerUtils.getTool(project, PNG_TOOL)
    }

    @TaskAction
    def run() {
        println "==========optimizer============="
        println("find webp tool :$webpTool")
        println("find jpg tool :$jpgTool")
        println("find png tool :$pngTool")
        def ns = new Namespace("http://schemas.android.com/apk/res/android", "android")
        Node xml = new XmlParser().parse(manifestFile)
        Node application = xml.application[0]
        //@mipmap/ic_launcher
        launcher = application.attributes()[ns.icon]
        round_launcher = application.attributes()[ns.roundIcon]
        launcher = launcher.substring(launcher.lastIndexOf("/") + 1, launcher.length())
        round_launcher = round_launcher.substring(round_launcher.lastIndexOf("/") + 1, round_launcher.length())
        println("find app launcher icon  :$launcher")
        println("find app round launcher icon  :$round_launcher")
        def pngs = []
        def jpgs = []
        res.eachDir { dir ->
            if (OptimizerUtils.isImgFolder(dir)) {
                dir.eachFile { f ->
                    //launcher就不管
                    if (OptimizerUtils.isPreOptimizeJpg(f) && isNonLauncher(f))
                        jpgs << f
                    if (OptimizerUtils.isPreOptimizePng(f) && isNonLauncher(f))
                        pngs << f
                }
            }
        }
        //Google Play accepts APKs only if the included app launcher icons use the PNG format.
        //Google Play does not support other file formats, like JPEG or WebP, for app launcher icons.

        println("current apiLevel is $apiLevel")
        if (apiLevel >= 14 && apiLevel < 18) {
            //能够使用非透明webp
            def compress = []
            pngs.each {
                //如果有alpha通道 则压缩
                if (OptimizerUtils.isTransparent(it)) {
                    compress << it
                    println("${it.name} has alpha channel,don't convert webp")
                } else {
                    //转换wenp
                    convertWebp(webpTool, it)
                }
            }
            compressImg(pngTool, true, compress)
            jpgs.each {
                convertWebp(webpTool, it)
            }

        } else if (apiLevel >= 18) {
            //能够使用有透明的webp
            pngs.each {
                convertWebp(webpTool, it)
            }
            jpgs.each {
                convertWebp(webpTool, it)
            }
        } else {
            //进行压缩
            compressImg(pngTool, true, pngs)
            compressImg(jpgTool, false, jpgs)
        }
    }

    def isNonLauncher(File f) {
        return f.name != "${launcher}.png" && f.name != "${launcher}.jpg" && f.name != "${round_launcher}.png" && f.name != "${round_launcher}.jpg"
    }

    //cwebp  -q quality in.png -o out.webp
    def convertWebp(String tool, File file) {
        //转换wenp
        String name = file.name
        name = name.substring(0, name.lastIndexOf("."))
        def result = "$tool -q $quality ${file.absolutePath} -o ${file.parent}/${name}.webp"
                .execute()
        result.waitForProcessOutput()
        if (result.exitValue() == 0) {
            file.delete()
            println("convert ${file.absolutePath} to webp success")
        } else {
            println("convert ${file.absolutePath} to webp error")
        }
    }

    //pngcrush  -brute -rem alla -reduce -q in.png out.png
    //guetzli --quality quality  in.jpg out.jpg
    def compressImg(String tool, boolean isPng, def files) {
        files.each {
            File file ->
                def output = new File(file.parent, "temp-preOptimizer-${file.name}")
                def result
                if (isPng)
                    result = "$tool -brute -rem alla -reduce -q ${file.absolutePath}  ${output.absolutePath}"
                            .execute()
                else
                    result = "$tool --quality $quality ${file.absolutePath}  ${output.absolutePath}"
                            .execute()
                result.waitForProcessOutput()
                if (result.exitValue() == 0) {
                    if (output.exists()) {
                        if (file.exists()) {
                            file.delete()
                        }
                        output.renameTo(file)
                    }
                    println("compress ${file.absolutePath} success")
                } else {
                    println("compress ${file.absolutePath} error")
                }
                if (output.exists()) {
                    output.delete()
                }
        }
    }

//    def compressJpg(String tool, def jpgs) {
//        jpgs.each {
//            File file ->
//                def output = new File(file.parent, "temp-preOptimizer-${file.name}")
//                def result = "$tool --quality $quality ${file.absolutePath}  ${output.absolutePath}"
//                        .execute()
//                result.waitForProcessOutput()
//                if (result.exitValue() == 0) {
//                    if (output.exists()) {
//                        if (file.exists()) {
//                            file.delete()
//                        }
//                        output.renameTo(file)
//                    }
//                    project.logger.info("compress ${file.absolutePath} success")
//                } else {
//                    project.logger.error("compress ${file.absolutePath} error")
//                }
//                if (output.exists()) {
//                    output.delete()
//                }
//        }
//    }

    def static main(args) {
        def i = new File("/Users/xiang/listener/optimizer/optimizer/app/build/intermediates/res/merged/aaa/debug/drawable-hdpi/ic_launcher2.png")
        def o = new File("/Users/xiang/listener/optimizer/optimizer/app/build/intermediates/res/merged/aaa/debug/drawable-hdpi/ic_launcher.png")
        println i.exists()
        println o.exists()
        if (i.exists() && o.exists()) {
            println o.delete()
            println i.renameTo(o)
        }


    }

}