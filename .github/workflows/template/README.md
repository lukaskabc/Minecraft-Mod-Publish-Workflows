# Template workflows
Those workflows needs to be copied to the caller repository to activate event triggers and call the reusable workflows.

- [`release-trigger-mod-publish.yml`](release-trigger-mod-publish.yml) - Uploads the mods JARs to the platforms when a GitHub release is published

**Choose one of:**
- [`manual-trigger-draft-release.yml`](manual-trigger-draft-release.yml) - Creates a draft release when the workflow is manually triggered from the GitHub workflow UI
- [`tag-trigger-draft-release.yml`](tag-trigger-draft-release.yml) - Creates a draft release when a GIT tag matching the "v1.2.3" format is created (you can even manually draft the release yourself)