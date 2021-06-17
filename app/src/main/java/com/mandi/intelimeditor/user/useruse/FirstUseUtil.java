package com.mandi.intelimeditor.user.useruse;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.dialog.FirstUseDialog;
import com.mandi.intelimeditor.R;


/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/02/10
 *      version : 1.0
 * <pre>
 */
public class FirstUseUtil {
    public static void usuLocalPicUseGuide(final Context context, View targetView, View targetView1) {
        if (!AllData.hasReadConfig.hasReadUsuPicUse()) {
            showGuideView(context, targetView, GuideView.Direction.TOP, GuideView.MyShape.CIRCULAR, "点击右下角图标可选择文件夹内图片哦",
                    () -> showGuideView(context, targetView1, GuideView.Direction.BOTTOM,
                            GuideView.MyShape.RECTANGULAR, "选择正脸照片创作效果更佳哟",
                            () -> AllData.hasReadConfig.put_usuPicUse(true)));
        } else if (AllData.globalSettings.isShowShortVideo() && !AllData.hasReadConfig.hasRead_video2Gif()) {
            showGuideView(context, "还可以选择短视频制作GIF哟", () -> AllData.hasReadConfig.put_video2Gif(true));
        }
    }

    public static void editMoveUseGuide(final Context context) {
        if (!AllData.hasReadConfig.hasReadEditMove()) {
            showGuideView(context, "按住编辑框四周调整尺寸，按住中间即可移动", new GuideView.OnClickCallback() {
                @Override
                public void onClickedGuideView() {
                    AllData.hasReadConfig.put_editMOve(true);
                }
            });
        }
    }

    public static void rendGuide(final Context context) {
        if (!AllData.hasReadConfig.hasRead_rendGuide()) {
            showGuideView(context, "双指按住图片，往外拉即可撕开图片哦", new GuideView.OnClickCallback() {
                @Override
                public void onClickedGuideView() {
                    AllData.hasReadConfig.put_rendGuide(true);
                }
            });
        }
    }


    public static void tietuGuide(final Context context) {
        if (!AllData.hasReadConfig.hasRead_tietuGuide()) {
            showGuideView(context, "选图后，双指按住图片，即可缩放和旋转哦", new GuideView.OnClickCallback() {
                @Override
                public void onClickedGuideView() {
                    AllData.hasReadConfig.put_tietuGuide(true);
                }
            });
        }
    }


    public static void digGuide(final Context context) {
        if (!AllData.hasReadConfig.hasRead_digGuide()) {
            showGuideView(context, "绕着人脸画圈，即可实现智能抠脸哦\n保存人脸抠图，即可用于贴图恶搞哦", new GuideView.OnClickCallback() {
                @Override
                public void onClickedGuideView() {
                    AllData.hasReadConfig.write_digGuide(true);
                }
            });
        }
    }

    public static void firstPicResourceDownload(Context context) {
        if (!AllData.hasReadConfig.hasRead_firstPicResourcesDownload()) {
            showGuideView(context, context.getString(R.string.first_download_pic_resource_notice), new GuideView.OnClickCallback() {
                @Override
                public void onClickedGuideView() {
                    AllData.hasReadConfig.write_firstPicResourcesDownload(true);
                }
            });
        }
    }

    public static void myTietuGuide(Context context) {
        if (!AllData.hasReadConfig.hasRead_myTietuGuide()) {
            showGuideView(context, context.getString(R.string.my_tietu_guide), new GuideView.OnClickCallback() {
                @Override
                public void onClickedGuideView() {
                    AllData.hasReadConfig.write_myTietuGuide(true);
                }
            });
        }
    }


    public static void readAdGuide(Context activity) {
        int adStateCount = AllData.hasReadConfig.getAdStateCount();
        if (adStateCount < AdData.DEFAULT_AD_STATE_COUNT) {
            final FirstUseDialog firstUseDialog = new FirstUseDialog(activity);
            firstUseDialog.createDialog(activity.getString(R.string.about_ad),
                    activity.getString(R.string.ad_state),
                    () -> AllData.hasReadConfig.put_adStateCount(adStateCount + 1));
        }
    }

    public static void firstGifSave(Context activity) {
        if (!AllData.hasReadConfig.hasRead_gifSaveGuide()) {
            final FirstUseDialog firstUseDialog = new FirstUseDialog(activity);
            firstUseDialog.createDialog(null,
                    activity.getString(R.string.gif_save_guide),
                    () -> AllData.hasReadConfig.put_gifSaveGuide(true));
        }
    }

    public static void firstDig(Context context) {
        if (!AllData.hasReadConfig.hasRead_digSaveGuide()) {
            showGuideView(context, context.getString(R.string.dig_save_guide), new GuideView.OnClickCallback() {
                @Override
                public void onClickedGuideView() {
                    AllData.hasReadConfig.put_digSaveGuide(true);
                }
            });
        }
    }

    public static void digBLurRadiusGuide(FragmentActivity activity) {
        if (!AllData.hasReadConfig.hasRead_blurRadiusGuide()) {
            showGuideView(activity, activity.getString(R.string.blur_radius_guide), new GuideView.OnClickCallback() {
                @Override
                public void onClickedGuideView() {
                    AllData.hasReadConfig.put_blurRadiusGuide(true);
                }
            });
        }
    }

    public static void tietuGifSecondarySureGuide(FragmentActivity activity) {
        if (!AllData.hasReadConfig.hasRead_tietuGifSecondarySure()) {
            final FirstUseDialog firstUseDialog = new FirstUseDialog(activity);
            firstUseDialog.createDialog(null,
                    activity.getString(R.string.tietu_gif_secondary_sure_guide),
                    () -> AllData.hasReadConfig.put_tietuGifSecondarySure(true));
        }
    }

    public static void drawGifSecondarySureGuide(FragmentActivity activity) {
        if (!AllData.hasReadConfig.hasRead_drawGifSecondarySure()) {
            final FirstUseDialog firstUseDialog = new FirstUseDialog(activity);
            firstUseDialog.createDialog(null,
                    activity.getString(R.string.draw_gif_secondary_sure_guide),
                    () -> AllData.hasReadConfig.put_drawGifSecondarySure(true));
        }
    }

    public static void gifPreviewGuide(FragmentActivity activity, View targetView, View targetView1) {
        if (!AllData.hasReadConfig.hasRead_gifPtuPreviewGuide()) {
            showGuideView(activity, targetView, GuideView.Direction.RIGHT_TOP, GuideView.MyShape.RECTANGULAR,
                    activity.getString(R.string.gif_ptu_preview_guide), () -> {
                        AllData.hasReadConfig.put_gifPtuPreviewGuide(true);
                        gifGuide(activity, targetView1);
                    });
        }
    }

    private static void gifGuide(Context activity, View targetView) {
        if (!AllData.hasReadConfig.hasRead_gifGuide()) {
            showGuideView(activity, targetView, GuideView.Direction.TOP, GuideView.MyShape.RECTANGULAR,
                    activity.getString(R.string.gif_guide), () -> {
                        AllData.hasReadConfig.write_gifGuide(true);
                    });
        }
    }

    public static boolean deleteResultPic(FragmentActivity activity) {
        if (!AllData.hasReadConfig.hasRead_deleteResultPic()) {
            showGuideView(activity, activity.getString(R.string.delete_result_or_re_save), new GuideView.OnClickCallback() {
                @Override
                public void onClickedGuideView() {
                    AllData.hasReadConfig.put_deleteResultPic(true);
                }
            });
            return true;
        }
        return false;
    }

    /**
     * 自动添加一张
     */
    public static void tietuAutoAddTietuNotice(FragmentActivity activity) {
        if (!AllData.hasReadConfig.hasRead_autoAddOneTietu()) {
            showGuideView(activity, activity.getString(R.string.auto_add_tietu_notice), new GuideView.OnClickCallback() {
                @Override
                public void onClickedGuideView() {
                    AllData.hasReadConfig.put_autoAddOneTietu(true);
                }
            });
        }
    }

    public static void gifAutoAddTips(FragmentActivity activity) {
        if (!AllData.hasReadConfig.hasRead_gifAutoAddTips()) {
            showGuideView(activity, activity.getString(R.string.auto_add_tietu_tips), new GuideView.OnClickCallback() {
                @Override
                public void onClickedGuideView() {
                    AllData.hasReadConfig.put_gifAutoAddTips(true);
                }
            });
        }
    }

    public static void goDigFaceNotice(FragmentActivity activity) {
        if (!AllData.hasReadConfig.hasRead_goDigFace()) {
            final FirstUseDialog firstUseDialog = new FirstUseDialog(activity);
            firstUseDialog.createDialog(null,
                    activity.getString(R.string.go_dig_face_notice),
                    () -> AllData.hasReadConfig.put_goDigFace(true));
        }
    }
    public static void deformationGuide(FragmentActivity activity) {
        if (!AllData.hasReadConfig.hasRead_deformationGuide()) {
            final FirstUseDialog firstUseDialog = new FirstUseDialog(activity);
            firstUseDialog.createDialog(null,
                    activity.getString(R.string.deformation_guide),
                    () -> AllData.hasReadConfig.put_deformationGuide(true));
        }
    }

    /**
     * 第一次引导
     */
    public static void showGuideView(Context context, View targetView, GuideView.Direction direction, GuideView.MyShape myShape, String tips, GuideView.OnClickCallback clickCallback) {
        GuideView.Builder.newInstance(context)
                .setTargetView(targetView)
                .setDirection(direction)
                .setShape(myShape)
                .setBgColor(context.getResources().getColor(R.color.guide_shadow))
                .setTips(tips)
                .setOnclickExit(true)
                .setOnclickListener(clickCallback).build().show();
    }

    public static void showGuideView(Context context, String tips, GuideView.OnClickCallback clickCallback) {
        GuideView.Builder.newInstance(context)
                .setBgColor(context.getResources().getColor(R.color.guide_shadow))
                .setTips(tips)
                .setOnclickExit(true)
                .setOnclickListener(clickCallback).build().show();
    }

    public static void tietuFuse() {

    }

    public static void release(AppCompatActivity appCompatActivity) {
    }
}
