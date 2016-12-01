[![Logo](/logo.png)](https://www.spigotmc.org/resources/shinyitems-1-8-x-1-12-x.12531/)

ShinyItems
==========
Enlighten your way with shiny items.  
Spigot plugin for Minecraft server 1.8, 1.9, 1.10, 1.11.

Original author: [sipsi133](https://github.com/sipsi133)  
[SPIGOT RESOURCE](https://www.spigotmc.org/resources/shinyitems-1-8-x-1-12-x.12531/)

*License, source code and commits was reconstructed from [jar files](https://www.spigotmc.org/resources/shinyitems-1-8-x-1-12-x.12531/history).*

## License

>The MIT License (MIT)
>
>Copyright (c) 2016 sipsi133
>
>Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
>files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
>merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
>furnished to do so, subject to the following conditions:
>
>The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
>
>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
>BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
>NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
>DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
>OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Dependencies

- [Maven](https://maven.apache.org) (version 3.0 or higher)
- [Spigot](https://www.spigotmc.org/)
- [LightAPI](https://www.spigotmc.org/resources/lightapi.4510/)

## Instructions to build

1. Build CraftBukkit server using [BuildToolsJar.jar](https://hub.spigotmc.org/jenkins/job/BuildTools/):
   `java -jar BuildTools.jar --rev 1.11`  
   *It will be added to your local maven repository.*
1. Download the appropriate version of [LightAPI](https://www.spigotmc.org/resources/lightapi.4510/)
   to the [lib](/lib) folder ([more info](/lib/readme.md))
1. Run the command from the root folder of the project: `mvn clear install`
