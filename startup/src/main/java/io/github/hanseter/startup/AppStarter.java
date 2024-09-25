package io.github.hanseter.startup;

import io.github.hanseter.startup.api.MainWindowProvider;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import javafx.application.Application;
import javafx.application.Platform;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class AppStarter {

    private static final String JAVAFX_PACKAGES = String.join(",",
            "javafx.animation;uses:=\"javafx.beans,javafx.beans.property,javafx.beans.value,javafx.collections,javafx.event,javafx.geometry,javafx.scene,javafx.scene.paint,javafx.scene.shape,javafx.util\"",
            "javafx.application;uses:=\"javafx.stage\"",
            "javafx.beans",
            "javafx.beans.binding;uses:=\"javafx.beans,javafx.beans.property,javafx.beans.value,javafx.collections,javafx.util\"",
            "javafx.beans.property;uses:=\"javafx.beans,javafx.beans.binding,javafx.beans.value,javafx.collections,javafx.util\"",
            "javafx.beans.property.adapter;uses:=\"javafx.beans,javafx.beans.property,javafx.beans.value\"",
            "javafx.beans.value;uses:=\"javafx.beans,javafx.collections\"",
            "javafx.collections;uses:=\"javafx.beans,javafx.collections.transformation,javafx.util\"",
            "javafx.collections.transformation;uses:=\"javafx.beans,javafx.beans.property,javafx.collections\"",
            "javafx.concurrent;uses:=\"javafx.beans,javafx.beans.property,javafx.event,javafx.util\"",
            "javafx.css;uses:=\"javafx.beans,javafx.beans.property,javafx.beans.value,javafx.collections,javafx.geometry,javafx.scene.effect,javafx.scene.paint,javafx.scene.text\"",
            "javafx.event;uses:=\"javafx.beans\"",
            "javafx.fxml;uses:=\"javafx.beans,javafx.collections,javafx.util,sun.reflect\"",
            "javafx.geometry;uses:=\"javafx.beans,javafx.util\"",
            "javafx.print;uses:=\"javafx.beans,javafx.beans.property,javafx.collections,javafx.scene,javafx.stage\"",
            "javafx.scene;uses:=\"javafx.beans,javafx.beans.binding,javafx.beans.property,javafx.beans.value,javafx.collections,javafx.css,javafx.event,javafx.geometry,javafx.scene.effect,javafx.scene.image,javafx.scene.input,javafx.scene.paint,javafx.scene.transform,javafx.stage,javafx.util\"",
            "javafx.scene.canvas;uses:=\"javafx.beans.property,javafx.geometry,javafx.scene,javafx.scene.effect,javafx.scene.image,javafx.scene.paint,javafx.scene.shape,javafx.scene.text,javafx.scene.transform,javafx.util\"",
            "javafx.scene.chart;uses:=\"javafx.animation,javafx.beans,javafx.beans.binding,javafx.beans.property,javafx.collections,javafx.css,javafx.geometry,javafx.scene,javafx.scene.layout,javafx.scene.paint,javafx.scene.text,javafx.util\"",
            "javafx.scene.control;uses:=\"javafx.beans,javafx.beans.property,javafx.beans.value,javafx.collections,javafx.css,javafx.event,javafx.geometry,javafx.scene,javafx.scene.input,javafx.scene.layout,javafx.scene.paint,javafx.scene.text,javafx.stage,javafx.util\"",
            "javafx.scene.control.cell;uses:=\"javafx.beans,javafx.beans.property,javafx.beans.value,javafx.collections,javafx.scene.control,javafx.util\"",
            "javafx.scene.effect;uses:=\"javafx.beans.property,javafx.scene,javafx.scene.image,javafx.scene.paint,javafx.util\"",
            "javafx.scene.image;uses:=\"javafx.beans,javafx.beans.property,javafx.css,javafx.geometry,javafx.scene,javafx.scene.paint,javafx.util\"",
            "javafx.scene.input;uses:=\"javafx.beans,javafx.collections,javafx.event,javafx.geometry,javafx.scene,javafx.scene.image,javafx.util\"",
            "javafx.scene.layout;uses:=\"javafx.beans,javafx.beans.property,javafx.collections,javafx.css,javafx.geometry,javafx.scene,javafx.scene.image,javafx.scene.paint,javafx.scene.shape,javafx.scene.text,javafx.util\"",
            "javafx.scene.media;uses:=\"javafx.beans,javafx.beans.property,javafx.collections,javafx.event,javafx.geometry,javafx.scene,javafx.util\"",
            "javafx.scene.paint;uses:=\"javafx.animation,javafx.beans,javafx.beans.property,javafx.scene.image,javafx.util\"",
            "javafx.scene.shape;uses:=\"javafx.beans.property,javafx.collections,javafx.css,javafx.geometry,javafx.scene,javafx.scene.paint,javafx.util\"",
            "javafx.scene.text;uses:=\"javafx.beans,javafx.beans.property,javafx.css,javafx.geometry,javafx.scene,javafx.scene.layout,javafx.scene.paint,javafx.scene.shape,javafx.util\"",
            "javafx.scene.transform;uses:=\"javafx.beans,javafx.beans.property,javafx.event,javafx.geometry,javafx.scene,javafx.util\"",
            "javafx.scene.web;uses:=\"javafx.beans,javafx.beans.property,javafx.collections,javafx.concurrent,javafx.css,javafx.event,javafx.geometry,javafx.print,javafx.scene,javafx.scene.control,javafx.scene.text,javafx.util,org.w3c.dom\"",
            "javafx.stage;uses:=\"javafx.beans,javafx.beans.property,javafx.collections,javafx.event,javafx.geometry,javafx.scene,javafx.scene.image,javafx.scene.input,javafx.util\"",
            "javafx.util;uses:=\"javafx.beans\"",
            "javafx.util.converter;uses:=\"javafx.beans,javafx.util\"",
            "javafx.embed.swing",
            "com.sun.javafx.event",
            "com.sun.javafx.collections",
            "com.sun.javafx.css",
            "com.sun.javafx.css.converters",
            "com.sun.javafx.geom",
            "com.sun.javafx.runtime",
            "com.sun.javafx.scene.control.skin",
            "com.sun.javafx.scene.text",
            "com.sun.javafx.scene.traversal",
            "com.sun.javafx.tk",
            "com.sun.javafx.webkit",
            "com.sun.javafx.binding",
            "com.sun.webkit",
            "com.sun.glass.ui",
            "sun.misc",
            "com.sun.javafx.scene.control",
            "com.sun.javafx.scene.control.behavior",
            "com.sun.javafx.sg.prism",
            "com.sun.javafx.scene.input",
            "com.sun.javafx.geom.transform",
            "com.sun.javafx.scene",
            "com.sun.javafx.jmx",
            "com.sun.javafx.beans.event",
            "javafx.css.converter",
            "javafx.scene.control.skin",
            "com.sun.management",
            "com.sun.image.codec.jpeg",
            "sun.io",
            "com.sun.medialib.mlib",
            "sun.awt.image",
            "sun.awt.image.codec",
            "sun.security.action",
            "sun.reflect"
    );


    static Framework framework;

    private static final ServiceLoader<FrameworkFactory> FRAMEWORK_FACTORY_LOADER =
            ServiceLoader.load(FrameworkFactory.class);


    @SuppressWarnings("java:S2096")
    public static void main(String[] args) throws URISyntaxException {
        Path rootDir = getAppInstallDir();

        initOsgiFramework(rootDir);

        Application.launch(FxApp.class, args);
    }

    private static Path getAppInstallDir() throws URISyntaxException {
        return getJarLocation().getParent().getParent().getParent();
    }

    private static Path getJarLocation() throws URISyntaxException {
        return new File(
                AppStarter.class.getProtectionDomain().getCodeSource().getLocation()
                        .toURI()).toPath();
    }

    private static void initOsgiFramework(Path rootDir) {
        Map<String, String> felixArgs = createFelixArgs();
        framework = FRAMEWORK_FACTORY_LOADER.iterator().next().newFramework(felixArgs);
        Thread t = new Thread(() -> {
            try {
                framework.init();
                AutoProcessor autoProcessor = new AutoProcessor();
                autoProcessor.process(framework, felixArgs, rootDir, rootDir.resolve("userDir"));
                startAndWait(framework);
            } catch (IOException | BundleException e) {
                e.printStackTrace();
            }
        }, "startOsgiThread");
        t.start();
    }

    private static Map<String, String> createFelixArgs() {
        Map<String, String> felixArgs = new HashMap<>();

        felixArgs.put("felix.cache.rootdir", ".");
        felixArgs.put("org.osgi.framework.storage", "cache/felix");
        felixArgs.put("org.osgi.framework.storage.clean", "onFirstInit");
        felixArgs.put("felix.auto.deploy.action", "install,start");
        felixArgs.put("felix.log.level", "1");

        felixArgs.put("org.osgi.framework.system.packages.extra",
                "io.github.hanseter.startup.api," + JAVAFX_PACKAGES);
        return felixArgs;
    }

    static void startAndWait(Framework framework)
            throws BundleException {
        framework.start();
    }

    static void stopAndWait() throws BundleException, InterruptedException {
        framework.stop();
        framework.waitForStop(0);
    }

}
