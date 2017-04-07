# clusterlib
Version 0.5

This is the library, which is developed as a cluster server-client library. It's writed on Java, using NIOAsync as the core
to communicate between server and clients. Currently, there is a few features in the project:  
- Listner Module: listener is an interface to registry with NIOAsync core to listen messages. Listeners will be seperated by Channel, such as a Listener A will registry on Channel ABC, only messages has channel ABC will be sent to this Listener. The channel is represent in the first 32 bytes of message.
- Http Module: this module is based on Netty 4.0. Thus, I implement a way to easily add more API-endpoint as Controller,just   like in MVC Spring concept. It will load Controllers by package names in configuration files.
- Scheduler Module: this module is to make cron job run, you only need to add Annotation on method you want it to be a cron job and config the package names to load on start-up.
- Some convention utilities: ShutdownHook to shutdown all the threads you has registried to this class, ThreadPoolUtil to run all the threads, whichs you don't want to manage it by yourself (This class will also registry to ShutdownHook to clean up)

