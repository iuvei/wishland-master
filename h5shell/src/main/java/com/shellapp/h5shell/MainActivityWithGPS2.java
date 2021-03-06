package com.shellapp.h5shell;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.callback.FileCallback;
import com.shellapp.h5shell.customview.MyWebView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by MSI on 2018/7/5.
 */

public class MainActivityWithGPS2 extends Activity {
    private String stringExtra;
    private MyWebView webview;
    private ProgressBar pb;
    private Map<Integer, Integer> index = new HashMap<>();
    private String imgUrl = "";
    //    private SwipeRefreshLayout swipeRefreshLayout;
    private String appStyle;
    private LinearLayout llHome;
    private LinearLayout llRefresh;
    private LinearLayout llBack;
    private LinearLayout llOption;
    private DrawerLayout drawer;
    private LinearLayout llGoHome;
    private LinearLayout llClear;


    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        getWindow().setContentView(R.layout.activity_gps2);

        appStyle = getString(R.string.AppStyle);
        webview = (MyWebView) findViewById(R.id.webview);
        pb = (ProgressBar) findViewById(R.id.pb);
        llHome = (LinearLayout) findViewById(R.id.ll_home);
        llRefresh = (LinearLayout) findViewById(R.id.ll_refresh);
        llBack = (LinearLayout) findViewById(R.id.ll_back);
        llOption = (LinearLayout) findViewById(R.id.ll_option);
        drawer = (DrawerLayout) findViewById(R.id.drawer);
        llGoHome = (LinearLayout) findViewById(R.id.ll_goHome);
        llClear = (LinearLayout) findViewById(R.id.ll_clear);

        initDrawer(drawer);
        setOnLLClickListener(llHome);
        setOnLLClickListener(llRefresh);
        setOnLLClickListener(llBack);
        setOnLLClickListener(llOption);
        setOnLLClickListener(llGoHome);
        setOnLLClickListener(llClear);

        //设置屏幕不可以旋转
        if ("type2".equals(appStyle)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

//        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_view);
//        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_bright), getResources().getColor(android.R.color.holo_green_light),
//                getResources().getColor(android.R.color.holo_orange_light), getResources().getColor(android.R.color.holo_red_light));
//        webview.setSwipeRefreshLayout(swipeRefreshLayout);


        init(webview);

        if (intent.getStringExtra("url").isEmpty()) {
            ToastUtil.showShort(this, "暂未开放");
        } else {
            stringExtra = intent.getStringExtra("url");
            webview.loadUrl(stringExtra);
        }

//        //增加刷新功能
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                webview.reload();
//            }
//        });


        webview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                WebView.HitTestResult result = ((WebView) v).getHitTestResult();
                int type = result.getType();
                if (type == WebView.HitTestResult.IMAGE_TYPE) {
                    imgUrl = result.getExtra();
                    setDataPopup(imgUrl);

                }
                return false;
            }
        });
        checkUpdate();
    }

    private void initDrawer(DrawerLayout drawer) {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void setOnLLClickListener(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.ll_home: {
                        webview.loadUrl(stringExtra);
                    }
                    break;
                    case R.id.ll_refresh: {
                        webview.reload();
                    }
                    break;
                    case R.id.ll_back: {
                        webview.goBack();
                    }
                    break;
                    case R.id.ll_option: {
                        drawer.openDrawer(Gravity.RIGHT);
                    }
                    break;
                    case R.id.ll_goHome: {
                        webview.loadUrl(stringExtra);
                        drawer.closeDrawers();
                    }
                    break;
                    case R.id.ll_clear: {
                        ToastUtil.showShort(MainActivityWithGPS2.this, "清除缓存成功！");
                        drawer.closeDrawers();
                    }
                    break;
                }
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init(final WebView webView) {
        webview.requestFocusFromTouch();
        WebSettings settings = webView.getSettings();
        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setBlockNetworkImage(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
//        settings.setDisplayZoomControls(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        //缩放开关，仅仅支持双击缩放
        settings.setSupportZoom(true);
        //设置是否可缩放，会出现缩放工具
        settings.setBuiltInZoomControls(true);
        //隐藏缩放工具
        settings.setDisplayZoomControls(false);
        //WebView.setWebContentsDebuggingEnabled(true);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();

            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e("abb", "description:" + error.getDescription().toString() + ", ErrorCode: " + error.getErrorCode() + ", errorString:" + error.toString());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.getSettings().setBlockNetworkImage(false);
                int currentIndex = view.copyBackForwardList().getCurrentIndex();
                boolean b = index.containsKey(currentIndex);
                if (b) {
                    int i = index.get(currentIndex) + 1;
                    index.put(currentIndex, i);
                } else {
                    index.put(currentIndex, 1);
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                setTitle(title);
            }

            @Override
            public void onProgressChanged(WebView view, int progress) {
                pb.setVisibility(View.VISIBLE);
                pb.setProgress(progress);
                if (progress == 100) {
                    pb.setVisibility(View.GONE);
//                    swipeRefreshLayout.setRefreshing(false);

                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, final String message,
                                     JsResult result) {
                //加这段可以证webview中的alert弹出来
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(MainActivityWithGPS2.this)
                                .setTitle("提示")
                                .setMessage(message)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //webView.reload();//重写刷新页面
                                    }
                                }).show();
                    }
                });
                result.confirm();//这里必须调用，否则页面会阻塞造成假死
                return true;
            }


            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> valueCallback) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android  >= 3.0
            public void openFileChooser(ValueCallback valueCallback, String acceptType) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            //For Android  >= 4.1
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android >= 5.0
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                uploadMessageAboveL = filePathCallback;
                openImageChooserActivity();
                return true;
            }
        });
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.setAcceptFileSchemeCookies(true);
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }
        webView.setWebViewClient(setWebViewClient());
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemNavigationBar();
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 加入竖屏要处理的代码
            showSystemNavigationBar();
        }
    }

    private void showSystemNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.VISIBLE;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void hideSystemNavigationBar() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public void onBackPressed() {
        try {
            if (this.webview.canGoBack()) {
                int currentIndex = this.webview.copyBackForwardList().getCurrentIndex();
                if (currentIndex >= 1) {
                    /***
                     * 如果非第一层，直接回退到 第一层即可
                     */
                    this.webview.goBackOrForward(-currentIndex);
                } else {
                    exitApp();
                }
            } else {
                exitApp();
            }
        } catch (Exception ex) {
            exitApp();
        }
    }

    private long lastBackTime = 0;

    public void exitApp() {
        long current = System.currentTimeMillis();
        if (current - lastBackTime > 1000) {
            Toast.makeText(this, "再按一次后退键退出应用程序", Toast.LENGTH_SHORT).show();
        } else {
            finish();
            lastBackTime = 0;
            System.exit(0);
        }
        lastBackTime = current;
    }

    public static void clearCookies(Context context) {
        // Edge case: an illegal state exception is thrown if an instance of
        // CookieSyncManager has not be created.  CookieSyncManager is normally
        // created by a WebKit view, but this might happen if you start the
        // app, restore saved state, and click logout before running a UI
        // dialog in a WebView -- in which case the app crashes
        @SuppressWarnings("unused")
        CookieSyncManager cookieSyncMngr =
                CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webview != null) {
            webview.removeAllViews();
            webview.destroy();
        }
        clearCookies(this);
        System.exit(0);
    }

    private WebViewClient setWebViewClient() {
        WebViewClient webViewClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView wv, String url) {
                if (url == null) return false;

                try {
                    Log.e("cww", url);
                    if (url.startsWith("weixin://") //微信
                            || url.startsWith("alipays://") //支付宝
                            || url.startsWith("mailto://") //邮件
                            || url.startsWith("tel://")//电话
                            || url.startsWith("dianping://")//大众点评
                            || url.startsWith("mqqapi://")//QQ钱包
                            || url.startsWith("alipayqr://") //支付宝
                        //其他自定义的scheme
                            ) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                } catch (Exception e) { //防止crash (如果手机上没有安装处理某个scheme开头的url的APP, 会导致crash)
                    return true;//没有安装该app时，返回true，表示拦截自定义链接，但不跳转，避免弹出上面的错误页面
                }

                //处理http和https开头的url
                wv.loadUrl(url);
                return true;
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);


                error.getDescription();
                error.getErrorCode();
                error.toString();
                Log.e("abb", "description:" + error.getDescription() + ", errorCode:" + error.getErrorCode() + ", string:" + error.toString());
            }

//            @Override
//            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
//                super.doUpdateVisitedHistory(view, url, false);
//            }
        };
        return webViewClient;
    }

    public void setDataPopup(final String imgUrl) {
        //通过UUID生成字符串文件名
        final String fileName1 = UUID.randomUUID().toString();
        //通过Random()类生成数组命名
//        Random random = new Random();
//        String fileName2 = String.valueOf(random.nextInt(Integer.MAX_VALUE));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("点击确定保存图片");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                ToastUtil.showShort(DetailsHtmlPageActivity.this, "地图地址：" + imgUrl);
                saveImage(imgUrl, Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/", fileName1 + ".png");
            }
        });
        builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveImage(String imgUrl, String destFileDir, String destFileName) {
        OkGo.get(imgUrl).tag(this).execute(new FileCallback(destFileDir, destFileName) {
            @Override
            public void onSuccess(File file, Call call, Response response) {
                //通知图库更新
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                MainActivityWithGPS2.this.sendBroadcast(intent);
                ToastUtil.showShort(MainActivityWithGPS2.this, "保存成功");
            }

            @Override
            public void onError(Call call, Response response, Exception e) {
                super.onError(call, response, e);
                ToastUtil.showShort(MainActivityWithGPS2.this, "保存失败");
            }
        });
    }

    private void checkUpdate() {
        String url = getResources().getString(R.string.updateURL);
        OkGo.get(url)
                .execute(new AbsCallback<String>() {
                    @Override
                    public String convertSuccess(Response response) throws Exception {
                        return response.body().string();
                    }

                    @Override
                    public void onSuccess(String s, Call call, Response response) {
//                        Log.e("cww",s+"后台返回的数据！");
                        final AppUpdateBean appUpdate = new Gson().fromJson(s, AppUpdateBean.class);
                        if (isCheckUpdate(appUpdate)) {
                            MaterialDialog.Builder builder = new MaterialDialog.Builder(MainActivityWithGPS2.this);
                            builder.title("发现新版本 V" + appUpdate.getVersionName());
                            if (4 == appUpdate.getVersionType()) {
                                builder.cancelable(false)
                                        .autoDismiss(false);
                                builder.content("重大版本更新，请下载安装新版本后继续使用");
                            } else if (3 == appUpdate.getVersionType()) {
                                builder.negativeText("暂不升级")
                                        .negativeColor(getResources().getColor(R.color.text_hint))
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                dialog.dismiss();


                                            }
                                        });
                            }
                            builder.positiveText("点击升级")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            try {
                                                Uri uri = Uri.parse(appUpdate.getUpdateUrl());
                                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                startActivity(intent);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).show();
                        }

                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        Log.e("cww", e.getMessage() + "后台返回的数据！");
                    }
                });
    }

    public boolean isCheckUpdate(AppUpdateBean appUpdate) {
        int appVersion = getAPPRealVersion();
        String version = appUpdate.getVersionName();
        if (version.contains(".")) {
            try {
                String numStr = version.replace(".", "");
                int num = Integer.parseInt(numStr);
                if (appVersion < num) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                int num = Integer.parseInt(version);
                if (appVersion < num) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public int getAPPRealVersion() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private final static int FILE_CHOOSER_RESULT_CODE = 10000;
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;

    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL)
                return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }
}
