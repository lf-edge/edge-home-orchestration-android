# Home-Edge-Android

As a continous process to support Home Edge (Linux) to multiple platforms, this project caters to support Home Edge for Android. There are various smart home devices and Android has been making its presence in many of these devices. The most essential part of the ecosystem has been Smart phone and specifically Android based smart phones. There are large number of applications developed to support user needs across various domains. Some of the service applications would need compute intensive or containerized applications which are not available on the Android phones. This project helps the service applications on the android phones to leverage on the service offered by devices in the same network. 

Home Edge helps the Android phone to discover the other nearby devices on same network and there capabilities/services offered. It is REST based peer to peer communication between all the devices. Every device in the network is a Home Edge node. Based on the CPU capability, memory, network bandwidth a score would be calculated by all the devices. The service application on Android requests to Home Edge for a service in nearby devices. Home Edge in turn communicates with other Linux based devices running Home Edge to check on the availability of the service. When there are more than one device offering the same service, the score helps in electing the device to offload the service. 

**Note :** Currently the Android can consume the services offered by other Linux based Home Edge devices. But in turn services offered by Android would not be discoverable by Linux Home Edge. This has been constraint version and would be worked upon as the project matures.

# How to work
## Request to execute a service container on Linux Home Edge :  
- Android application requests to Android HE : 
  - POST 
  - IP:56001/api/v1/orchestration/services
  - Body: 
    ```
    {
        "ServiceName": "hello-world",
        "ServiceInfo": [
        {
            "ExecutionType": "container",
            "ExecCmd": [
                "docker",
                "run",
                "-v", "/var/run:/var/run:rw",
                "hello-world"
            ]
        }]
    }
    ```

# License
The Edge Orchestration source code is distributed under the Apache License, Version 2.0 open source license.

# Contributing
If you want to contribute to the Edge Orchestration project and make it better, your help is very welcome. Contributing is also a great way to learn more about social coding on GitHub, new technologies and their ecosystems.
