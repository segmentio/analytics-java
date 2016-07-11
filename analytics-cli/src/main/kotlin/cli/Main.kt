package cli

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.segment.analytics.Analytics
import com.segment.analytics.Log
import com.segment.analytics.messages.*
import org.docopt.Docopt
import java.text.SimpleDateFormat
import java.util.*

val usage = """
Analytics Java CLI

Usage:
  analytics track <event> [--properties=<properties>] [--context=<context>] [--writeKey=<writeKey>] [--userId=<userId>] [--anonymousId=<anonymousId>] [--integrations=<integrations>] [--timestamp=<timestamp>]
  analytics screen <name> [--properties=<properties>] [--context=<context>] [--writeKey=<writeKey>] [--userId=<userId>] [--anonymousId=<anonymousId>] [--integrations=<integrations>] [--timestamp=<timestamp>]
  analytics page <name> [--properties=<properties>] [--context=<context>] [--writeKey=<writeKey>] [--userId=<userId>] [--anonymousId=<anonymousId>] [--integrations=<integrations>] [--timestamp=<timestamp>]
  analytics identify [--traits=<traits>] [--context=<context>] [--writeKey=<writeKey>] [--userId=<userId>] [--anonymousId=<anonymousId>] [--integrations=<integrations>] [--timestamp=<timestamp>]
  analytics group --groupId=<groupId> [--traits=<traits>] [--properties=<properties>] [--context=<context>] [--writeKey=<writeKey>] [--userId=<userId>] [--anonymousId=<anonymousId>] [--integrations=<integrations>] [--timestamp=<timestamp>]
  analytics alias --userId=<userId> --previousId=<previousId> [--traits=<traits>] [--properties=<properties>] [--context=<context>] [--writeKey=<writeKey>] [--anonymousId=<anonymousId>] [--integrations=<integrations>] [--timestamp=<timestamp>]

  analytics -h | --help
  analytics --version

Options:
  -h --help     Show this screen.
  --version     Show version.
"""

private val ISO_8601_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
private val GSON = Gson()

internal val stdout: Log = object : Log {
    override fun print(level: Log.Level, format: String, vararg args: Any) {
        println(String.format(format, *args))
    }

    override fun print(level: Log.Level, error: Throwable, format: String, vararg args: Any) {
        println(String.format(format, *args))
        println(error)
    }
}

fun main(vararg rawArgs: String) {
    val args = Docopt(usage).parse(rawArgs.toList())

    val message: MessageBuilder<*, *>
    if (args["track"] as Boolean) {
        message = TrackMessage.builder(args["<event>"] as String)
        val properties = args["--properties"]
        if (properties != null) {
            message.properties(parseJson(properties as String))
        }
    } else if (args["page"] as Boolean) {
        message = PageMessage.builder(args["<name>"] as String)
        val properties = args["--properties"]
        if (properties != null) {
            message.properties(parseJson(properties as String))
        }
    } else if (args["screen"] as Boolean) {
        message = ScreenMessage.builder(args["<name>"] as String)
        val properties = args["--properties"]
        if (properties != null) {
            message.properties(parseJson(properties as String))
        }
    } else if (args["identify"] as Boolean) {
        message = IdentifyMessage.builder()
        val traits = args["--traits"]
        if (traits != null) {
            message.traits(parseJson(traits as String))
        }
    } else if (args["alias"] as Boolean) {
        message = AliasMessage.builder(args["--previousId"] as String)
    } else if (args["group"] as Boolean) {
        message = GroupMessage.builder(args["--groupId"] as String)
        val traits = args["--traits"]
        if (traits != null) {
            message.traits(parseJson(traits as String))
        }
    } else {
        throw AssertionError("unknown command")
    }

    val userId = args["--userId"]
    if (userId != null) {
        message.userId(userId as String)
    }
    val anonymousId = args["--anonymousId"]
    if (anonymousId != null) {
        message.anonymousId(UUID.fromString(anonymousId as String?))
    }
    val context = args["--context"]
    if (context != null) {
        message.context(parseJson(context as String))
    }
    val timestamp = args["--timestamp"]
    if (timestamp != null) {
        message.timestamp(ISO_8601_DATE_FORMAT.parse(timestamp as String))
    }
    val integrations = args["--integrations"]
    if (integrations != null) {
        val integrationsMap = parseJson(integrations as String)
        for ((key, value) in integrationsMap) {
            if (value is Boolean) {
                message.enableIntegration(key, value)
            } else if (value is Map<*, *>) {
                message.integrationOptions(key, value as MutableMap<String, in Any>)
            } else {
                throw  AssertionError("Unknown type integrations map: $key, $value");
            }
        }
    }

    var writeKey = args["--writeKey"]
    if (writeKey == null) {
        writeKey = System.getenv("SEGMENT_WRITE_KEY")
    }

    val analytics = Analytics.builder(writeKey as String)
            .log(stdout)
            .flushQueueSize(1)
            .build()
    try {
        analytics.enqueue(message)
        Thread.sleep(2 * 1000)
    } finally {
        analytics.shutdown()
    }
}

fun parseJson(k: String): Map<String, *> {
    val collectionType = object : TypeToken<Map<String, Any>>() {}.getType()
    return GSON.fromJson(k, collectionType)
}