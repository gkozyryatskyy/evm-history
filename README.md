# evm-history

evm-history

## Configuration

- docker https://docs.docker.com/engine/install/

## LOCAL: Building the application

```
# build with local profile, without tests, with docker image build
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

## LOCAL: Stop the application

```
# stop the app from docker
cd {project-root}/src/main/docker
docker compose -f project.yml down 
# stop the infra
docker compose down
```

## API documentation
| Name         | URL                | Description                                                                                                                                              |
|--------------|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Auth page    | `{host}/`          | Login and get your token by accessing the authentication page. By using this simple authentication process, you can easily access our API documentation. |
| Api doc page | `{host}/q/doc`     | Once you have your token, use it to authorize your access to the Open API 3 page                                                                         |
| Healthcheck  | `{host}/q/health`  | Healthcheck endpoint                                                                                                                                     |
| Metrics      | `{host}/q/metrics` | Metrics endpoint. Return metrics in 'Prometheus' format                                                                                                  |

