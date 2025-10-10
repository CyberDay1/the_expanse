plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

// --- Global Java 21 toolchain enforcement ---
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

// --- Stonecutter configuration ---
stonecutter {
    shared {
        set("JAVA_VERSION", "21")
        set("MC_VERSION_NEXT", "1.22")
    }
    kotlin.set(false)

    create("1.21.1-neoforge") {
        set("MC_VERSION", "1.21.1")
        set("NEOFORGE_VERSION", "21.1.209")
        set("PACK_FORMAT", "48")
    }
    create("1.21.2-neoforge") {
        set("MC_VERSION", "1.21.2")
        set("NEOFORGE_VERSION", "21.2.1-beta")
        set("PACK_FORMAT", "57")
    }
    create("1.21.3-neoforge") {
        set("MC_VERSION", "1.21.3")
        set("NEOFORGE_VERSION", "21.3.93")
        set("PACK_FORMAT", "57")
    }
    create("1.21.4-neoforge") {
        set("MC_VERSION", "1.21.4")
        set("NEOFORGE_VERSION", "21.4.154")
        set("PACK_FORMAT", "61")
    }
    create("1.21.5-neoforge") {
        set("MC_VERSION", "1.21.5")
        set("NEOFORGE_VERSION", "21.5.95")
        set("PACK_FORMAT", "71")
    }
    create("1.21.6-neoforge") {
        set("MC_VERSION", "1.21.6")
        set("NEOFORGE_VERSION", "21.6.20-beta")
        set("PACK_FORMAT", "80")
    }
    create("1.21.7-neoforge") {
        set("MC_VERSION", "1.21.7")
        set("NEOFORGE_VERSION", "21.7.25-beta")
        set("PACK_FORMAT", "81")
    }
    create("1.21.8-neoforge") {
        set("MC_VERSION", "1.21.8")
        set("NEOFORGE_VERSION", "21.8.47")
        set("PACK_FORMAT", "81")
    }
    create("1.21.9-neoforge") {
        set("MC_VERSION", "1.21.9")
        set("NEOFORGE_VERSION", "21.9.16-beta")
        set("PACK_FORMAT", "88")
    }
    create("1.21.10-neoforge") {
        set("MC_VERSION", "1.21.10")
        set("NEOFORGE_VERSION", "21.10.5-beta")
        set("PACK_FORMAT", "88")
    }
}

// --- Dependency lock issue mitigation ---
configurations.all {
    resolutionStrategy {
        force(
            "io.netty:netty-transport-native-epoll:4.1.107.Final",
            "io.netty:netty-transport:4.1.107.Final",
            "io.netty:netty-buffer:4.1.107.Final",
            "io.netty:netty-common:4.1.107.Final",
            "io.netty:netty-resolver:4.1.107.Final",
            "io.netty:netty-codec:4.1.107.Final"
        )
    }
}
