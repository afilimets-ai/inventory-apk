package com.sysdevsolutions.kclientlibv50;

/**
 * JNI wrapper for libSFTP.so (SysDevSolutions SFTP native library).
 * This class MUST remain in package com.sysdevsolutions.kclientlibv50 —
 * JNI symbols are compiled with this name.
 *
 * Source: DCAPP 2.1.14 (com.sysdevmobile.DCAPP)
 */
public class SSHFTPClient {
    static {
        System.loadLibrary("SFTP");
    }

    public static native boolean ChangeDirectory(String path);

    public static native boolean Connect(String host, int port, String username,
        String password, String privateKeyPath, byte[] hostKey, int hostKeyLength,
        String[] fingerprints, String algorithm);

    public static native boolean DeleteFileDirectory(String path);

    public static native boolean Disconnect();

    public static native boolean FileExists(String path, boolean[] exists);

    public static native boolean Free();

    public static native boolean GetFile(String remotePath, String localPath, boolean overwrite);

    public static native String GetLastErrorMsg();

    public static native boolean Init();

    public static native boolean ListFiles(String path, String[] names, int[] sizes,
        String[] timestamps, int[] count, String pattern);

    public static native boolean MyCreateDirectory(String path);

    public static native boolean PutFile(String localPath, String remotePath, boolean overwrite);

    public static native boolean RenameFile(String from, String to);
}
