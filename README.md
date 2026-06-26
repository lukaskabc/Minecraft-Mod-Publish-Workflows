# Publishing Minecraft mod workflows
This repository contains reusable GitHub Action workflows that allows to automatically compile and publish minecraft mod to Github release, Curseforge, Modrinth and even Discord.
Internally uses [Mod publish plugin created by Modmuss](https://github.com/modmuss50/mod-publish-plugin).
The workflows are written with support for multi-branch releases for releasing different jars for different versions and mod loaders.

Minecraft mod release cycle and publishing with CI/CD (GitHub Workflows)

___

The automatic process consists of the following steps:
- The release pipeline is manually triggered with version number
- A GitHub release draft is created for the main branch
- All branches are tagged
- 
