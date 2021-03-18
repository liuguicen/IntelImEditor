package com.mandi.intelimeditor.common.appInfo;

import android.content.Context;
import android.content.SharedPreferences;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;

public class HasReadConfig {

    private final SharedPreferences sp;

    public HasReadConfig() {
        sp = AllData.appContext.getSharedPreferences("common_config", Context.MODE_PRIVATE);
    }

    public boolean hasAgreeAppPrivacy() {
        return sp.getBoolean("app_agree_privacy", false);
    }

    public void setAgreeAppPrivacy(boolean isAgree) {
        sp.edit().putBoolean("app_agree_privacy", isAgree)
                .apply();
    }


    public boolean hasReadAppGuide() {
        return sp.getBoolean("app_guide", false);
    }

    public void write_appGuide(boolean isRead) {
        sp.edit().putBoolean("app_guide", isRead)
                .apply();
    }

    /**
     * 常用图片使用阅读与否
     */
    public boolean hasReadUsuPicUse() {
        return sp.getBoolean("usu_pic_use", false);
    }

    public void put_usuPicUse(boolean isRead) {
        sp.edit().putBoolean("usu_pic_use", isRead)
                .apply();
    }

    public boolean hasReadGoSend() {
        return sp.getBoolean("go_send", false);
    }

    public void write_GoSend(boolean isRead) {
        sp.edit().putBoolean("go_send", isRead)
                .apply();
    }

    public boolean hasRead_absorb() {
        return sp.getBoolean("absorb", false);
    }

    public void put_absorb(boolean isRead) {
        sp.edit().putBoolean("absorb", isRead)
                .apply();
    }

    public boolean hasReadEditMove() {
        return sp.getBoolean("edit_move", false);
    }

    public void put_editMOve(boolean isRead) {
        sp.edit().putBoolean("edit_move", isRead)
                .apply();
    }

    public boolean hasRead_rendGuide() {
        return sp.getBoolean("rend_guide", false);
    }

    public void put_rendGuide(boolean isRead) {
        sp.edit().putBoolean("rend_guide", isRead)
                .apply();
    }

    public boolean hasRead_tietuGuide() {
        return sp.getBoolean("tietu_guide", false);
    }

    public void put_tietuGuide(boolean isRead) {
        sp.edit().putBoolean("tietu_guide", isRead)
                .apply();
    }

    public boolean hasRead_digGuide() {
        return sp.getBoolean("dig_guide", false);
    }

    public void write_digGuide(boolean isRead) {
        sp.edit().putBoolean("dig_guide", isRead)
                .apply();
    }

    public boolean hasRead_digFaceGuide() {
        return sp.getBoolean("dig_face_guide", false);
    }

    public void write_digFaceGuide(boolean isRead) {
        sp.edit().putBoolean("dig_face_guide", isRead)
                .apply();
    }

    public boolean hasRead_firstPicResourcesDownload() {
        return sp.getBoolean("first_pic_resources_download", false);
    }

    public void write_firstPicResourcesDownload(boolean isRead) {
        sp.edit().putBoolean("first_pic_resources_download", isRead)
                .apply();
    }

    public boolean hasRead_myTietuGuide() {
        return sp.getBoolean("my_tietu_guide", false);
    }

    public void write_myTietuGuide(boolean isRead) {
        sp.edit().putBoolean("my_tietu_guide", isRead)
                .apply();
    }

    public boolean hasRead_gifGuide() {
        return sp.getBoolean("gif_guide", false);
    }

    public void write_gifGuide(boolean isRead) {
        sp.edit().putBoolean("gif_guide", isRead)
                .apply();
    }

    public boolean hasRead_gifSaveGuide() {
        return sp.getBoolean("gif_save_guide", false);
    }

    public void put_gifSaveGuide(boolean isRead) {
        sp.edit().putBoolean("gif_save_guide", isRead)
                .apply();
    }

    public int getAdStateCount() {
        return sp.getInt("ad_state_count", 0);
    }

    public void put_adStateCount(int count) {
        sp.edit().putInt("ad_state_count", count)
                .apply();
    }

    public boolean hasRead_digSaveGuide() {
        return sp.getBoolean("dig_save_guide", false);
    }


    public void put_digSaveGuide(boolean isRead) {
        sp.edit().putBoolean("dig_save_guide", isRead)
                .apply();
    }

    public boolean hasRead_blurRadiusGuide() {
        return sp.getBoolean("blur_radius_guide", false);
    }


    public void put_blurRadiusGuide(boolean isRead) {
        sp.edit().putBoolean("blur_radius_guide", isRead)
                .apply();
    }

    public boolean hasRead_video2Gif() {
        return sp.getBoolean("video_make_gif", false);
    }


    public void put_video2Gif(boolean isRead) {
        sp.edit().putBoolean("video_make_gif", isRead)
                .apply();
    }

    public boolean hasRead_tietuGifSecondarySure() {
        return sp.getBoolean("tietu_gif_secondary_sure", false);
    }


    public void put_tietuGifSecondarySure(boolean isRead) {
        sp.edit().putBoolean("tietu_gif_secondary_sure", isRead)
                .apply();
    }

    public boolean hasRead_gifPtuPreviewGuide() {
        return sp.getBoolean("gif_preview_guide", false);
    }


    public void put_gifPtuPreviewGuide(boolean isRead) {
        sp.edit().putBoolean("gif_preview_guide", isRead)
                .apply();
    }

    public boolean hasRead_deleteResultPic() {
        return sp.getBoolean("delete_result_pic", false);
    }


    public void put_deleteResultPic(boolean isRead) {
        sp.edit().putBoolean("delete_result_pic", isRead)
                .apply();
    }

    public boolean hasRead_autoAddOneTietu() {
        return sp.getBoolean("autoAddTietu", false);
    }


    public void put_autoAddOneTietu(boolean isRead) {
        sp.edit().putBoolean("autoAddTietu", isRead)
                .apply();
    }

    public boolean hasRead_fuseBaoZouFace() {
        return sp.getBoolean("fuse_bao_zou_face", false);
    }

    public void put_fuseBaoZouFace(boolean isRead) {
        sp.edit().putBoolean("fuse_bao_zou_face", isRead)
                .apply();
    }

    public boolean hasRead_tietuErase() {
        return sp.getBoolean("tietuErase", false);
    }

    public void put_tietuErase(boolean isRead) {
        sp.edit().putBoolean("tietuErase", isRead)
                .apply();
    }

    public boolean hasRead_drawGifSecondarySure() {
        return sp.getBoolean("draw_gif_secondary_sure", false);
    }

    public void put_drawGifSecondarySure(boolean isRead) {
        sp.edit().putBoolean("draw_gif_secondary_sure", isRead)
                .apply();
    }

    public boolean hasRead_goDigFace() {
        return sp.getBoolean("goDigFace", false);
    }

    public void put_goDigFace(boolean isRead) {
        sp.edit().putBoolean("goDigFace", isRead)
                .apply();
    }

    public int get_eraseFaceAction() {
        return sp.getInt("eraseFaceAction", -1);
    }

    /**
     * @param action 0 去擦脸，1 不去
     */
    public void put_eraseFaceNotice(int action) {
        sp.edit().putInt("eraseFaceAction", action)
                .apply();
    }

    public boolean hasRead_changeFace_eraseBg() {
        return sp.getBoolean("changeFace_eraseBg", false);
    }

    public void put_changeFace_eraseBg(boolean isRead) {
        sp.edit().putBoolean("changeFace_eraseBg", isRead)
                .apply();
    }

    public boolean hasRead_changeFace_eraser() {
        return sp.getBoolean("changeFace_eraser", false);
    }

    public void put_changeFace_eraser(boolean isRead) {
        sp.edit().putBoolean("changeFace_eraser", isRead)
                .apply();
    }

    public boolean hasRead_changeFace_tiaose() {
        return sp.getBoolean("changeFace_tiaose", false);
    }

    public void put_changeFace_tiaose(boolean isRead) {
        sp.edit().putBoolean("changeFace_tiaose", isRead)
                .apply();
    }

    public boolean hasRead_changeFace_acGuide() {
        return sp.getBoolean("changeFace_acGuide", false);
    }

    public void put_changeFace_acGuide(boolean isRead) {
        sp.edit().putBoolean("changeFace_acGuide", isRead)
                .apply();
    }

    public boolean hasRead_fuseBaoZouFace_1() {
        return sp.getBoolean("fuseBaoZouFace_1", false);
    }

    public void put_fuseBaoZouFace_1(boolean isRead) {
        sp.edit().putBoolean("fuseBaoZouFace_1", isRead)
                .apply();
    }

//    public boolean hasRead_deformation2Gif() {
//        return sp.getBoolean("deformation2Gif", false);
//    }
//
//    public void put_deformation2Gif(boolean isRead) {
//        sp.edit().putBoolean("deformation2Gif", isRead)
//                .apply();
//    }

    public boolean hasRead_gifAutoAddEffect() {
        return sp.getBoolean("gifAutoAddTietu", false);
    }

    public void put_gifAutoAddEffect(boolean isRead) {
        sp.edit().putBoolean("gifAutoAddTietu", isRead)
                .apply();
    }

    public boolean hasRead_gifAutoAddTips() {
        return sp.getBoolean("gifAutoAddTips", false);
    }

    public void put_gifAutoAddTips(boolean isRead) {
        sp.edit().putBoolean("gifAutoAddTips", isRead)
                .apply();
    }

    public boolean hasRead_deformationGuide() {
        return sp.getBoolean("deformationGuide", false);
    }

    public void put_deformationGuide(boolean isRead) {
        sp.edit().putBoolean("deformationGuide", isRead)
                .apply();
    }
}
