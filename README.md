# sugo-ant程序使用说明

使用步骤:  
1. 从`git`上克隆本项目
```shell
git clone git@github.com:Datafruit/sugo-ant.git
```
2. 根据需求配置`sugo-ant/src/main/resources/config/system.properties`文件中的属性
3. 进入项目根目录下,使用`maven`对程序进行编译打包
```shell
mvn clean package
```
4. 从项目根目录下进入`target`目录解压相应的`tar`包
```
cd target
tar -zxvf sugo-ant-1.0-SNAPSHOT-bin.tar.gz
```

5. 进入解压目录并启动程序脚本
```
cd sugo-ant-1.0-SNAPSHOT
bin/cmd.sh start
```
6. 在解压目录下查看日志输出确保程序正常启动
```
tail -200f nohup.out 
```
输入以上命令后,若日志最后一行输出以下信息说明程序正常启动
``` 
Start...in 6061
```
7. 开始使用系统   
参考`sugo-ant/docs/api.md`接口文档进行访问
8. 停止系统
```
bin/cmd.sh stop
```

