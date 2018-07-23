xcode-select: error: tool 'xcodebuild' requires Xcode, but active developer directory '/Library/Developer/CommandLineTools' is a command line tools instance


$ sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer/

用这个命令切换到你正在用的Xcode安装路径下

xcodebuild -showsdks


<https://blog.csdn.net/woaizijiheni/article/details/50614062>
