import dev.kikugie.stonecutter.*

stonecutter {
    // Set the active version
    active("1.21.1-neoforge")

    // Register all supported versions
    versions {
        register("1.21.1-neoforge") {
            data {
                set("MC_VERSION", "1.21.1")
                set("NEOFORGE_VERSION", "21.1.209")
                set("PACK_FORMAT", "48")
            }
        }
        register("1.21.2-neoforge") {
            data {
                set("MC_VERSION", "1.21.2")
                set("NEOFORGE_VERSION", "21.2.133")
                set("PACK_FORMAT", "57")
            }
        }
        register("1.21.3-neoforge") {
            data {
                set("MC_VERSION", "1.21.3")
                set("NEOFORGE_VERSION", "21.3.78")
                set("PACK_FORMAT", "57")
            }
        }
        register("1.21.4-neoforge") {
            data {
                set("MC_VERSION", "1.21.4")
                set("NEOFORGE_VERSION", "21.4.154")
                set("PACK_FORMAT", "61")
            }
        }
        register("1.21.5-neoforge") {
            data {
                set("MC_VERSION", "1.21.5")
                set("NEOFORGE_VERSION", "21.5.89")
                set("PACK_FORMAT", "71")
            }
        }
        register("1.21.6-neoforge") {
            data {
                set("MC_VERSION", "1.21.6")
                set("NEOFORGE_VERSION", "21.6.102")
                set("PACK_FORMAT", "80")
            }
        }
        register("1.21.7-neoforge") {
            data {
                set("MC_VERSION", "1.21.7")
                set("NEOFORGE_VERSION", "21.7.65")
                set("PACK_FORMAT", "81")
            }
        }
        register("1.21.8-neoforge") {
            data {
                set("MC_VERSION", "1.21.8")
                set("NEOFORGE_VERSION", "21.8.41")
                set("PACK_FORMAT", "81")
            }
        }
        register("1.21.9-neoforge") {
            data {
                set("MC_VERSION", "1.21.9")
                set("NEOFORGE_VERSION", "21.9.17")
                set("PACK_FORMAT", "88")
            }
        }
    }
}
