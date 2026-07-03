# Publishing Minecraft mod workflows
This repository contains reusable GitHub Action workflows that allows to automatically compile and publish minecraft mod to Github release, Curseforge, Modrinth and even Discord.
Internally uses [Mod publish plugin created by Modmuss](https://github.com/modmuss50/mod-publish-plugin).
The workflows are written with support for multi-branch releases for publishing different jars for different versions and mod loaders.

___

## Publication process

The process consists of the following steps:
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

## Setup

### 1. Create GitHub workflows in your repository

In your repository create directory `/.github/workflows/` and copy files from this repository from [`/.github/workflows/template`](.github/workflows/template).

- Copy the [`release-trigger-mod-publish.yml`](.github/workflows/template/release-trigger-mod-publish.yml)
- Choose and copy **ONE OF** [`tag-trigger-draft-release.yml`](.github/workflows/template/tag-trigger-draft-release.yml) or [`manual-trigger-draft-release.yml`](.github/workflows/template/manual-trigger-draft-release.yml)
- Choose a specific version/ref from this repository (or rather a specific commit hash) and update **BOTH** copied files with the chosen version/hash/ref

### 2. Create a configuration file

In your repository, in the root directory, create file named `publish.config.json`.
This file has to be on the default branch (where release tags will be created) and contains configuration describing each involved branch.

The config must follow the [`artifacts.schema.json`](artifacts.schema.json) JSON schema.

You can use https://lukaskabc.github.io/Minecraft-Mod-Publish-Workflows/ to create the config contents.
If you are using a specific version of the workflow, you need to update the link to the schema there to respect your exact version.
The page must not show any errors, otherwise the config is invalid and the pipeline will fail.

### 3. Setup secrets

Go to your GitHub repository > Settings > Secrets and variables > Actions > **Repository secrets**

Based on platforms you enabled in the configuration file, create the following **Repository secrets**:

- `CURSEFORGE_API_KEY` 
  - You can create the API key in [account settings at Curseforge](https://legacy.curseforge.com/account/api-tokens).  
  - Example: `d6bf5492-ac5f-4ee4-ad2d-f863d24ef859`
- `MODRINTH_API_KEY` 
  - You can create personal access token in [account settings at Modrinth](https://modrinth.com/settings/pats).
  - The token MUST have the following scopes:
    - `Create versions`
    - `Read versions`
    - `Write versions`
    - You should not allow any other scopes
    - Make sure to not set too short expiration period, after that date, the token will no longer work!
- `DISCORD_WEBHOOK_URL`
  - On your discord server pick a channel in which you want the announcements to be posted
  - Text channel > Edit channel > Integrations > Webhooks
    - Or server settings > Integrations > Webhooks 
  - Choose `Copy Webhook URL` to get the URL and set it as the value of the GitHub repository secret
  - Note that the image and name of the webhook needs to be configured in the `publish.config.json` file


