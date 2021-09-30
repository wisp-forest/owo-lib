A general utility library for content-focused modding on Fabric. Particles, automatic registration, tabbed item groups and more

**Build Setup:**
```
repositories {
    maven { url 'https://jitpack.io' }
}
```
```
dependencies {
    modImplementation "com.github.glisco03:owo-lib:${project.owo_version}"
    
    //This is optional, but recommended so users dont have to install *another* library
    include "com.github.glisco03:owo-lib:${project.owo_version}"
}
```
You can check the latest version on the [Releases](https://github.com/glisco03/owo-lib/releases) page
 
***

Features include:

- A fully automatic registry system that is designed to be as generic as possible. It is simple and non-verbose to use for basic Minecraft registries but can just as well be used for any kind of custom system that you want to store values in class fields for.

- A custom implemetation of Item Groups which allows for sub-tabs inside your mod's group, removing the need to have 3 seperate groups for mods with many items

- The RegistryHelper, a simple and logical way to execute code (eg. registration) if and when one or multiple entries you specify are present in a registry. This makes inter-mod compatibility very quick to implement.

- A wrapper for vanilla's terrible particle system which makes spawning multiple particles with specified random distributions or precise geometric particles a breeze. This also includes a hassle-free system to trigger arbitrary particle events from the server without much packet setup.

- Common comparison and verification operations for ItemStacks to make handling them less painful

***

owo currently has thorough documentation in the form of Javadoc throught the entire codebase, a wiki with detailed instructions for each feature is on it's way
