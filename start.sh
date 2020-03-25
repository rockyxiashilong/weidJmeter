#!/bin/bash
set -e
if [ ! -f "./dist/apps/weid-jmeter-demo-1.0-SNAPSHOT.jar" ];then
echo "压测程序不存在"
exit 1
else
echo "jmeter_home = $JMETER_HOME"
cp -r ./dist/lib/* "$JMETER_HOME/dependencies/"
cp -r ./dist/apps/* "$JMETER_HOME/dependencies/"
fi
#delete HTTP/*
if [ -d "$JMETER_HOME/bin/HTTP/" ];then
echo "http file exist,delete history files"
rm -rf $JMETER_HOME/bin/HTTP/*
else
mkdir $JMETER_HOME/bin/HTTP
fi

if [ ! -f "$JMETER_HOME/bin/demo.jtl" ];then
echo "demo.jtl not exist"
else
echo "demo.jtl exist,del..."
rm -rf $JMETER_HOME/bin/demo.jtl
fi

#start jmeter auto test
jmeter -n -t $JMETER_HOME/bin/demo.jmx -l $JMETER_HOME/bin/demo.jtl -e -o $JMETER_HOME/bin/HTTP


echo "test running end,result http://localhost:9999/index.html"
