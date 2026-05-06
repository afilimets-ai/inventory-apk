package com.inventory.sftp

import com.sysdevsolutions.kclientlibv50.SSHFTPClient

/**
 * Delegates all calls to static native methods of SSHFTPClient.
 * Converts between Kotlin-friendly API and Java static API.
 */
object SftpBridge : SftpClientI {

    override fun init(): Boolean = SSHFTPClient.Init()

    override fun connect(host: String, port: Int, username: String, password: String): Boolean =
        SSHFTPClient.Connect(
            host, port, username, password,
            null, null, 0, null, "ssh-rsa"
        )

    override fun putFile(localPath: String, remotePath: String, overwrite: Boolean): Boolean =
        SSHFTPClient.PutFile(localPath, remotePath, overwrite)

    override fun getFile(remotePath: String, localPath: String, overwrite: Boolean): Boolean =
        SSHFTPClient.GetFile(remotePath, localPath, overwrite)

    override fun listFiles(path: String, pattern: String, names: Array<String?>,
                           sizes: IntArray, count: IntArray): Boolean {
        val timestamps = Array<String?>(names.size) { null }
        return SSHFTPClient.ListFiles(path, names, sizes, timestamps, count, pattern)
    }

    override fun fileExists(path: String, exists: BooleanArray): Boolean =
        SSHFTPClient.FileExists(path, exists)

    override fun makeDirectory(path: String): Boolean =
        SSHFTPClient.MyCreateDirectory(path)

    override fun disconnect(): Boolean = SSHFTPClient.Disconnect()

    override fun free(): Boolean = SSHFTPClient.Free()

    override fun lastError(): String = SSHFTPClient.GetLastErrorMsg() ?: ""
}
