    #!/bin/bash -eu

## Initializtion script for druid nodes
## Runs druid nodes as a daemon and pipes logs to log/ directory

usage="Usage: cmd.sh (start|stop|restart)"

startStop=$1
pid=robot.pid
basepath=$(cd `dirname $0`; cd ..; pwd;)
main_class="io.sugo.server.http.AntServer"
case $startStop in

  (start)

    PID=`jps -ml|grep $main_class|awk '{print $1;}'`
    if [ "$PID" != "" ] ;then
        echo "[$main_class] running as process ($PID).  Stop it first."
        exit 0
    fi

    cd $basepath
    nohup java `cat $basepath/conf/config/jvm.config | xargs` -Dlog.path=/data1/ant -cp $basepath/sugo-ant-1.0-SNAPSHOT.jar:$basepath/lib/* $main_class $basepath/conf/config &
    chmod 666 /data1/ant/ant.log
#    sleep 2
    nodeType_PID=`ps -ef | grep $main_class | grep -v "grep "| awk '{print $2}'`
#    echo $nodeType_PID > $pid
    echo "Started [$main_class] ($nodeType_PID)"
    ;;

  (stop)

    PID=`jps -ml|grep $main_class|awk '{print $1;}'`
    if [ "$PID" = "" ] ;then
        echo "[$main_class] is not started"
        exit 0;
    else
        echo "kill ($PID)"
        kill $PID
        for ((i=1;i<=1000;i+=1))
        do
            p=`jps -ml|grep $main_class|awk '{print $1;}'`
            if [ "$p" == "" ];then
                echo "($PID) has exit"
                exit 0
            else
                echo "waitting ($PID) to exit"
                sleep 1
            fi
        done
    fi
    ;;

  (restart)
    $0 stop
    $0 start
    ;;

  (*)
    echo $usage
    exit 1
    ;;

esac


