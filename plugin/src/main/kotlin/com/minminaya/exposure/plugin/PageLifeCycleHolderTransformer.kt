package com.minminaya.exposure.plugin

import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.google.auto.service.AutoService
import org.objectweb.asm.tree.ClassNode


@AutoService(ClassTransformer::class)
class PageLifeCycleHolderTransformer : ClassTransformer {

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
//        println("Transforming ${klass.name}")
        if (klass.name in IGNORE_CLASS_NAME_LIST) {
            return klass
        }

        //将继承自 androidx/activity/ComponentActivity 的类的父类改为 IGNORE_ACTIVITY_NAME
        if (klass.superName == "androidx/activity/ComponentActivity") {
            klass.superName = IGNORE_ACTIVITY_NAME
        }

        //将继承自 androidx/fragment/app/Fragment 的类的父类改为 IGNORE_FRAGMENT_NAME
        if (klass.superName == "androidx/fragment/app/Fragment") {
            klass.superName = IGNORE_FRAGMENT_NAME
        }
        return klass
    }

    companion object {
        private const val IGNORE_FRAGMENT_NAME =
            "com/minminaya/exposure/container/WrapExposureFragment"
        private const val IGNORE_ACTIVITY_NAME =
            "com/minminaya/exposure/container/WrapExposureActivity"

        private val IGNORE_CLASS_NAME_LIST = listOf(
            IGNORE_ACTIVITY_NAME,
            IGNORE_FRAGMENT_NAME,
        )
    }

}