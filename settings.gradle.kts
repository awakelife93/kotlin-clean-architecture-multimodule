rootProject.name = "demo"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

buildCache {
	local {
		directory = File(rootDir, ".gradle/build-cache")
	}
}

include(
	"demo-core",
	"demo-domain",
	"demo-application",
	"demo-infrastructure",
	"demo-internal-api",
	"demo-external-api",
	"demo-batch",
	"demo-bootstrap"
)
