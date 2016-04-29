# DAPNET CORE #
## Introduction ##
The DAPNET CORE offers the main functionality of the Decentralized Amateur Paging Network.
It builds up a cluster together with other DAPNET Core instances over the HAMNET and
controls connected paging transmitter. All functions can be accessed via the REST API.

## Installation and Requirements ##
Using the packed release version no installation is required. All external libraries are
included. Just make sure a JAVA 8+ runtime environment is available.

## Usage ##
### Basic Configuration ###
#### Cluster Configuration ####
If you would like to join an existing cluster contact an admin. He will register your DAPNET
Core instance and inform you about the configuration. If you want to create a new cluster
feel free to set your own configuration. In each case set the following parameters in the
`cofig/ClusterConfig.xml`:
   * Node name (name of your DAPNET Core instance) at
      `<pbcast.GMS name="NodeName@ClusterName"/>`
   * Cluster name (name of the DAPNET Cluster) at
      `<pbcast.GMS name="NodeName@ClusterName"/>`
   * Node authentication key (needed for authentication if you want to rejoin your
      cluster later) at `<AUTH auth_value="node name"/>`
   * Initial hosts (if you want to join an existing cluster at least one node have to be
     known... ) at `<TCPPING initial_hosts="HostA[7800],HostB[7800]\>`

#### Log Settings ####
The DAPNET Core uses Log4j2 for logging, which can be configured in the
`config/LogSettings.xml`. The default setting will inform you about nearly all events in the
console and additionally will store all logs in a file.

#### General Settings ####
In the `Setting.json` file various parameters can be set. The default settings should fit for
most use cases. However, important could be the `baseUrl": "http://localhost:8080/"` line to
configure the port of the REST API.

### First Start ###
1. Make sure you performed the basic configuration as described above.
2. Start the application with `java -jar DAPNET_CORE.jar`
3. In case you connect to an existing cluster all current data will be automatically downloaded.
   If you create a new cluster and no `date/state.json` is found a new will be created. Please
   follow the instructions in the console to create a first user to have access to the REST API.
4. After a successful startup you can start to play with the REST API
   ([API Description](https://bitbucket.org/DAPNET/dapnet-core/wiki/Beschreibung%20der%20REST%20API))


## Used Software ##
TODO

## Contribution ##
TODO

## More Information ##
TODO

**DAPNET CORE PROJECT | Copyright (C) 2016**

**Institut für Hochfrequenztechnik | RWTH AACHEN UNIVERSITY**

Melatener Str. 25 | 52074 Aachen

Daniel Sialkowski | daniel.sialkowski@rwth-aachen.de

Ralf Wilke | wilke@ihf.rwth-aachen.de

## License ##
TODO






