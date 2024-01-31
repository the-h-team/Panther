rootProject.name = "Panther"
sequenceOf(
    "annotations",
    "placeholders",
    "containers",
    "common",
    "paste"
).associateBy {
    ":panther-$it"
}.forEach { (name, path) ->
    include(name)
    project(name).projectDir = file(path)
}
