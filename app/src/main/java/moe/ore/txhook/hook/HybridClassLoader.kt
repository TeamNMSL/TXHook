package moe.ore.txhook.hook

import android.content.Context
import java.lang.Error
import java.net.URL

class HybridClassLoader(
    private var clPreload: ClassLoader? = null,
    private var clBase: ClassLoader? = null
) : ClassLoader() {

    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String?, resolve: Boolean): Class<*>? {
        try {
            try {
                return sBootClassLoader.loadClass(name)
            } catch (ignored: ClassNotFoundException) {
            }
            if (name != null && isConflictingClass(name)) {
                //Nevertheless, this will not interfere with the host application,
                //classes in host application SHOULD find with their own ClassLoader, eg Class.forName()
                //use shipped androidx and kotlin lib.
                throw ClassNotFoundException(name)
            }
            // The ClassLoader for some apk-modifying frameworks are terrible, XposedBridge.class.getClassLoader()
            // is the sane as Context.getClassLoader(), which mess up with 3rd lib, can cause the ART to crash.
            if (clPreload != null) {
                try {
                    return clPreload!!.loadClass(name)
                } catch (ignored: ClassNotFoundException) {
                }
            }
            if (clBase != null) {
                try {
                    return clBase!!.loadClass(name)
                } catch (ignored: ClassNotFoundException) {
                }
            }
            throw ClassNotFoundException(name)
        } catch (e: Throwable) {
            if (e is ClassNotFoundException) {
                throw ClassNotFoundException(name)
            }
            e.printStackTrace()
        }
        return null
    }

    override fun getResource(name: String?): URL? {
        return clPreload!!.getResource(name) ?: clBase!!.getResource(name)
    }

    companion object {
        private val sBootClassLoader: ClassLoader = Context::class.java.classLoader!!

        /**
         * 把宿主和模块共有的 package 扔这里.
         *
         * @param name NonNull, class name
         * @return true if conflicting
         */
        fun isConflictingClass(name: String): Boolean {
            return (name.startsWith("androidx.") || name.startsWith("android.support.v4.")
                    || name.startsWith("kotlin.") || name.startsWith("kotlinx.")
                    || name.startsWith("com.tencent.mmkv.")
                    || name.startsWith("com.android.tools.r8.")
                    || name.startsWith("com.google.android.material.")
                    || name.startsWith("com.google.gson.")
                    || name.startsWith("org.intellij.lang.annotations.")
                    || name.startsWith("org.jetbrains.annotations."))
        }
    }
}