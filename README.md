# Publishing Minecraft mod workflows
This repository contains reusable GitHub Action workflows that allows to automatically compile and publish minecraft mod to Github release, Curseforge, Modrinth and even Discord.
Internally uses [Mod publish plugin created by Modmuss](https://github.com/modmuss50/mod-publish-plugin).
The workflows are written with support for multi-branch releases for releasing different jars for different versions and mod loaders.

Minecraft mod release cycle and publishing with CI/CD (GitHub Workflows)

___

The process consists of the following steps:
1. The author manually triggers draft release pipeline with the version number
- A GitHub release draft is created for the main branch
- For each involved branch:
  - The artifact is compiled
  - 2 commits are pushed advancing the version set in the files
    - 1.The version being published (e.g. 1.2.3)
    - 2.The next development version (e.g. 1.2.4-alpha)
  - The artifact is attached to the release draft
  - (During the process, there must be no new commits added to the involved branches)

2. The author fills the GitHub release draft description with the changelog
3. The author can access the attached jars and test them locally if desired
4. The author publishes the GitHub release
5. The mod publish workflow is automatically triggered
- JARs are downloaded from the GitHub release
- They are published using the [Mod publish plugin created by Modmuss](https://github.com/modmuss50/mod-publish-plugin).
- The workflow is configured for publishing to: (each step being optional)
  - Modrinth
  - Curseforge 
  - and Discord
- Only a single configured artifact update will be published to discord (e.g. the main branch)

## Setup
