COMPILE_DEPS = CORE_DEPS + JACKSON + REST + [
    "//apps/ONOS_Integration_Service/api:onos-apps-ONOS_Integration_Service-api",
]

osgi_jar_with_tests(
    api_description = "REST API for Mao GUI",
    api_package = "com.maojianwei.onos.integrate.gui.rest",
    api_title = "Mao api_title DHCP Server",
    api_version = "1.0",
    #    karaf_command_packages = ["org.onosproject.mao.gui.cli"],
    test_deps = TEST_ADAPTERS,
    web_context = "/onos/mao",
    deps = COMPILE_DEPS,
)
