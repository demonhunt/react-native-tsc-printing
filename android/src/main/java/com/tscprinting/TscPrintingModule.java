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
import java.util.Arrays;


public class TscPrintingModule extends ReactContextBaseJavaModule {
	private final ReactApplicationContext reactContext;
	static Double heightRatio = 1.0;
	static Double widthRatio = 1.0;
	static Double numLabel = 4.0;
	public TscPrintingModule(ReactApplicationContext reactContext) {
		super(reactContext);
		this.reactContext = reactContext;
	}

	@Override
	public String getName() {
		return "TscPrinting";
	}

	public void prepareLabel(TscWifiActivity instance, ReadableMap config) {
		try {
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
		} catch (Exception e) {
			Log.v("ReactNative", e.getMessage());
		}
	}

	@ReactMethod
	public void printTote(ReadableMap config, int a, int b, String size, Promise promise) {
		TscWifiActivity instance = new TscWifiActivity();

		try {
			prepareLabel(instance, config);
			initializeTote(instance,a, b, size);
			promise.resolve("");
		} catch (Exception e) {
			promise.reject(e);
		}

		instance.closeport(5000);
	}

	public void initializeTote(TscWifiActivity instance,int a, int b, String size) {
		try {
			if(heightRatio<1){
				numLabel = 3.0;
			}
			String[] block = {"","","",""};
			int count = 0;
			for (int i = a; i <= b; i++) {
				block[count] = size + "-" + Integer.toString(i);
				if (count == numLabel-1) {
					proceedPrintMultiLabel(instance, block);
					count = 0;
					Arrays.fill(block,"");
				} else {
					count++;
				}
				if (i == b && count != 0) {
					proceedPrintMultiLabel(instance,block);
				}
			}
		} catch (Exception e) {

		}
	}

	public void proceedPrintMultiLabel(TscWifiActivity instance, String[] block) {
		String content = "";
		instance.sendcommand("CLS\n");
		int x = 22, y = 280, text = 80, qrcode = 50;
		Double wRatio = widthRatio;
		Double hRatio = (4/numLabel)*heightRatio;

		for (int i = 0; i < numLabel; i++) {
			if (block[i] != "") {
				
				instance.sendcommand(String.format("BOX %f,%f,%f,%f,12,10\n", 12 * wRatio, x * hRatio,
						790 * wRatio, y * hRatio));

				instance.sendcommand(String.format("BLOCK %f,%f,%f,%f,\"TAHOMAB.TTF\",0,70,70,5,0,1,\"%s\"\n ", 60 * wRatio,
						text * hRatio,450 * wRatio,120*hRatio, block[i]));

				// String.format("BLOCK %f,%f,%f,%f,\"TAHOMA.TTF\",0,%f,%f,%f,0,1,\"%s\"\n ",
				// 420 * widthRatio, 578 * heightRatio, 380 * widthRatio, 60 * heightRatio, 9 * widthRatio,
				// 9 * widthRatio, 5 * widthRatio, a.getString("province"))

				// instance.sendcommand(String.format("BLOCK %f,%f,%f,%f\"G.FNT\",0,10,10,10,0,1,\"%s\" \n ", 5.0,
				// 		5.0,400*wRatio,150*hRatio,"sds"));
				instance.sendcommand(String.format("QRCODE %f,%f,H,%f,M,0,M2, \"S%s\"\n", 555 * wRatio,
						qrcode * hRatio, 10 * hRatio, block[i]));
				x += 305 * wRatio;
				y += 305 * wRatio;
				text += 305 * wRatio;
				qrcode += 305 * wRatio;
			}
		}
		instance.sendcommand("PRINT 1,1\n");
	}


	@ReactMethod
	public void printShelf(ReadableMap config, ReadableMap data, Promise promise) {
		TscWifiActivity instance = new TscWifiActivity();

		try {
			prepareLabel(instance, config);
			initializeShelf(instance, data);
			promise.resolve("success");

		} catch (Exception e) {
			promise.reject(e);
		}

		instance.closeport(5000);
	}

	public void initializeShelf(TscWifiActivity instance, ReadableMap data) {
		try {
			if (heightRatio < 1) {
				numLabel = 3.0;
			}
			int floor = data.getInt("floor");
			int colFrom = data.getInt("colFrom");
			int colTo = data.getInt("colTo");
			String block = data.getString("block");
			int count = 0;
			String[] label = { "", "", "", "" };
			ReadableArray rows = data.getArray("rows");
			for (int i = colFrom; i <= colTo; i++) {
				for (int j = 0; j < rows.size(); j++) {
					label[count] = block + "-" + Integer.toString(i) + "-" + rows.getInt(j);
					if (floor != 0) {
						label[count] = "L" + Integer.toString(floor) + "-" + label[count];
					}
					if (count == numLabel - 1) {
						proceedPrintMultiLabel(instance, label);
						count = 0;
						Arrays.fill(label, "");
					} else {
						count++;
					}
				}
			}
			if (count != 0) {
				proceedPrintMultiLabel(instance, label);
			}
		} catch (Exception e) {
			Log.v("ReactNative", e.getMessage());
		}
	}


	@ReactMethod
	public void printLabel(ReadableMap config, ReadableMap data, Promise promise) {
		TscWifiActivity instance = new TscWifiActivity();

		try {
			prepareLabel(instance, config);
			printLayoutLabel(instance, data);
			printContentLabel(instance, data);
			instance.sendcommand("PRINT 1,1\n");
			promise.resolve("");
		} catch (Exception e) {
			promise.reject(e);
		}
		instance.closeport(5000);
	}


	public void printContentLabel(TscWifiActivity instance, ReadableMap a) {
		try {
			String content = "- Sách, văn hoá phẩm và văn phòng phẩm", header = "Fahasa.com - 1900 636467";

			instance.sendcommand(String.format("QRCODE %f,%f,H,%f,M,0,M2 ,\"S%s\"\n", 60 * widthRatio, 45 * heightRatio,
					9 * widthRatio, a.getString("deliveryId")));
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
					60 * widthRatio, 320 * heightRatio, 11 * heightRatio, 11 * heightRatio));
                    
			instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMA.TTF\",0,%f,%f,1,\"%s\" \n ", 220 * widthRatio,
					320 * heightRatio, 11 * heightRatio, 11 * heightRatio, a.getString("shippingName")));
                    
			instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"SĐT:    \" \n ", 60 * widthRatio,
					380 * heightRatio, 11 * heightRatio, 11 * heightRatio));
                    
			instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMA.TTF\",0,%f,%f,1,\"%s\" \n ", 220 * widthRatio,
					380 * heightRatio, 11 * heightRatio, 11 * heightRatio, a.getString("shippingPhone")));
                    
			instance.sendcommand(String.format("TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"Địa chỉ:\" \n ", 60 * widthRatio,
					440 * heightRatio, 10 * heightRatio, 10 * heightRatio));
                    
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
					60 * widthRatio, 480 * heightRatio, 380 * widthRatio, 60 * heightRatio, 9 * widthRatio,
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
		} catch (Exception e) {
			Log.v("ReactNative", e.getMessage());

		}
	}

	public void printLayoutLabel(TscWifiActivity instance, ReadableMap a) {
		instance.sendcommand(String.format("BOX %f,%f,%f,%f,8,10\n", 42 * widthRatio, 32 * heightRatio,
				790 * widthRatio, 1185 * heightRatio));

		instance.sendcommand(String.format("DIAGONAL %f,%f,%f,%f,4\n", 42 * widthRatio, 285 * heightRatio,
				790 * widthRatio, 285 * heightRatio));
		instance.sendcommand(String.format("DIAGONAL %f,%f,%f,%f,4\n", 500 * widthRatio, 457.5 * heightRatio,
				790 * widthRatio, 457.5 * heightRatio));
		instance.sendcommand(String.format("DIAGONAL %f,%f,%f,%f,4\n", 42 * widthRatio, 630 * heightRatio,
				790 * widthRatio, 630 * heightRatio));
		instance.sendcommand(String.format("DIAGONAL %f,%f,%f,%f,4\n", 500 * widthRatio, 285 * heightRatio,
				500 * widthRatio, 780 * heightRatio));
		instance.sendcommand(String.format("DIAGONAL %f,%f,%f,%f,4\n", 42 * widthRatio, 780 * heightRatio,
				790 * widthRatio, 780 * heightRatio));
		instance.sendcommand(String.format("DIAGONAL %f,%f,%f,%f,4\n", 42 * widthRatio, 930 * heightRatio,
				790 * widthRatio, 930 * heightRatio));

	}
}
