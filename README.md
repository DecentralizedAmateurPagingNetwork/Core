# DAPNET CORE
# At the moment, there is no need for new installations of DAPNET Cores of this version. A second one is under development. So please wait.

## Introduction
The DAPNET CORE offers the main functionality of the Decentralized Amateur Paging Network.
It builds up a cluster together with other DAPNET Core instances over the HAMNET and
controls connected paging transmitter. All functions can be accessed via the REST API.

## Installation and Requirements
Using the packed release version no installation is required. All external libraries are
included. Just make sure a JAVA 8+ runtime environment is available.


## Usage
### Basic Configuration
#### Cluster Configuration
If you would like to join an existing cluster contact an admin. He will register your
DAPNET Core instance and inform you about the configuration. If you want to create a new
cluster feel free to set your own configuration. In each case set the following parameters
in the `local/config/ClusterConfig.xml`:

* Node name (name of your DAPNET Core instance) at
  `<pbcast.GMS name="NodeName@ClusterName"/>`
* Cluster name (name of the DAPNET Cluster) at `<pbcast.GMS name="NodeName@ClusterName"/>`
* Node authentication key (needed for authentication if you want to rejoin your cluster
  later) of form XXXX-XXXX-XXXX-XXXX-XXXX (X is alphanumeric) at
  `<AUTH auth_value="key"/>`
* Initial hosts (if you want to join an existing cluster at least one node have to be
  known... ) at `<TCPPING initial_hosts="HostA[7800],HostB[7800]\>`

#### Log Settings
The DAPNET Core uses Log4j2 for logging, which can be configured in the
`config/LogSettings.xml`. The default setting will inform you about nearly all events in
the console and additionally will store all logs in a file.

#### General Settings
In the `Setting.json` file various parameters can be set. The default settings should fit
for most use cases. However, important could be the `"port": 8080"` line to configure the
port of the REST API.

### First Start
1. Make sure you performed the basic configuration as described above.
2. Start the application with `java -Dlog4j.configurationFile=../local/config/LogSettings_REST.xml -jar dapnet-core-x.x.x.jar`
3. In case you connect to an existing cluster all current data will be automatically
   downloaded. If you create a new cluster and no `date/state.json` is found the
   parameters of your node will be set to default values. Additionally, a default user
   `admin` with password `admin` will be automatically created, which you can used to
   connect with the REST interface.
4. After a successful startup you can start to play with the REST API
   ([API Description](https://bitbucket.org/DAPNET/dapnet-core/wiki/Beschreibung%20der%20REST%20API))
5. In case you created a new cluster please set all parameters of your node (e.g.
   longitude and latitude), set the `admin` user's mail address and especially set a new
   password.

## Used Software
TODO

## Contribution
TODO

## More Information
TODO

**DAPNET CORE PROJECT | Copyright (C) 2017**

**Institut für Hochfrequenztechnik | RWTH AACHEN UNIVERSITY**

Melatener Str. 25 | 52074 Aachen

Daniel Sialkowski | daniel.sialkowski@rwth-aachen.de

Ralf Wilke | wilke@ihf.rwth-aachen.de

Philipp Thiel

## License ##
This software may be distributed free of charge. It may be only used for non-commercial
application. It's use is limited to applications releated directly the amateur radio.
The software may be changed and redistributed, but in any case, this licences remarks
have to be maintained in the disribution. The original copyright holders have to be named.

For licensing of the used third party software, please refer to the Licences.txt file.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
