package ianmorgan.kandykoins.plugin

import net.corda.core.messaging.CordaRPCOps
import ianmorgan.kandykoins.api.KandyApi
import net.corda.webserver.services.WebServerPluginRegistry
import java.util.function.Function

class KandyBagPlugin : WebServerPluginRegistry {
    /**
     * A list of classes that expose web APIs.
     */
    override val webApis: List<Function<CordaRPCOps, out Any>> = listOf(Function(::KandyApi))

    /**
     * A list of directories in the resources directory that will be served by Jetty under /web.
     * The template's web frontend is accessible at /web/template.
     */
    override val staticServeDirs: Map<String, String> = mapOf(
            // This will serve the kandyWeb directory in resources to /web/template
            "kandy" to javaClass.classLoader.getResource("kandyWeb").toExternalForm()
    )
}