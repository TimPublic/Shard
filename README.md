# Shard
## What It Is
Shard is a Java / LWJGL based game framework, with extensive rendering flexibility and multithreading. It provides a simple API with optional core system customization.
## Why Use Shard
Shard provides you with low level control without enforcing it.
The framework has a simple API with which you can create applications and games quickly. However, Shard also provides you with many options to gain low level control through a clean and easy to use API.
Shard lets you touch core systems whenever required, such as the following:
* Easy render pipeline creation
* Custom batch allocation systems
* Extensive dependency injection
While having these options, Shard always provides at least one complete implementation.
This makes Shard the perfect framework for both:
* Developers just starting with engine design
* Experienced developers who want explicit low level control at certain points
## Features
Shard is still in an active state of development. Nonetheless, the following features are already functional in some extent.
* 📬 Event System - Port based, hierachial event system with filtering and custom port support.
* 🪟 Multiple Windows - GLFW based, one method call, automatic updates
* 🧵 Multithreading - Engine can run on a seperate thread, GLFW has its own thread, worker threads
* 📦 Mesh System - Easy mesh creation, custom attributes, easy mesh manipulation, automatic update detection
* 🔀 Mesh batching and routing - Automatic mesh batching based on material, automatic reroute and updates on mesh changes
* 🧩 Extensive dependency injection - Most systems provide dependency injection options, custom dependencies are widely supported
## Road Map
The following features are planned and will be implemented shortly.
* 🎞️ Scene tree - Per window, custom ordering, relative positions
* 🖌️ Post processing and render pipelines - Extensive and easy post processing, custom pipelines, custom shaders
* 📷 Viewports - Seperate from scene or window architecture, easy pipeline addition, custom ordering, viewport packages
## Documentation
Every system has its own dedicated entry in the docs folder for detailed instructions on usage, common misconceptions and best practices.
## Getting Started
To get started, just add Shard as a .jar library (available in the releases) and write the following code. This code will create a window and start the engine on a seperate thread.

```
Window window;

window = Window.create(1920, 1080, "Hello Shard!");

Shard.run(true);
```
