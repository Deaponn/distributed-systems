# Simple ZooKeeper app

## How to run:

1. Install ZooKeeper in `./zookeeper` directory (create `zookeeper` dir in the directory in which this README is located)
1. Copy configuration files `cp ./conf ./zookeeper/conf`
1. Run the ZooKeeper servers:
- `cd ./zookeeper/bin`
- `./zkServer.sh start ../conf/zoo1.cfg`
- `./zkServer.sh start ../conf/zoo2.cfg`
- `./zkServer.sh start ../conf/zoo3.cfg`
- `cd ../..`
1. Compile the java code: `/usr/lib/jvm/java-21-jdk/bin/javac -cp ".:$(pwd)/zookeeper/lib/*" AppManager.java`
1. Run the java code: `/usr/lib/jvm/java-21-jdk/bin/java -cp ".:$(pwd)/zookeeper/lib/*" AppManager "localhost:2181,localhost:2182,localhost:2183" /usr/bin/kate`
1. Run ZooKeeper client utility and manage the znodes:
- `cd ./zookeeper/bin`
- `./zkCli.sh`
- `create /a "hello world"`
- `create /a/child1 "data1"`
- `create /a/child1/grandchild1 "granddata1"`
- `create /a/child2 "data2"`
- `create /a/child3 "data3"`
- `create /a/child4 "data4"`
- `create /a/child4/grandchild2 "granddata2"`
- `create /a/child4/grandchild2/grandgrandchild1 "grandgranddata1"`
- `delete /a/child4/grandchild2/grandgrandchild1`
- `delete /a/child4/grandchild2`
- `delete /a/child4`
- `delete /a/child3`
- `delete /a/child2`
- `delete /a/child1/grandchild1`
- `delete /a/child1`
- `delete /a`
