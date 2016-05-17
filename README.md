Build: [![Circle CI](https://circleci.com/gh/eclipse/hawkbit.svg?style=svg)](https://circleci.com/gh/eclipse/hawkbit)

# Eclipse.IoT hawkBit - Update Server

[hawkBit](https://projects.eclipse.org/projects/iot.hawkbit) is an domain independent back end solution for rolling out software updates to constrained edge devices as well as more powerful controllers and gateways connected to IP based networking infrastructure.

# Documentation

see [hawkBit Wiki](https://github.com/eclipse/hawkbit/wiki)

# Contact us

* Want to chat with the team behind hawkBit? [![Join the chat at https://gitter.im/eclipse/hawkbit](https://badges.gitter.im/eclipse/hawkbit.svg)](https://gitter.im/eclipse/hawkbit?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
* Having issues with hawkBit? Open a [GitHub issue](https://github.com/eclipse/hawkbit/issues).
* You can also check out our [Project Homepage](https://projects.eclipse.org/projects/iot.hawkbit) for further contact options.

# hawkBit sandbox

We offer a sandbox installation that is free for everyone to try out hawkBit. However, keep in mind that the sandbox database will be reset from time to time. It is also not possible to upload any artifacts into the sandbox. But you can use it to try out the Management UI, Management API and DDI API.

https://hawkbit.eu-gb.mybluemix.net/UI/

# Compile, Run and Getting Started

We are not providing an off the shelf installation ready hawkBit update server. However, we recommend to check out the [Example Application](examples/hawkbit-example-app) for a runtime ready Spring Boot based update server that is empowered by hawkBit. In addition we have [guide](https://github.com/eclipse/hawkbit/wiki/Run-hawkBit) for setting up a complete landscape.

#### Clone and build hawkBit
```
$ git clone https://github.com/eclipse/hawkbit.git
$ cd hawkbit
$ mvn clean install
```
#### Start hawkBit example app
[Example Application](examples/hawkbit-example-app)
```
$ java -jar ./examples/hawkbit-example-app/target/hawkbit-example-app-#version#.jar
```
#### Start hawkBit device simulator
[Device Simulator](examples/hawkbit-device-simulator)
```
$ java -jar ./examples/hawkbit-device-simulator/target/hawkbit-device-simulator-#version#.jar
```
#### Generate Getting Started data
[Example Management API Client](examples/hawkbit-mgmt-api-client)
```
$ java -jar ./examples/hawkbit-mgmt-api-client/target/hawkbit-mgmt-api-client-#version#.jar
```

# Releases and Roadmap

* We are currently working on the first formal release under the Eclipse banner: 0.1 (see [Release 0.1 branch](https://github.com/eclipse/hawkbit/tree/release-train-0.1)).
* The master branch contains future development towards 0.2. We are currently focusing on:
  * Rollout Management for large scale rollouts.
  * Clustering capabilities for the update server.
  * Upgrade of Spring Boot and Vaadin dependencies.
  * And of course tons of usability improvements and bug fixes.


# Modules

* `examples` : hawkBit examples 
* `hawkbit-artifact-repository-mongo` : Artifact repository implementation to mongoDB. 
* `hawkbit-autoconfigure` : Spring-boot auto-configuration. 
* `hawkbit-cache-redis` : Spring cache manager configuration and implementation with redis, distributed cache and distributed events.
* `hawkbit-core` : Core elements for internal interfaces and utility classes.
* `hawkbit-ddi-api` : The hawkBit DDI API.
* `hawkbit-ddi-dl-api` : The hawkBit DDI Download API.
* `hawkbit-ddi-resource` : Implementation of the hawkBit DDI API
* `hawkbit-dmf-amqp` : AMQP endpoint implementation for the DMF API. 
* `hawkbit-dmf-api` : API for the Device Management Integration.  
* `hawkbit-http-security` : Implementation for security filters for HTTP.  
* `hawkbit-mgmt-api` : The hawkBit Management API
* `hawkbit-mgmt-resource` : Implementation of the hawkBit Management API
* `hawkbit-mgmt-ui` : Vaadin UI.  
* `hawkbit-repository` : Repository implementation based on SQL for all meta-data.   
* `hawkbit-rest-core` : Core elements for the rest modules.
* `hawkbit-security-core` : Core security elements.  
* `hawkbit-security-integration` : Security integration elements to integrate security into hawkBit.  
* `hawkbit-system-api` : The hawkBit System API
* `hawkbit-system-resource` : Implementation of the hawkBit System API
* `hawkbit-test-report` : Test reports
