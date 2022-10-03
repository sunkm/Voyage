package com.manchuan.tools.interceptor

import android.app.Activity
import com.hjq.permissions.*
import com.hjq.toast.ToastUtils
import com.lxj.androidktx.core.drawable
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.database.PermissionNameConvert.listToString
import com.manchuan.tools.database.PermissionNameConvert.permissionsToNames
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.extensions.tint


/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/XXPermissions
 * time   : 2021/01/04
 * desc   : 权限申请拦截器
 */
class PermissionInterceptor : IPermissionInterceptor {
    //    @Override
    //    public void requestPermissions(Activity activity, List<String> allPermissions, OnPermissionCallback callback) {
    //        List<String> deniedPermissions = XXPermissions.getDenied(activity, allPermissions);
    //        String permissionString = PermissionNameConvert.getPermissionString(activity, deniedPermissions);
    //
    //        // 这里的 Dialog 只是示例，没有用 DialogFragment 来处理 Dialog 生命周期
    //        new AlertDialog.Builder(activity)
    //                .setTitle(R.string.common_permission_hint)
    //                .setMessage(activity.getString(R.string.common_permission_message, permissionString))
    //                .setPositiveButton(R.string.common_permission_granted, new DialogInterface.OnClickListener() {
    //
    //                    @Override
    //                    public void onClick(DialogInterface dialog, int which) {
    //                        dialog.dismiss();
    //                        PermissionFragment.beginRequest(activity, new ArrayList<>(allPermissions), PermissionInterceptor.this, callback);
    //                    }
    //                })
    //                .setNegativeButton(R.string.common_permission_denied, new DialogInterface.OnClickListener() {
    //
    //                    @Override
    //                    public void onClick(DialogInterface dialog, int which) {
    //                        dialog.dismiss();
    //                        if (callback != null) {
    //                            callback.onDenied(deniedPermissions, false);
    //                        }
    //                    }
    //                })
    //                .show();
    //    }
    override fun grantedPermissions(
        activity: Activity?, allPermissions: List<String?>?, grantedPermissions: List<String?>?,
        all: Boolean, callback: OnPermissionCallback?,
    ) {
        if (callback == null) {
            return
        }
        callback.onGranted(grantedPermissions, all)
    }

    override fun deniedPermissions(
        activity: Activity, allPermissions: List<String>, deniedPermissions: List<String?>,
        never: Boolean, callback: OnPermissionCallback?,
    ) {
        callback?.onDenied(deniedPermissions, never)
        if (never) {
            if (deniedPermissions.size == 1 && Permission.ACCESS_MEDIA_LOCATION == deniedPermissions[0]) {
                ToastUtils.show(R.string.common_permission_media_location_hint_fail)
                return
            }
            showPermissionSettingDialog(activity, allPermissions, deniedPermissions, callback)
            return
        }
        if (deniedPermissions.size == 1) {
            val deniedPermission = deniedPermissions[0]
            if (Permission.ACCESS_BACKGROUND_LOCATION == deniedPermission) {
                ToastUtils.show(R.string.common_permission_background_location_fail_hint)
                return
            }
            if (Permission.BODY_SENSORS_BACKGROUND == deniedPermission) {
                ToastUtils.show(R.string.common_permission_background_sensors_fail_hint)
                return
            }
        }
        val message: String
        val permissionNames = permissionsToNames(activity, deniedPermissions)
        message = if (permissionNames.isNotEmpty()) {
            activity.getString(
                R.string.common_permission_fail_assign_hint,
                listToString(permissionNames)
            )
        } else {
            activity.getString(R.string.common_permission_fail_hint)
        }
        ToastUtils.show(message)
    }

    /**
     * 显示授权对话框
     */
    private fun showPermissionSettingDialog(
        activity: Activity?, allPermissions: List<String>,
        deniedPermissions: List<String?>, callback: OnPermissionCallback?,
    ) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            return
        }
        val message: String
        val permissionNames = permissionsToNames(activity, deniedPermissions)
        message = if (permissionNames.isNotEmpty()) {
            activity.getString(
                R.string.common_permission_manual_assign_fail_hint,
                listToString(permissionNames)
            )
        } else {
            activity.getString(R.string.common_permission_manual_fail_hint)
        }

        // 这里的 Dialog 只是示例，没有用 DialogFragment 来处理 Dialog 生命周期
        BaseAlertDialogBuilder(
            activity,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setTitle(R.string.common_permission_alert)
            .setIcon(
                activity.drawable(R.drawable.baseline_perm_device_information_24)
                    .tint(activity.colorPrimary())
            )
            .setMessage(message)
            .setPositiveButton(
                R.string.common_permission_goto_setting_page
            ) { dialog, which ->
                dialog.dismiss()
                XXPermissions.startPermissionActivity(activity,
                    deniedPermissions, object : OnPermissionPageCallback {
                        override fun onGranted() {
                            if (callback == null) {
                                return
                            }
                            callback.onGranted(allPermissions, true)
                        }

                        override fun onDenied() {
                            showPermissionSettingDialog(
                                activity, allPermissions,
                                XXPermissions.getDenied(activity, allPermissions), callback
                            )
                        }
                    })
            }
            .show()
    }
}