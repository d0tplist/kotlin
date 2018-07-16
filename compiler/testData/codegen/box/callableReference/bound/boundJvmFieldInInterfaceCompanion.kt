// IGNORE_BACKEND: JVM_IR
// !LANGUAGE: +JvmFieldInInterface
// TARGET_BACKEND: JVM
// WITH_RUNTIME
import kotlin.reflect.full.memberProperties

class Bar(val value: String)

interface  Foo {

    companion object {
        @JvmField
        val z = Bar("OK")
    }
}


fun box(): String {
    val field = Foo.Companion::z
    return field.get().value
}
