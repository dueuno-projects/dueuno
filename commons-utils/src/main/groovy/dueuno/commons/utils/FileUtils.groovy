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
package dueuno.commons.utils

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.nio.file.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Utility class for common file and directory operations.
 * <p>
 * Provides methods to normalize paths, handle filenames and extensions,
 * create, delete, copy, move files and directories, generate temporary filenames,
 * and serialize/deserialize log filenames.
 * <p>
 * Example usage:
 * <pre>
 * String normalizedPath = FileUtils.normalizePathname("C:\\Users\\Test")
 * String tempDir = FileUtils.getTempDirectory()
 * FileUtils.createDirectory("logs")
 * FileUtils.createFile("logs/example.log")
 * FileUtils.copyFile("logs/example.log", "logs/copy.log")
 * FileUtils.renameFile("logs/copy.log", "logs/copyRenamed.log")
 * boolean exists = FileUtils.exists("logs/example.log")
 * List<File> files = FileUtils.listFiles("logs")
 * </pre>
 *
 * Author: Gianluca Sartori
 */
@Slf4j
@CompileStatic
class FileUtils {

    /**
     * Normalizes a pathname by replacing backslashes with forward slashes.
     * @param pathname the original path
     * @return normalized pathname or null if input is null
     */
    static String normalizePathname(String pathname) {
        if (!pathname) {
            return pathname
        }

        return pathname.replace("\\", "/")
    }

    /**
     * Normalizes a path and ensures it ends with a slash.
     * @param path the original path
     * @return normalized path ending with '/'
     */
    static String normalizePath(String path) {
        if (!path) {
            return path
        }

        String result = normalizePathname(path)
        if (!result.endsWith('/')) {
            result += '/'
        }

        return result
    }

    /**
     * Extracts the directory path from a pathname.
     * @param pathname full path including filename
     * @return the path without the filename
     */
    static String stripPath(String pathname) {
        if (!pathname) {
            return pathname
        }

        def f = normalizePathname(pathname)
        int index = f.lastIndexOf('/')
        if (index == -1) {
            return ''
        }

        String result = f.substring(0, index + 1)
        return result
    }

    /**
     * Extracts the filename from a full pathname.
     * @param pathname full path including filename
     * @return filename only
     */
    static String stripFilename(String pathname) {
        if (!pathname) {
            return pathname
        }

        def f = normalizePathname(pathname)
        int index = f.lastIndexOf('/')
        String result = f.substring(index + 1)
        return result
    }

    /**
     * Extracts the file extension from a pathname.
     * @param pathname full path or filename
     * @return lowercase extension without the dot
     */
    static String stripExtension(String pathname) {
        if (!pathname) {
            return pathname
        }

        def dot = pathname.lastIndexOf('.')
        return pathname
            .substring(dot + 1)
            .toLowerCase()
    }

    /**
     * Removes the extension from a filename or pathname.
     * @param pathname full path or filename
     * @return path or filename without extension
     */
    static String removeExtension(String pathname) {
        if (!pathname) {
            return pathname
        }

        def f = normalizePathname(pathname)
        int index = f.lastIndexOf('.')
        String result = f.substring(0, index)
        return result
    }

    /**
     * Builds a safe filename by replacing invalid characters and spaces.
     * @param filename the original filename
     * @param spaceReplacementChar character to replace spaces (default '-')
     * @return sanitized filename
     */
    static String buildSafeFilename(String filename, String spaceReplacementChar = '-') {
        def unixFilenameInvalidChars = '[/]'
        def unixFilenameInvalidNonPrintableChars = '[\\u0000]' // NULL
        def win32FilenameInvalidChars = '[<>:"/\\\\|?*]'
        def win32FilenameInvalidNonPrintableChars = '[\\u0000-\\u001F]|[\\u007F]' // ASCII 0-31 e 127
        def urlInvalidChars = '[<>#%\\+\\{\\}|\\^~\\[\\]`;/\\?:@=&$]'
        def space = '[ ]'

        return filename.replaceAll(
            unixFilenameInvalidChars
                + '|' + unixFilenameInvalidNonPrintableChars
                + '|' + win32FilenameInvalidChars
                + '|' + win32FilenameInvalidNonPrintableChars
                + '|' + urlInvalidChars
                + '|' + space,
            spaceReplacementChar)
    }

    /**
     * Returns the system temporary directory with normalized path.
     * @return normalized temporary directory path ending with '/'
     */
    static String getTempDirectory() {
        def result = System.getProperty("java.io.tmpdir")
        return normalizePath(result)
    }

    /**
     * Returns the current working directory with normalized path.
     * @return normalized working directory path ending with '/'
     */
    static String getWorkingDirectory() {
        Path userDirPath = Paths.get(System.getProperty("user.dir")).normalize()
        return normalizePath(userDirPath.toString())
    }

    /**
     * Generates a temporary filename with timestamp and random token.
     * @param length length of random token (default 10)
     * @return temporary filename string
     */
    static String getTempFilename(Integer length = 10) {
        String token = StringUtils.generateRandomToken(length)
        def fileName = "${getFilenameTimestamp('yyyyMMddHHmmssS')}-${token}"
        return fileName
    }

    /**
     * Returns the path where the application is running.
     * @return normalized application path
     */
    static String getApplicationPath() {
        Path workDirPath = Paths.get(FileUtils.getProtectionDomain().getCodeSource().getLocation().toString()).normalize()
        return normalizePath(workDirPath.toString())
    }

    /**
     * Returns the current timestamp formatted for filenames.
     * @param pattern timestamp pattern (default 'YYYYMMddHHmm')
     * @return formatted timestamp string
     */
    static String getFilenameTimestamp(String pattern = 'YYYYMMddHHmm') {
        return getFilenameTimestamp(LocalDateTime.now(), pattern)
    }

    /**
     * Returns the timestamp of a given LocalDateTime formatted for filenames.
     * @param dateTime date-time to format
     * @param pattern timestamp pattern (default 'YYYYMMddHHmm')
     * @return formatted timestamp string
     */
    static String getFilenameTimestamp(LocalDateTime dateTime, String pattern = 'YYYYMMddHHmm') {
        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern)
        return dateTime.format(format)
    }

    /**
     * Serializes a log filename with current timestamp and optional parts.
     * @param parts additional parts to include in filename
     * @return serialized log filename ending with '.log'
     */
    static String serializeLogFilename(String... parts) {
        serializeLogFilename(LocalDateTime.now(), parts)
    }

    /**
     * Serializes a log filename with given date-time and parts.
     * @param dateTime timestamp for filename
     * @param parts additional parts to include
     * @return serialized log filename ending with '.log'
     */
    static String serializeLogFilename(LocalDateTime dateTime, Object... parts) {
        String result = getFilenameTimestamp(dateTime)
        for (part in parts) {
            result += '_' + part.toString().toLowerCase()
        }
        result += '.log'
        return result
    }

    /**
     * Deserializes a log filename into timestamp and named parts.
     * @param logFilename the filename to deserialize
     * @param parts names of the parts to extract
     * @return map containing timestamp and part values
     * @throws Exception if filename format or extension is invalid
     */
    static Map deserializeLogFilename(String logFilename, String... parts) {
        String fileName = stripFilename(logFilename)
        String fileExt = stripExtension(logFilename)
        List fileParts = fileName.split('_') as List
        Integer filePartsCount = fileParts.size() - 1
        Integer userPartsCount = parts.size()
        Map result = [:]

        if (filePartsCount < 2 || fileExt != 'log') {
            throw new Exception("Invalid log file (does not have'.log' extension and does not have at least 2 parts separated by '_')")
        }

        if (filePartsCount != userPartsCount) {
            throw new Exception("Error deserializing filename '${logFilename}'. Cannot match filename " +
                "'${filePartsCount}' parts with the supplied '${userPartsCount}' parts: ${parts}.")
        }

        result['timestamp'] = fileParts[0]
        Integer i = 1
        for (part in parts) {
            result[part] = fileParts[i]
            i++
        }

        return result
    }

    /**
     * Updates the last modified timestamp of a file, creating it if it does not exist.
     * @param pathname path of the file
     */
    static void touch(String pathname) {
        File f = new File(pathname)
        if (!f.exists()) {
            new FileOutputStream(f).close()
        }
        f.setLastModified(System.currentTimeMillis())
    }

    /**
     * Checks if a file exists.
     * @param pathname path of the file
     * @return true if file exists, false otherwise
     */
    static Boolean exists(String pathname) {
        File f = new File(pathname)
        return f.exists()
    }

    /**
     * Lists all files in a directory.
     * @param path directory path
     * @return list of files
     */
    static List<File> listFiles(String path) {
        return listFiles(path, '*')
    }

    /**
     * Lists files in a directory matching a pattern.
     * @param path directory path
     * @param pattern glob pattern
     * @return list of files matching the pattern
     */
    static List<File> listFiles(String path, String pattern) {
        Path folderPath = Paths.get(path)
        DirectoryStream<Path> dir = Files.newDirectoryStream(folderPath, pattern)

        List<File> results = []
        for (part in dir) {
            if (!Files.isDirectory(part)) {
                results.add(part.toFile())
            }
        }
        return results
    }

    /**
     * Creates a new empty file.
     * @param pathname path of the file to create
     * @param indent optional log indentation
     */
    static void createFile(String pathname, String indent = '') {
        String newPathname = normalizePathname(pathname)

        log.info "${indent}Creating file '${newPathname}'"
        File emptyFile = new File(newPathname)
        emptyFile.createNewFile()
    }

    /**
     * Deletes a file if it exists.
     * @param pathname path of the file to delete
     * @param indent optional log indentation
     */
    static void deleteFile(String pathname, String indent = '') {
        String deletePathname = normalizePathname(pathname)
        Path fileToDelete = Paths.get(deletePathname)

        log.info "${indent}Deleting '${fileToDelete}'"
        Files.deleteIfExists(fileToDelete)
    }

    /**
     * Renames a file.
     * @param fromPathname current path
     * @param toPathname new path
     * @param indent optional log indentation
     */
    static void renameFile(String fromPathname, String toPathname, String indent = '') {
        String renameFromPathname = normalizePathname(fromPathname)
        String renameToPathname = normalizePathname(toPathname)
        File from = new File(renameFromPathname)
        File to = new File(renameToPathname)

        log.info "${indent}Renaming '${from}' to '${to}'"
        from.renameTo(to)
    }

    /**
     * Copies a file to another location.
     * @param fromPathname source path
     * @param toPathname destination path
     * @param indent optional log indentation
     */
    static void copyFile(String fromPathname, String toPathname, String indent = '') {
        String copyFromPathname = normalizePathname(fromPathname)
        String copyToPathname = normalizePathname(toPathname)
        Path from = Paths.get(copyFromPathname)
        Path to = Paths.get(copyToPathname)

        log.info "${indent}Copying '${from}' to '${to}'"
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING)
    }

    /**
     * Copies a file to a directory, preserving its name.
     * @param fromPathname source file
     * @param toPath destination directory
     * @param indent optional log indentation
     */
    static void copyFileToDirectory(String fromPathname, String toPath, String indent = '') {
        String copyFromPathname = normalizePathname(fromPathname)
        String copyToPath = normalizePath(toPath)
        Path from = Paths.get(copyFromPathname)
        Path to = Paths.get(copyToPath)
        Path toFile = Paths.get("${to}/${from.fileName}")

        log.info "${indent}Copying '${from}' to '${toFile}'"
        Files.copy(from, toFile, StandardCopyOption.REPLACE_EXISTING)
    }

    /**
     * Moves a file to a new path.
     * @param fromPathname source path
     * @param toPathname destination path
     * @param indent optional log indentation
     * @return path of the moved file
     */
    static String moveFile(String fromPathname, String toPathname, String indent = '') {
        String moveFromPathname = normalizePathname(fromPathname)
        String moveToPathname = normalizePathname(toPathname)
        Path from = Paths.get(moveFromPathname)
        Path to = Paths.get(moveToPathname)

        log.info "${indent}Moving '${from}' to '${to}'"
        return Files.move(from, to, StandardCopyOption.REPLACE_EXISTING).toString()
    }

    /**
     * Moves a file to a directory, preserving its filename.
     * @param fromPathname source file
     * @param toPath destination directory
     * @param indent optional log indentation
     * @return path of the moved file
     */
    static String moveFileToDirectory(String fromPathname, String toPath, String indent = '') {
        String moveFromPathname = normalizePathname(fromPathname)
        String moveToPath = normalizePath(toPath)
        Path from = Paths.get(moveFromPathname)
        Path toDir = Paths.get(moveToPath)
        Path to = Paths.get("${toDir}/${from.fileName}")

        log.info "${indent}Moving '${from}' to '${to}'"
        return Files.move(from, to, StandardCopyOption.REPLACE_EXISTING).toString()
    }

    /**
     * Creates a directory and any nonexistent parent directories.
     * @param path directory path
     * @param indent optional log indentation
     */
    static void createDirectory(String path, String indent = '') {
        String newPath = normalizePath(path)
        Path pathToCreate = Paths.get(newPath)

        if (Files.notExists(pathToCreate)) {
            log.info "${indent}Creating '${pathToCreate}'"
            Files.createDirectories(pathToCreate)
        }
    }

    /**
     * Deletes a directory and all its contents recursively.
     * @param path directory path
     * @param indent optional log indentation
     */
    static void deleteDirectory(String path, String indent = '') {
        String deleteDir = normalizePath(path)
        Path dirToDelete = Paths.get(deleteDir)
        if (Files.notExists(dirToDelete)) {
            log.info "${indent}Deleting '${dirToDelete}' but does not exist, skipping."
            return
        }
        log.info "${indent}Deleting '${dirToDelete.fileName}'"
        Files.walk(dirToDelete)
            .sorted(Comparator.reverseOrder())
            .forEach {
                deleteFile(it.toString(), indent * 2)
            }
        log.info "${indent}... done."
    }

    /**
     * Copies a directory recursively to another location.
     * @param fromPath source directory
     * @param toPath destination directory
     * @param indent optional log indentation
     */
    static void copyDirectory(String fromPath, String toPath, String indent = '') {
        String copyFromPath = normalizePath(fromPath)
        String copyToPath = normalizePath(toPath)

        log.info "${indent}Copying '${Paths.get(copyFromPath)}' to '${Paths.get(copyToPath)}'"
        copyDirectoryRecursive(copyFromPath, copyToPath, indent)
        log.info "${indent}... done."
    }

    /**
     * Helper method for recursively copying a directory.
     */
    private static void copyDirectoryRecursive(String fromPath, String toPath, String indent = '') {
        createDirectory(toPath)
        DirectoryStream<Path> directory = Files.newDirectoryStream(Paths.get(fromPath))
        for (path in directory) {
            String copyFromPath = path
            String copyToPath = "${toPath}/${path.getFileName()}"
            if (Files.isDirectory(path)) {
                copyDirectoryRecursive(copyFromPath, copyToPath, indent)
            } else {
                copyFile(copyFromPath, copyToPath, indent)
            }
        }
    }
}
