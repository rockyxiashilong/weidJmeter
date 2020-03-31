#!/bin/bash
set -e
JMETER_HOME=/data/rocky/apache-jmeter-5.1.1-weid 
#JMETER_HOME=
if [ -z "$JMETER_HOME" ];then
echo "please set JMETER_HOME"
exit 1
fi
if [ ! -f "./dist/apps/weid-jmeter-demo-1.0-SNAPSHOT.jar" ];then
echo "压测程序不存在"
exit 1
else
echo "jmeter_home = $JMETER_HOME"
cp -r ./dist/lib/* "$JMETER_HOME/dependencies/"
cp -r ./dist/apps/* "$JMETER_HOME/dependencies/"
fi
#delete HTTP/*
if [ -d "http/" ];then
echo "http file exist,delete history files"
rm -rf http/*
else
mkdir /http
fi

if [ ! -f "demo.jtl" ];then
echo "demo.jtl not exist"
else
echo "demo.jtl exist,del..."
rm -rf demo.jtl
fi

#at the jmeter home,then start jmeter auto test
#cp demo.jmx $JMETER_HOME/bin
#cd $JMETER_HOME/bin
jmeter -n -t demo.jmx -l demo.jtl -e -o http

#jmeter -n -t $JMETER_HOME/bin/demo.jmx -l $JMETER_HOME/bin/demo.jtl -e -o $JMETER_HOME/bin/HTTP


echo "test running end,result http://localhost:9999/index.html"
