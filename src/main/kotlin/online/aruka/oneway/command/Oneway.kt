package online.aruka.oneway.command

import picocli.CommandLine

@CommandLine.Command(
    name = "oneway",
    subcommands = [VersionCheck::class, Publish::class],
    description = ["Check and upload"]
)
class Oneway {
    
}