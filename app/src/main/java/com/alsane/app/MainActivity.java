package com.alsane.app;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.ViewGroup;
import android.widget.Toast;
import android.util.Base64;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
public class MainActivity extends Activity {
 private static final int REQUEST_STORAGE=4102;
 private WebView webView; private String pendingName,pendingData;
 @Override public void onCreate(Bundle b){super.onCreate(b);webView=new WebView(this);webView.setLayoutParams(new ViewGroup.LayoutParams(-1,-1));WebSettings s=webView.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);webView.addJavascriptInterface(new BackupBridge(),"AndroidBackup");webView.setWebViewClient(new WebViewClient());setContentView(webView);webView.loadUrl("file:///android_asset/index.html");}
 private class BackupBridge{@JavascriptInterface public void save(String name,String base64){runOnUiThread(()->{pendingName=name;pendingData=base64;if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_STORAGE);else writeBackup();});}}
 private void writeBackup(){try{File folder=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);if(!folder.exists()&&!folder.mkdirs())throw new Exception("تعذر إنشاء مجلد التنزيلات");String safeName=pendingName==null?"Makhzani-Backup.json":pendingName.replaceAll("[^a-zA-Z0-9._-]","_");File file=new File(folder,safeName);byte[] bytes=Base64.decode(pendingData,Base64.DEFAULT);try(FileOutputStream out=new FileOutputStream(file)){out.write(bytes);out.flush();}Toast.makeText(this,"تم حفظ النسخة في التنزيلات: "+safeName,Toast.LENGTH_LONG).show();webView.evaluateJavascript("backupSaved("+org.json.JSONObject.quote(safeName)+")",null);}catch(Exception e){Toast.makeText(this,"فشل حفظ النسخة الاحتياطية: "+e.getMessage(),Toast.LENGTH_LONG).show();webView.evaluateJavascript("backupFailed("+org.json.JSONObject.quote(String.valueOf(e.getMessage()))+")",null);}finally{pendingName=null;pendingData=null;}}
 @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){super.onRequestPermissionsResult(requestCode,permissions,grantResults);if(requestCode==REQUEST_STORAGE){if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)writeBackup();else{Toast.makeText(this,"يلزم السماح بالوصول إلى الملفات لحفظ النسخة الاحتياطية",Toast.LENGTH_LONG).show();webView.evaluateJavascript("backupFailed('لم يتم منح إذن حفظ الملفات')",null);}}}
 @Override public void onBackPressed(){if(webView!=null&&webView.canGoBack())webView.goBack();else super.onBackPressed();}
}