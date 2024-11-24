# evm-history

evm-history

## Configuration

- docker https://docs.docker.com/engine/install/

## LOCAL: Building the application

```
# build with local profile, without tests, with docker image build
# (!!!) Use java 21 to build, because lombok do not support java 23+
mvn clean install
# with tests, test using dev services, so no need to run any infra
mvn clean install -DskipTests=false
```

## LOCAL: Running the application

```
# run infra
cd {project-root}/src/main/docker
docker compose up -d 
# run project from docker
docker compose -f project.yml up -d
# for runing project from IDE, just run io.evm.history.Main.main(String... args)
```

## LOCAL: Dashboard

```
# Open dashboard
http://localhost:8080/
```

## LOCAL: Stop the application

```
# stop the app from docker
cd {project-root}/src/main/docker
docker compose -f project.yml down 
# stop the infra
docker compose down
# if you want the volumes also to be deleted
docker compose down -v
```

## HTTP routes

| Name        | URL                        | Description                                             |
|-------------|----------------------------|---------------------------------------------------------|
| Dashboard   | `localhost:8080`           | Dashboard endpoint                                      |
| Healthcheck | `localhost:8080/q/health`  | Healthcheck endpoint                                    |
| Metrics     | `localhost:8080/q/metrics` | Metrics endpoint. Return metrics in 'Prometheus' format |

## Results
- top 10 contracts called in the transactions that make up a blocks (by amount of calls)
![Screenshot 1.png](Screenshot%201.png)
- For each top 10 contract calculate
  - the original bytecode size
  - the maximum gas amount provided by users in calls to the contract
  ![Screenshot 2.png](Screenshot%202.png)
  - the top 3 callers of each contract and how many times they called it (click on any contract and whole dashboard will be filtered by this contract)
  ![Screenshot 3.png](Screenshot%203.png)

## INFO
- All RPC calls (blocks, receipts, contract code) are `batched`. See:
  - https://github.com/gkozyryatskyy/evm-history/blob/main/src/main/java/io/evm/history/config/EvmHistoryConfig.java#L18
  - https://github.com/gkozyryatskyy/evm-history/blob/main/src/main/java/io/evm/history/service/BlockAndReceiptsService.java#L67
- Added `throttling` and `retries` to address rate limits and service unavailability. See:
  - https://github.com/gkozyryatskyy/evm-history/blob/main/src/main/java/io/evm/history/util/retry/RetryUtil.java#L23
  - https://github.com/gkozyryatskyy/evm-history/blob/main/src/main/java/io/evm/history/util/retry/ThrottlingUtil.java
- Data is `yearly partitioned`, so we assume to be ok on performance even with whole blockchain data (testnet for sure). See:
  - https://github.com/gkozyryatskyy/evm-history/blob/main/src/main/java/io/evm/history/config/core/EsIndexConfig.java#L45
- Data is `indexed`, so we assume to be ok on search performance even with whole blockchain data (testnet for sure). See:
  - https://github.com/gkozyryatskyy/evm-history/blob/main/src/main/java/io/evm/history/db/model/mapping/TransactionDataIndexMapping.java#L14
- Partial search indexed as `edge_ngram`, so we assume to be ok on search performance even with whole blockchain data (testnet for sure). See:
  - https://github.com/gkozyryatskyy/evm-history/blob/main/src/main/java/io/evm/history/db/model/core/IIndexMapping.java#L16

# TODO
- Switch contracts cache to Redis or DB calls
- Track other contract interactions from logs
- Get ABI and add tx ABI parsing
- Try to add contract code decompilation if there is no ABI available