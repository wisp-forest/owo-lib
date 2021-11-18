# oωo (owo-lib)

[![curseforge](https://img.shields.io/badge/-CurseForge-gray?style=for-the-badge&logo=curseforge&labelColor=orange)](https://www.curseforge.com/minecraft/mc-mods/owo-lib)
[![modrinth](https://img.shields.io/badge/-modrinth-gray?style=for-the-badge&labelColor=green&labelWidth=15&logo=appveyor&logoColor=white)](https://modrinth.com/mod/owo-lib)
[![release](https://img.shields.io/github/v/release/glisco03/owo-lib?logo=github&style=for-the-badge)](https://github.com/glisco03/owo-lib/releases)
[![discord](https://img.shields.io/discord/825828008644313089?label=wisp%20forest&logo=discord&logoColor=white&style=for-the-badge)](https://discord.gg/xrwHKktV2d)

## Overview

A general utility library for content-focused modding on Fabric. Particles, automatic registration, tabbed item groups and more

**Build Setup:**
```groovy
repositories {
    maven { url 'https://maven.wispforest.io' }
}
```
```groovy
dependencies {
    // Versions tagged with +1.18 onwards
    modImplementation "io.wispforest:owo-lib:${project.owo_version}"
    
    // Versions for 1.17
    modImplementation "com.glisco:owo-lib:${project.owo_version}"
}
```
You can check the latest version on the [Releases](https://github.com/glisco03/owo-lib/releases) page

owo currently has thorough documentation in the form of [Javadoc](https://docs.wispforest.io/javadoc/owo/) throught the entire codebase, a wiki with detailed instructions for each feature is under
construction over at https://docs.wispforest.io/owo/
 
## Features

This is by no means an exhaustive list, it only provides a rough overview

- A fully automatic registry system that is designed to be as generic as possible. It is simple and non-verbose to use for basic Minecraft registries but can just as well be used for any kind of custom system that you want to store values in class fields for.

- A custom implemetation of Item Groups which allows for sub-tabs inside your mod's group, removing the need to have 3 seperate groups for mods with many items

- The RegistryHelper, a simple and logical way to execute code (eg. registration) if and when one or multiple entries you specify are present in a registry. This makes inter-mod compatibility very quick to implement.

- A wrapper for vanilla's terrible particle system which makes spawning multiple particles with specified random distributions or precise geometric particles a breeze. This also includes a hassle-free system to trigger arbitrary particle events from the server without much packet setup.

- Common comparison and verification operations for ItemStacks to make handling them less painful

