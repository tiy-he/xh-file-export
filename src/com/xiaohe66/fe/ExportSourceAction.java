package com.xiaohe66.fe;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author xh
 * @date 18-01-31 031
 */
public class ExportSourceAction extends AnAction{

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Export.getInstance().export(anActionEvent,false);
    }
}
