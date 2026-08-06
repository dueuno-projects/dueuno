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
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import org.apache.commons.net.ftp.FTPSClient

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.Paths

/**
 * Utility class for working with FTPS (FTP over SSL/TLS).
 * <p>
 * Provides methods to verify connections, list files, download and upload files,
 * rename or delete files and directories, and create remote folders on an FTPS server.
 * All operations automatically handle FTPS connections and disconnections.
 * </p>
 * Example usage:
 * <pre>
 * boolean isConnected = FtpsUtils.verifyConnection("host", 21, "user", "pass")
 * List<File> files = FtpsUtils.listFiles("/remote/path/*.txt", "host", 21, "user", "pass")
 * FtpsUtils.downloadFile("/remote/path/file.txt", "/local/path/file.txt", "host", 21, "user", "pass")
 * FtpsUtils.uploadFile("/local/path/file.txt", "/remote/path/file.txt", "host", 21, "user", "pass")
 * </pre>
 *
 * Author: Gianluca Sartori
 */
@Slf4j
@CompileStatic
class FtpsUtils {

    /**
     * Verifies if an FTPS connection can be established with the given credentials.
     * @param host FTPS server hostname
     * @param port FTPS server port
     * @param username username for login
     * @param password password for login
     * @param indent optional indentation for logging
     * @return true if connection is successful, false otherwise
     */
    static Boolean verifyConnection(String host, Integer port, String username, String password, String indent = '') {
        log.info "${indent}Verifing connection to 'ftps://${host}:${port}'"
        try {
            connect(host, port, username, password, indent)
            return true

        } catch (Exception ignore) {
            return false
        }
    }

    /**
     * Lists remote files matching a path or pattern on the FTPS server.
     * @param remotePath remote path including filename or glob pattern
     * @param host FTPS server hostname
     * @param port FTPS server port
     * @param username login username
     * @param password login password
     * @param indent optional log indentation
     * @return list of matching File objects (paths only)
     * @throws Exception if FTPS operation fails
     */
    static List<File> listFiles(String remotePath, String host, Integer port, String username, String password, String indent = '') {
        String remotePathN = FileUtils.normalizePathname(remotePath)
        File remotePathFile = new File(remotePathN)
        String remoteDirN = FileUtils.normalizePath(remotePathFile.parent)
        String filenameGlob = "glob:${remotePathFile.name}"

        FTPSClient ftp = connect(host, port, username, password, indent)

        ftp.execPROT("P") // encrypt data channel
        verifyErrors(ftp, indent * 2)

        FTPFile[] remoteFileList = ftp.listFiles(remoteDirN)
        verifyErrors(ftp, indent * 2)

        List<File> files = []
        for (remoteFile in remoteFileList) {
            FTPFile file = remoteFile
            String absoluteFilename = "${remoteDirN}${file.name}"

            PathMatcher matcher = FileSystems.getDefault().getPathMatcher(filenameGlob)
            if (matcher.matches(Paths.get(file.name))) {
                files.add(new File(absoluteFilename))
            }
        }

        disconnect(ftp, indent)
        return files
    }

    /**
     * Downloads a remote file from the FTPS server to a local path.
     * @param remoteFile remote file path on the server
     * @param localFile local file path to save
     * @param host FTPS server hostname
     * @param port FTPS server port
     * @param username login username
     * @param password login password
     * @param indent optional log indentation
     * @throws IOException if download fails
     */
    static void downloadFile(String remoteFile, String localFile, String host, Integer port, String username, String password, String indent = '') {
        String remoteFileN = FileUtils.normalizePathname(remoteFile)
        String localFileN = FileUtils.normalizePathname(localFile)
        log.info "${indent}Downloading from 'ftps://${host}:${port}${remoteFileN}' to '${localFileN}'"
        FTPSClient ftp = connect(host, port, username, password, indent)

        ftp.bufferSize = 0
        ftp.enterLocalPassiveMode()
        ftp.fileType = FTP.BINARY_FILE_TYPE
        verifyErrors(ftp, indent * 2)

        ftp.execPROT("P") // encrypt data channel
        verifyErrors(ftp, indent * 2)

        OutputStream fos = new BufferedOutputStream(new FileOutputStream(localFileN))
        ftp.retrieveFile(remoteFileN, fos)
        fos.close()
        verifyErrors(ftp, indent * 2)

        disconnect(ftp, indent)
    }

    /**
     * Downloads a remote file into a local directory, preserving the filename.
     * @param remoteFile remote file path on the server
     * @param localDir local directory path
     * @param host FTPS server hostname
     * @param port FTPS server port
     * @param username login username
     * @param password login password
     * @param indent optional log indentation
     */
    static void downloadFileToDir(String remoteFile, String localDir, String host, Integer port,
                                  String username, String password, String indent = '') {
        String filenameFrom = new File(remoteFile).name
        String localFile = FileUtils.normalizePath(localDir) + filenameFrom

        downloadFile(remoteFile, localFile, host, port, username, password, indent)
    }

    /**
     * Renames a remote file on the FTPS server.
     * @param remoteFile current remote file path
     * @param remoteFileRenamed new remote file path
     * @param host FTPS server hostname
     * @param port FTPS server port
     * @param username login username
     * @param password login password
     * @param indent optional log indentation
     */
    static void renameFile(String remoteFile, String remoteFileRenamed, String host, Integer port,
                           String username, String password, String indent = '') {
        String fileN = FileUtils.normalizePathname(remoteFile)
        String fileNewN = FileUtils.normalizePathname(remoteFileRenamed)
        log.info "${indent}Renaming '${fileN}' in '${fileNewN}'"

        FTPSClient ftp = connect(host, port, username, password, indent)

        try {
            ftp.rename(fileN, fileNewN)
            verifyErrors(ftp, indent * 2)

        } catch (e) {
            log.warn e.message
        }

        disconnect(ftp, indent)
    }

    /**
     * Deletes a remote file from the FTPS server.
     * @param remoteFile remote file path
     * @param host FTPS server hostname
     * @param port FTPS server port
     * @param username login username
     * @param password login password
     * @param indent optional log indentation
     */
    static void deleteFile(String remoteFile, String host, Integer port, String username, String password, String indent = '') {
        String fileN = FileUtils.normalizePathname(remoteFile)
        log.info "${indent}Deleting '${fileN}'"

        FTPSClient ftp = connect(host, port, username, password, indent)

        try {
            ftp.deleteFile(fileN)
            verifyErrors(ftp, indent * 2)

        } catch (e) {
            log.warn e.message
        }

        disconnect(ftp, indent)
    }

    /**
     * Deletes a remote directory from the FTPS server.
     * @param remoteDir remote directory path
     * @param host FTPS server hostname
     * @param port FTPS server port
     * @param username login username
     * @param password login password
     * @param indent optional log indentation
     */
    static void deleteDir(String remoteDir, String host, Integer port, String username, String password, String indent = '') {
        String dirN = FileUtils.normalizePath(remoteDir)
        log.info "${indent}Deleting '${dirN}'"

        FTPSClient ftp = connect(host, port, username, password, indent)

        try {
            ftp.removeDirectory(dirN)
            verifyErrors(ftp, indent * 2)

        } catch (e) {
            log.warn e.message
        }

        disconnect(ftp, indent)
    }

    /**
     * Creates a folder on the FTPS server.
     * @param folder remote folder path
     * @param host FTPS server hostname
     * @param port FTPS server port
     * @param username login username
     * @param password login password
     * @param indent optional log indentation
     */
    static void createFolder(String folder, String host, Integer port, String username, String password, String indent = '') {
        log.info "${indent}Creating 'ftps://${host}:${port}${folder}'"
        FTPSClient ftp = connect(host, port, username, password, indent)

        try {
            ftp.makeDirectory(folder)
            verifyErrors(ftp, indent * 2)

        } catch (e) {
            log.warn e.message
        }

        disconnect(ftp, indent)
    }

    /**
     * Uploads a local file to a remote directory on the FTPS server.
     * @param localFile local file path
     * @param remoteDir remote directory path
     * @param host FTPS server hostname
     * @param port FTPS server port
     * @param username login username
     * @param password login password
     * @param indent optional log indentation
     */
    static void uploadFileToDir(String localFile, String remoteDir, String host, Integer port, String username, String password, String indent = '') {
        String localFileN = FileUtils.normalizePathname(localFile)
        String remoteDirN = FileUtils.normalizePath(remoteDir)
        Path localFilePath = Paths.get(localFileN)
        String remoteFileN = "${remoteDirN}${localFilePath.fileName}"

        uploadFile(
            localFileN,
            remoteFileN,
            host,
            port,
            username,
            password,
            indent
        )
    }

    /**
     * Uploads a local file to a remote path on the FTPS server.
     * @param localFile local file path
     * @param remoteFile remote file path
     * @param host FTPS server hostname
     * @param port FTPS server port
     * @param username login username
     * @param password login password
     * @param indent optional log indentation
     */
    static void uploadFile(String localFile, String remoteFile, String host, Integer port, String username, String password, String indent = '') {
        log.info "${indent}Uploading '${localFile}' to 'ftps://${host}:${port}${remoteFile}'"
        FTPSClient ftp = connect(host, port, username, password, indent)

        ftp.bufferSize = 0
        ftp.fileType = FTP.BINARY_FILE_TYPE
        ftp.enterLocalPassiveMode()
        verifyErrors(ftp, indent * 2)

        ftp.execPROT("P") // encrypt data channel
        verifyErrors(ftp, indent * 2)

        def fis = new FileInputStream(localFile)
        ftp.storeFile(remoteFile, fis)
        fis.close()
        verifyErrors(ftp, indent * 2)

        disconnect(ftp, indent)
    }

    /**
     * Connects to an FTPS server and logs in.
     * @return connected FTPSClient
     */
    private static FTPSClient connect(String host, Integer port, String username, String password, String indent = '') {
        FTPSClient ftp = new FTPSClient()

        ftp.connect(host, port)
        verifyErrors(ftp, indent * 2)

        ftp.login(username, password)
        verifyErrors(ftp, indent * 2)

        return ftp
    }

    /**
     * Disconnects from the FTPS server.
     */
    private static void disconnect(FTPSClient ftp, indent = '') {
        ftp.logout()
        ftp.disconnect()
    }

    /**
     * Checks for FTPS errors and throws an exception if any.
     * @param ftp connected FTPSClient
     * @param indent optional log indentation
     * @throws Exception if FTPS reply code is not positive
     */
    private static void verifyErrors(FTPSClient ftp, String indent = '') {
        //log.info "${indent}${ftp.replyString}" - "\r\n"

        if (!FTPReply.isPositiveCompletion(ftp.replyCode)) {
            throw new Exception("FTPS ERROR: ${ftp.replyString}")
        }
    }

}
