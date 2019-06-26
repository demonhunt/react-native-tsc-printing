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

	public void prepareLabel(TscWifiActivity instance, ReadableMap config) throws Exception {
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

	@ReactMethod
	public void printTote(ReadableMap config, ReadableMap data, Promise promise) {
		TscWifiActivity instance = new TscWifiActivity();

		try {
			int a = data.getInt("fromId");
			int b = data.getInt("toId");
			String size = data.getString("size");
			prepareLabel(instance, config);
			initializeTote(instance, a, b, size);
			promise.resolve("");
		} catch (Exception e) {
			promise.reject(e);
		}

		instance.closeport(5000);
	}

	public void initializeTote(TscWifiActivity instance, int a, int b, String size) throws Exception {
		if (heightRatio < 1) {
			numLabel = 3.0;
		}
		String[] block = { "", "", "", "" };
		int count = 0;
		for (int i = a; i <= b; i++) {
			block[count] = size + "-" + Integer.toString(i);
			if (count == numLabel - 1) {
				proceedPrintMultiLabel(instance, block);
				count = 0;
				Arrays.fill(block, "");
			} else {
				count++;
			}
			if (i == b && count != 0) {
				proceedPrintMultiLabel(instance, block);
			}
		}
	}

	public void proceedPrintMultiLabel(TscWifiActivity instance, String[] block) throws Exception {
		String content = "";
		instance.sendcommand("CLS\n");
		int x = 22, y = 280, text = 80, qrcode = 50;
		Double wRatio = widthRatio;
		Double hRatio = (4 / numLabel) * heightRatio;

		for (int i = 0; i < numLabel; i++) {
			if (block[i] != "") {

				instance.sendcommand(
						String.format(Locale.US,"BOX %f,%f,%f,%f,12,10\n", 12 * wRatio, x * hRatio, 790 * wRatio, y * hRatio));
				instance.sendcommand(String.format(Locale.US,"BLOCK %f,%f,%f,%f,\"TAHOMAB.TTF\",0,70,70,5,0,1,\"%s\"\n ",
						60 * wRatio, text * hRatio, 450 * wRatio, 120 * hRatio, block[i]));
				instance.sendcommand(String.format(Locale.US,"QRCODE %f,%f,H,%f,M,0,M2, \"S%s\"\n", 555 * wRatio, qrcode * hRatio,
						10 * hRatio, block[i]));
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
			promise.resolve("success2");

		} catch (Exception e) {
			promise.reject(e);
		}

		instance.closeport(5000);
	}

	public void initializeShelf(TscWifiActivity instance, ReadableMap data) throws Exception {
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

	public void printContentLabel(TscWifiActivity instance, ReadableMap a) throws Exception {
		String content = "- Sách, văn hoá phẩm và văn phòng phẩm";

		instance.sendcommand(String.format(Locale.US,"QRCODE %f,%f,H,%f,M,0,M2 ,\"S%s\"\n", 60 * widthRatio, 50 * heightRatio,
				8 * widthRatio, a.getString("deliveryId")));

		instance.sendcommand(String.format(Locale.US,"QRCODE %f,%f,H,%f,M,0,M2 ,\"S%s\"\n", 560 * widthRatio, 940 * heightRatio,
				8 * widthRatio, a.getString("orderId").replace("_", "")));

		instance.sendcommand(String.format(Locale.US,"TEXT %f,%f,\"TAHOMA.TTF\",0,%f,%f,1,\"%s\" \n ", 300 * widthRatio,
				135 * heightRatio, 10 * heightRatio, 10 * heightRatio, a.getString("date")));

		instance.sendcommand(String.format(Locale.US,"TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"%s\" \n ", 300 * widthRatio,
				175 * heightRatio, 10 * heightRatio, 10 * heightRatio, a.getString("orderId")));

		instance.sendcommand(String.format(Locale.US,"TEXT %f,%f,\"TAHOMA.TTF\",0,%f,%f,1,\"%s\" \n ", 300 * widthRatio,
				215 * heightRatio, 10 * heightRatio, 10 * heightRatio, a.getString("deliveryPartner")));

		instance.sendcommand(String.format(Locale.US,"TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"Người nhận:   \" \n ",
				60 * widthRatio, 320 * heightRatio, 10 * heightRatio, 10 * heightRatio));

		instance.sendcommand(String.format(Locale.US,"TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"SĐT:    \" \n ", 60 * widthRatio,
				360 * heightRatio, 11 * heightRatio, 11 * heightRatio));

		instance.sendcommand(String.format(Locale.US,"TEXT %f,%f,\"TAHOMA.TTF\",0,%f,%f,1,\"%s\" \n ", 230 * widthRatio,
				360 * heightRatio, 11 * heightRatio, 11 * heightRatio, a.getString("shippingPhone")));

		instance.sendcommand(String.format(Locale.US,"TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"Địa chỉ:\" \n ", 60 * widthRatio,
				400 * heightRatio, 10 * heightRatio, 10 * heightRatio));

		instance.sendcommand(String.format(Locale.US,"TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"Quận/Huyện: \" \n ",
				520 * widthRatio, 320 * heightRatio, 11 * heightRatio, 11 * heightRatio));

		instance.sendcommand(String.format(Locale.US,"TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"Tỉnh/TP: \" \n ", 520 * widthRatio,
				490 * heightRatio, 11 * heightRatio, 11 * heightRatio));

		instance.sendcommand(String.format(Locale.US,"TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"ĐƯỢC XEM HÀNG\" \n ",
				520 * widthRatio, 690 * heightRatio, 10 * heightRatio, 10 * heightRatio));

		instance.sendcommand(String.format(Locale.US,"TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"Tiền thu hộ: \" \n ",
				60 * widthRatio, 660 * heightRatio, 11 * heightRatio, 11 * heightRatio));

		instance.sendcommand(String.format(Locale.US,"TEXT %f,%f,\"TAHOMA.TTF\",0,%f,%f,1,\"%s\" \n ", 60 * widthRatio,
				720 * heightRatio, 18 * heightRatio, 12 * heightRatio, a.getString("total")));

		instance.sendcommand(String.format(Locale.US,"TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"Ghi chú: \" \n ", 60 * widthRatio,
				800 * heightRatio, 11 * heightRatio, 11 * heightRatio));

		instance.sendcommand(String.format(Locale.US,"TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"Nội dung hàng hoá: \" \n ",
				60 * widthRatio, 950 * heightRatio, 10 * heightRatio, 10 * heightRatio));

		instance.sendcommand(String.format(Locale.US,"TEXT %f,%f,\"TAHOMAB.TTF\",0,%f,%f,1,\"Được giao bởi: \" \n ",
				60 * widthRatio, 1100 * heightRatio, 10 * heightRatio, 10 * heightRatio));

		instance.sendcommand(String.format(Locale.US,"BLOCK %f,%f,%f,%f,\"TAHOMA.TTF\",0,%f,%f,%f,0,1,\"%s\"\n ", 60 * widthRatio,
				440 * heightRatio, 400 * widthRatio, 180 * heightRatio, 10 * widthRatio, 10 * widthRatio,
				5 * widthRatio, a.getString("shippingStreet")));

		instance.sendcommand(String.format(Locale.US,"BLOCK %f,%f,%f,%f,\"TAHOMA.TTF\",0,%f,%f,%f,0,1,\"%s\" \n ",
				520 * widthRatio, 370 * heightRatio, 380 * widthRatio, 60 * heightRatio, 9 * widthRatio, 9 * widthRatio,
				5 * widthRatio, a.getString("district")));

		instance.sendcommand(String.format(Locale.US,"BLOCK %f,%f,%f,%f,\"TAHOMA.TTF\",0,%f,%f,%f,0,1,\"%s\" \n ",
				230 * widthRatio, 320 * heightRatio, 250 * widthRatio, 50 * heightRatio, 11 * heightRatio,
				11 * heightRatio, 5 * widthRatio, a.getString("shippingName")));

		instance.sendcommand(String.format(Locale.US,"BLOCK %f,%f,%f,%f,\"TAHOMA.TTF\",0,%f,%f,%f,0,1,\"%s\" \n ",
				520 * widthRatio, 540 * heightRatio, 380 * widthRatio, 60 * heightRatio, 9 * widthRatio, 9 * widthRatio,
				5 * widthRatio, a.getString("province")));

		instance.sendcommand(String.format(Locale.US,"BLOCK %f,%f,%f,%f,\"TAHOMA.TTF\",0,%f,%f,%f,0,1,\"%s\"\n ",
				220 * widthRatio, 800 * heightRatio, 500 * widthRatio, 120 * heightRatio, 10 * widthRatio,
				10 * widthRatio, 5 * widthRatio, a.getString("note")));

		instance.sendcommand(String.format(Locale.US,"BLOCK %f,%f,%f,%f,\"TAHOMA.TTF\",0,%f,%f,%f,0,1,\"%s\"\n ", 60 * widthRatio,
				1000 * heightRatio, 450 * widthRatio, 200 * heightRatio, 10 * widthRatio, 10 * widthRatio,
				5 * widthRatio, content));

		String dPartner = a.getString("deliveryPartner");

		instance.sendcommand(String.format(Locale.US,"PUTBMP %f,%f,\"kerry.bmp\"\n", 300 * widthRatio, 1050 * heightRatio));

		String command = "";

		String fHeader = widthRatio >= 1 ? "fhbig.bmp" : "fhsmall.bmp";
		String fFooter = widthRatio >= 1 ? "ffbig.bmp" : "ffsmall.bmp";
		String speedlink = widthRatio >= 1 ? "splbig.bmp" : "splsmall.bmp";
		String ninja = widthRatio >= 1 ? "ninjabig.bmp" : "ninjasmall.bmp";
		String kerry = widthRatio >= 1 ? "kerrybig.bmp" : "kerrysmall.bmp";
		switch (dPartner) {
		case "Ninja Van":
		case "Ninja Van_SD":
			command = String.format(Locale.US,"PUTBMP %f,%f,\"%s\"\n", 270 * widthRatio, 1020 * heightRatio, ninja);
			break;
		case "Kerry":
		case "Kerry_SD":
			command = String.format(Locale.US,"PUTBMP %f,%f,\"%s\"\n", 270 * widthRatio, 1050 * heightRatio, kerry);
			break;
		case "Speedlink":
		case "Speedlink_SD":
			command = String.format(Locale.US,"PUTBMP %f,%f,\"%s\"\n", 270 * widthRatio, 1100 * heightRatio, speedlink);
			break;
		case "Fahasa":
		case "Fahasa_SD":
			command = String.format(Locale.US,"PUTBMP %f,%f,\"%s\"\n", 270 * widthRatio, 1080 * heightRatio, fFooter);
			break;
		default:
			command = String.format(Locale.US,"BLOCK %f,%f,%f,%f,\"TAHOMAB.TTF\",0,%f,%f,%f,0,1,\"%s\"\n ", 280 * widthRatio,
					1095 * heightRatio, 250 * widthRatio, 60 * heightRatio, 15 * widthRatio, 15 * widthRatio,
					5 * widthRatio, dPartner);
		}
		instance.sendcommand(command);
		instance.sendcommand(String.format(Locale.US,"PUTBMP %f,%f,\"%s\"\n", 300 * widthRatio, 55 * heightRatio, fHeader));
	}

	public void printLayoutLabel(TscWifiActivity instance, ReadableMap a) throws Exception {
		instance.sendcommand(String.format(Locale.US,"BOX %f,%f,%f,%f,8,10\n", 42 * widthRatio, 32 * heightRatio,
				790 * widthRatio, 1185 * heightRatio));

		instance.sendcommand(String.format(Locale.US,"DIAGONAL %f,%f,%f,%f,4\n", 42 * widthRatio, 285 * heightRatio,
				790 * widthRatio, 285 * heightRatio));
		instance.sendcommand(String.format(Locale.US,"DIAGONAL %f,%f,%f,%f,4\n", 500 * widthRatio, 457.5 * heightRatio,
				790 * widthRatio, 457.5 * heightRatio));
		instance.sendcommand(String.format(Locale.US,"DIAGONAL %f,%f,%f,%f,4\n", 42 * widthRatio, 630 * heightRatio,
				790 * widthRatio, 630 * heightRatio));
		instance.sendcommand(String.format(Locale.US,"DIAGONAL %f,%f,%f,%f,4\n", 500 * widthRatio, 285 * heightRatio,
				500 * widthRatio, 780 * heightRatio));
		instance.sendcommand(String.format(Locale.US,"DIAGONAL %f,%f,%f,%f,4\n", 42 * widthRatio, 780 * heightRatio,
				790 * widthRatio, 780 * heightRatio));
		instance.sendcommand(String.format(Locale.US,"DIAGONAL %f,%f,%f,%f,4\n", 42 * widthRatio, 930 * heightRatio,
				790 * widthRatio, 930 * heightRatio));

	}
}
