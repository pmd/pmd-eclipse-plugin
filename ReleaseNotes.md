# PMD For Eclipse - Release Notes

Installation instructions: <https://pmd.github.io/eclipse/>

Eclipse Update Site:

*   Releases: <https://pmd.github.io/pmd-eclipse-plugin-p2-site/>
*   Snapshots: <https://pmd.github.io/pmd-eclipse-plugin-p2-site/snapshot/>

## 29-January-2022: 4.31.0.v20220129-0959-r

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.42.0

## 27-November-2021: 4.30.0.v20211127-1115-r

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.41.0

## 30-October-2021: 4.29.0.v20211030-0859-r

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.40.0

## 25-September-2021: 4.28.0.v20210925-1222-r

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.39.0

## 28-August-2021: 4.27.0.v20210828-1623-r

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.38.0

### Fixed Issues

*   [#159](https://github.com/pmd/pmd-eclipse-plugin/issues/159): Error executing command ReviewCode: java.util.regex.PatternSyntaxException: Illegal/unsupported escape sequence near index (part 2)

## 31-July-2021: 4.26.0.v20210731-1741-r

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.37.0

### Fixed Issues

*   [#148](https://github.com/pmd/pmd-eclipse-plugin/issues/148): Eclipse nearly-consistently crashes on startup when workspace contains PMD enabled projects
*   [#150](https://github.com/pmd/pmd-eclipse-plugin/issues/150): Error executing command ReviewCode: java.util.regex.PatternSyntaxException: Illegal/unsupported escape sequence near index
*   [#153](https://github.com/pmd/pmd-eclipse-plugin/issues/153): Not properly disposed SWT resource

### API Changes

#### Deprecated for removal

*   `net.sourceforge.pmd.eclipse.plugin.EclipseUtil`
*   `net.sourceforge.pmd.eclipse.plugin.PMDPlugin.getPluginFolder()`
*   `net.sourceforge.pmd.eclipse.runtime.cmd.BaseVisitor.isUseTaskMarker()`
*   `net.sourceforge.pmd.eclipse.runtime.cmd.BaseVisitor.setUseTaskMarker(boolean)`
*   `net.sourceforge.pmd.eclipse.ui.preferences.br.FilterManager`
*   `net.sourceforge.pmd.eclipse.util.IOUtil`
*   `net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptor.refreshImages()`
*   All classes that couldn't be instantiated because they had a private constructor only
    are now also `final`.

## 26-June-2021: 4.25.0.v20210626-0908

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.36.0

### Fixed Issues

*   [#133](https://github.com/pmd/pmd-eclipse-plugin/issues/133): Updates with outdated certificates from orbit
*   [#134](https://github.com/pmd/pmd-eclipse-plugin/issues/134): Incompatibility between Groovy Eclipse Plugin and PMD Plugin

## 29-May-2021: 4.24.0.v20210529-0600

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.35.0

### Fixed Issues

*   [#146](https://github.com/pmd/pmd-eclipse-plugin/pull/146): Refactor actions to use correct selection

## 24-April-2021: 4.23.0.v20210424-1524

This is a minor release.

### New and noteworthy

*   The Eclipse Update site is now hosted on GitHub Pages: <https://pmd.github.io/pmd-eclipse-plugin-p2-site/>
    The old update site on Bintray is no longer updated.
*   Updated to PMD 6.34.0

### Fixed Issues

*   [#143](https://github.com/pmd/pmd-eclipse-plugin/pull/143): Support JAR Signing for the update site
*   [#144](https://github.com/pmd/pmd-eclipse-plugin/pull/144): Remove support and scripts for bintray
*   [#145](https://github.com/pmd/pmd-eclipse-plugin/pull/145): Bump tycho from 1.7.0 to 2.3.0

### API Changes

*   The following methods in `net.sourceforge.pmd.eclipse.ui.actions.RuleSetUtil` are deprecated:
    *   `setExcludePatterns(RuleSet ruleSet, Collection<String> excludePatterns)`
    *   `setIncludePatterns(RuleSet ruleSet, Collection<String> includePatterns)`
    *   `addExcludePatterns(RuleSet ruleSet, Collection<String> activeExclusionPatterns, Collection<String> buildPathExcludePatterns)`
    *   `addIncludePatterns(RuleSet ruleSet, Collection<String> activeInclusionPatterns, Collection<String> buildPathIncludePatterns)`
    *   `addExcludePatterns(RuleSet rs, Collection<String> excludePatterns)`
    *   `addIncludePatterns(RuleSet rs, Collection<String> includePatterns)`
    *   These methods are not supposed to be public API and will be removed eventually.
*   The property `net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants.MAX_VIOLATIONS_DESCRIPTOR`
    is deprecated and will be removed.
*   The preference "net.sourceforge.pmd.eclipse.plugin.max_violations_pfpr" is deprecated and will be removed.
    It was never implemented. The following methods and fields are deprecated:
    *   `net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences.getMaxViolationsPerFilePerRule()`
    *   `net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences.setMaxViolationsPerFilePerRule(int)`
    *   `net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences.MAX_VIOLATIONS_PFPR_DEFAULT`
*   `net.sourceforge.pmd.eclipse.runtime.cmd.DetectCutAndPasteCmd.setRenderer(Renderer)` is deprecated.
*   Removed usages of deprecated class `net.sourceforge.pmd.RuleSets`. Therefore the following methods are
    deprecated now:
    *   `net.sourceforge.pmd.eclipse.runtime.cmd.BaseVisitor.getRuleSets()`
    *   `net.sourceforge.pmd.eclipse.runtime.cmd.BaseVisitor.setRuleSets(RuleSets)`
    *   `net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties.getProjectRuleSets()`
    *   `net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties.setProjectRuleSets(RuleSets)`
    *   `net.sourceforge.pmd.eclipse.runtime.properties.impl.ProjectPropertiesImpl.getProjectRuleSets()`
    *   `net.sourceforge.pmd.eclipse.runtime.properties.impl.ProjectPropertiesImpl.setProjectRuleSets(RuleSets)`
    *   `net.sourceforge.pmd.eclipse.ui.properties.PMDPropertyPageBean.getProjectRuleSets()`
    *   `net.sourceforge.pmd.eclipse.ui.properties.PMDPropertyPageBean.setProjectRuleSets(RuleSets)`
    *   `net.sourceforge.pmd.eclipse.ui.properties.UpdateProjectPropertiesCmd.setProjectRuleSets(RuleSets)`

## 27-March-2021: 4.22.0.v20210327-1603

This is a minor release.

### New and noteworthy

*   The update site moved from Bintray to Github Pages. The new URL is from now on:
    <https://pmd.github.io/pmd-eclipse-plugin-p2-site/>
*   Archives of the old versions can be found on sourceforge:
    <https://sourceforge.net/projects/pmd/files/pmd-eclipse/zipped/>
*   Updated to PMD 6.33.0

### Fixed Issues

*   [#140](https://github.com/pmd/pmd-eclipse-plugin/issues/140): Use gh pages for update site

## 27-February-2021: 4.21.0.v20210227-1131

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.32.0

## 30-January-2021: 4.20.0.v20210130-1744

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.31.0

## 12-December-2020: 4.19.0.v20201212-1115

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.30.0

### Fixed Issues

*   [#135](https://github.com/pmd/pmd-eclipse-plugin/pull/135): Fix JavaProjectClassLoader (auxclasspath support) for workspace relative libraries

## 24-October-2020: 4.18.0.v20201024-1129

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.29.0

### Fixed Issues

*   [#131](https://github.com/pmd/pmd-eclipse-plugin/issues/131): Conflict with Jaspersoft Studio plugin (log4j)

#### Breaking Changes

*   With 4.10.0 usage of log4j has been deprecated. In order to fix [#131](https://github.com/pmd/pmd-eclipse-plugin/issues/131)
    these deprecated references have been removed now.
    
    The following methods/fields in `net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences`
    have been removed:
    *   `getLogLevel()`
    *   `setLogLevel(Level)`
    *   `LOG_LEVEL`

### External Contributions

*   [#132](https://github.com/pmd/pmd-eclipse-plugin/pull/132): Remove log4j - [Rich DiCroce](https://github.com/rdicroce)

## 26-September-2020: 4.17.0.v20200926-0854

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.28.0

## 31-August-2020: 4.16.0.v20200831-1731

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.27.0

## 25-July-2020: 4.15.0.v20200725-1231

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.26.0

## 27-June-2020: 4.14.0.v20200627-1203

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.25.0

### Fixed Issues

*   [#123](https://github.com/pmd/pmd-eclipse-plugin/pull/123): Support external configuration changes
*   [#124](https://github.com/pmd/pmd-eclipse-plugin/issues/124): No repository found containing: osgi.bundle,org.slf4j.log4j,1.7.2.v20130115-1340
*   [#125](https://github.com/pmd/pmd-eclipse-plugin/issues/125): NoSuchMethodError SizeBasedTriggeringPolicy.setMaxFileSize(Ljava/lang/String;)V
*   [#128](https://github.com/pmd/pmd-eclipse-plugin/pull/128): Improve progress report of ReviewCodeCmd

## 24-May-2020: 4.13.0.v20200524-1630

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.24.0

## 24-April-2020: 4.12.0.v20200424-1006

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.23.0

## 12-March-2020: 4.11.0.v20200312-1712

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.22.0

### Fixed Issues

### API Changes

### External Contributions

## 07-February-2020: 4.10.1.v20200207-1909

This is a bugfix release.

### Fixed Issues

*   [#114](https://github.com/pmd/pmd-eclipse-plugin/issues/114): NPE in PriorityDescriptorCache
*   [#115](https://github.com/pmd/pmd-eclipse-plugin/issues/115): Missing dependencies when installing
*   [#116](https://github.com/pmd/pmd-eclipse-plugin/issues/116): SWTError: No more handles

## 25-January-2020: 4.10.0.v20200125-1230

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.21.0

### Fixed Issues

*   [#108](https://github.com/pmd/pmd-eclipse-plugin/issues/108): GenericPropertyDescriptor cannot be cast to NumericPropertyDescriptor

### API Changes

*   The minimum eclipse version is now definitely Kepler (4.3) as
    it is declared in the market place. This is enforced via a
    target definition.
*   Usage of Log4j is deprecated and is going to be removed. Logback via slf4j is replacing it.
    Therefore the following methods/fields are deprecated for removal in `net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences`:
    *   `getLogLevel()`
    *   `setLogLevel(Level)`
    *   `LOG_LEVEL`
    *   `net.sourceforge.pmd.eclipse.plugin.PMDPlugin#ROOT_LOG_ID`
*   The following property editors are deprecated and not used anymore:
    *   `net.sourceforge.pmd.eclipse.ui.preferences.editors.FileEditorFactory`
    *   `net.sourceforge.pmd.eclipse.ui.preferences.editors.FloatEditorFactory`
    *   `net.sourceforge.pmd.eclipse.ui.preferences.editors.MethodEditorFactory`
    *   `net.sourceforge.pmd.eclipse.ui.preferences.editors.MultiMethodEditorFactory`
    *   `net.sourceforge.pmd.eclipse.ui.preferences.editors.MultiTypeEditorFactory`
    *   `net.sourceforge.pmd.eclipse.ui.preferences.editors.TypeEditorFactory`

### External Contributions

## 29-November-2019: 4.9.0.v20191129-1951

This is a minor release.

### New and noteworthy

*   Updated to PMD 6.20.0

## 31-October-2019: 4.8.0.v20191031-1916

This is a minor release.

### New and noteworthy

*   Updated PMD to 6.19.0

## 15-September-2019: 4.7.0.v20190915-0943

This is a minor release.

### New and noteworthy

*   Updated PMD to 6.18.0

### Fixed Issues

*   [#110](https://github.com/pmd/pmd-eclipse-plugin/issues/110): PMD markers should be removed on Project->Clean

### External Contributions

*   [#111](https://github.com/pmd/pmd-eclipse-plugin/pull/111): Delete project markers on Project -> Clean - [Sebastian Ratz](https://github.com/sratz)

## 28-July-2019: 4.6.0.v20190728-0905

This is a minor release.

### New and noteworthy

*   Updated PMD to 6.17.0

## 30-June-2019: 4.5.0.v20190630-0922

This is a minor release.

### New and noteworthy

*   Updated PMD to 6.16.0

*   PMD specific marker property page: This version adds a PMD specific property page to the
    marker properties which shows more information from the rule violation and rule itself.
    For more information, see [Marker Property Page (wiki)](https://github.com/pmd/pmd-eclipse-plugin/wiki/Marker-Property-Page).

*   Marker Icons: Instead of the geometrical figures, the old icons for the PMD markers can
    be selected again:
    ![Screenshot of PMD Marker Property Page](https://raw.githubusercontent.com/wiki/pmd/pmd-eclipse-plugin/images/PMDMarkerPriorityIcons.png)

### Fixed Issues

*   [#82](https://github.com/pmd/pmd-eclipse-plugin/issues/82): Support old PMD icons
*   [#84](https://github.com/pmd/pmd-eclipse-plugin/issues/84): Rule Name in Tooltip

### API Changes

### External Contributions

## 20-June-2019: 4.4.1.v20190620-0836

This is a bugfix release.

### Fixed Issues

*   [#103](https://github.com/pmd/pmd-eclipse-plugin/issues/103): IllegalStateException: Must be called in the UI thread

## 26-May-2019: 4.4.0.v20190526-1012

This is a minor release.

### New and noteworthy

*   Updated PMD to 6.15.0

### External Contributions

*   [#100](https://github.com/pmd/pmd-eclipse-plugin/pull/100): Add missing french translations - [Nicolas HENRY](https://github.com/nicolashenry)

## 28-April-2019: 4.3.0.v20190428-0918

This is a minor release.

### New and noteworthy

*   Updated PMD to 6.14.0

## 26-April-2019: 4.2.1.v20190426-1741

This is a bugfix release.

### New and noteworthy

*   Filtering by priority has been revamped. In both views "Violation Outline" and "Violation Overview" you can
    enable and disable the priorities. Changes in one view affects the other view as well as the markers displayed
    in the files. The selected priorities are stored in the preferences. PMD is not rerun when the priority filter
    is changed. The markers are only hidden instead. Changing the priority filter therefore affects the settings under
    General, Editors, Text Editors, Annotation for PMD Marker Annotations.

### Fixed Issues

*   [#56](https://github.com/pmd/pmd-eclipse-plugin/issues/56): PMD not filtering
*   [#85](https://github.com/pmd/pmd-eclipse-plugin/issues/85): Gutter Markers not Configured Color
*   [#96](https://github.com/pmd/pmd-eclipse-plugin/issues/96): Wrong auxclasspath if project is stored outside of workspace

### API Changes

*   In `net.sourceforge.pmd.eclipse.plugin.UISettings` the following methods are deprecated for removal:
    *   `reloadPriorities()`
    *   `markerFilenameFor(RulePriority)`
    *   `relativeMarkerFilenameFor(RulePriority)`
    *   `markerDescriptorFor(RulePriority)`
    *   `markerImgDescriptorsByPriority()`
    *   `createRuleMarkerIcons(Display)`
    *   `descriptionFor(RulePriority)`
    *   `descriptorFor(RulePriority)`
    *   `priorityFor(int)`
    *   `getPriorityLabels()`
*   In `net.sourceforge.pmd.eclipse.ui.RuleLabelDecorator` the method `reloadDecorators()` is deprecated
    for removal.
*   In `net.sourceforge.pmd.eclipse.ui.ShapePainter` the method `disposeAll()` is deprecated for removal.
*   In `net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptor` the following methods are deprecated
    for removal:
    *   `getImageDescriptor()`
    *   `getImage(Display)`
    *   `getImage(Display, int)`
*   In `net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptorCache` the method `dumpTo(PrintStream)` is
    deprecated for removal.
*   The whole class `net.sourceforge.pmd.eclipse.ui.views.AbstractViolationLabelProvider` is deprecated
    for removal.
*   In `net.sourceforge.pmd.eclipse.plugin.PMDPlugin` the method `getOpenFiles()` is deprecated
    for removal.
*   The class `net.sourceforge.pmd.eclipse.ui.views.PriorityFilter` must not be instantiated directly
    in the future. Therefore the public constructor has been deprecated. Additionally the following methods
    are deprecated for removal:
    *   `setPriorityFilterList(List<Integer>)`
    *   `getPriorityFilterList()`
    *   `addPriorityToList(Integer)`
    *   `removePriorityFromList(Integer)`
    *   `setPriorityFilterListFromString(String, String)`
    *   `getPriorityFilterListAsString(String)`
*   In `net.sourceforge.pmd.eclipse.ui.views.ViolationOverview` the method `getPriorityFilterList()`
    is deprecated for removal.
*   The whole class `net.sourceforge.pmd.eclipse.util.PriorityUtil` is deprecated for removal.

## 31-March-2019: 4.2.0.v20190331-1136

This is a minor release.

### New and noteworthy

*   Updated PMD to 6.13.0

### Fixed Issues

*   [#94](https://github.com/pmd/pmd-eclipse-plugin/issues/94): Changing a project's PMD configuration no longer prompts for build
*   [#95](https://github.com/pmd/pmd-eclipse-plugin/issues/95): Upgrade to PMD 6.13.0

## 07-March-2019: 4.1.0.v20190307-2036

This is a minor release.

### New and noteworthy

*   Updated PMD to 6.12.0
*   The package `name.herlin` is deprecated and will be removed with the next major version of the plugin.
*   PMD markers are now problem markers again. This means, that rule violations appear in the Problem View.
    The project property "Handle high priority violations as Eclipse errors" now works again.
*   The PMD markers are now also visible on the overview ruler. They can be customized or disabled via
    workspace preferences, General, Editors, Text Editors, Annotations.
*   In PMD's general preferences, there is a new option "Determine applicable file types automatically". This new
    option is enabled by default. When enabled, only the files of the languages, for which rules are active,
    are considered. This is a fix for [bug #88](https://github.com/pmd/pmd-eclipse-plugin/issues/88).

### Fixed Issues

*   [#54](https://github.com/pmd/pmd-eclipse-plugin/issues/54): "violationsAsErrors" is completely ineffective
*   [#70](https://github.com/pmd/pmd-eclipse-plugin/issues/70): UnsupportedOperationException opening Rule Configuration
*   [#76](https://github.com/pmd/pmd-eclipse-plugin/issues/76): Global rule management is saved even if cancelled
*   [#78](https://github.com/pmd/pmd-eclipse-plugin/issues/78): Project properties cannot be loaded anymore
*   [#83](https://github.com/pmd/pmd-eclipse-plugin/issues/83): "Restore defaults" button in PMD preferences is always deactivated
*   [#86](https://github.com/pmd/pmd-eclipse-plugin/issues/86): Add Markers next to Scroll bar
*   [#88](https://github.com/pmd/pmd-eclipse-plugin/issues/88): PMD is executed for all file types regardless of active rules
*   [#89](https://github.com/pmd/pmd-eclipse-plugin/issues/89): Upgrade to PMD 6.12.0
*   [#1359](https://sourceforge.net/p/pmd/bugs/1359/): PMD violations in eclipse should be shown on editor by scrollbar

### External Contributions

*   [#75](https://github.com/pmd/pmd-eclipse-plugin/pull/75): Prevent UnsupportedOperationException #70 - [phoenix384](https://github.com/phoenix384)


## 20-December-2018: 4.0.18.v20181220-1448

### New and noteworthy

*   Updated PMD to 6.10.0
*   Eclipse SimRel 2018-09 and 2018-12 is supported.
    To do this, the plugin doesn't expose log4j for other plugins anymore.
    (the package `org.apache.log4j` is not exported anymore). In case you used this in a fragment,
    you should now use the eclipse platform logging facilities.

### Fixed Issues

*   [#48](https://github.com/pmd/pmd-eclipse-plugin/issues/48): Upgrade to PMD 6.10.0
*   [#65](https://github.com/pmd/pmd-eclipse-plugin/issues/65): Support multiple rulesets
*   [#67](https://github.com/pmd/pmd-eclipse-plugin/issues/67): PMD's XML Schemas should be registered in XML Catalog
*   [#73](https://github.com/pmd/pmd-eclipse-plugin/issues/73): Support openjdk 11 as runtime

### External Contributions

*   [#51](https://github.com/pmd/pmd-eclipse-plugin/pull/51): Allow multiple ruleset files - [Phillip Krall](https://github.com/pkrall520)
*   [#53](https://github.com/pmd/pmd-eclipse-plugin/pull/53): README: markdown, fix links - [Lars Hvam](https://github.com/larshp)
*   [#60](https://github.com/pmd/pmd-eclipse-plugin/pull/60): Update to PMD 6.7.0 - [Jan](https://github.com/jgerken)
*   [#61](https://github.com/pmd/pmd-eclipse-plugin/pull/61): Support Eclipse SimRel 2018-09 - [jftsunami](https://github.com/jftsunami)
*   [#63](https://github.com/pmd/pmd-eclipse-plugin/pull/63): Minor changes - [jftsunami](https://github.com/jftsunami)
*   [#66](https://github.com/pmd/pmd-eclipse-plugin/pull/66): Multi ruleset files -  - [Phillip Krall](https://github.com/pkrall520)

## 01-August-2018: 4.0.17.v20180801-1551

### New and noteworthy

*   This is only a bugfix release.

### Fixed Issues

*   [#52](https://github.com/pmd/pmd-eclipse-plugin/issues/52): Eclipse Internal Error - Out of Memory
*   [#57](https://github.com/pmd/pmd-eclipse-plugin/pull/57): De-duplicate project ruleset when loading the project properties


## 12-April-2018: 4.0.16.v20180412-0833

### New and noteworthy

*   Updated PMD to 6.2.0
*   At least Java 1.7 is required now.
*   Two new options under Preferences: "Show PMD violations overview when checking code" and "Show PMD violations
    outline when checking code". If these options are checked, then theses PMD views are shown automatically,
    when PMD check is executed for a project. This behaves similar like "Show PMD perspective", but just doesn't
    switch the current perspective.
*   PMD is now only executed automatically, if the option "Check code after saving" is enabled. This allows
    to simply disable automatic PMD checks temporarily. Executing PMD via the project's context menu "Check Code"
    is not affected and can still be used to manually execute PMD to update the markers.

### Fixed Issues

*   [#20](https://github.com/pmd/pmd-eclipse-plugin/issues/20): category.xml maybe broken
*   [#29](https://github.com/pmd/pmd-eclipse-plugin/issues/29): Processing errors without cause
*   [#32](https://github.com/pmd/pmd-eclipse-plugin/issues/32): Upgrade PMD to 6.2.0
*   [#42](https://github.com/pmd/pmd-eclipse-plugin/issues/42): Marker colors not updated in all views after change
*   [#43](https://github.com/pmd/pmd-eclipse-plugin/issues/43): Update unit tests to use new ruleset categories
*   [#46](https://github.com/pmd/pmd-eclipse-plugin/issues/46): PMD Preferences are overridden and lost

### External Contributions

*   [#25](https://github.com/pmd/pmd-eclipse-plugin/pull/25): \[core] Typesafe properties - [Clément Fournier](https://github.com/oowekyala)
*   [#26](https://github.com/pmd/pmd-eclipse-plugin/pull/26): Updated french translations - [Clément Fournier](https://github.com/oowekyala)
*   [#37](https://github.com/pmd/pmd-eclipse-plugin/pull/37): Global Priority Filter for Violations Overview/Outline - [Phillip Krall](https://github.com/pkrall520)
*   [#39](https://github.com/pmd/pmd-eclipse-plugin/pull/39): Show PMD violations overview/outline views when checking code - [Phillip Krall](https://github.com/pkrall520)
*   [#40](https://github.com/pmd/pmd-eclipse-plugin/pull/40): Only execute PMD when check on save is enabled - [Phillip Krall](https://github.com/pkrall520)
*   [#41](https://github.com/pmd/pmd-eclipse-plugin/pull/41): Update all views after marker color changed - [Phillip Krall](https://github.com/pkrall520)
*   [#44](https://github.com/pmd/pmd-eclipse-plugin/pull/44): Fix saving preferences for violation overview/outline - [Phillip Krall](https://github.com/pkrall520)


## 24-June-2017: 4.0.15.v20170624-2134

*   Updated PMD to 5.8.0


## 28-May-2017: 4.0.14.v20170528-1456

*   Fixed Add travis build ([issue #21](https://github.com/pmd/pmd-eclipse-plugin/issues/21))
*   Fixed NPE while creating ruleset due to hashcode ([issue #23](https://github.com/pmd/pmd-eclipse-plugin/issues/23))


## 29-April-2017: 4.0.13.v20170429-1921

*   Updated PMD to 5.6.1


## 31-March-2017: 4.0.12.v20170331-0813

*   Updated PMD to 5.5.5


## 28-January-2017: 4.0.11.v20170128-2103

*   Updated PMD to 5.5.3


## 26-June-2016: 4.0.10.v20160626-1043

*   Fix classpath errors due to multiple version on asm being on the classpath.
    See also [bug #1492](https://sourceforge.net/p/pmd/bugs/1492/).


## 25-June-2016: 4.0.9.v20160625-2101

*   Updated PMD to 5.5.0
*   Fixed Violation Overview + Outline don't work for Non-Java Rule violations ([issue #13](https://github.com/pmd/pmd-eclipse-plugin/issues/13), [pull request #14](https://github.com/pmd/pmd-eclipse-plugin/pull/14))
*   squid:S1854 - Dead stores should be removed ([pull request #17](https://github.com/pmd/pmd-eclipse-plugin/pull/17))
*   squid:S1213 - The members of an interface declaration or class should appear in a pre-defined order ([pull request #18](https://github.com/pmd/pmd-eclipse-plugin/pull/18))
*   Fixed Executing Rule.start() ([bug #974](https://sourceforge.net/p/pmd/bugs/974/))
*   Fixed Cannot create reports for non-Java projects ([bug #1473](https://sourceforge.net/p/pmd/bugs/1473/))
*   Fixed Check Code doesn't work from project root in non-java projects ([bug #1474](https://sourceforge.net/p/pmd/bugs/1474/))
*   Fixed Not Able to enable PMD for Apex in Eclipse ([bug #1483](https://sourceforge.net/p/pmd/bugs/1483/))


## 04-December-2015: 4.0.8.v20151204-2156

*   Updated PMD to 5.3.6
*   Fixed PMD-Plugin does not work if run with IBM JDK 1.7.0 ([bug #1419](https://sourceforge.net/p/pmd/bugs/1419/))
*   Fixed PMD Eclipse is not executed if "Full build" is not enabled ([bug #1435](https://sourceforge.net/p/pmd/bugs/1435/))
*   Fixed PMD is changing encoding of source code ([bug #1386](https://sourceforge.net/p/pmd/bugs/1386/))

**API Changes**:

*   The package `net.sourceforge.pmd.eclipse.core.rulesets` including sub-packages has been removed from the plugin.
    It won't export these package anymore. The purpose of this package was probably to provide a way of serializing
    and deserializing rulesets. As only the writer has been implemented and the reader was missing, it's not useful.
    If you need to read/write rulesets, use the core PMD functionality provided by `net.sourceforge.pmd.RuleSetWriter`.
    In order to read it back, use `net.sourceforge.pmd.RuleSetFactory`.


## 22-May-2015: 4.0.7.v20150522-1709

* Updated PMD to 5.3.2
* Fixed Unable to check more than one class without FullBuildEnabled ([#1352](https://sourceforge.net/p/pmd/bugs/1352/))
* Fixed Manually checking code with PMD only works if PMD is activated for the project ([bug #1351](https://sourceforge.net/p/pmd/bugs/1351/))
* Fixed Check code after saving runs PMD unnecessarily ([bug #1350](https://sourceforge.net/p/pmd/bugs/1350/))


## 01-April-2015: 4.0.6.v20150401-1945

* Updated PMD to 5.3.0
* Updated PMD to 5.2.3 ([pull request #4](https://github.com/pmd/pmd-eclipse-plugin/pull/4))
* Some performance improvements when using a BUNCH of projects with pmd settings ([pull request #3](https://github.com/pmd/pmd-eclipse-plugin/pull/3))
* Fixed global rule management persistence broken ([bug #1248](https://sourceforge.net/p/pmd/bugs/1248))
* More PMD plugin performance updates ([pull request #5](https://github.com/pmd/pmd-eclipse-plugin/pull/5))


## 05-November-2014: 4.0.5.v20141105-1906

* Updated PMD to 5.2.1


## 09-September-2014: 4.0.4.v20140909-1748

* Updated PMD to 5.1.3
* Fix packaging ([pull request #1](https://github.com/pmd/pmd-eclipse-plugin/pull/1), [bug #1129](https://sourceforge.net/p/pmd/bugs/1129/))
* Fixed Code review in Luna always throws "An internal error occurred during: "ReviewCode"." ([bug #1210](https://sourceforge.net/p/pmd/bugs/1210/))
* Fixed NPE when selecting a Working Set in Eclipse ([bug #1152](https://sourceforge.net/p/pmd/bugs/1152/))
* Fixed Eclipse plugin don't apply global rule configuration after restart ([bug #1184](https://sourceforge.net/p/pmd/bugs/1184/))
* Fixed "Use type resolution" does not work ([bug #1145](https://sourceforge.net/p/pmd/bugs/1145/))
* Fixed NullPointerException when selecting/unselecting items in the Projects Properties view ([bug #1242](https://sourceforge.net/p/pmd/bugs/1242/))
* Fixed sizing of first column in Rule Configuration ([bug #1237](https://sourceforge.net/p/pmd/bugs/1237/))
* Fixed Violations outline should remember column widths ([bug #1240](https://sourceforge.net/p/pmd/bugs/1240/))
* Fixed Rule Configuration window does not use extra horizontal space ([bug #1238](http://sourceforge.net/p/pmd/bugs/1238/))


## 27-April-2014: 4.0.3.v20140427-0831

* Updated PMD to 5.1.1
* Support workspace-relative path in ruleSetFile property ([pull request #36], [feature request #1133])
* Update Updatesite for Kepler and PMD 5.1.0 ([feature request #1179])
* "Generate Abstract Syntax Tree" always run in JDK 1.4 mode ([bug #1174])

[pull request #36]: https://github.com/pmd/pmd/pull/36
[feature request #1133]: https://sourceforge.net/p/pmd/bugs/1133/
[feature request #1179]: https://sourceforge.net/p/pmd/bugs/1179/
[bug #1174]: https://sourceforge.net/p/pmd/bugs/1174/


## 31-October-2013: 4.0.2.v20131031-1124

* Fixed Plugin does not execute custom xpath rules ([bug #1132])
* Fixed After changing exclude filters violations are not cleared ([bug #1148])

[bug #1132]: https://sourceforge.net/p/pmd/bugs/1132/
[bug #1148]: https://sourceforge.net/p/pmd/bugs/1148/


## 11-August-2013: 4.0.1.v20130811-0001

* Updated PMD to 5.0.5
* Fixed Build path exclusions not honored ([bug  #988])
* Fixed right click to add reviewed comment missing ([bug #1052])
* Fixed PMD Eclipse: How to ... documentation missing ([bug #1061])
* Fixed Properties page: "Rule-Selection" should be disabled if project-local config is selected ([bug #1070])
* Fixed Violations are reported multiple times ([bug #1071])
* Fixed Exclude pattern does not work ([bug #1079])
* Fixed SWTError: No more handles on Violation Outline View ([bug #1096])
* Fixed Selecting non-duplicates during import doesn't work. ([bug #1110])
* Fixed Eclipse log file will not be filled ([bug #1112])
* Fixed PMD Eclipse plugin doesn't analyze project if it has non-existing source folders ([bug #1116])
* Fixed An internal error occurred during: "RenderReport" ([bug #1117])
* The official update site is now: <https://sourceforge.net/projects/pmd/files/pmd-eclipse/update-site/>

[bug  #988]: https://sourceforge.net/p/pmd/bugs/988/
[bug #1052]: https://sourceforge.net/p/pmd/bugs/1052/
[bug #1061]: https://sourceforge.net/p/pmd/bugs/1061/
[bug #1070]: https://sourceforge.net/p/pmd/bugs/1070/
[bug #1071]: https://sourceforge.net/p/pmd/bugs/1071/
[bug #1079]: https://sourceforge.net/p/pmd/bugs/1079/
[bug #1096]: https://sourceforge.net/p/pmd/bugs/1096/
[bug #1110]: https://sourceforge.net/p/pmd/bugs/1110/
[bug #1112]: https://sourceforge.net/p/pmd/bugs/1112/
[bug #1116]: https://sourceforge.net/p/pmd/bugs/1116/
[bug #1117]: https://sourceforge.net/p/pmd/bugs/1117/


## 10-May-2013: 4.0.0.v20130510-1000

* Updated PMD to 5.0.4
* Fixed False Positive: LocalVariableCouldBeFinal ([bug #1075])

[bug #1075]: http://sourceforge.net/p/pmd/bugs/1075/

