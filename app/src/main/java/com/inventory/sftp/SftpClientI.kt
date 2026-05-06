package com.inventory.sftp

/**
 * Abstraction over SFTP client.
 * Allows mocking in tests without real libSFTP.so.
 */
interface SftpClientI {
    fun init(): Boolean
    fun connect(host: String, port: Int, username: String, password: String): Boolean
    fun putFile(localPath: String, remotePath: String, overwrite: Boolean): Boolean
    fun getFile(remotePath: String, localPath: String, overwrite: Boolean): Boolean
    fun listFiles(path: String, pattern: String, names: Array<String?>,
                  sizes: IntArray, count: IntArray): Boolean
    fun fileExists(path: String, exists: BooleanArray): Boolean
    fun makeDirectory(path: String): Boolean
    fun disconnect(): Boolean
    fun free(): Boolean
    fun lastError(): String
}
