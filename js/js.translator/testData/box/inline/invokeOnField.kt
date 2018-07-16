// EXPECTED_REACHABLE_NODES: 1189
class Bar {
    inline operator fun invoke(f: () -> String) { f() }
}

class Foo {
    val bar = Bar()
}

//inline operator fun Bar.invoke(f: () -> String) { f() }

fun fox(): String {
    Bar()() { return "OK" }
    return "fail"
}

fun bax(): String {
    Foo().bar() { return "OK" }
    return "fail"
}

fun box(): String {
    Foo().bar { return "OK" }
    return "fail"
}