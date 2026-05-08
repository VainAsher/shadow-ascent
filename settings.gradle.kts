rootProject.name = "shadow-ascent-clean-start"
include(":core", ":client", ":server")
project(":core").projectDir = file("java/core")
project(":client").projectDir = file("java/client")
project(":server").projectDir = file("java/server")
