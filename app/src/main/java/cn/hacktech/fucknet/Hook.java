package cn.hacktech.fucknet;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {
    List<String> inetaddress_gethostaddress = new ArrayList();

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 包名列表
        inetaddress_gethostaddress.add("cn.hacktech.getip");    //测试
        inetaddress_gethostaddress.add("com.asiainfo.sec.hubeiwifi");   //湖北飞young

        if (inetaddress_gethostaddress.contains(lpparam.packageName)) {

            XposedBridge.log("[FuckNet] Entry app: " + lpparam.packageName);

            XposedHelpers.findAndHookMethod("java.net.InetAddress", lpparam.classLoader,"getHostAddress", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("[FuckNet] Start hook: " + lpparam.packageName);
                    Httpthread myhttp = new Httpthread();
                    myhttp.start();
                    while (true) {
                        if (myhttp.userip != null)
                            break;
                    }
                    if (myhttp.userip != "") {
                        param.setResult(myhttp.userip);
                    }
                }
            });
        }
    }
}

class Httpthread extends Thread
{
    public String userip;

    public void run()
    {
        try{
            String url = "http://www.qq.com";
            URL serverUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) serverUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            String location = conn.getHeaderField("Location");

            String pattern = "userip=(.+?)&";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(location);
            if (m.find()) {
                this.userip = m.group(1);
            }
        } catch (Exception e) {
            XposedBridge.log("[FuckNet] Error: " + e.toString());
            this.userip = "";
        }
    }

}