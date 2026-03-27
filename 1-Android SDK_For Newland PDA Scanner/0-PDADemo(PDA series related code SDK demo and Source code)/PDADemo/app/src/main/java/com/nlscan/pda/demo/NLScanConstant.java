package com.nlscan.pda.demo;

/**
 * @author Alan
 * @Company nlscan
 * @date 2017/12/16 23:02
 * @Description:
 */

public class NLScanConstant {
    // 0 表示禁用扫描功能 1 表示打开扫描功能*
    public static final String EXTRA_SCAN_POWER = "EXTRA_SCAN_POWER";

    // 0 配置扫描头为普通触发模式   1 配置扫描头为连续扫描模式   2 配置扫描头为超时扫描模式*
    public static final String EXTRA_TRIG_MODE = "EXTRA_TRIG_MODE";

    // 1 ：直接填充模式*  2 ：虚拟按键模式  3 ： 广播输出模式
    public static final String EXTRA_SCAN_MODE = "EXTRA_SCAN_MODE";

    // 0 关闭自动换行*  1 允许自动换行
    public static final String EXTRA_SCAN_AUTOENT = "EXTRA_SCAN_AUTOENT";

    // 0 关闭声音提示  1 打开声音提示*
    public static final String EXTRA_SCAN_NOTY_SND = "EXTRA_SCAN_NOTY_SND";

    // 0 关闭振动提示*   1 打开振动提示
    public static final String EXTRA_SCAN_NOTY_VIB = "EXTRA_SCAN_NOTY_VIB";

    // 0 关闭指示灯提示  1 打开指示灯提示*
    public static final String EXTRA_SCAN_NOTY_LED = "EXTRA_SCAN_NOTY_LED";

    // 启动扫描的Action
    public static final String SCANNER_TRIG = "nlscan.action.SCANNER_TRIG";

    // 获得扫描结果的广播Action
    public static final String SCANNER_RESULT = "nlscan.action.SCANNER_RESULT";

    // 修改扫描设置默认值的Action
    public static final String ACTION_BAR_SCANCFG = "ACTION_BAR_SCANCFG";

    // 修改码制配置的Action
    public static final String ACTION_BARCODE_CFG = "ACTION_BARCODE_CFG";

    // 未知条码类型名称
    public static final String NLSCAN_UNKNOW_CODETYPE_NAME = "NLSCAN_UNKNOW_CODETYPE_NAME";

    // 未知条码类型名称
    public static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    // CODE_ID
    public static final String CODE_ID = "CODE_ID";

    // PROPERTY
    public static final String PROPERTY = "PROPERTY";

    // VALUE
    public static final String VALUE = "VALUE";


}
