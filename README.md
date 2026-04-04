# Shard

![Shard's logo](shard_logo.png)

## What It Is

Shard is a Java / LWJGL based game framework, with extensive rendering flexibility and multithreading.
It provides a simple API with optional core system customization.

## Why Use Shard

Shard provides you with low level control without enforcing it.

The framework has a simple API with which you can create applications and games quickly. <br>
However, Shard also provides you with many options to gain low level control through a clean and easy to use API.

Shard lets you touch core systems whenever required, such as the following:
- Easy render pipeline creation
- Custom batch allocation systems
- Extensive dependency injection

While having these options, Shard always provides at least one complete implementation.

This makes Shard the perfect framework for both:
- Developers just starting with engine design
- Experienced developers who want explicit low level control at certain points

## Features

Shard is still in an active state of development. <br>
Nonetheless, the following features are already functional in some extent.

<ul>
<li> 📬 <strong>Event System</strong> - Port based, hierachial event system with filtering and custom port support. </li>
<li> 🪟 <strong>Multiple Windows</strong> - GLFW based, one method call, automatic updates </li>
<li> 🧵 <strong>Multithreading</strong> - Engine can run on a seperate thread, GLFW has its own thread, worker threads </li>
<li> 📦 <strong>Mesh System</strong> - Easy mesh creation, custom attributes, easy mesh manipulation, automatic update detection</li>
<li> 🔀 <strong>Mesh batching and routing</strong> - Automatic mesh batching based on material, automatic reroute and updates on mesh changes</li>
<li> 🧩 <strong>Extensive dependency injection</strong> - Most systems provide dependency injection options, custom dependencies are widely supported </li>
</ul>

## Road Map

The following features are planned and will be implemented shortly.

<ul>
<li> 🎞️ <strong>Scene tree</strong> - Per window, custom ordering, relative positions </li>
<li> 🖌️ <strong>Post processing and render pipelines</strong> - Extensive and easy post processing, custom pipelines, custom shaders </li>
<li> 📷 <strong>Viewports</strong> - Seperate from scene or window architecture, easy pipeline addition, custom ordering, viewport packages </li>
</ul>

## Documentation

Every system has its own dedicated entry in the docs folder for detailed instructions on usage, common misconceptions and best practices.

## Getting Started

To get started, just add Shard as a .jar library (available in the releases) and write the following code. <br>
This code will create a window and start the engine on a seperate thread.

```
Window window;

window = Window.create(1920, 1080, "Hello Shard!");

Shard.run(true);
```
