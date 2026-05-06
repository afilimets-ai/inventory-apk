package com.honeywell.aidc;

import com.honeywell.Message;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes3.dex */
public class DcsJsonRpcHelper {
    // JADX decompiler resolved this as ScanManager.DECODE_DATA_TAG; actual value from Honeywell AIDC SDK
    private static final String DECODE_DATA_TAG = "barcodeData";
    private static AtomicInteger sId = new AtomicInteger();

    public static Message build(String str, Map<String, Object> map) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("id", sId.incrementAndGet());
            jSONObject.put("jsonrpc", "2.0");
            jSONObject.put("method", str);
            jSONObject.put("params", JsonUtil.mapToJson(map));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Message message = new Message(jSONObject.toString());
        message.extras = new HashMap();
        return message;
    }

    static BarcodeReaderInfo buildBarcodeReaderInfo(JSONObject jSONObject) {
        String strOptString = "dcs.scanner.imager";
        if (jSONObject.has("IMG_SCANNER_NAME")) {
            strOptString = jSONObject.optString("IMG_SCANNER_NAME", "dcs.scanner.imager");
        } else if (jSONObject.has("scanner")) {
            jSONObject.optString("scanner", "dcs.scanner.imager");
            strOptString = "";
        }
        return new BarcodeReaderInfo(strOptString, jSONObject.optString("IMG_FRIENDLY_SCANNER_NAME", "Internal Scanner"), jSONObject.optString("IMG_SCAN_ENGINE", null), jSONObject.optString("DEC_REVISION_FULL_DECODER", null), jSONObject.optString("DEC_REVISION_FAST_DECODER", null), jSONObject.optString("DEC_REVISION_CONTROL_LOGIC", null), jSONObject.optString("IMG_SCAN_ENGINE_VERSION", null), jSONObject.optString("IMG_SCAN_ENGINE_FIRMWARE_VERSION", null), jSONObject.optString("IMG_SCAN_ENGINE_SERIAL_NUMBER", null), jSONObject.optInt("IMG_FRAME_HEIGHT"), jSONObject.optInt("IMG_FRAME_WIDTH"));
    }

    static void checkRuntimeAndScannerExceptions(Message message) throws InvalidScannerNameException {
        String string;
        try {
            if (message.action != null) {
                JSONObject jSONObject = new JSONObject(message.action);
                if (jSONObject.has("error")) {
                    JSONObject jSONObject2 = jSONObject.getJSONObject("error");
                    int i = jSONObject2.getInt("code");
                    if (i == -32602) {
                        throw new IllegalArgumentException("Invalid parameter(s) provided.");
                    }
                    if (i == -30002) {
                        throw new InvalidScannerNameException("Specified scanner name is invalid.");
                    }
                    string = jSONObject2.getString("message");
                } else {
                    string = null;
                }
            } else {
                string = "Action is null";
            }
            if (string != null) {
                throw new RuntimeException(string);
            }
        } catch (JSONException e) {
            throw new RuntimeException("An error occurred while communicating with the scanner service.", e);
        }
    }

    public static void checkRuntimeError(Message message) {
        String string;
        try {
            if (message.action != null) {
                JSONObject jSONObject = new JSONObject(message.action);
                if (jSONObject.has("error")) {
                    JSONObject jSONObject2 = jSONObject.getJSONObject("error");
                    if (jSONObject2.getInt("code") == -32602) {
                        throw new IllegalArgumentException("Invalid parameter(s) provided.");
                    }
                    string = jSONObject2.getString("message");
                } else {
                    string = null;
                }
            } else {
                string = "Action is null";
            }
            if (string != null) {
                throw new RuntimeException(string);
            }
        } catch (JSONException e) {
            throw new RuntimeException("An error occurred while communicating with the scanner service.", e);
        }
    }

    static void checkScannerNotClaimedException(Message message) throws ScannerNotClaimedException {
        try {
            JSONObject jSONObject = new JSONObject(message.action);
            if ((jSONObject.has("error") ? jSONObject.getJSONObject("error").getInt("code") : 0) == -30000) {
                throw new ScannerNotClaimedException("Scanner is not claimed.");
            }
        } catch (JSONException e) {
            throw new RuntimeException("An error occurred while communicating with the scanner service.", e);
        }
    }

    static void checkScannerUnavailable(Message message) throws ScannerUnavailableException {
        try {
            JSONObject jSONObject = new JSONObject(message.action);
            if ((jSONObject.has("error") ? jSONObject.getJSONObject("error").getInt("code") : 0) == -30001) {
                throw new ScannerUnavailableException("Specified scanner is unavailable.");
            }
        } catch (JSONException e) {
            throw new RuntimeException("An error occurred while communicating with the scanner service.", e);
        }
    }

    static EventObject getEvent(Object obj, Message message) {
        try {
            JSONObject jSONObject = new JSONObject(message.action);
            String string = jSONObject.getString("method");
            if (string.equals("scanner.barcodeEvent")) {
                JSONObject jSONObject2 = jSONObject.getJSONObject("params").getJSONObject(DECODE_DATA_TAG);
                return new BarcodeReadEvent(obj, jSONObject2.getString("data"), jSONObject2.getString("charset"), jSONObject2.getString("honeywellId"), jSONObject2.getString("aimId"), jSONObject2.getString("timestamp"), jSONObject2.has("bounds") ? jSONObject2.getString("bounds") : null);
            }
            if (string.equals("scanner.failureEvent")) {
                return new BarcodeFailureEvent(obj, jSONObject.getJSONObject("params").getString("timestamp"));
            }
            if (string.equals("scanner.triggerEvent")) {
                return new TriggerStateChangeEvent(string, jSONObject.getJSONObject("params").getBoolean("state"));
            }
            if (string.equals("internal.scannerDeviceEvent")) {
                JSONObject jSONObject3 = jSONObject.getJSONObject("params").getJSONObject("info");
                return new BarcodeDeviceConnectionEvent(string, buildBarcodeReaderInfo(jSONObject3), jSONObject3.optInt("IMG_CONNECTION_STATUS", 0));
            }
            if (string.equals("scanner.menuCommandResponse")) {
                return new MenuCommandEvent(string, jSONObject.getString("params"), 1);
            }
            return null;
        } catch (JSONException e) {
            throw new RuntimeException("An error occurred while communicating with the scanner service.", e);
        }
    }

    public static Message build(String str, String str2, Object obj) {
        HashMap map = new HashMap();
        map.put(str2, obj);
        return build(str, map);
    }

    public static Message build(String str) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("id", sId.incrementAndGet());
            jSONObject.put("jsonrpc", "2.0");
            jSONObject.put("method", str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Message message = new Message(jSONObject.toString());
        message.extras = new HashMap();
        return message;
    }
}
