# PMD For Eclipse - Release Notes

Installation instructions: <http://pmd.sourceforge.net/eclipse/>

Eclipse Update Site: <https://sourceforge.net/projects/pmd/files/pmd-eclipse/update-site/>

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

