package com.xiaohe66.fe;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 *
 * 导出
 * todo:export需要增加日期以区分不同时间导出的
 *
 * @author xh
 * @date 18-01-31 031
 */
public class Export {

    private static final String JAVA_FILE_EXTENSION = "java";
    private static final String CLASS_FILE_EXTENSION = "class";

    private static final String DEFAULT_EXPORT_DIRECTORY_PATH = "/export";

    private static final String DEFAULT_WAR_CLASS_RELATIVE_PATH = "/WEB-INF/classes";

    private static final String EXPORT_DIRECTORY_PATH_EXISTS_TITLE = "目录已存在";
    private static final String EXPORT_DIRECTORY_PATH_EXISTS_MESSAGE = "存在今天导出旧文件，是否删除？\n" +
            "确定：删除旧文件\n取消：将新导出的文件覆盖到旧目录中（同路径的同名旧文件将会被删除）";

    private static Export export;

    private Export() {
        System.out.println("初始化");
    }

    public static Export getInstance() {
        if (export == null) {
            synchronized (Export.class) {
                if (export == null) {
                    export = new Export();
                }
            }
        }
        return export;
    }

    public void export(AnActionEvent anActionEvent, boolean isExportWar){
        //当前项目
        Project project = anActionEvent.getData(PlatformDataKeys.PROJECT);

        //当前选择的所有文件
        VirtualFile[] files = anActionEvent.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);

        String projectPath = project.getBasePath();

        String datePath = "/"+XhUtils.getCurrentDateStr();

        String exportRootPath = projectPath + DEFAULT_EXPORT_DIRECTORY_PATH + datePath;

        File exportRootFile = new File(exportRootPath);
        if(exportRootFile.exists()){
            int result = Messages.showOkCancelDialog(EXPORT_DIRECTORY_PATH_EXISTS_MESSAGE,
                    EXPORT_DIRECTORY_PATH_EXISTS_TITLE,Messages.getQuestionIcon());

            //0：确定，1：取消
            if(result == 0){
                XhUtils.deleteFile(exportRootFile);
            }
        }

        //导出war
        if(isExportWar){

            Set<VirtualFile> notJavaFileSet = new HashSet<>();
            Set<VirtualFile> javaFileSet = new HashSet<>();

            //区分java文件和非java文件
            for (VirtualFile file : files) {
                //文件的扩展名
                String extension = file.getExtension();
                if(JAVA_FILE_EXTENSION.equalsIgnoreCase(extension)){
                    javaFileSet.add(file);
                }else{
                    notJavaFileSet.add(file);
                }
            }

            if(javaFileSet.size() > 0){
                //编译管理器
                CompilerManager compilerManager = CompilerManager.getInstance(project);
                //导出java前，先编译
                compilerManager.compile(javaFileSet.toArray(new VirtualFile[javaFileSet.size()]), (b, i, i1, compileContext) -> {
                        exportClass(javaFileSet,project,exportRootPath);
                        exportFile(notJavaFileSet,project,exportRootPath);
                        XhUtils.openDirectory(projectPath+DEFAULT_EXPORT_DIRECTORY_PATH);
                });
            }else{
                exportFile(notJavaFileSet,project,exportRootPath);
                XhUtils.openDirectory(projectPath+DEFAULT_EXPORT_DIRECTORY_PATH);
            }
        }
        //导出源
        else{
            //todo:导出java和导出jsp等这些文件不能共用一个方法，因为他们的目录不一样，需要另外识别
            Set<VirtualFile> set = new HashSet<>(Arrays.asList(files));
            exportFile(set,project,exportRootPath);
            //打开导出后的目录
            XhUtils.openDirectory(projectPath+DEFAULT_EXPORT_DIRECTORY_PATH);
        }

    }



    /**
     * 导出class
     * @param javaFileSet
     * @param project
     */
    private void exportClass(Set<VirtualFile> javaFileSet, Project project,String exportRootPath){

        //项目的上下文
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);

        //psi管理器
        PsiManager psiManager = PsiManager.getInstance(project);

        for (VirtualFile javaFile : javaFileSet) {
            //忽略目录
            if (javaFile.isDirectory()) {
                return;
            }

            //获取当前文件所在的module
            Module module = projectRootManager.getFileIndex().getModuleForFile(javaFile);

            //所有的资源文件目录
            VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();

            //资源的全路径
            String sourcePath = javaFile.getPath();

            //资源的相对路径
            String sourceRelativePath;

            String sourceRoot = "";
            for (VirtualFile virtualFile : sourceRoots) {

                if(sourcePath.startsWith(virtualFile.getPath())){
                    sourceRoot = virtualFile.getPath();
                    break;
                }
            }

            //在资源文件目录下
            if (!"".equals(sourceRoot)) {

                //获取的值示例：/com/xiaohe66/xh.java
                sourceRelativePath = sourcePath.substring(sourceRoot.length(),sourcePath.length());
            }
            //不在资源目录下
            else{
                //获取包名
                String packageName = ((PsiJavaFile)psiManager.findFile(javaFile)).getPackageName();

                //将包名中的点替换成斜杠：com.xiaohe66   -->  /com/xiaohe66
                String packagePath = packageName.length() == 0 ? "" : "/" + packageName.replace(".", "/");

                //获取的值示例：/com/xiaohe66.xh.java
                sourceRelativePath = packagePath +"/"+ javaFile.getName();
            }

            //把.java替换为.class   /com/xiaohe66/xh.java    -->    /com/xiaohe66/xh.class
            String sourceClassPath = sourceRelativePath.replace(JAVA_FILE_EXTENSION,CLASS_FILE_EXTENSION);

            //获取class编译存放的根路径：F://XX/XX
            String classHomePath = CompilerPaths.getModuleOutputPath(module, false);

            //重设源和目标的路径
            sourcePath = classHomePath + sourceClassPath;
            String targetPath = exportRootPath + DEFAULT_WAR_CLASS_RELATIVE_PATH + sourceClassPath;
            XhUtils.copy(sourcePath,targetPath);
        }
    }

    /**
     * 导出普通文件
     * @param fileSet
     * @param project
     */
    private void exportFile(Set<VirtualFile> fileSet, Project project,String exportRootPath){
        //项目的上下文
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);

        for (VirtualFile notJavaFile : fileSet) {
            //忽略目录
            if (notJavaFile.isDirectory()) {
                return;
            }
            //获取当前文件所在的module
            Module module = projectRootManager.getFileIndex().getModuleForFile(notJavaFile);

            //项目根路径
            String projectPath = project.getBasePath();

            String sourcePath = notJavaFile.getPath();
            String targetPath = null;

            String sourceRelativePath;// = sourcePath.substring(projectPath.length());

            //所有的资源文件目录
            VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
            String sourceRoot = "";
            for (VirtualFile virtualFile : sourceRoots) {

                if(sourcePath.startsWith(virtualFile.getPath())){
                    sourceRoot = virtualFile.getPath();
                    break;
                }
            }

            //在资源文件目录下
            if (!"".equals(sourceRoot)) {
                sourceRelativePath = sourcePath.substring(sourceRoot.length());
                targetPath = exportRootPath + DEFAULT_WAR_CLASS_RELATIVE_PATH + sourceRelativePath;
            }
            //不在资源文件目录下
            else{

                //todo:需要支持maven目录下webRoot文件的导出
                /*String webRootPath = "";
                for (Facet facet : FacetManager.getInstance(module).getAllFacets()) {
                    FacetType facetType = facet.getType();
                    System.out.println(facetType.getPresentableName());
                }

                facetFor : for (WebFacet webFacet : WebFacet.getInstances(module)) {

                    for (WebRoot webRoot : webFacet.getWebRoots(true)) {
                        webRootPath = webRoot.getDirectoryUrl();
                        break facetFor;
                    }
                }
                System.out.println(webRootPath);*/
                /*
                * F:/XX/WebRoot/js/jquery.js    -->     /WebRoot/js/jquery.js
                * */
                sourceRelativePath = sourcePath.substring(projectPath.length());
                /*
                * /WebRoot/js/jquery.js     -->     /js/jquery.js
                * */
                sourceRelativePath = sourceRelativePath.substring(sourceRelativePath.indexOf("/",2));
                targetPath = exportRootPath + sourceRelativePath;
            }

            XhUtils.copy(sourcePath,targetPath);
        }
    }
}
