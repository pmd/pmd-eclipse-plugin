# Build Scripts for pmd-eclipse-plugin

## JAR Signing

Same solution as <https://github.com/spotbugs/spotbugs/issues/779>, using the Let's Encrypt certificate
for pmd-code.org:

```
$ export CI_SIGN_PASSPHRASE=...
$ openssl pkcs12 -export -in Lets_Encrypt_pmd-code.org_2022-06-25.pem \
    -name eclipse-plugin \
    -password env:CI_SIGN_PASSPHRASE \
    -out pmd-eclipse-plugin.p12 \
    -legacy
$ jarsigner -verbose \
  -keystore .ci/files/pmd-eclipse-plugin.p12 \
  -storepass changeit \
  -keypass changeit \
  -tsa http://timestamp.digicert.com \
  path/to/plugin-jar.jar \
  eclipse-plugin
```

Note: The file "Lets_Encrypt_pmd-code.org_2022-06-25.pem" contains the private key, the certificate
and intermediate certificates.

Note: with openssl 3, the option `-legacy` is required in order to use the pkcs12 keystore with java8.
See <https://stackoverflow.com/questions/69170537/is-openssl-v3-0-0-compatible-with-v1-1-1>

The file `pmd-eclipse-plugin.p12` is stored as `.ci/files/pmd-eclipse-plugin.p12.asc`, encrypted with PMD_CI_SECRET_PASSPHRASE.

Encrypt it via:

    printenv PMD_CI_SECRET_PASSPHRASE | gpg --symmetric --cipher-algo AES256 --batch --armor \
      --passphrase-fd 0 \
      pmd-eclipse-plugin.p12

Decrypt it via:

    printenv PMD_CI_SECRET_PASSPHRASE | gpg --batch --yes --decrypt \
        --passphrase-fd 0 \
        --output pmd-eclipse-plugin.p12 pmd-eclipse-plugin.p12.asc
    chmod 600 pmd-eclipse-plugin.p12

Signing the jar manually via `jarsigner` is difficult, since it changes the jar file and the p2 repo metadata
fails with the wrong checksum. Therefore jarsigning is integrated via [maven-jarsigner-plugin](https://maven.apache.org/plugins/maven-jarsigner-plugin/). See also <https://stackoverflow.com/questions/7956267/tycho-jar-signing>.

Note: The Let's Encrypt certificate expires in September 2022. But while signing a digital timestamp is created
using [DigiCert's Timestamp Server](https://knowledge.digicert.com/generalinformation/INFO4231.html). That's
why the signature is valid longer than the certificate.

Note: Eclipse 2022-03 seems to check now the expiration date of the certificates.
See <https://bugs.eclipse.org/bugs/show_bug.cgi?id=578024>.
