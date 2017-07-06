package com.dmajewski.gow.windows.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.slf4j.LoggerFactory;

import net.sourceforge.tess4j.util.LoggHelper;

public class JarUtils {

    private static final String VFS_PROTOCOL = "vfs";
    public static final String GOW_TEMP_DIR = new File(System.getProperty("java.io.tmpdir"), "gow").getPath();

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(new LoggHelper().toString());

    /**
     * Extracts tesseract resources to temp folder.
     *
     * @param resourceName name of file or directory
     * @return target path, which could be file or directory
     */
    public static synchronized File extractJarResources(String resourceName) {
        File targetPath = null;
        URLClassLoader urlClassLoader;
        try {
            targetPath = new File(GOW_TEMP_DIR, resourceName);
            org.apache.commons.io.FileUtils.deleteDirectory(targetPath);
            
            Files.createDirectories(targetPath.toPath());
 
            URL url = JarUtils.class.getProtectionDomain().getCodeSource().getLocation();
            
            urlClassLoader = new URLClassLoader(new URL[] {url});
			Enumeration<URL> resources = urlClassLoader.getResources(resourceName);
                        
            while (resources.hasMoreElements()) {
                URL resourceUrl = resources.nextElement();
                logger.debug("copy: {}", resourceUrl);
                copyResources(resourceUrl, targetPath);
            }
            urlClassLoader.close();
        } catch (IOException | URISyntaxException e) {
            logger.warn(e.getMessage(), e);
        }

        return targetPath;
    }

    /**
     * Copies resources to target folder.
     *
     * @param resourceUrl
     * @param targetPath
     * @return
     */
    static void copyResources(URL resourceUrl, File targetPath) throws IOException, URISyntaxException {
        if (resourceUrl == null) {
            return;
        }

        URLConnection urlConnection = resourceUrl.openConnection();

        logger.debug("Processing {}", urlConnection);
        
        /**
         * Copy resources either from inside jar or from project folder.
         */
        if (urlConnection instanceof JarURLConnection) {
            copyJarResourceToPath((JarURLConnection) urlConnection, targetPath);
        } else if (VFS_PROTOCOL.equals(resourceUrl.getProtocol())) {
            VirtualFile virtualFileOrFolder = VFS.getChild(resourceUrl.toURI());
            copyFromWarToFolder(virtualFileOrFolder, targetPath);
        } else if("file".equals(resourceUrl.getProtocol())){
            File file = new File(resourceUrl.getPath());
            logger.debug("Processing {}", file);
            if (file.isDirectory()) {
                org.apache.commons.io.FileUtils.copyDirectory(file, targetPath);
            } else {
            	org.apache.commons.io.FileUtils.copyFile(file, targetPath);
            }
        }
    }

    /**
     * Copies resources from the jar file of the current thread and extract it
     * to the destination path.
     *
     * @param jarConnection
     * @param destPath destination file or directory
     */
    static void copyJarResourceToPath(JarURLConnection jarConnection, File destPath) {
        try (JarFile jarFile = jarConnection.getJarFile()) {
            String jarConnectionEntryName = jarConnection.getEntryName();
            logger.debug("jarConnectionEntryName: {}", jarConnectionEntryName);


            /**
             * Iterate all entries in the jar file.
             */
            for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();) {
                JarEntry jarEntry = e.nextElement();
                String jarEntryName = jarEntry.getName();
                logger.debug("jarEntryName: {}", jarEntryName);

                /**
                 * Extract files only if they match the path.
                 */
                if (jarEntryName.startsWith(jarConnectionEntryName)) {
                    String filename = jarEntryName.substring(jarConnectionEntryName.length());
                    File currentFile = new File(destPath, filename);

                    if (jarEntry.isDirectory()) {
                        currentFile.mkdirs();
                    } else {
                        currentFile.deleteOnExit();
                        try (InputStream is = jarFile.getInputStream(jarEntry);
                                OutputStream out = org.apache.commons.io.FileUtils.openOutputStream(currentFile)) {
                            IOUtils.copy(is, out);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    /**
     * Copies resources from WAR to target folder.
     *
     * @param virtualFileOrFolder
     * @param targetFolder
     * @throws IOException
     */
    static void copyFromWarToFolder(VirtualFile virtualFileOrFolder, File targetFolder) throws IOException {
        if (virtualFileOrFolder.isDirectory() && !virtualFileOrFolder.getName().contains(".")) {
            if (targetFolder.getName().equalsIgnoreCase(virtualFileOrFolder.getName())) {
                for (VirtualFile innerFileOrFolder : virtualFileOrFolder.getChildren()) {
                    copyFromWarToFolder(innerFileOrFolder, targetFolder);
                }
            } else {
                File innerTargetFolder = new File(targetFolder, virtualFileOrFolder.getName());
                innerTargetFolder.mkdir();
                for (VirtualFile innerFileOrFolder : virtualFileOrFolder.getChildren()) {
                    copyFromWarToFolder(innerFileOrFolder, innerTargetFolder);
                }
            }
        } else {
        	org.apache.commons.io.FileUtils.copyURLToFile(virtualFileOrFolder.asFileURL(),
                    new File(targetFolder, virtualFileOrFolder.getName()));
        }
    }

}
