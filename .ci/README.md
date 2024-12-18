# Build Scripts for pmd-eclipse-plugin

## GPG Signing

Since 7.9.0, the plugin is signed with the same GPG key, that is used to sign the main PMD artifacts
for maven central.

See <https://github.com/pmd/build-tools/blob/main/scripts/files/release-signing-key-D0BF1D737C9A1C22.asc>.

Tycho's [GPG Plugin](https://tycho.eclipseprojects.io/doc/latest/tycho-gpg-plugin/sign-p2-artifacts-mojo.html)
is used for that.

There is no need anymore to use jar signer and use a real Let's Encrypt certificate.

**How it works:**

* During build setup, `.m2/settings.xml` contains properties for signing:
  ```xml
    <profile>
      <id>sign</id>
      <properties>
        <gpg.keyname>${env.CI_SIGN_KEYNAME}</gpg.keyname>
        <gpg.passphrase>${env.CI_SIGN_PASSPHRASE}</gpg.passphrase>
      </properties>
    </profile>
  ```
* These environment variables (`CI_SIGN_KEYNAME`) are set by `pmd_ci_setup_secrets_private_env`
  which is called by `build.sh` (but not for pull requests).

* The tycho gpg plugin is activated only when profile `sign` is activated.
