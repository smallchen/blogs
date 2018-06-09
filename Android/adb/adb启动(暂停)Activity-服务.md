停止App：
`adb shell am force-stop package`
am force-stop com.jokin.example.sidebar

启动App/Activity：
`adb shell am start -n package/.path ... 其中path是相对于package的路径，和manifest.xml里注册的路径一致`
am start -n "com.jokin.example.sidebar/.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER

am start -n "com.jokin.example.sidebar/.test.TestActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER


启动服务：
`adb shell am startservice -n package/.path`
am startservice -n com.jokin.example.sidebar/.MainService
am startservice -n com.jokin.example.sidebar/.monitor.MonitorService

停止服务：
`adb shell am stopservice -n package/.path`

发送广播：
adb shell am broadcast -a android.intent.action.ACTION_SHUTDOWN -c android.intent.category.HOME
-n com.andy.androidtest/.ShutdownBroadcastReceiver
