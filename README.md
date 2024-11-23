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
![Screenshot 2024-11-24 at 01.21.50.png](Screenshot%202024-11-24%20at%2001.21.50.png)

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

## INFO

- All RPC calls (blocks, receipts, contract code) are `batched`
- Added `throttling` and `retries` to address rate limits and service unavailability
- Data is `yearly partitioned`, so we assume to be ok on performance even with whole blockchain data (testnet for sure)
- Data is `indexed`, so we assume to be ok on search performance even with whole blockchain data (testnet for sure)
- Partial search indexed as `edge_ngram`, so we assume to be ok on search performance even with whole blockchain data (testnet for sure)

# TODO
- Switch contracts cache to Redis or DB calls
- Track other contract interactions from logs
- Get ABI and add abi parsing
- Try to add contract code decompilation if there is no ABI available