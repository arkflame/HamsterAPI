package dev._2lstudios.hamsterapi.injector;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;

import dev._2lstudios.hamsterapi.utils.Reflection;
import io.netty.channel.ChannelFuture;;

public class HamsterInjector {
    private final List<ChannelFuture> injectedFutures = new ArrayList<>();
    private final List<Pair<Field, Object>> injectedLists = new ArrayList<>();
    private final Reflection reflection;
    private final Logger logger;
    private Object serverConnection = null;

    public static Object getServerConnection() throws Exception {
        if (serverConnection != null) {
            return serverConnection;
        }

        Class<?> serverClass = reflection.getNMSClass("MinecraftServer");
        Object server = serverClass.getMethod("getServer").invoke(null);

        for (final Method method : serverClass.getDeclaredMethods()) {
            if (method.getReturnType() != null) {
                if (method.getReturnType().getSimpleName().equals("ServerConnection")) {
                    if (method.getParameterTypes().length == 0) {
                        return serverConnection = method.invoke(server);
                    }
                }
            }
        }
    }

    private boolean isChannelFutureList(final List<?> list) {
        for (Object o : (List) value) {
            return o instanceof ChannelFuture;
        }

        return false;
    }

    @Override
    public void inject() throws Exception {
        try {
            Object connection = getServerConnection();

            if (connection == null) {
                throw new Exception(
                        "We failed to find the core component 'ServerConnection', please file an issue on our GitHub.");
            }

            for (Field field : connection.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(connection);

                if (value instanceof List) {
                    if (isChannelFutureList(value)) {
                        List wrapper = new ListWrapper((List) value) {
                            @Override
                            public void handleAdd(Object o) {
                                try {
                                    injectChannelFuture((ChannelFuture) o);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };

                        for (Object o : (List) value) {
                            injectChannelFuture((ChannelFuture) o);
                        }

                        injectedLists.add(new Pair<>(field, connection));
                        field.set(connection, wrapper);
                    }
                }
            }
        } catch (Exception e) {
            logger.severe(
                    "Unable to inject ViaVersion, please post these details on our GitHub and ensure you're using a compatible server version.");
            throw e;
        }
    }

    private void injectChannelFuture(ChannelFuture future) throws Exception {
        try {
            List<String> names = future.channel().pipeline().names();
            ChannelHandler bootstrapAcceptor = null;
            // Pick best
            for (String name : names) {
                ChannelHandler handler = future.channel().pipeline().get(name);
                try {
                    ReflectionUtil.get(handler, "childHandler", ChannelInitializer.class);
                    bootstrapAcceptor = handler;
                } catch (Exception e) {
                    // Not this one
                }
            }
            // Default to first (Also allows blame to work)
            if (bootstrapAcceptor == null) {
                bootstrapAcceptor = future.channel().pipeline().first();
            }
            try {
                ChannelInitializer<Channel> oldInit = ReflectionUtil.get(bootstrapAcceptor, "childHandler",
                        ChannelInitializer.class);
                ChannelInitializer newInit = new BukkitChannelInitializer(oldInit);

                ReflectionUtil.set(bootstrapAcceptor, "childHandler", newInit);
                injectedFutures.add(future);
            } catch (NoSuchFieldException e) {
                // let's find who to blame!
                ClassLoader cl = bootstrapAcceptor.getClass().getClassLoader();
                if (cl.getClass().getName().equals("org.bukkit.plugin.java.PluginClassLoader")) {
                    PluginDescriptionFile yaml = ReflectionUtil.get(cl, "description", PluginDescriptionFile.class);
                    throw new Exception("Unable to inject, due to " + bootstrapAcceptor.getClass().getName()
                            + ", try without the plugin " + yaml.getName() + "?");
                } else {
                    throw new Exception(
                            "Unable to find core component 'childHandler', please check your plugins. issue: "
                                    + bootstrapAcceptor.getClass().getName());
                }

            }
        } catch (Exception e) {
            logger.severe("We failed to inject ViaVersion, have you got late-bind enabled with something else?");
            throw e;
        }
    }

    @Override
    public void uninject() throws Exception {
        for (ChannelFuture future : injectedFutures) {
            List<String> names = future.channel().pipeline().names();
            ChannelHandler bootstrapAcceptor = null;
            // Pick best
            for (String name : names) {
                ChannelHandler handler = future.channel().pipeline().get(name);
                try {
                    ChannelInitializer<Channel> oldInit = ReflectionUtil.get(handler, "childHandler",
                            ChannelInitializer.class);
                    if (oldInit instanceof BukkitChannelInitializer) {
                        bootstrapAcceptor = handler;
                    }
                } catch (Exception e) {
                    // Not this one
                }
            }
            // Default to first
            if (bootstrapAcceptor == null) {
                bootstrapAcceptor = future.channel().pipeline().first();
            }

            try {
                ChannelInitializer<Channel> oldInit = ReflectionUtil.get(bootstrapAcceptor, "childHandler",
                        ChannelInitializer.class);
                if (oldInit instanceof BukkitChannelInitializer) {
                    ReflectionUtil.set(bootstrapAcceptor, "childHandler",
                            ((BukkitChannelInitializer) oldInit).getOriginal());
                }
            } catch (Exception e) {
                logger.severe("Failed to remove injection handler, reload won't work with connections, please reboot!");
            }
        }
        injectedFutures.clear();

        for (Pair<Field, Object> pair : injectedLists) {
            try {
                Object o = pair.getKey().get(pair.getValue());
                if (o instanceof ListWrapper) {
                    pair.getKey().set(pair.getValue(), ((ListWrapper) o).getOriginalList());
                }
            } catch (IllegalAccessException e) {
                logger.severe("Failed to remove injection, reload won't work with connections, please reboot!");
            }
        }

        injectedLists.clear();
    }

    @Override
    public boolean lateProtocolVersionSetting() {
        return true;
    }

    @Override
    public int getServerProtocolVersion() throws Exception {
        if (PaperViaInjector.PAPER_PROTOCOL_METHOD) {
            // noinspection deprecation
            return Bukkit.getUnsafe().getProtocolVersion();
        }

        try {
            // Grab a static instance of the server
            Class<?> serverClass = reflection.getNMSClass("MinecraftServer");
            Object server = serverClass.getMethod("getServer").invoke(null);

            // Grab the ping class and find the field to access it
            Class<?> pingClass = NMSUtil.nms("ServerPing");
            Object ping = null;
            // Search for ping method
            for (Field f : serverClass.getDeclaredFields()) {
                if (f.getType() != null) {
                    if (f.getType().getSimpleName().equals("ServerPing")) {
                        f.setAccessible(true);
                        ping = f.get(server);
                    }
                }
            }
            if (ping != null) {
                Object serverData = null;
                for (Field f : pingClass.getDeclaredFields()) {
                    if (f.getType() != null) {
                        if (f.getType().getSimpleName().endsWith("ServerData")) {
                            f.setAccessible(true);
                            serverData = f.get(ping);
                        }
                    }
                }
                if (serverData != null) {
                    int protocolVersion = -1;
                    for (Field f : serverData.getClass().getDeclaredFields()) {
                        if (f.getType() != null) {
                            if (f.getType() == int.class) {
                                f.setAccessible(true);
                                protocolVersion = (int) f.get(serverData);
                            }
                        }
                    }
                    if (protocolVersion != -1) {
                        return protocolVersion;
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to get server", e);
        }
        throw new Exception("Failed to get server");
    }

    @Override
    public String getEncoderName() {
        return "encoder";
    }

    @Override
    public String getDecoderName() {
        return protocolLib ? "protocol_lib_decoder" : "decoder";
    }

    public static boolean isBinded() {
        if (PaperViaInjector.PAPER_INJECTION_METHOD)
            return true;
        try {
            Object connection = getServerConnection();
            if (connection == null) {
                return false;
            }
            for (Field field : connection.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                final Object value = field.get(connection);
                if (value instanceof List) {
                    // Inject the list
                    synchronized (value) {
                        for (Object o : (List) value) {
                            if (o instanceof ChannelFuture) {
                                return true;
                            } else {
                                break; // not the right list.
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public JsonObject getDump() {
        JsonObject data = new JsonObject();

        // Generate information about current injections
        JsonArray injectedChannelInitializers = new JsonArray();
        for (ChannelFuture cf : injectedFutures) {
            JsonObject info = new JsonObject();
            info.addProperty("futureClass", cf.getClass().getName());
            info.addProperty("channelClass", cf.channel().getClass().getName());

            // Get information about the pipes for this channel future
            JsonArray pipeline = new JsonArray();
            for (String pipeName : cf.channel().pipeline().names()) {
                JsonObject pipe = new JsonObject();
                pipe.addProperty("name", pipeName);
                if (cf.channel().pipeline().get(pipeName) != null) {
                    pipe.addProperty("class", cf.channel().pipeline().get(pipeName).getClass().getName());
                    try {
                        Object child = ReflectionUtil.get(cf.channel().pipeline().get(pipeName), "childHandler",
                                ChannelInitializer.class);
                        pipe.addProperty("childClass", child.getClass().getName());
                        if (child instanceof BukkitChannelInitializer) {
                            pipe.addProperty("oldInit",
                                    ((BukkitChannelInitializer) child).getOriginal().getClass().getName());
                        }
                    } catch (Exception e) {
                        // Don't display
                    }
                }
                // Add to the pipeline array
                pipeline.add(pipe);
            }
            info.add("pipeline", pipeline);

            // Add to the list
            injectedChannelInitializers.add(info);
        }
        data.add("injectedChannelInitializers", injectedChannelInitializers);

        // Generate information about lists we've injected into
        JsonObject wrappedLists = new JsonObject();
        JsonObject currentLists = new JsonObject();
        try {
            for (Pair<Field, Object> pair : injectedLists) {
                Object list = pair.getKey().get(pair.getValue());
                // Note down the current value (could be overridden by another plugin)
                currentLists.addProperty(pair.getKey().getName(), list.getClass().getName());
                // Also if it's not overridden we can display what's inside our list (possibly
                // another plugin)
                if (list instanceof ListWrapper) {
                    wrappedLists.addProperty(pair.getKey().getName(),
                            ((ListWrapper) list).getOriginalList().getClass().getName());
                }
            }
            data.add("wrappedLists", wrappedLists);
            data.add("currentLists", currentLists);
        } catch (Exception e) {
            // Ignored, fields won't be present
        }

        data.addProperty("binded", isBinded());
        return data;
    }

    public static void patchLists() throws Exception {
        if (PaperViaInjector.PAPER_INJECTION_METHOD)
            return;

        Object connection = getServerConnection();
        if (connection == null) {
            logger.warning(
                    "We failed to find the core component 'ServerConnection', please file an issue on our GitHub.");
            return;
        }

        for (Field field : connection.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(connection);
            if (!(value instanceof List))
                continue;
            if (value instanceof ConcurrentList)
                continue;

            ConcurrentList list = new ConcurrentList();
            list.addAll((Collection) value);
            field.set(connection, list);
        }
    }

    public void setProtocolLib(boolean protocolLib) {
        this.protocolLib = protocolLib;
    }
}