/*
 * Copyright 2021 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dueuno.utils

import dueuno.commons.utils.FileUtils
import dueuno.commons.utils.LogUtils
import grails.io.IOUtils
import grails.util.BuildSettings
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.io.support.PathMatchingResourcePatternResolver
import org.grails.io.support.Resource

import java.nio.file.*

/**
 * @author Gianluca Sartori
 */

@Slf4j
@CompileStatic
class ResourceUtils {

    /**
     * Extract the specified directory from '/src/main/resources' into a location on the file system.
     *
     * @param fromPath The directory to extract from '/src/main/resources'. MUST START WITH '/'.
     * @param toPath A directory on the file system where the files will be extracted
     */
    static void extractDirectory(String fromPath, String toPath) {
        def rpr = new PathMatchingResourcePatternResolver()
        Resource[] resources = rpr.getResources("classpath*:${fromPath}/**").reverse()

        fromPath = FileUtils.normalizePath(fromPath) ?: '/'
        toPath = FileUtils.normalizePath(toPath)

        log.trace "extractDirectory()"
        log.trace " --> From Path: ${fromPath}"
        log.trace " --> To Path:   ${toPath}"
        log.trace " --- "

        for (Resource resource in resources) {
            Boolean isDeployedAsBootJar = resource.URL.protocol.equals('jar')

            String resourceDir
            String resourceFile
            if (fromPath == '/') {
                resourceDir = normalizeResourcePath(resource.URL.path)
                resourceFile = ''

            } else if (isDeployedAsBootJar) {
                log.trace "Extracting Resources: Application deployed as JAR"
                List<String> resourceParts = resource.URL.path.split('/!BOOT-INF/classes/!' + fromPath) as List<String>
                resourceDir = resourceParts.first() + '!/BOOT-INF/classes' + fromPath
                resourceFile = resourceParts.size() > 1 ? resourceParts.last() : ''

            } else {
                log.trace "Extracting Resources: Application launched from IDE or Plugin as JAR dependency"
                List<String> resourceParts = normalizeResourcePath(resource.URL.path).split(fromPath) as List<String>
                if (resourceParts.size() < 3) {
                    resourceDir = resourceParts.first() + fromPath
                    resourceFile = resourceParts.size() > 1 ? resourceParts.last() : ''
                } else {
                    resourceDir = resourceParts.removeLast().join(fromPath) + fromPath
                    resourceFile = resourceParts.size() > 1 ? resourceParts.last() : ''
                }
            }

            String normalizedResourcePathname = resourceDir + resourceFile

            log.trace "extractDirectory()"
            log.trace " --> Resource:            " + resource.URL.path
            log.trace " --> Resource Dir:        " + resourceDir
            log.trace " --> Resource File:       " + resourceFile
            log.trace " --> Normalized resource: " + normalizedResourcePathname
            log.trace ""

            Path rootPath
            Path resourcePath
            FileSystem filesystem

            if (isDeployedAsBootJar) {
                try {
                    List<String> path = normalizedResourcePathname.split('!') as List<String>
                    URI jarFile = new URI('jar:' + path.first())
                    filesystem = FileSystems.newFileSystem(jarFile, [:])
                    rootPath = filesystem.getPath(resourceDir.split('!').last())
                    resourcePath = filesystem.getPath(normalizedResourcePathname.split('!').last())

                } catch (Exception e) {
                    log.error LogUtils.logStackTrace(e)
                    if (filesystem) filesystem.close()
                    return
                }

            } else {
                rootPath = Paths.get(resourceDir)
                resourcePath = Paths.get(normalizedResourcePathname)
            }

            extractResource(rootPath, resourcePath, toPath)

            if (filesystem) filesystem.close()
        }
    }

    private static String normalizeResourcePath(String path) {
        // On Windows a '/' is prefixed to the absolute path, we need to remove it
        if (path.startsWith('/') && path.contains(':')) {
            path = path.substring(1)
        }

        // resources inside a JARs  contains a '!' after 'classes', again we need to remove it
        return path.replace('classes!', 'classes')
    }

    static void extractDirectoryFromPlugin(Class pluginClass, String pluginName, String fromPath, String toPath) {
        URI baseUri = IOUtils.findResourceRelativeToClass(pluginClass, '/').toURI()
        URI uri = IOUtils.findResourceRelativeToClass(pluginClass, fromPath).toURI()
        Boolean isJarDependency = uri.getScheme().equals("jar")
        Boolean isDeployedAsBootJar = baseUri.toString().findAll('.jar!').size() > 1 // a .jar inside a .jar

        Path baseResourcePath
        Path resourcePath
        Path tempPath

        FileSystem filesystem
        if (isDeployedAsBootJar) {
            URI bootJarBaseUri = new URI(baseUri.toString().split('.jar!')[0] + '.jar/')
            filesystem = FileSystems.newFileSystem(bootJarBaseUri, [:])

            // We need to extract the plugin-name.jar to a filesystem location
            // then we can extract our resources
            Path bootJarResourcePath = filesystem.getPath('/')
            Files.walk(bootJarResourcePath).forEach {
                if (Files.isRegularFile(it) && it.fileName.toString().endsWith('.jar')) {
                    if (it.fileName.toString().startsWith(pluginName)) {
                        tempPath = Paths.get(FileUtils.tempDirectory + '/' + it.fileName)
                        log.debug "Extracting '${pluginName}' to '${tempPath}'"
                        Files.copy(it, tempPath, StandardCopyOption.REPLACE_EXISTING)
                        return
                    }
                }
            }
            filesystem.close()

            // We need this for Windows
            String bootJarPath = tempPath.toString().contains(':')
                ? '/' + tempPath.toString().replace('\\', '/')
                : tempPath

            URI bootJarUri = new URI('jar:file:' + bootJarPath)
            log.debug "Reading resources from '${bootJarUri}'"
            try {
                filesystem = FileSystems.newFileSystem(bootJarUri, [:])
                baseResourcePath = filesystem.getPath('/')
                resourcePath = filesystem.getPath(fromPath)
            } catch (Exception e) {
                log.error LogUtils.logStackTrace(e)
                if (filesystem) filesystem.close()
                if (tempPath) FileUtils.deleteFile(tempPath.toString())
                return
            }

        } else if (isJarDependency) {
            log.debug "Reading resources from '${uri}'"
            try {
                filesystem = FileSystems.newFileSystem(uri, [:])
                baseResourcePath = filesystem.getPath('/')
                resourcePath = filesystem.getPath(fromPath)
            } catch (Exception e) {
                log.error LogUtils.logStackTrace(e)
                if (filesystem) filesystem.close()
                if (tempPath) FileUtils.deleteFile(tempPath.toString())
                return
            }

        } else { // launched from the IDE
            log.debug "Reading resources from '${uri}'"
            baseResourcePath = Paths.get(baseUri)
            resourcePath = Paths.get(uri)
        }

        extractResource(baseResourcePath, resourcePath, toPath)

        if (filesystem) filesystem.close()
        if (tempPath) FileUtils.deleteFile(tempPath.toString())
    }

    static void extractResource(Path root, Path resource, String toPath) {
        toPath = FileUtils.normalizePath(toPath)

        log.trace "extractResource()"
        log.trace " --> Root:     ${root}"
        log.trace " --> Resource: ${resource}"
        log.trace " --> To Path:  ${toPath}"
        log.trace " --- "

        FileUtils.createDirectory(toPath)
        Files.walk(resource).forEach {
            String baseDirectory = root.toString() == '/' ? '' : root.toString()
            String directory = it.parent.toString() + '/' - baseDirectory

            if (it.toString() == baseDirectory)
                return

            log.trace " --> Resource:          ${it}"
            log.trace " --> Resource base dir: ${baseDirectory}"
            log.trace " --> Resource dir:      ${directory}"
            log.trace " --> To path:           ${toPath + directory + '/' + it.fileName}"
            log.trace ""

            if (Files.isDirectory(it) && !FileUtils.exists(toPath + directory + '/' + it.fileName)) {
                FileUtils.createDirectory(toPath + directory + '/' + it.fileName)

            } else if (!FileUtils.exists(toPath + directory + '/' + it.fileName)) {
                FileUtils.createDirectory(toPath + directory)
                Files.copy(it, Paths.get(toPath + directory + '/' + it.fileName))
            }
        }
    }

    static String getPluginResourcePathname(Class pluginClass, String filename) {
        String s = File.separator
        URL url = IOUtils.findResourceRelativeToClass(pluginClass, s + filename)
        if (!url)
            throw new Exception("Cannot find resource '$filename'")

        return url.file
    }

    static InputStream getPluginResourceInputStream(Class pluginClass, String filename) {
        URL url = null
        try {
            String pathToClassFile = '/' + pluginClass.name.replace(".", "/") + ".class"
            URL classRes = pluginClass.getResource(pathToClassFile)
            if (classRes) {
                String rootPath = classRes.toString() - pathToClassFile
                if (rootPath.endsWith(BuildSettings.BUILD_CLASSES_PATH)) {
                    rootPath = rootPath.replace(BuildSettings.BUILD_CLASSES_PATH, BuildSettings.BUILD_RESOURCES_PATH)
                }
                String rootPathname = rootPath + '/' + filename
                url = new URL(rootPathname)
            }
        } catch (Exception e) {
            log.error e.message
        }

        if (!url)
            throw new Exception("Cannot find resource '$filename'")

        return url.openStream()
    }

    static String getResourcePathname(String filename) {
        URL result = Thread.currentThread().getContextClassLoader().getResource(filename)
        if (!result)
            throw new Exception("Cannot find resource '$filename'")

        return result.file
    }

    static InputStream getResourceInputStream(String filename) {
        InputStream result = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)
        if (!result)
            throw new Exception("Cannot find resource '$filename'")

        return result
    }
}
