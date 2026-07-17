# Nightly builds workflow

[For setup, see the section below.](#setup)

The workflows consist of the following steps:
1. The workflow is triggered
   1. Workflow is triggered manually
   2. A commit is pushed to a branch
2. If the workflow was triggered by pushed commits, it evaluates if any commit contains trigger keyword
3. The workflow compiles the artifact
4. Nightly build announce is sent to Discord webhook
5. The artifact is uploaded to the announcement message

# Setup

## 1. Create a configuration file

In your repository **only on your main branch**, in the root directory, create file named `publish.config.json`.
This file has to be on the default branch (where release tags will be created) and contains configuration describing each involved branch.

The config must follow the [`publish.config.schema.json`](publish.config.schema.json) JSON schema.

You can use config editor at https://lukaskabc.github.io/Minecraft-Mod-Publish-Workflows/  
If you are using a specific version of the workflow, you need to update the link to the schema to respect your exact version.
The page must not show any errors, otherwise the config is invalid and the pipeline will fail.
The schema is not able to validate everything.
Make sure to carefully read the description for every field.

## 2. Create the workflow file

In your repository at **each branch** from which you want to compile nightly builds:  
- Copy the [`keyword-nightly-build.yml`](../.github/workflows/template/keyword-nightly-build.yml) to `/.github/workflows/`

## 3. Edit the workflow

- Change the reference/version to the reusable workflow to a specific version/commit hash
- Update the branch at which it is used, you can list all the branches and then copy the same workflow to each
- Change the keywords that should trigger the nightly build

## 4. Setup repository secrets

## 3. Setup secrets

Go to your GitHub repository > Settings > Secrets and variables > Actions > **Repository secrets**

Create the following **Repository secret**:
- `DISCORD_NIGHTLY_WEBHOOK_URL`
   - On your discord server pick a channel in which you want the announcements to be posted
   - Text channel > Edit channel > Integrations > Webhooks
      - Or server settings > Integrations > Webhooks
   - Choose `Copy Webhook URL` to get the URL and set it as the value of the GitHub repository secret


