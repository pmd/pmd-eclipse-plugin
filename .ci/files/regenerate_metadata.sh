#!/bin/bash

echo "Regenerating metadata for p2-site..."
mapfile -t releases < <(find . -maxdepth 1 -type d -regex "\./[0-9]+\.[0-9]+\.[0-9]+\..*" -printf '%f\n'| tr '.' '\0' | sort -t '\0' -k1,1nr -k2,2nr -k3,3nr -k4dr |awk -F '\0' '{printf "%s.%s.%s.%s\n", $1, $2, $3, $4}')
# remove old releases
for i in "${releases[@]:5}"; do
  echo "  Removing old release $i..."
  rm -rf "$i"
done
releases=("${releases[@]:0:5}")

# regenerate metadata
now=$(date +%s000)
children=""
children_index=""
for i in "${releases[@]}"; do
  echo "  Adding release $i"
  children="${children}    <child location=\"$i\"/>\n"
  children_index="${children_index}  * [$i]($i/)\n"
  echo "This is a Eclipse Update Site for the [PMD Eclipse Plugin](https://github.com/pmd/pmd-eclipse-plugin/) ${i}.

Use <https://pmd.github.io/pmd-eclipse-plugin-p2-site/${i}/> to install the plugin with the Eclipse Update Manager.

<dl>
  <dt>Feature ID</dt>
  <dd>net.sourceforge.pmd.eclipse</dd>
  <dt>Version</dt>
  <dd>${i}</dd>
</dl>

" > "$i"/index.md

  git add "$i"/index.md
done

site_name="PMD for Eclipse - Update Site"
artifactsTemplate="<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<?compositeArtifactRepository version=\"1.0.0\"?>
<repository name=\"${site_name}\" type=\"org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository\" version=\"1.0.0\">
  <properties size=\"2\">
    <property name=\"p2.timestamp\" value=\"${now}\"/>
    <property name=\"p2.atomic.composite.loading\" value=\"true\"/>
  </properties>
  <children size=\"${#releases[@]}\">
${children}  </children>
</repository>"
echo -e "${artifactsTemplate}" > compositeArtifacts.xml
git add compositeArtifacts.xml

contentTemplate="<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<?compositeMetadataRepository version=\"1.0.0\"?>
<repository name=\"${site_name}\" type=\"org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository\" version=\"1.0.0\">
  <properties size=\"2\">
    <property name=\"p2.timestamp\" value=\"${now}\"/>
    <property name=\"p2.atomic.composite.loading\" value=\"true\"/>
  </properties>
  <children size=\"${#releases[@]}\">
${children}  </children>
</repository>"
echo -e "${contentTemplate}" > compositeContent.xml
git add compositeContent.xml

# p2.index
p2_index="version = 1
metadata.repository.factory.order = compositeContent.xml,\!
artifact.repository.factory.order = compositeArtifacts.xml,\!"
echo -e "${p2_index}" > p2.index
git add p2.index

# regenerate index.md
echo -e "This is a composite Eclipse Update Site for the [PMD Eclipse Plugin](https://github.com/pmd/pmd-eclipse-plugin/).

Use <https://pmd.github.io/pmd-eclipse-plugin-p2-site/> to install the plugin with the Eclipse Update Manager.

----

Versions available at <https://pmd.github.io/pmd-eclipse-plugin-p2-site/>:

${children_index}

For older versions, see <https://sourceforge.net/projects/pmd/files/pmd-eclipse/zipped/>

" > index.md
git add index.md

echo "Done."

