# ONOS_Integration_Service

An Interface to integrate ONOS with other platforms, to introduce any devices into ONOS for visualization or management purpose.

ONOS Version: [**2.5.8**](https://github.com/opennetworkinglab/onos/tree/2.5.8), and it is easy to migrate to other versions. ([**2.5.1**](https://github.com/opennetworkinglab/onos/tree/2.5.1) was verified)

.

**Notes:** This project have been published at the **[ONOS_TopologyShow_ChineseMap](https://github.com/MaoJianwei/ONOS_TopologyShow_ChineseMap)** project, with CI/CD support, providing the Docker image. (2024.09.16)

.

## Demo 1: Visualization for your network, cluster nodes, and more imagination.

![https://raw.githubusercontent.com/MaoJianwei/ONOS_with_China_Map/master/Screenshot.png](https://raw.githubusercontent.com/MaoJianwei/ONOS_with_China_Map/master/demo-screenshoot-multiple-links.png)

*More details about Chinese map, please refer to [ONOS_with_China_Map](https://github.com/MaoJianwei/ONOS_with_China_Map) project.*

.

## Matched Version

* **ONOS**: git tag 2.5.8
* **Intellij IDEA**: 2022.1.4 (with its own JRE. And I use Ultimate Edition.)
* **Bazel Plugin for Intellij IDEA**: 2022.11.29.0.1-api-version-221 by Google
* **Bazel**: 3.7.2 (for building and compiling)
* **OpenJDK**: 64-Bit Server VM **Zulu**11.37+17-CA (build 11.0.6+10-LTS, mixed mode) (for running ONOS, you must use Zulu JDK, you have no choice)
* **Maven**: you don't need to install maven.


## Compile

Before compiling our project, you should merge the content of **modules.bzl** to ./tools/build/bazel/modules.bzl

**Especially,** for chinese network environment, you might disable the onos-gui app (version 1) in the **modules.bzl**, so you can success to import the project to IDEA (bazel sync).


## Community Support

:) [Jianwei Mao @ BUPT FNLab](https://www.maojianwei.com/) - ONOS China Ambassador - MaoJianwei2012@126.com / MaoJianwei2020@gmail.com
