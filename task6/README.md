# Simple ZooKeeper app

## How to run:

1. Run the ZooKeeper servers: `docker compose up`
1. Compile the java code: `/usr/lib/jvm/java-21-jdk/bin/javac -cp ".:$(pwd)/zookeeper/lib/*" AppManager.java`
1. Run the java code: `/usr/lib/jvm/java-21-jdk/bin/java -cp ".:$(pwd)/zookeeper/lib/*" AppManager "localhost:2181,localhost:2182,localhost:2183" /usr/bin/google-chrome`
1. Run ZooKeeper client utility and manage the znodes (use `docker ps` to find container id):
```
docker exec -it <container_id> zkCli.sh -server localhost:2181

create /a
create /a/child1
create /a/child1/grandchild1
create /a/child2
create /a/child3
create /a/child4
create /a/child4/grandchild2
create /a/child4/grandchild2/grandgrandchild1
delete /a/child4/grandchild2/grandgrandchild1
delete /a/child4/grandchild2
delete /a/child4
delete /a/child3
delete /a/child2
delete /a/child1/grandchild1
delete /a/child1
delete /a
```
