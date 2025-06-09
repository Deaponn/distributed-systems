import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class AppManager implements Watcher {

    private static final String ZNODE_A = "/a";
    private static final int SESSION_TIMEOUT = 5000;

    private ZooKeeper zk;
    private final CountDownLatch connectedSignal = new CountDownLatch(1);
    private final String zkConnectionString;
    private final String externalAppPath;
    private Process externalAppProcess;

    public AppManager(String zkConnectionString, String externalAppPath) {
        this.zkConnectionString = zkConnectionString;
        this.externalAppPath = externalAppPath;
    }

    public void start() throws IOException, InterruptedException {
        zk = new ZooKeeper(zkConnectionString, SESSION_TIMEOUT, this);
        connectedSignal.await();
        setWatcherForA();
        listenForUserInput();
    }

    private void setWatcherForA() {
        try {
            Stat stat = zk.exists(ZNODE_A, true);
            if (stat != null) {
                launchExternalApp();
                setWatcherForAChildren();
            }
        } catch (KeeperException | InterruptedException e) {
            System.err.println("Error setting watcher on " + ZNODE_A + ": " + e.getMessage());
        }
    }

    private void setWatcherForAChildren() {
        try {
            zk.getChildren(ZNODE_A, true);
        } catch (KeeperException | InterruptedException e) {
            System.err.println("Could not set child watcher on " + ZNODE_A + ": " + e.getMessage());
        }
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.println("WATCHER TRIGGERED: " + event);

        if (event.getType() == Event.EventType.None) {
            if (event.getState() == Event.KeeperState.SyncConnected) {
                System.out.println("Successfully connected to ZooKeeper.");
                connectedSignal.countDown();
            } else if (event.getState() == Event.KeeperState.Expired) {
                System.err.println("ZooKeeper session expired. Shutting down.");
                shutdown();
            }
            return;
        }

        String path = event.getPath();
        if (path != null && path.equals(ZNODE_A)) {
            try {
                switch (event.getType()) {
                    case Event.EventType.NodeCreated -> {
                        launchExternalApp();
                        zk.exists(ZNODE_A, true);
                        zk.getChildren(ZNODE_A, true);
                    }
                    case Event.EventType.NodeDeleted -> {
                        stopExternalApp();
                        zk.exists(ZNODE_A, true);
                    }
                    case Event.EventType.NodeChildrenChanged -> {
                        List<String> children = zk.getChildren(ZNODE_A, true);
                        System.out.println("Children count: " + children.size());
                    }
                }
            } catch (KeeperException | InterruptedException e) {
                System.err.println("Error handling event: " + e.getMessage());
            }
        }
    }

    private void launchExternalApp() {
        if (externalAppProcess != null && externalAppProcess.isAlive()) return;
        try {
            System.out.println("Launching external application: " + externalAppPath);
            externalAppProcess = new ProcessBuilder(externalAppPath).start();
        } catch (IOException e) {
            System.err.println("Failed to launch application: " + e.getMessage());
        }
    }

    private void stopExternalApp() {
        if (externalAppProcess == null) return;

        ProcessHandle handle = externalAppProcess.toHandle();
        handle.descendants().forEach(ProcessHandle::destroy);
        handle.children().forEach(ProcessHandle::destroy);

        if (handle.isAlive()) handle.destroy();

        externalAppProcess = null;
        System.out.println("Termination sequence complete.");
    }

    private void listenForUserInput() {
        System.out.println("\nAppManager running. Enter 'tree' or 'exit'.");
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String line = scanner.nextLine();
                if ("tree".equalsIgnoreCase(line)) {
                    displayTree(ZNODE_A, "");
                } else if ("exit".equalsIgnoreCase(line)) {
                    break;
                }
            }
        } finally {
            shutdown();
        }
    }

    private void displayTree(String path, String indent) {
        try {
            if (zk.exists(path, false) == null) {
                System.out.println(indent + path + " [DOES NOT EXIST]");
                return;
            }
            System.out.println(indent + path);
            List<String> children = zk.getChildren(path, false);
            Collections.sort(children);
            for (String child : children) {
                displayTree(path + "/" + child, indent + "  ");
            }
        } catch (KeeperException | InterruptedException e) {
            System.err.println("Could not display tree for path " + path + ": " + e.getMessage());
        }
    }

    public void shutdown() {
        stopExternalApp();
        if (zk != null) {
            try {
                zk.close();
            } catch (InterruptedException ignored) {}
        }
        System.out.println("Shutdown complete.");
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("USAGE: java AppManager <zk_connection_string> <path_to_external_app>");
            System.err.println("EXAMPLE: java AppManager \"host1:2181,host2:2182\" /usr/bin/kate");
            System.exit(1);
        }

        System.out.println("Starting AppManager at " + java.time.LocalDateTime.now());

        try {
            AppManager manager = new AppManager(args[0], args[1]);
            Runtime.getRuntime().addShutdownHook(new Thread(manager::shutdown));
            manager.start();
        } catch (Exception e) {
            System.err.println("Application failed to start: " + e.getMessage());
        }
    }
}
