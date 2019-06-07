package club.moredata.entity;

import java.io.Serializable;

/**
 * @author yeluodev1226
 */
public class UploadImage  implements Serializable {

    private static final long serialVersionUID = 8880903124863533682L;
    private int width;
    private String filename;
    private String url;
    private int height;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
