# hydra

### 订阅火币网的websocket

```

java -classpath target/hydra-1.0-SNAPSHOT.jar com.github.hydra.client.Start --logLevel debug -ssl --host api.huobiasia.vip --port 443 --path /ws --subscribe '{"sub":"market.btcusdt.detail","symbol":"btcusdt"}'  --heartBeat '{"pong":%s}'

```
