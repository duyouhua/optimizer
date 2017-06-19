package com.dongnao.optimizer

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Plugin
import org.gradle.api.Project

class OptimizerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
//        com.android.build.gradle.internal.api.ApplicationVariantImpl
        //println it.apkVariantData.variantConfiguration.minSdkVersion.apiLevel
        //println it.mergeResources.minSdk
        //println it.mergedFlavor.minSdkVersion.apiLevel
        project.afterEvaluate {
            project.android.applicationVariants.all {
                BaseVariant variant ->
                    def task = project.tasks.create("optimize${variant.name.capitalize()}", OptimizerTask) {
                        //最好是获得处理之后的 确定了的manifest文件
                        manifestFile = variant.outputs.first().processManifest.manifestOutputFile
                        res = variant.mergeResources.outputDir
                        apiLevel = variant.mergeResources.minSdk
                    }
                    //将android插件的处理资源任务依赖于自定义任务
                    variant.outputs.first().processResources.dependsOn task
                    task.dependsOn variant.outputs.first().processManifest
            }
        }

    }


}