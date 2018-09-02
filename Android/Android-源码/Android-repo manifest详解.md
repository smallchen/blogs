## Android repo manifest详解

```xml
<?xml version="1.0" encoding="UTF-8"?>
<manifest>
  <remote fetch="ssh://git@gitlab.jokin.com/demoos" name="gitlab"/>
  <remote fetch="ssh://git@gitlab.jokin.com/demo-android-commons" name="gitlab_common"/>
  <remote fetch="ssh://git@gitlab.jokin.com/module/scm" name="gitlab_module_scm"/>
  <remote fetch="ssh://git@gitlab.jokin.com/demosystem" name="gitlab_demosystem"/>
  <remote fetch="ssh://git@gitlab.jokin.com/demosystem" name="origin"/>

  <default remote="origin" revision="refs/heads/module-release"/>

  <project name="FramebufferJNI" path="./external/demo/FramebufferJNI" remote="gitlab" revision="f708fcb461900c34dd7d364bbec6eb8b2e73250c" upstream="refs/heads/3399-6.0-master"/>
  <project name="UwstBin" path="./device/rockchip/module/module_preinstall/UwstBin" remote="gitlab_demosystem" revision="efa22f9680f695bc933b0b7e41bed8d45a70ef9f" upstream="refs/heads/module_uwst"/>
  <project name="android_apps" path="./android_apps" revision="ab57957c9d4a7c041f3c5f1f990414eb47546f59" upstream="refs/heads/module"/>
  <project name="repo_scm_utils" path="./repo_scm_utils" remote="gitlab_module_scm" revision="refs/heads/new_3399">
      <copyfile dest="./apps_parse.py" src="apps_parse.py"/>
      <copyfile dest="./build_system.sh" src="build_system.sh"/>
      <copyfile dest="./sync_apk.sh" src="sync_apk.sh"/>
  </project>
  <project name="rk3399" path="./" revision="a626a99edceeb85e7a88807f9d1fca49c1793473" upstream="refs/heads/module-release"/>
  <project name="scm_module_apps" path="./scm_module_apps" remote="gitlab_module_scm" revision="refs/heads/master"/>
  <project name="wired" path="./device/rockchip/common/wired" revision="919de6bd1ff752024af0bde0ac894ae8135d6389" upstream="refs/heads/release"/>
</manifest>
```
参考<https://blog.csdn.net/hansel/article/details/9798189>
