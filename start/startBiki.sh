java -classpath hydra-1.0-SNAPSHOT.jar com.github.hydra.client.Start --logLevel debug -ssl --host wspool.mpuuss.top --port 443 --path /kline-api/ws --subscribe '{"event":"sub","params":{"channel":"market_bikiusdt_kline_30min","cb_id":"bikiusdt"}}' --heartBeat '{"pong":%s}' --ungzip