package com.mandi.intelimeditor.user.userSetting;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mandi.intelimeditor.common.BaseActivity;
import com.mandi.intelimeditor.common.appInfo.AppConfig;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.R;


import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class FeedBackActivity extends BaseActivity {
    String lastComment;
    private EditText contactEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_feedback;
    }

    private void initView() {
        final EditText commentEdit = findViewById(R.id.feedback_comment);
        contactEdit = findViewById(R.id.feedback_contact_edit);
        Button btnCommit = findViewById(R.id.feedback_btn_commit);
        btnCommit.setOnClickListener(
                v -> {
                    if (Util.DoubleClick.isDoubleClick()) return;
                    if (commentEdit.getText() == null) return;
                    commitComment(commentEdit.getText().toString());
                }
        );
    }

    public void toCommunicateGroup(View view) {
        SettingActivity.toQQGroup(this, AppConfig.QQ_GROUP_COMMUNICATE_KEY);
    }

    private void commitComment(String comment) {
//        检查
        if (comment.trim().isEmpty()) { // 没有输入内容不提交
            return;
        } else if (comment.length() > 500) {//太长
            comment = comment.substring(0, 500);
        }
        if (comment.equals(lastComment) || filterComment(comment)) {
            ToastUtils.show(this, getString(R.string.feedback_has_commit));
            return;
        }
        lastComment = comment;
        final Comment commentObj = new Comment(comment);
//        附加信息
        if (contactEdit.getText() != null)
            commentObj.setContact(contactEdit.getText().toString());

//        提交
        commentObj.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    ToastUtils.show(FeedBackActivity.this, getString(R.string.commit_success_thanks));
                } else {
                    ToastUtils.show(FeedBackActivity.this, getString(R.string.network_error_try_latter));
                }
            }
        });
    }

    /**
     * 脏话过滤
     */
    private boolean filterComment(String comment) {
        if (TextUtils.isEmpty(comment))
            return true;
        if (comment.contains("TM") ||
                comment.contains("tm") ||
                comment.contains("垃圾") ||
                comment.contains("傻") ||
                comment.contains("病") ||
                comment.contains("sb") ||
                comment.contains("妈") ||
                comment.contains("lj") ||
                comment.contains("nm") ||
                comment.contains("逼") ||
                comment.contains("死") ||
                comment.contains("你妹") ||
                comment.contains("fuck") ||
                comment.contains("日") ||
                comment.contains("艹") ||
                comment.contains("操") ||
                comment.contains("靠") ||
                comment.contains("猪") ||
                comment.contains("c") ||
                comment.contains("屎")) {
            return true;
        }
        return false;
    }
}
