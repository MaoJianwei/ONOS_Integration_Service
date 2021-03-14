BUNDLES = [
    "//apps/mao/api:onos-apps-mao-api",
    "//apps/mao/cli:onos-apps-mao-cli",
    "//apps/mao/core:onos-apps-mao-core",
    #    "//apps/mao/web:onos-apps-mao-web",
]

osgi_jar_with_tests(
    api_package = "com.maojianwei.mao.device",
)

onos_app(
    category = "GUI",
    description = "Mao GUI Framework",
    included_bundles = BUNDLES,
    #    required_apps = ["org.onosproject.hostprovider"],
    title = "Mao Device Service",
    url = "http://onosproject.org",
)
