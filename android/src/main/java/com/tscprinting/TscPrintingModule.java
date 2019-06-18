package com.tscprinting;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Callback;
import com.example.tscdll.TscWifiActivity;
import com.facebook.react.bridge.Promise;
import android.util.Log;
public class TscPrintingModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;
    static Double heightRatio = 1.0;
    static Double widthRatio = 1.0;

    public TscPrintingModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "TscPrinting";
    }

    @ReactMethod
    public void printLabel(ReadableMap config, ReadableMap data,Promise promise) {
        TscWifiActivity instance = new TscWifiActivity();

        try {
            prepareLabel(instance, config);
            printLayoutLabel(instance, data);
            printContentLabel(instance, data);
            instance.sendcommand("PRINT 1,1\n");
            instance.closeport(5000);
            promise.resolve("");
        } catch (Exception e) {
            promise.reject(e);
        }

    }


    @ReactMethod
    public void printTote(ReadableMap config, int a, int b, String size) {

        // try {
        //     TscWifiActivity instance = new TscWifiActivity();
        //     prepareLabel(instance, config);
        //     instance.closeport(5000);
        //     resolve.invoke();

        // } catch (Exception e) {
        //     resolve.invoke(e.getMessage());
        // }

        // TscWifiActivity TscEthernetDll = new TscWifiActivity();
        // TscEthernetDll.openport(ip, 9100);
        // TscEthernetDll.setup(100, 150, 4, 10, 0, 3, 0);
        // TscEthernetDll.sendcommand("DIRECTION 1\n");
        // int[] a1 = { 0, 0, 0, 0 };
        // int[] b1 = { 0, 0, 0, 0 };
        // int count = 0;
        // for (int i = a; i <= b; i++) {
        //     a1[count] = i;
        //     if (count == 3) {
        //         printTote2(TscEthernetDll, size, a1, b1, false);
        //         count = 0;
        //         a1[0] = 0;
        //         a1[1] = 0;
        //         a1[2] = 0;
        //         a1[3] = 0;
        //     } else {
        //         count++;
        //     }
        //     if (i == b&&count!=0) {
        //         printTote2(TscEthernetDll, size, a1, b1, false);
        //     }

        // }

        // TscEthernetDll.closeport(5000);
    }

    public void printTote2(TscWifiActivity TscEthernetDll,String block,int[] a, int[] b,boolean isShelf){
        String temp = "";
        TscEthernetDll.sendcommand("CLS\n");
        int x = 22,y = 280, text = 124, qrcode = 50;
        for(int i = 0;i<4;i++){
            if(a[i]!=0){
                if(isShelf){
                    temp = block+ "-"+String.valueOf(a[i]) +"-"+ String.valueOf(b[i]);
                }
                else temp = block + "-" + String.valueOf(a[i]);           
                TscEthernetDll.sendcommand("BOX 12,"+x+",790,"+y+",12,10\n");
                TscEthernetDll.sendcommand("TEXT 60,"+text+",\"G.FNT\",0,2,2,0,\"" + temp + "\" \n ");
                TscEthernetDll.sendcommand("QRCODE 555,"+qrcode+" ,H,10,M,0,M2, \"S" + temp + "\"\n");
                x+=(300);
                y+=(300);
                text +=300; qrcode += 300;
            }
        }
        TscEthernetDll.sendcommand("PRINT 1,1\n");
    }


    public void prepareLabel(TscWifiActivity instance, ReadableMap config) {
        String ip = config.getString("ip");
        heightRatio = config.getDouble("heightRatio");
        widthRatio = config.getDouble("widthRatio");
        Integer height = (int) (heightRatio * 150);
        Integer width = (int) (widthRatio * 100);
        instance.openport(ip, 9100);
        instance.setup(width, height, 4, 10, 0, 3, 0);
        instance.sendcommand("CLS\n");
        instance.sendcommand("CODEPAGE UTF-8\n");
        instance.sendcommand("DIRECTION 1\n");

    }

    public void sendcommand(TscWifiActivity instance, String content) {

    };

    public void printContentLabel(TscWifiActivity instance, ReadableMap a) {
        try {
        String content = "- Sách, văn hoá phẩm và văn phòng phẩm", header = "Fahasa.com - 1900 636467";

            instance.sendcommand(String.format("QRCODE %f,%f,H,%f,M,0,M2 ,\"S%s\"\n", 60 * widthRatio, 45 * heightRatio,
                    7 * widthRatio, a.getString("deliveryId")));
            instance.sendcommand(String.format("QRCODE %f,%f,H,%f,M,0,M2 ,\"S%s\"\n", 560 * widthRatio,
                    965 * heightRatio, 7 * widthRatio, a.getString("orderId").replace("_", "")));

            instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMA.TTF\",0,%f,%f,1,\"%s\" \n ", 300 * widthRatio,
                    45 * heightRatio, 10 * heightRatio, 10 * heightRatio, header));
            instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMA.TTF\",0,%f,%f,1,\"%s\" \n ", 300 * widthRatio,
                    85 * heightRatio, 10 * heightRatio, 10 * heightRatio, a.getString("date")));
            instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"%s\" \n ", 300 * widthRatio,
                    125 * heightRatio, 10 * heightRatio, 10 * heightRatio, a.getString("orderId")));
            instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMA.TTF\",0,%f,%f,1,\"%s\" \n ", 300 * widthRatio,
                    165 * heightRatio, 10 * heightRatio, 10 * heightRatio, a.getString("deliveryPartner")));
            instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"Người nhận:   \" \n ",
                    60 * widthRatio, 240 * heightRatio, 11 * heightRatio, 11 * heightRatio));
            instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMA.TTF\",0,%f,%f,1,\"%s\" \n ", 300 * widthRatio,
                    240 * heightRatio, 11 * heightRatio, 11 * heightRatio, a.getString("shippingName")));
            instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"SĐT:    \" \n ", 60 * widthRatio,
                    290 * heightRatio, 11 * heightRatio, 11 * heightRatio));
            instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMA.TTF\",0,%f,%f,1,\"%s\" \n ", 300 * widthRatio,
                    290 * heightRatio, 11 * heightRatio, 11 * heightRatio, a.getString("shippingPhone")));
            instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"Địa chỉ:\" \n ", 60 * widthRatio,
                    345 * heightRatio, 10 * heightRatio, 10 * heightRatio));
            instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"Quận/Huyện: \" \n ",
                    60 * widthRatio, 530 * heightRatio, 9 * heightRatio, 9 * heightRatio));
            instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"Tỉnh/TP: \" \n ",
                    420 * widthRatio, 530 * heightRatio, 9 * heightRatio, 9 * heightRatio));
            instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"ĐƯỢC XEM HÀNG\" \n ",
                    80 * widthRatio, 680 * heightRatio, 11 * heightRatio, 11 * heightRatio));
            instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"Tiền thu hộ: \" \n ",
                    420 * widthRatio, 640 * heightRatio, 9 * heightRatio, 9 * heightRatio));
            instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMA.TTF\",0,%f,%f,1,\"%s\" \n ", 420 * widthRatio,
                    680 * heightRatio, 18 * heightRatio, 12 * heightRatio, a.getString("total")));
            instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"Ghi chú: \" \n ",
                    60 * widthRatio, 770 * heightRatio, 10 * heightRatio, 10 * heightRatio));
            instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"Nội dung hàng hoá: \" \n ",
                    60 * widthRatio, 970 * heightRatio, 10 * heightRatio, 10 * heightRatio));

            instance.sendcommand(String.format("BLOCK %f,%f,%f,%f,\"TAHOMA.TTF\",0,%f,%f,%f,0,1,\"%s\"\n ",
                    230 * widthRatio, 345 * heightRatio, 560 * widthRatio, 180 * heightRatio, 10 * widthRatio,
                    10 * widthRatio, 10 * widthRatio, a.getString("shippingStreet")));
            instance.sendcommand(String.format("BLOCK %f,%f,%f,%f,\"TAHOMA.TTF\",0,%f,%f,%f,0,1,\"%s\" \n ",
                    60 * widthRatio, 578 * heightRatio, 380 * widthRatio, 60 * heightRatio, 9 * widthRatio,
                    9 * widthRatio, 5 * widthRatio, a.getString("district")));
            instance.sendcommand(String.format("BLOCK %f,%f,%f,%f,\"TAHOMA.TTF\",0,%f,%f,%f,0,1,\"%s\" \n ",
                    420 * widthRatio, 578 * heightRatio, 380 * widthRatio, 60 * heightRatio, 9 * widthRatio,
                    9 * widthRatio, 5 * widthRatio, a.getString("province")));
            instance.sendcommand(String.format("BLOCK %f,%f,%f,%f,\"TAHOMA.TTF\",0,%f,%f,%f,0,1,\"%s\"\n ",
                    60 * widthRatio, 815 * heightRatio, 720 * widthRatio, 150 * heightRatio, 10 * widthRatio,
                    10 * widthRatio, 5 * widthRatio, a.getString("note")));
            instance.sendcommand(String.format("BLOCK %f,%f,%f,%f,\"TAHOMA.TTF\",0,%f,%f,%f,0,1,\"%s\"\n ",
                    60 * widthRatio, 1020 * heightRatio, 450 * widthRatio, 200 * heightRatio, 10 * widthRatio,
                    10 * widthRatio, 5 * widthRatio, content));

            Log.v("ReactNative", "Success");

        }
        catch(Exception e){
            Log.v("ReactNative", e.getMessage());

        }
    }

    public void printLayoutLabel(TscWifiActivity instance, ReadableMap a) {
        instance.sendcommand(String.format("BOX %f,%f,%f,%f,8,10\n", 42 * widthRatio, 32 * heightRatio, 790 * widthRatio, 1185 * heightRatio));
        instance.sendcommand(String.format("DIAGONAL %f,%f,%f,%f,5\n", 42 * widthRatio, 230 * heightRatio, 790 * widthRatio, 230 * heightRatio));
        instance.sendcommand(String.format("DIAGONAL %f,%f,%f,%f,6\n", 42 * widthRatio, 330 * heightRatio, 790 * widthRatio, 330 * heightRatio));
        instance.sendcommand(String.format("DIAGONAL %f,%f,%f,%f,4\n", 42 * widthRatio, 515 * heightRatio, 790 * widthRatio, 515 * heightRatio));
        instance.sendcommand(String.format("DIAGONAL %f,%f,%f,%f,4\n", 42 * widthRatio, 625 * heightRatio, 790 * widthRatio, 625 * heightRatio));
        instance.sendcommand(String.format("DIAGONAL %f,%f,%f,%f,4\n", 400 * widthRatio, 515 * heightRatio, 400 * widthRatio, 750 * heightRatio));
        instance.sendcommand(String.format("DIAGONAL %f,%f,%f,%f,4\n", 42 * widthRatio, 750 * heightRatio, 790 * widthRatio, 750 * heightRatio));
        instance.sendcommand(String.format("DIAGONAL %f,%f,%f,%f,4\n", 42 * widthRatio, 950 * heightRatio, 790 * widthRatio, 950 * heightRatio));

    }
}
