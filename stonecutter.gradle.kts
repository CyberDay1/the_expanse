plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.1-forge"

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("build")
}
