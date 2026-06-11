import online.aruka.oneway.command.Oneway
import picocli.CommandLine
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val exitCode = CommandLine(Oneway()).execute(*args)
    exitProcess(exitCode)
}