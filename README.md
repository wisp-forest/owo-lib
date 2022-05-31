<h1 align="center">
    <img src="https://i.imgur.com/VXjFso4.png">
    <br>
    oωo (owo-lib)
    <br>
    <a href="https://www.curseforge.com/minecraft/mc-mods/owo-lib">
        <img src="https://img.shields.io/badge/-CurseForge-gray?style=for-the-badge&logo=curseforge&labelColor=orange">
    </a>
    <a href="https://modrinth.com/mod/owo-lib">
        <img src="https://img.shields.io/badge/-modrinth-gray?style=for-the-badge&labelColor=green&labelWidth=15&logo=appveyor&logoColor=white">
    </a>
    <br>
    <a href="https://github.com/glisco03/owo-lib/releases">
        <img src="https://img.shields.io/github/v/release/glisco03/owo-lib?logo=github&style=for-the-badge">
    </a>
    <a href="https://discord.gg/xrwHKktV2d">
        <img src="https://img.shields.io/discord/825828008644313089?label=wisp%20forest&logo=discord&logoColor=white&style=for-the-badge">
    </a>
</h1>
    
## Overview

A general utility library for content-focused modding on Fabric. oωo is generally aimed at reducing code verbosity and making developement more ergonomic. It covers a wide range of applications from networking and serialization over data handling and registration. 

**Build Setup:**
```properties
# https://maven.wispforest.io/io/wispforest/owo-lib/
owo_version=...
```

```groovy
repositories {
    maven { url 'https://maven.wispforest.io' }
}

<...>

dependencies {
    // Versions tagged with +1.18 onwards
    modImplementation "io.wispforest:owo-lib:${project.owo_version}"
    
    // include this if you don't want force your users to install owo
    // sentinel will warn them and give the option to download it automatically
    include "io.wispforest:owo-sentinel:${project.owo_version}"
    
    // Versions for 1.17
    modImplementation "com.glisco:owo-lib:${project.owo_version}"
}
```
You can check the latest version on the [Releases](https://github.com/glisco03/owo-lib/releases) page

owo currently has thorough documentation in the form of [Javadoc](https://docs.wispforest.io/javadoc/owo/) throughout the entire codebase, a wiki with detailed instructions for each feature is under construction over at https://docs.wispforest.io/owo/
 
## Features

This is by no means an exhaustive list, for a more complete overview head to https://docs.wispforest.io/owo/features/

- A fully automatic [registration system](https://docs.wispforest.io/owo/registration/) that is designed to be as generic as possible. It is simple and non-verbose to use for basic registries, yet the underlying API tree is flexible and can also be used for many custom registration solutions

- Item Groups extensions which allow for sub-tabs inside your mod's group as well as a host of other features like custom buttons, textures and item variant handling

- A fully-featured networking layer with fully automatic serialization, handshaking to ensure client compatibility and a built-in solution for triggering parametrized particle events in a side-agnostic manner

- Client-sided particle helpers that allow for easily composing multi-particle effects 

- Common comparison and verification operations for item stacks, which can save lots of `if...else` blocks and implement missing functionality like checking if two stacks can stack onto each other 
