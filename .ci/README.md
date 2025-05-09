# Build Scripts for pmd-eclipse-plugin

## GPG Signing

Since 7.9.0, the plugin is signed with the same GPG key, that is used to sign the main PMD artifacts
for maven central.

See <https://docs.pmd-code.org/latest/pmd_userdocs_signed_releases.html>.

Tycho's [GPG Plugin](https://tycho.eclipseprojects.io/doc/latest/tycho-gpg-plugin/sign-p2-artifacts-mojo.html)
is used for that.

There is no need anymore to use jar signer and use a real Let's Encrypt certificate.

**How it works:**

* The GPG key is only needed for the two workflows `publish-snapshot.yml` and `publish-releases.yml`
* The key is setup with setup-java, the private gpg key is imported from the (secret) environment
  variable `PMD_CI_GPG_PRIVATE_KEY`.
* The gpg plugin uses the environment variable `MAVEN_GPG_PASSPHRASE` for the passphrase. This is
  configured as well as a secret. The tycho gpg plugin is activated only when profile `sign` is activated.
