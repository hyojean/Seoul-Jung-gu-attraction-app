package ddwu.mobile.finalproject.ma01_20181801;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

public class PlaceXmlParser {

    private XmlPullParser parser;

    public PlaceXmlParser() {
        try {
            parser = XmlPullParserFactory.newInstance().newPullParser();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Place> parse(String xml) {
        ArrayList<Place> resultList = new ArrayList<>();
        Place dto = null;
        String tagType = "";

        try {
            parser.setInput(new StringReader(xml));
            int eventType = parser.getEventType();

            while(eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        String tag = parser.getName();
                        if (tag.equals("item")) {
                            dto = new Place();
                        } else if (tag.equals("addr1") && (dto != null)) {
                            tagType = "addr1";
                        } else if (tag.equals("addr2") && (dto != null)) {
                            tagType = "addr2";
                        } else if (tag.equals("image1") && (dto != null)) {
                            tagType = "image1";
                        } else if (tag.equals("image2") && (dto != null)) {
                            tagType = "image2";
                        } else if (tag.equals("mapX") && (dto != null)) {
                            tagType = "mapX";
                        } else if (tag.equals("mapY") && (dto != null)) {
                            tagType = "mapY";
                        } else if (tag.equals("sigunguCode") && (dto != null)) {
                            tagType = "sigunguCode";
                        } else if (tag.equals("title") && (dto != null)) {
                            tagType = "title";
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("item")) {
                            resultList.add(dto);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        switch (tagType) {
                            case "addr1":
                                dto.setAddr1(parser.getText());
                                break;
                            case "addr2":
                                dto.setAddr2(parser.getText());
                                break;
                            case "image1":
                                if (parser.getText().contains("http")) {
                                    dto.setImage1(parser.getText());
                                } else
                                    dto.setImage1("http://www.billking.co.kr/index/skin/board/basic_support/img/noimage.gif");
                                break;
                            case "image2":
                                dto.setImage2(parser.getText());
                                break;
                            case "mapX":
                                dto.setMapX(parser.getText());
                                break;
                            case "mapY":
                                dto.setMapY(parser.getText());
                                break;
                            case "sigunguCode":
                                dto.setSigunguCode(parser.getText());
                                break;
                            case "title":
                                dto.setTitle(parser.getText());
                                break;
                        }
                        tagType = "";
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

}
