## Building proto for JavaScript client

```sh
cd client
npm i
npm run build-grpc
```

## Building proto for Java server

Remember to have properly named protoc java plugin file (make it executable on linux aswell)

In my example the filename is `protoc-java-plugin.exe`

The proto plugin should be located in `server/protoc-java-plugin.exe`

```sh
cd server
./build-grpc.sh
```
