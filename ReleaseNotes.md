# PMD For Eclipse - Release Notes

Installation instructions: <http://pmd.sourceforge.net/eclipse/>

Eclipse Update Site: <https://sourceforge.net/projects/pmd/files/pmd-eclipse/update-site/>

## ????: 4.0.4.v????

* Updated PMD to 5.1.2
* Fix packaging ([pull request #1](https://github.com/pmd/pmd-eclipse-plugin/pull/1), [bug #1129](https://sourceforge.net/p/pmd/bugs/1129/))
* Fixed Code review in Luna always throws "An internal error occurred during: "ReviewCode"." ([bug #1210](https://sourceforge.net/p/pmd/bugs/1210/))
* Fixed NPE when selecting a Working Set in Eclipse ([bug #1152](https://sourceforge.net/p/pmd/bugs/1152/))
* Fixed Eclipse plugin don't apply global rule configuration after restart ([bug #1184](https://sourceforge.net/p/pmd/bugs/1184/))
* Fixed "Use type resolution" does not work ([bug #1145](https://sourceforge.net/p/pmd/bugs/1145/))


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

