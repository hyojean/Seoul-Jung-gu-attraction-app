package ddwu.mobile.finalproject.ma01_20181801;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "myPlace_table")
public class MyPlace implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int _id;
    private String addr1;
    private String addr2;
    private String image1;
    private String image2;
    private String mapX;
    private String mapY;
    private String sigunguCode;
    private String title;
    private String imageFileName;       // 외부저장소에 저장했을 때의 파일명

    public MyPlace() {
        this.imageFileName = null;      // 생성 시에는 외부저장소에 파일이 없으므로 null로 초기화
    }

    public MyPlace(int _id, String addr1, String addr2, String image1, String image2, String mapX, String mapY, String sigunguCode, String title, String imageFileName) {
        this._id = _id;
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.image1 = image1;
        this.image2 = image2;
        this.mapX = mapX;
        this.mapY = mapY;
        this.sigunguCode = sigunguCode;
        this.title = title;
        this.imageFileName = imageFileName;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getAddr1() {
        return addr1;
    }

    public void setAddr1(String addr1) {
        this.addr1 = addr1;
    }

    public String getAddr2() {
        return addr2;
    }

    public void setAddr2(String addr2) {
        this.addr2 = addr2;
    }

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public String getMapX() {
        return mapX;
    }

    public void setMapX(String mapX) {
        this.mapX = mapX;
    }

    public String getMapY() {
        return mapY;
    }

    public void setMapY(String mapY) {
        this.mapY = mapY;
    }

    public String getSigunguCode() {
        return sigunguCode;
    }

    public void setSigunguCode(String sigunguCode) {
        this.sigunguCode = sigunguCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

}
