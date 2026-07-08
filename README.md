# Publishing Minecraft mod workflows
This repository contains reusable GitHub Action workflows that allows to automatically compile and publish minecraft mod to Github release, [Curseforge](https://www.curseforge.com/), [Modrinth](https://modrinth.com/) and even Discord.
Internally uses [Mod publish plugin created by Modmuss](https://github.com/modmuss50/mod-publish-plugin).
The workflows are written with support for multi-branch releases for publishing different jars for different versions and mod loaders.

Available workflows:
- Release a new version, upload it to Curseforge, Modrinth, Github release and send announcement to Discord webhook
- Build a nightly (preview) jar and optionally post it to Discord webhook

___

## Publication process

The process to release a new version consists of the following steps:
1. The author manually triggers draft release pipeline with the **version number**
- A GitHub release draft is created for the main branch
- For each involved branch:
  - The artifact is compiled
  - 2 commits are pushed advancing the version set in the files
    - 1.The version being published (e.g. 1.2.3)
    - 2.The next development version (e.g. 1.2.4-alpha)
  - The artifact is attached to the release draft
  - **(During the process, there must be no new commits added to the involved branches)**

2. The author fills the GitHub release draft description with the **changelog**
3. The author can access the attached jars and test them locally if desired
4. The author publishes the GitHub release
5. The mod publish workflow is automatically triggered
- JARs are downloaded from the GitHub release
- They are published using the [Mod publish plugin created by Modmuss](https://github.com/modmuss50/mod-publish-plugin).
- The workflow is configured for publishing to: (each step being optional)
  - Modrinth
  - Curseforge 
  - and Discord
- Only a single configured (main) artifact update will be published to discord (e.g. the main branch).  
  Otherwise each released artifact would be announced to the discord, 
  each with the same changelog resulting in spam, which is not desired.

## Release version setup
Described in [MAIN-RELEASE](./MAIN-RELEASE.md) file

## Nightly (preview) builds
Optionally, in addition to the main release workflow, there is pipeline for building preview jars.

First configure the main release workflow and include the options for nightly builds in the same configuration file.

1. Add [keyword-nightly-build.yml](./.github/workflows/template/keyword-nightly-build.yml) to your repository workflows.
2. Configure the branches and commit keywords that should trigger the nightly build
3. At the bottom of the file, pin the workflow version and configure the file upload limit for your discord server
- If the compiled file has bigger size than the configured value, the upload job will be skipped.
- The downside of not uploading to discord is, that the user needs to be logged in to download the build from GitHub.

4. Configure nightly webhook secret (see [MAIN-RELEASE](./MAIN-RELEASE.md) for detailed instructions)
- `DISCORD_NIGHTLY_WEBHOOK_URL` the webhook URL to which nightly builds should be announced and uploaded

## Comparison of popular tools

The goal of this repository is to provide reusable workflows for my (and possibly others) mods.
This configures the GitHub workflows and does not directly upload the compiled mod, it uses a tool under the hood for it.

Out there mods seem to often use the following tools (sorted by popularity):
- Gradle plugin [Mod Publish](https://github.com/modmuss50/mod-publish-plugin) by [Modmuss](https://github.com/modmuss50)
  - Supports publishing to GitHub release, Curseforge, Modrinth, Discord (and other git platforms).
  - Created by Fabric core maintainer
  - Used for example by [Fabric API](https://github.com/FabricMC/fabric-api) and related projects, 
  [Sodium](https://github.com/CaffeineMC/sodium),
  [Lithium](https://github.com/CaffeineMC/lithium),
  [FerriteCore](https://github.com/malte0811/FerriteCore),
  [JustEnoughItems](https://github.com/mezz/JustEnoughItems),
  [ModernFix](https://github.com/embeddedt/ModernFix)
- GitHub action [MC Publish](https://github.com/Kira-NT/mc-publish) by [Kira NT](https://github.com/Kira-NT)
  - Supports publishing to GitHub release, Curseforge and Modrinth
  - Used for example by [Iris](https://github.com/IrisShaders/Iris)
- Gradle plugin [Mod publisher](https://github.com/firstdarkdev/modpublisher) by [First Dark Development](https://github.com/firstdarkdev)
  - Supports publishing to GitHub release, Curseforge, Modrinth, NightBloom and Modtale
  - Used for example by [LostCities](https://github.com/McJtyMods/LostCities),
  [Oh The Biomes We've Gone](https://github.com/Potion-Studios/Oh-The-Biomes-Weve-Gone),
  [Simple RPC](https://github.com/firstdarkdev/simple-rpc)



- Gradle plugins [Cursegradle](https://github.com/matthewprenger/CurseGradle) and [Minotaur](https://github.com/modrinth/minotaur)
  - Gradle plugins developed for publishing to individual platforms, Minotaur is even official tool
  - Used under the hood for example by [ModMenu](https://github.com/TerraformersMC/ModMenu),
    [Architectury API](https://github.com/architectury/architectury-api)
    


Personally when I first searched for a solution to release my mod automatically to Curseforge and Modrinth
I was primarly looking for a GitHub action and I ended up with [MC Publish](https://github.com/Kira-NT/mc-publish) by [Kira NT](https://github.com/Kira-NT).
I configured my own pipeline using this action and it worked well!

However, the workflow configuration was not a simple one and consisted of two full workflows.
As I created more mods, copying and configuring those full-blown workflows for each mod repository started to feel repetitive
and unfeasible for doing changes to those workflows across multiple projects.

Primarily for this reason I decided to create this repository and create reusable workflows that would be triggered from my other repositories
with a lightweight and short workflows configured in the caller projects.

Naturally my original plan was to use the [MC Publish](https://github.com/Kira-NT/mc-publish) GitHub action for the actuall artifact upload again.
While it works well, it wasn't updated in last 2 years (as per 2026-07-03).
Once I checked other projects (and `creator-resources` channel on Modrinth discord) 
I learned about the other tools listed above.
Using
[Cursegradle](https://github.com/matthewprenger/CurseGradle) 
and [Minotaur](https://github.com/modrinth/minotaur)
would certainly ensure some independency and official support from Modrinth.
However, I think
[Mod Publish](https://github.com/modmuss50/mod-publish-plugin) by [Modmuss](https://github.com/modmuss50)
showed it's trustworthiness.
It is being developed by developer of Fabric and it is being used by some of the largest modern Minecraft mods.

For this reason, this workflow uses
[Mod Publish](https://github.com/modmuss50/mod-publish-plugin) by [Modmuss](https://github.com/modmuss50)
under the hood.