# Build Scripts for pmd-eclipse-plugin

## GPG Signing

Since 7.9.0, the plugin is signed with the same GPG key, that is used to sign the main PMD artifacts
for maven central.

See <https://github.com/pmd/build-tools/blob/main/scripts/files/release-signing-key-2EFA55D0785C31F956F2F87EA0B5CA1A4E086838-public.asc>.

Tycho's [GPG Plugin](https://tycho.eclipseprojects.io/doc/latest/tycho-gpg-plugin/sign-p2-artifacts-mojo.html)
is used for that.

There is no need anymore to use jar signer and use a real Let's Encrypt certificate.

**How it works:**

* During build setup, the private gpg key is imported from the environment variable `PMD_CI_GPG_PRIVATE_KEY`
  which is a secret in GitHub Action. This environment variable is used by `pmd_ci_setup_secrets_private_env`
  which is called by `build.sh` (but not for pull requests).
* The gpg plugin uses the environment variable `MAVEN_GPG_PASSPHRASE` for the passphrase. This is
  configured as well as a secret. The tycho gpg plugin is activated only when profile `sign` is activated.
