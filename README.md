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

## LOCAL: Checking data
```
# Open dashboard
http://localhost:5601/app/dashboards#/view/5f077338-3ff2-401d-8053-ce95347866bf?_g=(filters:!(),refreshInterval:(pause:!f,value:60000),time:(from:now-1y,to:now))
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
| Name         | URL                | Description                                             |
|--------------|--------------------|---------------------------------------------------------|
| Healthcheck  | `{host}/q/health`  | Healthcheck endpoint                                    |
| Metrics      | `{host}/q/metrics` | Metrics endpoint. Return metrics in 'Prometheus' format |

