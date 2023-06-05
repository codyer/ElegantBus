/*
 * ************************************************************
 * 文件：HostStubActivity.java  模块：ElegantBus.ipc.main  项目：ElegantBus
 * 当前修改时间：2023年06月05日 20:59:58
 * 上次修改时间：2023年06月05日 20:43:19
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HostStubActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
        ElegantLog.d("HostStubActivity onCreate");
    }

    static final class RequestBusService extends ActivityResultContract<String, Boolean> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, @NonNull String hostPkg) {
            Intent intent = new Intent();
            ComponentName comp = new ComponentName(hostPkg, HostStubActivity.class.getName());
            intent.setComponent(comp);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }

        @NonNull
        @Override
        public Boolean parseResult(int resultCode, @Nullable Intent intent) {
            return ProcessManager.ready().isBound();
        }

        @Override
        public @Nullable SynchronousResult<Boolean> getSynchronousResult(
                @NonNull Context context, @Nullable String hostPkg) {
            if (hostPkg == null) {
                return new SynchronousResult<>(false);
            } else {
                if (ProcessManager.ready().isBound()) {
                    return new SynchronousResult<>(true);
                }
            }
            return null;
        }
    }
}