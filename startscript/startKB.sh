java -classpath ../target/hydra-1.0-SNAPSHOT.jar com.github.hydra.client.Start --logLevel debug --ssl --host www.kbtrade.me --port 9443 --path /ws  --subscribeInterval 20  --heartBeat '{"event":"ping"}' --unGzipJson --subscribe '{"event":"sub","biz":"exchange","type":"ticker","pairCode":"ALL"}'
