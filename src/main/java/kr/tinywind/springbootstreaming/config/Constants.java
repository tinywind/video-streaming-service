package kr.tinywind.springbootstreaming.config;

/**
 * Created by tinywind on 2016-06-01.
 */
public class Constants {
    public static final String VIDEO_ROOT;  //동영상 파일이 저장된 디렉토리 경로

    static {
        final String path = System.getProperty("video.root");
        final String fileSeparator = System.getProperty("file.separator");
        VIDEO_ROOT = path + (path.matches("^.*[" + (fileSeparator.equals("\\") ? "\\\\" : fileSeparator) + "]$") ? "" : fileSeparator);
    }
}
