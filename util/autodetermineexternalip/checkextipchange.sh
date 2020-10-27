#!/bin/bash
cd /opt/dapnet/Core/util/autodetermineexternalip/
CurrentIP=`curl -s --noproxy "*" http://web.db0sda.ampr.org/ip.php`
echo $CurrentIP
LastIP=`cat ./lastip.hamnet.ipv4`
if [ "$CurrentIP" != "$LastIP" ]
  then
    echo "Public IP changed"
    echo $CurrentIP > ./lastip.hamnet.ipv4
    cp ../../local/config/ClusterConfig.xml.template1 ../../local/config/ClusterConfig.xml
    echo -n "         external_addr=\"" >> ../../local/config/ClusterConfig.xml
    echo -n $CurrentIP >> ../../local/config/ClusterConfig.xml
    echo "\"" >> ../../local/config/ClusterConfig.xml
    cat ../../local/config/ClusterConfig.xml.template2 >> ../../local/config/ClusterConfig.xml
    echo "Restarting DAPNET Core"
    sudo systemctl restart dapnetcore.service
    echo "DAPNET Core restarted"
  else
    echo "Public IP unchanged"
fi

