<p align="center">
<img src="https://raw.githubusercontent.com/2LStudios-MC/HamsterAPI/main/docs/logo.png?token=AKWYIEBFNWCVUX7SOORJPD27R6OKI">
</p>

<h1 align="center">Documentation</h1>

### Get started
The first step is to get the HamsterAPI instance to access most of the methods of the API with the static method:
```java
HamsterAPI.getInstance()
```

[Developers] Don't forget to place HamsterAPI as a dependency in your "plugin.yml" file

### From the HamsterAPI instance you can access the following methods:
**getBufferIO()** - Util to split/decompress/decode bytebufs into packets.  
**getBungeeMessenger()** - Util to ease CustomPayload communication with BungeeCord.  
**getReflection()** - Optimized util to get NMS and CraftBukkit classes without losing compatibility.  
**getPacketInjector()** - Util to inject/remove HamsterAPI from the pipeline of a player.  

### You can listen to the following events:
- PacketDecodeEvent
- PacketReceiveEvent
- PacketSendEvent
  
**Decode** is ran after splitter & decompress on the pipeline and lets you access to a bytebuf object.  
You can decode it with BufferIO to get a PacketWrapper object. (Perfect for exploit fixing and security stuff)  

**Receive/Send** is ran after decode on the pipeline and lets you access to a PacketWrapper object. (Perfect for cosmetic/common stuff)  
  
To know how to listen to Events please read the (https://www.spigotmc.org/wiki/using-the-event-api/)[following guide]

### Send Title
```java
HamsterAPI.getInstance().getHamsterPlayerManager().get(player)
  .sendTitle(String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime);
```

### Send Action-bar
```java
HamsterAPI.getInstance().getHamsterPlayerManager().get(player)
  .sendActionbar(String message);
```

### Safe Disconnect
```java
HamsterAPI.getInstance().getHamsterPlayerManager().get(player).disconnect(String reason);
```

### Close Connection (Instant kick)
```java
HamsterAPI.getInstance().getHamsterPlayerManager().get(player).closeChannel();
```

### Sends to another Bungeecord Server
```java
HamsterAPI.getInstance().getHamsterPlayerManager().get(player).sendServer(String serverName);
```
