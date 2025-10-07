plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

stonecutter {
    shared {
        set("JAVA_VERSION", "21")
    }
    kotlin.set(false)

    create("1.21.1-neoforge") {
        set("MC_VERSION", "1.21.1")
        set("NEOFORGE_VERSION", "21.1.209")
    }
    create("1.21.4-neoforge") {
        set("MC_VERSION", "1.21.4")
        set("NEOFORGE_VERSION", "21.4.154")
    }
}
