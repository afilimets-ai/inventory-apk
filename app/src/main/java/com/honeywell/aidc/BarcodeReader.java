package com.honeywell.aidc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import com.honeywell.IExecutor;
import com.honeywell.Message;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public final class BarcodeReader implements Parcelable {
    public static final String BAD_READ_NOTIFICATION = "badRead";
    public static final String CODABAR_CHECK_DIGIT_MODE_CHECK = "check";
    public static final String CODABAR_CHECK_DIGIT_MODE_CHECK_AND_STRIP = "checkAndStrip";
    public static final String CODABAR_CHECK_DIGIT_MODE_NO_CHECK = "noCheck";
    public static final String CODE_11_CHECK_DIGIT_MODE_DOUBLE_DIGIT_CHECK = "doubleDigitCheck";
    public static final String CODE_11_CHECK_DIGIT_MODE_DOUBLE_DIGIT_CHECK_AND_STRIP = "doubleDigitCheckAndStrip";
    public static final String CODE_11_CHECK_DIGIT_MODE_SINGLE_DIGIT_CHECK = "singleDigitCheck";
    public static final String CODE_11_CHECK_DIGIT_MODE_SINGLE_DIGIT_CHECK_AND_STRIP = "singleDigitCheckAndStrip";
    public static final String CODE_39_CHECK_DIGIT_MODE_CHECK = "check";
    public static final String CODE_39_CHECK_DIGIT_MODE_CHECK_AND_STRIP = "checkAndStrip";
    public static final String CODE_39_CHECK_DIGIT_MODE_NO_CHECK = "noCheck";
    public static final Parcelable.Creator<BarcodeReader> CREATOR;
    public static final String DATA_PROCESSOR_SYMBOLOGY_ID_AIM = "aim";
    public static final String DATA_PROCESSOR_SYMBOLOGY_ID_HONEYWELL = "honeywell";
    public static final String DATA_PROCESSOR_SYMBOLOGY_ID_NONE = "none";
    public static final String DEC_ID_PROP_USE_ROI_DISABLE = "Disable";
    public static final String DEC_ID_PROP_USE_ROI_DPM_AIMER_CENTERED = "DPM, Aimer centered";
    public static final String DEC_ID_PROP_USE_ROI_KIOSK_OR_PRESENTATION = "Kiosk/Presentation application";
    public static final String DEC_ID_PROP_USE_ROI_STANDARD = "Standard";
    public static final String DEC_ID_PROP_USE_ROI_STANDARD_AIMER_CENTERED = "Standard, Aimer centered";
    public static final String DIGIMARC_CONVERSION_CONVERT_TO_EQUIVALENT = "convertToEquivalent";
    public static final String DIGIMARC_CONVERSION_NO_CONVERSION = "noConversion";
    public static final String DIGIMARC_SCALE_BLOCKS_USE_BOTH_SCALE1_AND_SCALE3_BLOCKS = "useBothScale1AndScale3Blocks";
    public static final String DIGIMARC_SCALE_BLOCKS_USE_SCALE1_BLOCKS = "useScale1Blocks";
    public static final String DIGIMARC_SCALE_BLOCKS_USE_SCALE3_BLOCKS = "useScale3Blocks";
    public static final String DPM_ENABLED_DOTPEEN_DECODING = "dotpeen";
    public static final String DPM_ENABLED_NO_DPM_OPTIMIZATION = "none";
    public static final String DPM_ENABLED_REFLECTIVE_DECODING = "reflective";
    public static final String EANUCC_EMULATION_MODE_GS1_128_EMULATION = "gs1128Emulation";
    public static final String EANUCC_EMULATION_MODE_GS1_CODE_EXPANSION_OFF = "gs1CodeExpansionOff";
    public static final String EANUCC_EMULATION_MODE_GS1_DATABAR_EMULATION = "gs1DatabarEmulation";
    public static final String EANUCC_EMULATION_MODE_GS1_EAN8_TO_EAN13_CONVERSION = "gs1EAN8toEAN13Conversion";
    public static final String EANUCC_EMULATION_MODE_GS1_EMULATION_OFF = "gs1EmulationOff";
    public static final String GOOD_READ_NOTIFICATION = "goodRead";
    public static final String IMAGER_EXPOSURE_MODE_AUTO_EXPOSURE = "autoExposure";
    public static final String IMAGER_EXPOSURE_MODE_AUTO_SENSOR = "autoSensor";
    public static final String IMAGER_EXPOSURE_MODE_CONTEXT_SENSITIVE = "contextSensitive";
    public static final String IMAGER_EXPOSURE_MODE_FIXED = "fixed";
    public static final String IMAGER_SAMPLE_METHOD_CENTER = "center";
    public static final String IMAGER_SAMPLE_METHOD_CENTER_WEIGHTED = "centerWeighted";
    public static final String IMAGER_SAMPLE_METHOD_UNIFORM = "uniform";
    public static final String INTERLEAVED_25_CHECK_DIGIT_MODE_CHECK = "check";
    public static final String INTERLEAVED_25_CHECK_DIGIT_MODE_CHECK_AND_STRIP = "checkAndStrip";
    public static final String INTERLEAVED_25_CHECK_DIGIT_MODE_NO_CHECK = "noCheck";
    public static final String MSI_CHECK_DIGIT_MODE_DOUBLE_MOD_10_CHECK = "doubleMod10Check";
    public static final String MSI_CHECK_DIGIT_MODE_DOUBLE_MOD_10_CHECK_AND_STRIP = "doubleMod10CheckAndStrip";
    public static final String MSI_CHECK_DIGIT_MODE_NO_CHECK = "noCheck";
    public static final String MSI_CHECK_DIGIT_MODE_SINGLE_MOD_10_CHECK = "singleMod10Check";
    public static final String MSI_CHECK_DIGIT_MODE_SINGLE_MOD_10_CHECK_AND_STRIP = "singleMod10CheckAndStrip";
    public static final String MSI_CHECK_DIGIT_MODE_SINGLE_MOD_11_PLUS_MOD_10_CHECK = "singleMod11PlusMod10Check";
    public static final String MSI_CHECK_DIGIT_MODE_SINGLE_MOD_11_PLUS_MOD_10_CHECK_AND_STRIP = "singleMod11PlusMod10CheckAndStrip";
    public static final String POSTAL_2D_MODE_AUSTRALIA = "australia";
    public static final String POSTAL_2D_MODE_BPO = "bpo";
    public static final String POSTAL_2D_MODE_CANADA = "canada";
    public static final String POSTAL_2D_MODE_DUTCH = "dutch";
    public static final String POSTAL_2D_MODE_INFOMAIL = "infomail";
    public static final String POSTAL_2D_MODE_INFOMAIL_AND_BPO = "infomailAndBpo";
    public static final String POSTAL_2D_MODE_JAPAN = "japan";
    public static final String POSTAL_2D_MODE_NONE = "none";
    public static final String POSTAL_2D_MODE_PLANET = "planet";
    public static final String POSTAL_2D_MODE_PLANET_AND_POSTNET = "planetAndPostnet";
    public static final String POSTAL_2D_MODE_PLANET_AND_POSTNET_AND_UPU = "planetAndPostnetAndUpu";
    public static final String POSTAL_2D_MODE_PLANET_AND_POSTNET_AND_UPU_AND_USPS = "planetAndPostnetAndUpuAndUsps";
    public static final String POSTAL_2D_MODE_PLANET_AND_POSTNET_AND_UPU_AND_USPS_PLUS_BNB = "planetAndPostnetAndUpuAndUspsPlusBnB";
    public static final String POSTAL_2D_MODE_PLANET_AND_POSTNET_AND_UPU_PLUS_BNB = "planetAndPostnetAndUpuPlusBnB";
    public static final String POSTAL_2D_MODE_PLANET_AND_POSTNET_AND_USPS = "planetAndPostnetAndUsps";
    public static final String POSTAL_2D_MODE_PLANET_AND_POSTNET_AND_USPS_PLUS_BNB = "planetAndPostnetAndUspsPlusBnB";
    public static final String POSTAL_2D_MODE_PLANET_AND_POSTNET_PLUS_BNB = "planetAndPostnetPlusBnB";
    public static final String POSTAL_2D_MODE_PLANET_AND_UPU = "planetAndUpu";
    public static final String POSTAL_2D_MODE_PLANET_AND_UPU_AND_USPS = "planetAndUpuAndUsps";
    public static final String POSTAL_2D_MODE_PLANET_AND_USPS = "planetAndUsps";
    public static final String POSTAL_2D_MODE_POSTNET = "postnet";
    public static final String POSTAL_2D_MODE_POSTNET_AND_UPU = "postnetAndUpu";
    public static final String POSTAL_2D_MODE_POSTNET_AND_UPU_AND_USPS = "postnetAndUpuAndUsps";
    public static final String POSTAL_2D_MODE_POSTNET_AND_UPU_AND_USPS_PLUS_BNB = "postnetAndUpuAndUspsPlusBnB";
    public static final String POSTAL_2D_MODE_POSTNET_AND_UPU_PLUS_BNB = "postnetAndUpuPlusBnB";
    public static final String POSTAL_2D_MODE_POSTNET_AND_USPS = "postnetAndUsps";
    public static final String POSTAL_2D_MODE_POSTNET_AND_USPS_PLUS_BNB = "postnetAndUspsPlusBnB";
    public static final String POSTAL_2D_MODE_POSTNET_PLUS_BNB = "postnetPlusBnB";
    public static final String POSTAL_2D_MODE_UPU = "upu";
    public static final String POSTAL_2D_MODE_UPU_AND_USPS = "upuAndUsps";
    public static final String POSTAL_2D_MODE_USPS = "usps";
    public static final String POSTAL_OCR_MODE_INVERSE = "inverseVideo";
    public static final String POSTAL_OCR_MODE_NORMAL = "normalVideo";
    public static final String POSTAL_OCR_MODE_NORMAL_AND_INVERSE = "normalAndInverseVideo";
    public static final String POSTAL_OCR_MODE_OFF = "off";
    public static final String PROPERTY_AZTEC_ENABLED = "DEC_AZTEC_ENABLED";
    public static final String PROPERTY_AZTEC_MAXIMUM_LENGTH = "DEC_AZTEC_MAX_LENGTH";
    public static final String PROPERTY_AZTEC_MINIMUM_LENGTH = "DEC_AZTEC_MIN_LENGTH";
    public static final String PROPERTY_CENTER_DECODE = "DEC_WINDOW_MODE";
    public static final String PROPERTY_CHINA_POST_ENABLED = "DEC_HK25_ENABLED";
    public static final String PROPERTY_CHINA_POST_MAXIMUM_LENGTH = "DEC_HK25_MAX_LENGTH";
    public static final String PROPERTY_CHINA_POST_MINIMUM_LENGTH = "DEC_HK25_MIN_LENGTH";
    public static final String PROPERTY_CODABAR_CHECK_DIGIT_MODE = "DEC_CODABAR_CHECK_DIGIT_MODE";
    public static final String PROPERTY_CODABAR_CONCAT_ENABLED = "DEC_CODABAR_CONCAT_ENABLED";
    public static final String PROPERTY_CODABAR_ENABLED = "DEC_CODABAR_ENABLED";
    public static final String PROPERTY_CODABAR_MAXIMUM_LENGTH = "DEC_CODABAR_MAX_LENGTH";
    public static final String PROPERTY_CODABAR_MINIMUM_LENGTH = "DEC_CODABAR_MIN_LENGTH";
    public static final String PROPERTY_CODABAR_START_STOP_TRANSMIT_ENABLED = "DEC_CODABAR_START_STOP_TRANSMIT";
    public static final String PROPERTY_CODABLOCK_A_ENABLED = "DEC_CODABLOCK_A_ENABLED";
    public static final String PROPERTY_CODABLOCK_A_MAXIMUM_LENGTH = "DEC_CODABLOCK_A_MAX_LENGTH";
    public static final String PROPERTY_CODABLOCK_A_MINIMUM_LENGTH = "DEC_CODABLOCK_A_MIN_LENGTH";
    public static final String PROPERTY_CODABLOCK_F_ENABLED = "DEC_CODABLOCK_F_ENABLED";
    public static final String PROPERTY_CODABLOCK_F_MAXIMUM_LENGTH = "DEC_CODABLOCK_F_MAX_LENGTH";
    public static final String PROPERTY_CODABLOCK_F_MINIMUM_LENGTH = "DEC_CODABLOCK_F_MIN_LENGTH";
    public static final String PROPERTY_CODE_11_CHECK_DIGIT_MODE = "DEC_CODE11_CHECK_DIGIT_MODE";
    public static final String PROPERTY_CODE_11_ENABLED = "DEC_CODE11_ENABLED";
    public static final String PROPERTY_CODE_11_MAXIMUM_LENGTH = "DEC_CODE11_MAX_LENGTH";
    public static final String PROPERTY_CODE_11_MINIMUM_LENGTH = "DEC_CODE11_MIN_LENGTH";
    public static final String PROPERTY_CODE_128_ENABLED = "DEC_CODE128_ENABLED";
    public static final String PROPERTY_CODE_128_FNC4_ENABLED = "DEC_C128_FNC4_ENABLED";
    public static final String PROPERTY_CODE_128_MAXIMUM_LENGTH = "DEC_CODE128_MAX_LENGTH";
    public static final String PROPERTY_CODE_128_MINIMUM_LENGTH = "DEC_CODE128_MIN_LENGTH";
    public static final String PROPERTY_CODE_128_SHORT_MARGIN = "DEC_C128_SHORT_MARGIN";
    public static final String PROPERTY_CODE_39_BASE_32_ENABLED = "DEC_CODE39_BASE32_ENABLED";
    public static final String PROPERTY_CODE_39_CHECK_DIGIT_MODE = "DEC_CODE39_CHECK_DIGIT_MODE";
    public static final String PROPERTY_CODE_39_ENABLED = "DEC_CODE39_ENABLED";
    public static final String PROPERTY_CODE_39_FULL_ASCII_ENABLED = "DEC_CODE39_FULL_ASCII_ENABLED";
    public static final String PROPERTY_CODE_39_MAXIMUM_LENGTH = "DEC_CODE39_MAX_LENGTH";
    public static final String PROPERTY_CODE_39_MINIMUM_LENGTH = "DEC_CODE39_MIN_LENGTH";
    public static final String PROPERTY_CODE_39_START_STOP_TRANSMIT_ENABLED = "DEC_CODE39_START_STOP_TRANSMIT";
    public static final String PROPERTY_CODE_93_ENABLED = "DEC_CODE93_ENABLED";
    public static final String PROPERTY_CODE_93_MAXIMUM_LENGTH = "DEC_CODE93_MAX_LENGTH";
    public static final String PROPERTY_CODE_93_MINIMUM_LENGTH = "DEC_CODE93_MIN_LENGTH";
    public static final String PROPERTY_CODE_DOTCODE_ENABLED = "DEC_DOTCODE_ENABLED";
    public static final String PROPERTY_CODE_DOTCODE_MAXIMUM_LENGTH = "DEC_DOTCODE_MAX_LENGTH";
    public static final String PROPERTY_CODE_DOTCODE_MINIMUM_LENGTH = "DEC_DOTCODE_MIN_LENGTH";
    public static final String PROPERTY_COMBINE_COMPOSITES = "DEC_COMBINE_COMPOSITES";
    public static final String PROPERTY_COMPOSITE_ENABLED = "DEC_COMPOSITE_ENABLED";
    public static final String PROPERTY_COMPOSITE_MAXIMUM_LENGTH = "DEC_COMPOSITE_MAX_LENGTH";
    public static final String PROPERTY_COMPOSITE_MINIMUM_LENGTH = "DEC_COMPOSITE_MIN_LENGTH";
    public static final String PROPERTY_COMPOSITE_WITH_UPC_ENABLED = "DEC_COMPOSITE_WITH_UPC_ENABLED";
    public static final String PROPERTY_DATAMATRIX_ENABLED = "DEC_DATAMATRIX_ENABLED";
    public static final String PROPERTY_DATAMATRIX_MAXIMUM_LENGTH = "DEC_DATAMATRIX_MAX_LENGTH";
    public static final String PROPERTY_DATAMATRIX_MINIMUM_LENGTH = "DEC_DATAMATRIX_MIN_LENGTH";
    public static final String PROPERTY_DATA_PROCESSOR_CHARSET = "DPR_CHARSET";
    public static final String PROPERTY_DATA_PROCESSOR_DATA_INTENT = "DPR_DATA_INTENT";
    public static final String PROPERTY_DATA_PROCESSOR_DATA_INTENT_ACTION = "DPR_DATA_INTENT_ACTION";
    public static final String PROPERTY_DATA_PROCESSOR_DATA_INTENT_CATEGORY = "DPR_DATA_INTENT_CATEGORY";
    public static final String PROPERTY_DATA_PROCESSOR_DATA_INTENT_CLASS_NAME = "DPR_DATA_INTENT_CLASS_NAME";
    public static final String PROPERTY_DATA_PROCESSOR_DATA_INTENT_PACKAGE_NAME = "DPR_DATA_INTENT_PACKAGE_NAME";
    public static final String PROPERTY_DATA_PROCESSOR_EDIT_DATA_PLUGIN = "DPR_EDIT_DATA_PLUGIN";
    public static final String PROPERTY_DATA_PROCESSOR_LAUNCH_BROWSER = "DPR_LAUNCH_BROWSER";
    public static final String PROPERTY_DATA_PROCESSOR_LAUNCH_EZ_CONFIG = "DPR_LAUNCH_EZ_CONFIG";
    public static final String PROPERTY_DATA_PROCESSOR_PREFIX = "DPR_PREFIX";
    public static final String PROPERTY_DATA_PROCESSOR_SCAN_TO_INTENT = "DPR_SCAN_TO_INTENT";
    public static final String PROPERTY_DATA_PROCESSOR_SUFFIX = "DPR_SUFFIX";
    public static final String PROPERTY_DATA_PROCESSOR_SYMBOLOGY_PREFIX = "DPR_SYMBOLOGY_PREFIX";
    public static final String PROPERTY_DECODER_TIMEOUT = "DEC_DECODER_TIMEOUT";
    public static final String PROPERTY_DECODE_MOBILE_READ_ENABLE = "DECODE_MOBILE_READ_ENABLE";
    public static final String PROPERTY_DECODE_SECURITY_LEVEL = "DEC_SECURITY_LEVEL";
    public static final String PROPERTY_DECODE_WINDOW_BOTTOM = "DEC_WINDOW_BOTTOM";
    public static final String PROPERTY_DECODE_WINDOW_LEFT = "DEC_WINDOW_LEFT";
    public static final String PROPERTY_DECODE_WINDOW_RIGHT = "DEC_WINDOW_RIGHT";
    public static final String PROPERTY_DECODE_WINDOW_TOP = "DEC_WINDOW_TOP";
    public static final String PROPERTY_DEC_CODE93_HIGH_DENSITY = "DEC_CODE93_HIGH_DENSITY";
    public static final String PROPERTY_DEC_DPM_ENABLED = "DEC_DPM_ENABLED";
    public static final String PROPERTY_DEC_ID_PROP_USE_ROI = "DEC_ID_PROP_USE_ROI";
    public static final String PROPERTY_DIGIMARC_CONVERSION = "DEC_DIGIMARC_CONVERSION";
    public static final String PROPERTY_DIGIMARC_ENABLED = "DEC_DIGIMARC_ENABLED";
    public static final String PROPERTY_DIGIMARC_NON_RETAIL_CODES = "DEC_DIGIMARC_NON_RETAIL_CODES";
    public static final String PROPERTY_DIGIMARC_SCALE_BLOCKS = "DEC_DIGIMARC_SCALE_BLOCKS";
    public static final String PROPERTY_DIGIMARC_SHAPE_DETECTION = "DEC_DIGIMARC_SHAPE_DETECTION";
    public static final String PROPERTY_EANUCC_EMULATION_MODE = "DEC_EANUCC_EMULATION_MODE";
    public static final String PROPERTY_EAN_13_ADDENDA_REQUIRED_ENABLED = "DEC_EAN13_ADDENDA_REQUIRED";
    public static final String PROPERTY_EAN_13_ADDENDA_SEPARATOR_ENABLED = "DEC_EAN13_ADDENDA_SEPARATOR";
    public static final String PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED = "DEC_EAN13_CHECK_DIGIT_TRANSMIT";
    public static final String PROPERTY_EAN_13_ENABLED = "DEC_EAN13_ENABLED";
    public static final String PROPERTY_EAN_13_FIVE_CHAR_ADDENDA_ENABLED = "DEC_EAN13_5CHAR_ADDENDA_ENABLED";
    public static final String PROPERTY_EAN_13_TWO_CHAR_ADDENDA_ENABLED = "DEC_EAN13_2CHAR_ADDENDA_ENABLED";
    public static final String PROPERTY_EAN_8_ADDENDA_REQUIRED_ENABLED = "DEC_EAN8_ADDENDA_REQUIRED";
    public static final String PROPERTY_EAN_8_ADDENDA_SEPARATOR_ENABLED = "DEC_EAN8_ADDENDA_SEPARATOR";
    public static final String PROPERTY_EAN_8_CHECK_DIGIT_TRANSMIT_ENABLED = "DEC_EAN8_CHECK_DIGIT_TRANSMIT";
    public static final String PROPERTY_EAN_8_ENABLED = "DEC_EAN8_ENABLED";
    public static final String PROPERTY_EAN_8_FIVE_CHAR_ADDENDA_ENABLED = "DEC_EAN8_5CHAR_ADDENDA_ENABLED";
    public static final String PROPERTY_EAN_8_TWO_CHAR_ADDENDA_ENABLED = "DEC_EAN8_2CHAR_ADDENDA_ENABLED";
    public static final String PROPERTY_GRIDMATRIX_ENABLED = "DEC_GRIDMATRIX_ENABLED";
    public static final String PROPERTY_GRIDMATRIX_MAXIMUM_LENGTH = "DEC_GRIDMATRIX_MAX_LENGTH";
    public static final String PROPERTY_GRIDMATRIX_MINIMUM_LENGTH = "DEC_GRIDMATRIX_MIN_LENGTH";
    public static final String PROPERTY_GROUP_DATA_PROCESSING = "DATA_PROCESSING_SETTINGS";
    public static final String PROPERTY_GROUP_IMAGER = "IMAGER_SETTINGS";
    public static final String PROPERTY_GROUP_NOTIFICATION = "NOTIFICATION_SETTINGS";
    public static final String PROPERTY_GROUP_SYMBOLOGY = "SYMBOLOGY_SETTINGS";
    public static final String PROPERTY_GROUP_TRIGGER = "TRIGGER_SETTINGS";
    public static final String PROPERTY_GS1_128_ENABLED = "DEC_GS1_128_ENABLED";
    public static final String PROPERTY_GS1_128_MAXIMUM_LENGTH = "DEC_GS1_128_MAX_LENGTH";
    public static final String PROPERTY_GS1_128_MINIMUM_LENGTH = "DEC_GS1_128_MIN_LENGTH";
    public static final String PROPERTY_HAX_XIN_ENABLED = "DEC_HANXIN_ENABLED";
    public static final String PROPERTY_HAX_XIN_MAXIMUM_LENGTH = "DEC_HANXIN_MAX_LENGTH";
    public static final String PROPERTY_HAX_XIN_MINIMUM_LENGTH = "DEC_HANXIN_MIN_LENGTH";
    public static final String PROPERTY_IATA_25_ENABLED = "DEC_IATA25_ENABLED";
    public static final String PROPERTY_IATA_25_MAXIMUM_LENGTH = "DEC_IATA25_MAX_LENGTH";
    public static final String PROPERTY_IATA_25_MINIMUM_LENGTH = "DEC_IATA25_MIN_LENGTH";
    public static final String PROPERTY_IMAGER_EXPOSURE = "IMG_EXPOSURE";
    public static final String PROPERTY_IMAGER_EXPOSURE_MODE = "IMG_EXPOSURE_MODE";
    public static final String PROPERTY_IMAGER_GAIN = "IMG_GAIN";
    public static final String PROPERTY_IMAGER_LIGHT_INTENSITY = "IMG_ILLUM_INTENSITY";
    public static final String PROPERTY_IMAGER_MAXIMUM_EXPOSURE = "IMG_MAX_EXPOSURE";
    public static final String PROPERTY_IMAGER_MAXIMUM_GAIN = "IMG_MAX_GAIN";
    public static final String PROPERTY_IMAGER_OVERRIDE_RECOMMENDED_VALUES = "IMG_OVERRIDE_RECOMMENDED_VALUES";
    public static final String PROPERTY_IMAGER_REJECTION_LIMIT = "IMG_REJECTION_LIMIT";
    public static final String PROPERTY_IMAGER_SAMPLE_METHOD = "IMG_SAMPLE_METHOD";
    public static final String PROPERTY_IMAGER_TARGET_ACCEPTABLE_OFFSET = "IMG_TARGET_ACCEPTABLE_OFFSET";
    public static final String PROPERTY_IMAGER_TARGET_PERCENTILE = "IMG_TARGET_PERCENTILE";
    public static final String PROPERTY_IMAGER_TARGET_VALUE = "IMG_TARGET_VALUE";
    public static final String PROPERTY_INTERLEAVED_25_CHECK_DIGIT_MODE = "DEC_I25_CHECK_DIGIT_MODE";
    public static final String PROPERTY_INTERLEAVED_25_ENABLED = "DEC_I25_ENABLED";
    public static final String PROPERTY_INTERLEAVED_25_MAXIMUM_LENGTH = "DEC_I25_MAX_LENGTH";
    public static final String PROPERTY_INTERLEAVED_25_MINIMUM_LENGTH = "DEC_I25_MIN_LENGTH";
    public static final String PROPERTY_ISBT_128_ENABLED = "DEC_C128_ISBT_ENABLED";
    public static final String PROPERTY_KOREAN_POST_ENABLED = "DEC_KOREA_POST_ENABLED";
    public static final String PROPERTY_KOREAN_POST_MAXIMUM_LENGTH = "DEC_KOREA_POST_MAX_LENGTH";
    public static final String PROPERTY_KOREAN_POST_MINIMUM_LENGTH = "DEC_KOREA_POST_MIN_LENGTH";
    public static final String PROPERTY_LINEAR_DAMAGE_IMPROVEMENTS = "DEC_LINEAR_DAMAGE_IMPROVEMENTS";
    public static final String PROPERTY_MATRIX_25_ENABLED = "DEC_M25_ENABLED";
    public static final String PROPERTY_MATRIX_25_MAXIMUM_LENGTH = "DEC_M25_MAX_LENGTH";
    public static final String PROPERTY_MATRIX_25_MINIMUM_LENGTH = "DEC_M25_MIN_LENGTH";
    public static final String PROPERTY_MAXICODE_ENABLED = "DEC_MAXICODE_ENABLED";
    public static final String PROPERTY_MAXICODE_MAXIMUM_LENGTH = "DEC_MAXICODE_MAX_LENGTH";
    public static final String PROPERTY_MAXICODE_MINIMUM_LENGTH = "DEC_MAXICODE_MIN_LENGTH";
    public static final String PROPERTY_MICRO_PDF_417_ENABLED = "DEC_MICROPDF_ENABLED";
    public static final String PROPERTY_MICRO_PDF_417_MAXIMUM_LENGTH = "DEC_MICROPDF_MAX_LENGTH";
    public static final String PROPERTY_MICRO_PDF_417_MINIMUM_LENGTH = "DEC_MICROPDF_MIN_LENGTH";
    public static final String PROPERTY_MSI_CHECK_DIGIT_MODE = "DEC_MSI_CHECK_DIGIT_MODE";
    public static final String PROPERTY_MSI_ENABLED = "DEC_MSI_ENABLED";
    public static final String PROPERTY_MSI_MAXIMUM_LENGTH = "DEC_MSI_MAX_LENGTH";
    public static final String PROPERTY_MSI_MINIMUM_LENGTH = "DEC_MSI_MIN_LENGTH";
    public static final String PROPERTY_MSI_OUT_OF_SPEC_SYMBOL = "DEC_PROP_MSIP_OUT_OF_SPEC_SYMBOL";
    public static final String PROPERTY_MSI_SHORT_MARGIN = "DEC_MSIP_SHORT_MARGIN";
    public static final String PROPERTY_NOTIFICATION_BAD_READ_ENABLED = "NTF_BAD_READ_ENABLED";
    public static final String PROPERTY_NOTIFICATION_GOOD_READ_ENABLED = "NTF_GOOD_READ_ENABLED";
    public static final String PROPERTY_NOTIFICATION_VIBRATE_ENABLED = "NTF_VIBRATE_ENABLED";
    public static final String PROPERTY_OCR_ACTIVE_TEMPLATE = "DEC_OCR_ACTIVE_TEMPLATES";
    public static final String PROPERTY_OCR_MODE = "DEC_OCR_MODE";
    public static final String PROPERTY_OCR_TEMPLATE = "DEC_OCR_TEMPLATE";
    public static final String PROPERTY_PDF_417_ENABLED = "DEC_PDF417_ENABLED";
    public static final String PROPERTY_PDF_417_MAXIMUM_LENGTH = "DEC_PDF417_MAX_LENGTH";
    public static final String PROPERTY_PDF_417_MINIMUM_LENGTH = "DEC_PDF417_MIN_LENGTH";
    public static final String PROPERTY_POSTAL_2D_MODE = "DEC_POSTAL_ENABLED";
    public static final String PROPERTY_POSTAL_2D_PLANET_CHECK_DIGIT_TRANSMIT_ENABLED = "DEC_PLANETCODE_CHECK_DIGIT_TRANSMIT";
    public static final String PROPERTY_POSTAL_2D_POSTNET_CHECK_DIGIT_TRANSMIT_ENABLED = "DEC_POSTNET_CHECK_DIGIT_TRANSMIT";
    public static final String PROPERTY_QR_CODE_ENABLED = "DEC_QR_ENABLED";
    public static final String PROPERTY_QR_CODE_MAXIMUM_LENGTH = "DEC_QR_MAX_LENGTH";
    public static final String PROPERTY_QR_CODE_MINIMUM_LENGTH = "DEC_QR_MIN_LENGTH";
    public static final String PROPERTY_RSS_ENABLED = "DEC_RSS_14_ENABLED";
    public static final String PROPERTY_RSS_EXPANDED_ENABLED = "DEC_RSS_EXPANDED_ENABLED";
    public static final String PROPERTY_RSS_EXPANDED_MAXIMUM_LENGTH = "DEC_RSS_EXPANDED_MAX_LENGTH";
    public static final String PROPERTY_RSS_EXPANDED_MINIMUM_LENGTH = "DEC_RSS_EXPANDED_MIN_LENGTH";
    public static final String PROPERTY_RSS_LIMITED_ENABLED = "DEC_RSS_LIMITED_ENABLED";
    public static final String PROPERTY_SENDMENU = "PROPERTY_SENDMENU";
    public static final String PROPERTY_STANDARD_25_ENABLED = "DEC_S25_ENABLED";
    public static final String PROPERTY_STANDARD_25_MAXIMUM_LENGTH = "DEC_S25_MAX_LENGTH";
    public static final String PROPERTY_STANDARD_25_MINIMUM_LENGTH = "DEC_S25_MIN_LENGTH";
    public static final String PROPERTY_TELEPEN_ENABLED = "DEC_TELEPEN_ENABLED";
    public static final String PROPERTY_TELEPEN_MAXIMUM_LENGTH = "DEC_TELEPEN_MAX_LENGTH";
    public static final String PROPERTY_TELEPEN_MINIMUM_LENGTH = "DEC_TELEPEN_MIN_LENGTH";
    public static final String PROPERTY_TELEPEN_OLD_STYLE_ENABLED = "DEC_TELEPEN_OLD_STYLE";
    public static final String PROPERTY_TLC_39_ENABLED = "DEC_TLC39_ENABLED";
    public static final String PROPERTY_TRIGGER_AUTO_MODE_TIMEOUT = "TRIG_AUTO_MODE_TIMEOUT";
    public static final String PROPERTY_TRIGGER_CONTROL_MODE = "TRIG_CONTROL_MODE";
    public static final String PROPERTY_TRIGGER_ENABLE = "TRIG_ENABLE";
    public static final String PROPERTY_TRIGGER_SCAN_DELAY = "TRIG_SCAN_DELAY";
    public static final String PROPERTY_TRIGGER_SCAN_MODE = "TRIG_SCAN_MODE";
    public static final String PROPERTY_TRIGGER_SCAN_SAME_SYMBOL_TIMEOUT = "TRIG_SCAN_SAME_SYMBOL_TIMEOUT";
    public static final String PROPERTY_TRIGGER_SCAN_SAME_SYMBOL_TIMEOUT_ENABLED = "TRIG_SCAN_SAME_SYMBOL_TIMEOUT_ENABLED";
    public static final String PROPERTY_TRIOPTIC_ENABLED = "DEC_TRIOPTIC_ENABLED";
    public static final String PROPERTY_UPC_A_ADDENDA_REQUIRED_ENABLED = "DEC_UPCA_ADDENDA_REQUIRED";
    public static final String PROPERTY_UPC_A_ADDENDA_SEPARATOR_ENABLED = "DEC_UPCA_ADDENDA_SEPARATOR";
    public static final String PROPERTY_UPC_A_CHECK_DIGIT_TRANSMIT_ENABLED = "DEC_UPCA_CHECK_DIGIT_TRANSMIT";
    public static final String PROPERTY_UPC_A_COMBINE_COUPON_CODE_MODE_ENABLED = "DEC_COMBINE_COUPON_CODES";
    public static final String PROPERTY_UPC_A_COUPON_CODE_MODE_ENABLED = "DEC_COUPON_CODE_MODE";
    public static final String PROPERTY_UPC_A_ENABLE = "DEC_UPCA_ENABLE";
    public static final String PROPERTY_UPC_A_FIVE_CHAR_ADDENDA_ENABLED = "DEC_UPCA_5CHAR_ADDENDA_ENABLED";
    public static final String PROPERTY_UPC_A_NUMBER_SYSTEM_TRANSMIT_ENABLED = "DEC_UPCA_NUMBER_SYSTEM_TRANSMIT";
    public static final String PROPERTY_UPC_A_TRANSLATE_EAN13 = "DEC_UPCA_TRANSLATE_TO_EAN13";
    public static final String PROPERTY_UPC_A_TWO_CHAR_ADDENDA_ENABLED = "DEC_UPCA_2CHAR_ADDENDA_ENABLED";
    public static final String PROPERTY_UPC_E_ADDENDA_REQUIRED_ENABLED = "DEC_UPCE_ADDENDA_REQUIRED";
    public static final String PROPERTY_UPC_E_ADDENDA_SEPARATOR_ENABLED = "DEC_UPCE_ADDENDA_SEPARATOR";
    public static final String PROPERTY_UPC_E_CHECK_DIGIT_TRANSMIT_ENABLED = "DEC_UPCE_CHECK_DIGIT_TRANSMIT";
    public static final String PROPERTY_UPC_E_E1_ENABLED = "DEC_UPCE1_ENABLED";
    public static final String PROPERTY_UPC_E_ENABLED = "DEC_UPCE0_ENABLED";
    public static final String PROPERTY_UPC_E_EXPAND_TO_UPC_A = "DEC_UPCE_EXPAND";
    public static final String PROPERTY_UPC_E_FIVE_CHAR_ADDENDA_ENABLED = "DEC_UPCE_5CHAR_ADDENDA_ENABLED";
    public static final String PROPERTY_UPC_E_NUMBER_SYSTEM_TRANSMIT_ENABLED = "DEC_UPCE_NUMBER_SYSTEM_TRANSMIT";
    public static final String PROPERTY_UPC_E_TWO_CHAR_ADDENDA_ENABLED = "DEC_UPCE_2CHAR_ADDENDA_ENABLED";
    public static final String PROPERTY_VIDEO_REVERSE_ENABLED = "DEC_VIDEO_REVERSE_ENABLED";
    public static final String SHORT_MARGIN_DISABLED = "disabled";
    public static final String SHORT_MARGIN_ENABLED = "partial";
    public static final String SHORT_MARGIN_ENABLE_BOTH_ENDS = "full";
    public static final String TRIGGER_CONTROL_MODE_AUTO_CONTROL = "autoControl";
    public static final String TRIGGER_CONTROL_MODE_CLIENT_CONTROL = "clientControl";
    public static final String TRIGGER_CONTROL_MODE_DISABLE = "disable";
    public static final String TRIGGER_SCAN_MODE_CONTINUOUS = "continuous";
    public static final String TRIGGER_SCAN_MODE_ONESHOT = "oneShot";
    public static final String TRIGGER_SCAN_MODE_READ_ON_RELEASE = "readOnRelease";
    public static final String TRIGGER_SCAN_MODE_READ_ON_SECOND_TRIGGER_PRESS = "readOnSecondTriggerPress";
    public static final String VIDEO_REVERSE_ENABLED_BOTH = "both";
    public static final String VIDEO_REVERSE_ENABLED_INVERSE = "inverse";
    public static final String VIDEO_REVERSE_ENABLED_NORMAL = "normal";
    private boolean mBarcodeReaderClosed = false;
    private final IExecutor mExecutor;
    private static Map<Class<?>, Map<Object, IExecutor>> sListeners = new HashMap();
    private static Map<Class<?>, Map<Object, Integer>> sListenerCounts = new HashMap();

    public interface BarcodeListener extends EventListener {
        void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent);

        void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent);
    }

    /* JADX INFO: loaded from: classes3.dex */
    public interface MenuCommandListener extends EventListener {
        void onMenuCommandResponseEvent(MenuCommandEvent menuCommandEvent);
    }

    public interface TriggerListener extends EventListener {
        void onTriggerEvent(TriggerStateChangeEvent triggerStateChangeEvent);
    }

    static {
        sListeners.put(BarcodeListener.class, new HashMap());
        sListeners.put(TriggerListener.class, new HashMap());
        sListeners.put(MenuCommandListener.class, new HashMap());
        sListenerCounts.put(BarcodeListener.class, new HashMap());
        sListenerCounts.put(TriggerListener.class, new HashMap());
        sListenerCounts.put(MenuCommandListener.class, new HashMap());
        CREATOR = new Parcelable.Creator<BarcodeReader>() { // from class: com.honeywell.aidc.BarcodeReader.2
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public BarcodeReader createFromParcel(Parcel parcel) {
                return new BarcodeReader(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public BarcodeReader[] newArray(int i) {
                return new BarcodeReader[i];
            }
        };
    }

    BarcodeReader(IExecutor iExecutor) {
        DebugLog.d("Enter BarcodeReader constructor");
        this.mExecutor = iExecutor;
        DebugLog.d("Exit BarcodeReader constructor");
    }

    private void addListener(final Object obj, final Class<?> cls) {
        IExecutor iExecutor;
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("BarcodeReader is closed");
        }
        synchronized (sListeners) {
            try {
                Map<Object, IExecutor> map = sListeners.get(cls);
                iExecutor = null;
                if (map != null) {
                    IExecutor iExecutor2 = map.get(obj);
                    if (iExecutor2 == null) {
                        iExecutor = new IExecutor.Stub() { // from class: com.honeywell.aidc.BarcodeReader.1
                            @Override // com.honeywell.IExecutor
                            public Message execute(Message message) {
                                EventObject event = DcsJsonRpcHelper.getEvent(BarcodeReader.this, message);
                                if ((event instanceof BarcodeReadEvent) && BarcodeListener.class.equals(cls)) {
                                    ((BarcodeListener) obj).onBarcodeEvent((BarcodeReadEvent) event);
                                    return null;
                                }
                                if ((event instanceof BarcodeFailureEvent) && BarcodeListener.class.equals(cls)) {
                                    ((BarcodeListener) obj).onFailureEvent((BarcodeFailureEvent) event);
                                    return null;
                                }
                                if ((event instanceof TriggerStateChangeEvent) && TriggerListener.class.equals(cls)) {
                                    ((TriggerListener) obj).onTriggerEvent((TriggerStateChangeEvent) event);
                                    return null;
                                }
                                if (!(event instanceof MenuCommandEvent) || !MenuCommandListener.class.equals(cls)) {
                                    return null;
                                }
                                ((MenuCommandListener) obj).onMenuCommandResponseEvent((MenuCommandEvent) event);
                                return null;
                            }

                            @Override // com.honeywell.IExecutor
                            public void executeAsync(Message message, IExecutor iExecutor3) {
                                execute(message);
                            }
                        };
                        incrementListeners(cls, obj, iExecutor);
                    } else {
                        incrementListeners(cls, obj, null);
                        iExecutor = iExecutor2;
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        Message messageBuild = DcsJsonRpcHelper.build("scanner.addListener");
        messageBuild.extras.put("listener", iExecutor);
        DcsJsonRpcHelper.checkRuntimeError(execute(messageBuild));
    }

    private void decrementListeners(Class<?> cls, Object obj) {
        Map<Object, Integer> map = sListenerCounts.get(cls);
        if (map != null) {
            int iIntValue = map.get(obj).intValue();
            if (iIntValue != 1) {
                map.put(obj, Integer.valueOf(iIntValue - 1));
                return;
            }
            Map<Object, IExecutor> map2 = sListeners.get(cls);
            if (map2 != null) {
                map2.remove(obj);
                map.remove(obj);
            }
        }
    }

    private <T> T getTypedProperty(String str, Class<T> cls) throws UnsupportedPropertyException {
        if (str == null) {
            throw new IllegalArgumentException("Parameters cannot be null.");
        }
        TreeSet treeSet = new TreeSet();
        treeSet.add(str);
        Map<String, Object> properties = getProperties(treeSet);
        if (!properties.containsKey(str)) {
            throw new UnsupportedPropertyException("Property not found: " + str);
        }
        if (cls.isAssignableFrom(properties.get(str).getClass())) {
            return cls.cast(properties.get(str));
        }
        throw new RuntimeException("Property is not of type " + cls.getSimpleName());
    }

    private void incrementListeners(Class<?> cls, Object obj, IExecutor iExecutor) {
        Map<Object, Integer> map = sListenerCounts.get(cls);
        if (map != null) {
            if (iExecutor == null) {
                map.put(obj, Integer.valueOf(map.get(obj).intValue() + 1));
                return;
            }
            Map<Object, IExecutor> map2 = sListeners.get(cls);
            if (map2 != null) {
                map2.put(obj, iExecutor);
                map.put(obj, 1);
            }
        }
    }

    private Map<String, Object> internalGetProperties(Set<String> set) {
        return internalGetProperties(set, false);
    }

    private void removeListener(Object obj, Class<?> cls) {
        synchronized (sListeners) {
            try {
                IExecutor iExecutor = sListeners.get(cls).get(obj);
                if (iExecutor == null) {
                    return;
                }
                decrementListeners(cls, obj);
                Message messageBuild = DcsJsonRpcHelper.build("scanner.removeListener");
                messageBuild.extras.put("listener", iExecutor);
                DcsJsonRpcHelper.checkRuntimeError(execute(messageBuild));
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public void addBarcodeListener(BarcodeListener barcodeListener) {
        addListener(barcodeListener, BarcodeListener.class);
    }

    public void addMenuCommandListener(MenuCommandListener menuCommandListener) {
        addListener(menuCommandListener, MenuCommandListener.class);
    }

    public void addTriggerListener(TriggerListener triggerListener) {
        addListener(triggerListener, TriggerListener.class);
    }

    public void aim(boolean z) throws ScannerNotClaimedException, ScannerUnavailableException {
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("BarcodeReader is closed");
        }
        Message messageExecute = execute(DcsJsonRpcHelper.build("scanner.aim", "state", Boolean.valueOf(z)));
        DcsJsonRpcHelper.checkScannerNotClaimedException(messageExecute);
        DcsJsonRpcHelper.checkScannerUnavailable(messageExecute);
        DcsJsonRpcHelper.checkRuntimeError(messageExecute);
    }

    public Bitmap captureImage() {
        Message messageExecute = execute(DcsJsonRpcHelper.build("scanner.captureImage"));
        DcsJsonRpcHelper.checkRuntimeError(messageExecute);
        return (Bitmap) messageExecute.extras.get("image");
    }

    public void claim() throws ScannerUnavailableException {
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("BarcodeReader is closed");
        }
        Message messageExecute = execute(DcsJsonRpcHelper.build("scanner.claim"));
        DcsJsonRpcHelper.checkScannerUnavailable(messageExecute);
        DcsJsonRpcHelper.checkRuntimeError(messageExecute);
    }

    public void close() {
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("BarcodeReader is closed");
        }
        this.mBarcodeReaderClosed = true;
        DcsJsonRpcHelper.checkRuntimeError(execute(DcsJsonRpcHelper.build("scanner.disconnect")));
    }

    public void decode(boolean z) throws ScannerNotClaimedException, ScannerUnavailableException {
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("BarcodeReader is closed");
        }
        Message messageExecute = execute(DcsJsonRpcHelper.build("scanner.decode", "state", Boolean.valueOf(z)));
        DcsJsonRpcHelper.checkScannerNotClaimedException(messageExecute);
        DcsJsonRpcHelper.checkScannerUnavailable(messageExecute);
        DcsJsonRpcHelper.checkRuntimeError(messageExecute);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    Message execute(Message message) {
        try {
            return this.mExecutor.execute(message);
        } catch (RemoteException e) {
            throw new RuntimeException("An error occurred while communicating with the scanner service.", e);
        }
    }

    public Map<String, Object> getAllDefaultProperties() {
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("BarcodeReader is closed");
        }
        return internalGetProperties(new HashSet(), true);
    }

    public Map<String, Object> getAllProperties() {
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("BarcodeReader is closed");
        }
        return internalGetProperties(new HashSet());
    }

    public boolean getBooleanProperty(String str) {
        return ((Boolean) getTypedProperty(str, Boolean.class)).booleanValue();
    }

    public BarcodeReaderInfo getInfo() throws ScannerUnavailableException {
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("BarcodeReader is closed");
        }
        Message messageExecute = execute(DcsJsonRpcHelper.build("scanner.getInfo"));
        DcsJsonRpcHelper.checkScannerUnavailable(messageExecute);
        DcsJsonRpcHelper.checkRuntimeError(messageExecute);
        try {
            return DcsJsonRpcHelper.buildBarcodeReaderInfo(new JSONObject(messageExecute.action).getJSONObject("result").getJSONObject("info"));
        } catch (JSONException e) {
            throw new RuntimeException("Failed to retrieve barcode reader info", e);
        }
    }

    public int getIntProperty(String str) {
        return ((Integer) getTypedProperty(str, Integer.class)).intValue();
    }

    public Bitmap getLastImage(int i) {
        Message messageExecute = execute(DcsJsonRpcHelper.build("scanner.getLastImage", "sensorType", Integer.valueOf(i)));
        DcsJsonRpcHelper.checkRuntimeError(messageExecute);
        return (Bitmap) messageExecute.extras.get("image");
    }

    public List<String> getProfileNames() {
        DebugLog.d("Enter getProfileNames");
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("getProfileNames, BarcodeReader is closed");
        }
        ArrayList arrayList = new ArrayList();
        Message messageExecute = execute(DcsJsonRpcHelper.build("scanner.getProfileNames"));
        DcsJsonRpcHelper.checkRuntimeError(messageExecute);
        try {
            JSONArray jSONArray = new JSONObject(messageExecute.action).getJSONObject("result").getJSONArray("values");
            for (int i = 0; i < jSONArray.length(); i++) {
                arrayList.add(jSONArray.getJSONObject(i).getString("profile"));
            }
            DebugLog.d("Exit getProfileNames");
            return arrayList;
        } catch (JSONException e) {
            throw new RuntimeException("An error occurred while communicating with the scanner service.", e);
        }
    }

    public Map<String, Object> getProperties(Set<String> set) {
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("BarcodeReader is closed");
        }
        if (set != null) {
            return set.isEmpty() ? new HashMap() : internalGetProperties(set);
        }
        throw new IllegalArgumentException("Names set cannot be null.");
    }

    public Signature getSignature(SignatureParameters signatureParameters) throws ScannerNotClaimedException, ScannerUnavailableException {
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("BarcodeReader is closed");
        }
        HashMap map = new HashMap();
        map.put("aspectRatio", Integer.valueOf(signatureParameters.getAspectRatio()));
        map.put("horizontalOffset", Integer.valueOf(signatureParameters.getHorizontalOffset()));
        map.put("verticalOffset", Integer.valueOf(signatureParameters.getVerticalOffset()));
        map.put("width", Integer.valueOf(signatureParameters.getWidth()));
        map.put("height", Integer.valueOf(signatureParameters.getHeight()));
        map.put("resolution", Integer.valueOf(signatureParameters.getResolution()));
        map.put("binarized", Boolean.valueOf(signatureParameters.isBinarized()));
        Message messageExecute = execute(DcsJsonRpcHelper.build("scanner.getSignature", map));
        DcsJsonRpcHelper.checkScannerNotClaimedException(messageExecute);
        DcsJsonRpcHelper.checkScannerUnavailable(messageExecute);
        DcsJsonRpcHelper.checkRuntimeError(messageExecute);
        try {
            String string = new JSONObject(messageExecute.action).getJSONObject("result").getString("guidance");
            Map<String, Object> map2 = messageExecute.extras;
            return new Signature(string, (map2 == null || !map2.containsKey("image")) ? null : (Bitmap) messageExecute.extras.get("image"));
        } catch (JSONException e) {
            throw new RuntimeException("An error occurred while communicating with the scanner service.", e);
        }
    }

    public String getStringProperty(String str) {
        return (String) getTypedProperty(str, String.class);
    }

    public void light(boolean z) throws ScannerNotClaimedException, ScannerUnavailableException {
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("BarcodeReader is closed");
        }
        Message messageExecute = execute(DcsJsonRpcHelper.build("scanner.light", "state", Boolean.valueOf(z)));
        DcsJsonRpcHelper.checkScannerNotClaimedException(messageExecute);
        DcsJsonRpcHelper.checkScannerUnavailable(messageExecute);
        DcsJsonRpcHelper.checkRuntimeError(messageExecute);
    }

    public boolean loadProfile(String str) {
        DebugLog.d("Enter loadProfile");
        if (str == null || str.length() == 0) {
            DebugLog.d("loadProfile, profile param is empty");
            throw new IllegalArgumentException("profile param is empty");
        }
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("loadProfile, BarcodeReader is closed");
        }
        new HashMap();
        HashMap map = new HashMap();
        map.put("profile", str);
        Message messageExecute = execute(DcsJsonRpcHelper.build("scanner.loadProfile", map));
        DcsJsonRpcHelper.checkRuntimeError(messageExecute);
        try {
            boolean z = new JSONObject(messageExecute.action).getJSONObject("result").getJSONObject("values").getBoolean("profileFound");
            DebugLog.d("Exit loadProfile");
            return z;
        } catch (JSONException e) {
            throw new RuntimeException("An error occurred while communicating with the scanner service.", e);
        }
    }

    public void notify(String str) {
        DcsJsonRpcHelper.checkRuntimeError(execute(DcsJsonRpcHelper.build("scanner.notify", "notification", str)));
    }

    public void release() {
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("BarcodeReader is closed");
        }
        DcsJsonRpcHelper.checkRuntimeError(execute(DcsJsonRpcHelper.build("scanner.release")));
    }

    public void removeBarcodeListener(BarcodeListener barcodeListener) {
        removeListener(barcodeListener, BarcodeListener.class);
    }

    public void removeMenuCommandListener(MenuCommandListener menuCommandListener) {
        removeListener(menuCommandListener, MenuCommandListener.class);
    }

    public void removeTriggerListener(TriggerListener triggerListener) {
        removeListener(triggerListener, TriggerListener.class);
    }

    public void sendMenuCommand(String str, String str2) {
        if (str == null || str2 == null || str2.isEmpty() || str.isEmpty()) {
            throw new IllegalArgumentException("Parameters cannot be null.");
        }
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("BarcodeReader is closed");
        }
        HashMap map = new HashMap();
        map.put(str, str2);
        HashMap map2 = new HashMap();
        map2.put("values", map);
        DcsJsonRpcHelper.checkRuntimeError(execute(DcsJsonRpcHelper.build("scanner.sendMenuCommand", map2)));
    }

    public void setProperties(Map<String, Object> map) {
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("BarcodeReader is closed");
        }
        HashMap map2 = new HashMap();
        map2.put("values", map);
        DcsJsonRpcHelper.checkRuntimeError(execute(DcsJsonRpcHelper.build("scanner.setProperties", map2)));
    }

    public void setProperty(String str, int i) throws UnsupportedPropertyException {
        setProperty(str, Integer.valueOf(i));
        if (getIntProperty(str) != i) {
            throw new UnsupportedPropertyException("Property was rejected by the scanner service.");
        }
    }

    public void softwareTrigger(boolean z) throws ScannerNotClaimedException, ScannerUnavailableException {
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("BarcodeReader is closed");
        }
        Message messageExecute = execute(DcsJsonRpcHelper.build("internal.setTrigger", "state", Boolean.valueOf(z)));
        DcsJsonRpcHelper.checkScannerNotClaimedException(messageExecute);
        DcsJsonRpcHelper.checkScannerUnavailable(messageExecute);
        DcsJsonRpcHelper.checkRuntimeError(messageExecute);
    }

    public void startPropertyEditor(Context context) {
        startPropertyEditor(context, null, null);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStrongBinder(this.mExecutor.asBinder());
    }

    private Map<String, Object> internalGetProperties(Set<String> set, boolean z) {
        HashMap map = new HashMap();
        HashMap map2 = new HashMap();
        map2.put("names", set.toArray());
        Message messageExecute = execute(DcsJsonRpcHelper.build(z ? "scanner.getDefaultProperties" : "scanner.getProperties", map2));
        DcsJsonRpcHelper.checkRuntimeError(messageExecute);
        try {
            JSONObject jSONObject = new JSONObject(messageExecute.action).getJSONObject("result").getJSONObject("values");
            Iterator<String> itKeys = jSONObject.keys();
            while (itKeys.hasNext()) {
                String next = itKeys.next();
                map.put(next, jSONObject.get(next));
            }
            return map;
        } catch (JSONException e) {
            throw new RuntimeException("An error occurred while communicating with the scanner service.", e);
        }
    }

    public void startPropertyEditor(Context context, String str, String str2) {
        if (this.mBarcodeReaderClosed) {
            throw new IllegalStateException("BarcodeReader is closed");
        }
        Intent intent = new Intent(DecodeIntent.ACTION_EDIT_SETTINGS);
        intent.putExtra("barcodeReader", this);
        if (str != null) {
            intent.putExtra("propertyGroup", str);
        }
        if (str2 != null) {
            intent.putExtra("activityTitle", str2);
        }
        context.startActivity(intent);
    }

    public void setProperty(String str, boolean z) throws UnsupportedPropertyException {
        setProperty(str, Boolean.valueOf(z));
        if (getBooleanProperty(str) != z) {
            throw new UnsupportedPropertyException("Property was rejected by the scanner service.");
        }
    }

    BarcodeReader(Parcel parcel) {
        DebugLog.d("Enter BarcodeReader constructor");
        this.mExecutor = IExecutor.Stub.asInterface(parcel.readStrongBinder());
        DebugLog.d("Exit BarcodeReader constructor");
    }

    public void setProperty(String str, String str2) throws UnsupportedPropertyException {
        setProperty(str, (Object) str2);
        String stringProperty = getStringProperty(str);
        if (str2 != null && !str2.equals(stringProperty)) {
            throw new UnsupportedPropertyException("Property was rejected by the scanner service.");
        }
    }

    private void setProperty(String str, Object obj) {
        if (str != null && obj != null) {
            HashMap map = new HashMap();
            map.put(str, obj);
            setProperties(map);
            return;
        }
        throw new IllegalArgumentException("Parameters cannot be null.");
    }
}
