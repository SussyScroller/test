extension {
    // Unique resource name. All patch bundles in one patching run share a class loader,
    // so a common name such as "extensions/extension.mpe" or "extensions/reddit.mpe"
    // (official Morphe) could load another bundle's extension instead of this one.
    name = "extensions/sussyscroller-redgifs.mpe"
}

dependencies {
    // Reddit classes the extension calls. Compile only, so they are not packaged
    // into the extension and the app's own classes are used at runtime.
    compileOnly(project(":extensions:redgifs:stub"))
}
