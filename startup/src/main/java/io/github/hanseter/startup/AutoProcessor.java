
package io.github.hanseter.startup;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.startlevel.FrameworkStartLevel;

class AutoProcessor {

    /**
     * The property name used for the bundle directory.
     */
    public static final String AUTO_DEPLOY_DIR_PROPERY = "felix.auto.deploy.dir";
    /**
     * The default name used for the bundle directory.
     */
    public static final String AUTO_DEPLOY_DIR_VALUE = "bundle";
    /**
     * The property name used to specify auto-deploy start level.
     */
    public static final String AUTO_DEPLOY_STARTLEVEL_PROPERY = "felix.auto.deploy.startlevel";
    /**
     * The property name prefix for the launcher's auto-install property.
     */
    public static final String AUTO_INSTALL_PROP = "felix.auto.install";
    /**
     * The property name prefix for the launcher's auto-start property.
     */
    public static final String AUTO_START_PROP = "felix.auto.start";

    public void process(Framework framework, Map<String, String> configMap, Path installDirPath,
            Path userDirPath)
            throws IOException {
        BundleContext frameworkContext = framework.getBundleContext();
        FrameworkStartLevel fsl = framework.adapt(FrameworkStartLevel.class);
        processAutoDeploy(configMap, frameworkContext, fsl, installDirPath, userDirPath);
        processAutoProperties(configMap, frameworkContext, fsl);
    }

    private void getAutoDeployBundlePaths(Path autoDirPath, final List<Path> jarPaths)
            throws IOException {
        if (Files.exists(autoDirPath)) {
            Files.walkFileTree(autoDirPath, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    Path fileName = file.getFileName();
                    if (fileName != null && fileName.toString().endsWith(".jar")) {
                        jarPaths.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private void processAutoDeploy(Map<String, String> configMap, BundleContext frameworkContext,
            FrameworkStartLevel fsl,
            Path installDirPath, Path userDirPath) throws IOException {
        // Perform auto-deploy actions.
        int startLevel = getStartLevel(fsl, configMap);

        Map<String, Bundle> uninstallBundlesMap = getInstalledBundlesMap(frameworkContext);

        List<Path> jarPaths = getAutoDeployBundlePaths(configMap, installDirPath, userDirPath);

        List<Bundle> startBundleList = new ArrayList<>();
        for (Path jarPath : jarPaths) {
            Bundle b = uninstallBundlesMap.remove(jarPath.toUri().toString());

            try {
                // If the bundle is not already installed, then install it
                // if the 'install' action is present.
                if ((b == null)) {
                    b = frameworkContext.installBundle(jarPath.toUri().toString());
                } else {
                    // If the bundle is already installed, then update it
                    // if the 'update' action is present.
                    b.update();
                }

                // If we have found and/or successfully installed a bundle,
                // then add it to the list of bundles to potentially start
                // and also set its start level accordingly.
                if ((b != null) && !isFragment(b)) {
                    startBundleList.add(b);
                    setStartLevel(b, startLevel);
                }
            } catch (BundleException ex) {
                ex.printStackTrace();
            }
        }

        startBundles(startBundleList);

    }

    private void processAutoProperties(Map<String, String> configMap,
            BundleContext frameworkContext,
            FrameworkStartLevel fsl) {
        installAutoInstallBundles(configMap, frameworkContext, fsl);
        startAutoStartBundles(configMap, frameworkContext);

    }

    private void installAutoInstallBundles(Map<String, String> configMap,
            BundleContext frameworkContext,
            FrameworkStartLevel fsl) {
        configMap.keySet().stream().
                map(String::toLowerCase).
                filter(key -> (key.startsWith(AUTO_INSTALL_PROP) || key.startsWith(
                        AUTO_START_PROP))).
                forEach(key -> {
                    int startLevel = getAutoStartLevel(fsl, key);

                    StringTokenizer st = createStringTokenizer(configMap.get(key));
                    for (String location = nextLocation(st); location != null;
                            location = nextLocation(st)) {
                        installBundle(location, frameworkContext, startLevel);
                    }
                });
    }

    private void startAutoStartBundles(Map<String, String> configMap,
            BundleContext frameworkContext) {
        configMap.keySet().stream().
                map(String::toLowerCase).
                filter(key -> (key.startsWith(AUTO_START_PROP))).
                map(key -> createStringTokenizer(configMap.get(key))).
                forEach(st -> {
                    for (String location = nextLocation(st); location != null;
                            location = nextLocation(st)) {
                        startBundle(frameworkContext, location);
                    }
                });
    }

    private static StringTokenizer createStringTokenizer(String value) {
        return new StringTokenizer(value, "\" ", true);
    }

    private int getAutoStartLevel(FrameworkStartLevel fsl, String key) {

        int startLevel = fsl.getInitialBundleStartLevel();
        if (!key.equals(AUTO_INSTALL_PROP) && !key.equals(AUTO_START_PROP)) {
            try {
                startLevel = Integer.parseInt(key.substring(key.lastIndexOf('.') + 1));
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
        return startLevel;
    }

    private void installBundle(String location, BundleContext frameworkContext, int startLevel) {
        try {
            Bundle b = frameworkContext.installBundle(location, null);
            setStartLevel(b, startLevel);
        } catch (BundleException | RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    private void startBundles(List<Bundle> startBundleList) {
        startBundleList.forEach(bundle -> {
            try {
                bundle.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void setStartLevel(Bundle bundle, int startLevel) {
        BundleStartLevel bsl = bundle.adapt(BundleStartLevel.class);
        bsl.setStartLevel(startLevel);
    }

    @SuppressWarnings("java:S2221")
    private void startBundle(BundleContext frameworkContext, String location) {
        // Installing twice just returns the same bundle.
        try {
            Bundle b = frameworkContext.installBundle(location, null);
            if (b != null) {
                b.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int getStartLevel(FrameworkStartLevel fsl, Map<String, String> configMap) {
        int startLevel = fsl.getInitialBundleStartLevel();
        if (configMap.get(AUTO_DEPLOY_STARTLEVEL_PROPERY) != null) {
            try {
                startLevel = Integer.parseInt(
                        configMap.get(AUTO_DEPLOY_STARTLEVEL_PROPERY));
            } catch (NumberFormatException ex) {
                // Ignore and keep default level.
            }
        }
        return startLevel;
    }

    private List<Path> getAutoDeployBundlePaths(Map<String, String> configMap, Path installDirPath,
            Path userDirPath)
            throws IOException {
        String autoDir = getAutoDeployDir(configMap);
        final List<Path> jarPaths = new ArrayList<>();

        getAutoDeployBundlePaths(installDirPath.resolve(autoDir), jarPaths);
        getAutoDeployBundlePaths(userDirPath.resolve(autoDir), jarPaths);

        Collections.sort(jarPaths);
        return jarPaths;
    }

    private String getAutoDeployDir(Map<String, String> configMap) {
        String autoDir = configMap.get(AUTO_DEPLOY_DIR_PROPERY);
        if (autoDir == null) {
            autoDir = AUTO_DEPLOY_DIR_VALUE;
        }
        return autoDir;
    }

    private Map<String, Bundle> getInstalledBundlesMap(BundleContext frameworkContext) {
        return Arrays.stream(frameworkContext.getBundles()).
                collect(Collectors.toMap(Bundle::getLocation, bundle -> bundle));
    }

    @SuppressWarnings("java:S3776")
    private String nextLocation(StringTokenizer st) {
        String retVal = null;

        if (st.countTokens() > 0) {
            String tokenList = "\" ";
            StringBuilder tokBuf = new StringBuilder(10);
            boolean inQuote = false;
            boolean tokStarted = false;
            boolean exit = false;
            while ((st.hasMoreTokens()) && (!exit)) {
                String tok = st.nextToken(tokenList);
                switch (tok) {
                    case "\"":
                        inQuote = !inQuote;
                        if (inQuote) {
                            tokenList = "\"";
                        } else {
                            tokenList = "\" ";
                        }
                        break;
                    case " ":
                        if (tokStarted) {
                            retVal = tokBuf.toString();
                            tokStarted = false;
                            tokBuf = new StringBuilder(10);
                            exit = true;
                        }
                        break;
                    default:
                        tokStarted = true;
                        tokBuf.append(tok.trim());
                        break;
                }
            }

            // Handle case where end of token stream and
            // still got data
            if ((!exit) && (tokStarted)) {
                retVal = tokBuf.toString();
            }
        }

        return retVal;
    }

    private boolean isFragment(Bundle bundle) {
        return bundle.getHeaders().get(Constants.FRAGMENT_HOST) != null;
    }
}

