package com.xiaohe66.fe;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;

/**
 * @author xh
 * @date 18-01-30 030
 */
public class ExportWarAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Export.getInstance().export(anActionEvent,true);
    }
}
