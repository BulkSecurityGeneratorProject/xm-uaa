[![Build Status](https://travis-ci.org/xm-online/xm-uaa.svg?branch=master)](https://travis-ci.org/xm-online/xm-uaa) [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?&metric=sqale_index&branch=master&project=xm-online:xm-uaa)](https://sonarcloud.io/dashboard/index/xm-online:xm-uaa) [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?&metric=ncloc&branch=master&project=xm-online:xm-uaa)](https://sonarcloud.io/dashboard/index/xm-online:xm-uaa) [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?&metric=coverage&branch=master&project=xm-online:xm-uaa)](https://sonarcloud.io/dashboard/index/xm-online:xm-uaa)

# uaa

This application was generated using JHipster 6.5.1, you can find documentation and help at [https://www.jhipster.tech/documentation-archive/v6.5.1](https://www.jhipster.tech/documentation-archive/v6.5.1).

This is a "uaa" application intended to be part of a microservice architecture, please refer to the [Doing microservices with JHipster][] page of the documentation for more information.

This is also a JHipster User Account and Authentication (UAA) Server, refer to [Using UAA for Microservice Security][] for details on how to secure JHipster microservices with OAuth2.
This application is configured for Service Discovery and Configuration with Consul. On launch, it will refuse to start if it is not able to connect to Consul at [http://localhost:8500](http://localhost:8500). For more information, read our documentation on [Service Discovery and Configuration with Consul][].

## Development

To start your application in the dev profile, simply run:

    ./gradlew

For further instructions on how to develop with JHipster, have a look at [Using JHipster in development][].

## Building for production

### Packaging as jar

To build the final jar and optimize the uaa application for production, run:

    ./gradlew -Pprod clean bootJar

To ensure everything worked, run:

    java -jar build/libs/*.jar

Refer to [Using JHipster in production][] for more details.

### Packaging as war

To package your application as a war in order to deploy it to an application server, run:

    ./gradlew -Pprod -Pwar clean bootWar

## Testing

To launch your application's tests, run:

    ./gradlew test integrationTest jacocoTestReport

For more information, refer to the [Running tests page][].

### Code quality

Sonar is used to analyse code quality. You can start a local Sonar server (accessible on http://localhost:9001) with:

```
docker-compose -f src/main/docker/sonar.yml up -d
```

You can run a Sonar analysis with using the [sonar-scanner](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) or by using the gradle plugin.

Then, run a Sonar analysis:

```
./gradlew -Pprod clean check jacocoTestReport sonarqube
```

For more information, refer to the [Code quality page][].

## Using Docker to simplify development (optional)

You can use Docker to improve your JHipster development experience. A number of docker-compose configuration are available in the [src/main/docker](src/main/docker) folder to launch required third party services.

For example, to start a postgresql database in a docker container, run:

    docker-compose -f src/main/docker/postgresql.yml up -d

To stop it and remove the container, run:

    docker-compose -f src/main/docker/postgresql.yml down

You can also fully dockerize your application and all the services that it depends on.
To achieve this, first build a docker image of your app by running:

    ./gradlew bootJar -Pprod jibDockerBuild

Then run:

    docker-compose -f src/main/docker/app.yml up -d

For more information refer to [Using Docker and Docker-Compose][], this page also contains information on the docker-compose sub-generator (`jhipster docker-compose`), which is able to generate docker configurations for one or several JHipster applications.

## Continuous Integration (optional)

To configure CI for your project, run the ci-cd sub-generator (`jhipster ci-cd`), this will let you generate configuration files for a number of Continuous Integration systems. Consult the [Setting up Continuous Integration][] page for more information.

[jhipster homepage and latest documentation]: https://www.jhipster.tech
[jhipster 6.5.1 archive]: https://www.jhipster.tech/documentation-archive/v6.5.1
[doing microservices with jhipster]: https://www.jhipster.tech/documentation-archive/v6.5.1/microservices-architecture/

[Using UAA for Microservice Security]: https://www.jhipster.tech/documentation-archive/v6.5.1/using-uaa/[Using JHipster in development]: https://www.jhipster.tech/documentation-archive/v6.5.1/development/
[Service Discovery and Configuration with Consul]: https://www.jhipster.tech/documentation-archive/v6.5.1/microservices-architecture/#consul
[Using Docker and Docker-Compose]: https://www.jhipster.tech/documentation-archive/v6.5.1/docker-compose
[Using JHipster in production]: https://www.jhipster.tech/documentation-archive/v6.5.1/production/
[Running tests page]: https://www.jhipster.tech/documentation-archive/v6.5.1/running-tests/
[Code quality page]: https://www.jhipster.tech/documentation-archive/v6.5.1/code-quality/
[Setting up Continuous Integration]: https://www.jhipster.tech/documentation-archive/v6.5.1/setting-up-ci/

## Enabling database persistence for permissions and roles (optional)

By default, role and permission configurations are stored in xm-ms-config service which uses a git repository as persistent storage.
This approach has some disadvantages as merge conflicts for manual handling when merging between different configuration branches (dev/stage/prod).
In order to avoid this and keep environment configuration separately from the source code it's possible to enable database persistence on UAA side.
The feature is tenant-specific so system can operate in both modes simultaneously.
In this case roles and permissions will be persisted in the UAA xm-uaa database, xm-ms-config will ask xm-uaa for the configuration on startup.

Use the following steps:
1. (Optional) To migrate the current roles and permissions to the database, use the endpoint below:

    `curl --location --request POST '<UAA-MS-URL>/roles/<TENANT-NAME>/migrate' \
    --header 'x-tenant: XM' \
    --header 'Authorization: Bearer <YOUR-AUTH-TOKEN>'`


2. For a specific tenant:
2.1. Under the configuration git repository, add files to .gitignore and delete them

    `config/tenants/XM/roles.yml
    config/tenants/XM/permissions.yml`

2.2. Add the following line to tenant-config.yml

    uaa-permissions: true

2.3. Commit and push changes to the repo.

2.4. Restart xm-ms-config and xm-uaa services. 
