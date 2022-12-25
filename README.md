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
    <a href="https://github.com/wisp-forest/owo-lib/releases">
        <img src="https://img.shields.io/github/v/release/glisco03/owo-lib?logo=github&style=for-the-badge">
    </a>
    <a href="https://discord.gg/xrwHKktV2d">
        <img src="https://img.shields.io/discord/825828008644313089?label=wisp%20forest&logo=discord&logoColor=white&style=for-the-badge">
    </a>
</h1>
    
## Overview

A general utility, GUI and config library for modding on Fabric. oωo is generally aimed at reducing code verbosity and making development more ergonomic. It covers a wide range of features from networking and serialization over GUI applications and configuration to data handling and registration. 

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
    modImplementation "io.wispforest:owo-lib:${project.owo_version}"
    // only if you plan to use owo-config
    annotationProcessor "io.wispforest:owo-lib:${project.owo_version}"
    
    // include this if you don't want force your users to install owo
    // sentinel will warn them and give the option to download it automatically
    include "io.wispforest:owo-sentinel:${project.owo_version}"
}
```

<details>
<summary><strong>Kotlin DSL</strong></summary>
    
```kotlin
repositories {
    maven("https://maven.wispforest.io")
}
    
dependencies {
    modImplementation("io.wispforest:owo-lib:${properties["owo_version"]}")
    // only if you plan to use owo-config
    annotationProcessor("io.wispforest:owo-lib:${properties["owo_version"]}")
    
    // include this if you don't want force your users to install owo
    // sentinel will warn them and give the option to download it automatically
    include("io.wispforest:owo-sentinel:${properties["owo_version"]}")
} 
```
    
</details>

You can check the latest version on the [Releases](https://github.com/wisp-forest/owo-lib/releases) page

owo is documented in two main ways:
 - There is rich, detailed JavaDoc throughout the entire codebase
 - There is a wiki with in-depth explanations and tutorials for most of owo's features over at https://docs.wispforest.io/owo/features/

## Features

This is by no means an exhaustive list, for a more complete overview head to https://docs.wispforest.io/owo/features/

 - [owo-ui](https://docs.wispforest.io/owo/ui/), a fully-featured declarative UI library for building dynamic, beautiful screens with blazingly fast development times
 - [owo-config](https://docs.wispforest.io/owo/config/), a built-in, customizable configuration system built on top of owo-ui. It provides many of the same features as [Cloth Config](https://modrinth.com/mod/cloth-config) while many new conveniences, like server-client config synchronization, added on top
 - A fully automatic [registration system](https://docs.wispforest.io/owo/registration/) that is designed to be as generic as possible. It is simple and non-verbose to use for basic registries, yet the underlying API tree is flexible and can also be used for many custom registration solutions
 - [Item Group extensions](https://docs.wispforest.io/owo/item-groups/) which allow for sub-tabs inside your mod's group as well as a host of other features like custom buttons, textures and item variant handling
 - A fully-featured [networking layer](https://docs.wispforest.io/owo/networking/) with fully automatic serialization, handshaking to ensure client compatibility and a built-in solution for triggering parametrized particle events in a side-agnostic manner
 - Client-sided particle helpers that allow for easily composing multi-particle effects
 - Rich text translations, allowing you to use Minecraft's text component format in your language files to provide styled text without any code
