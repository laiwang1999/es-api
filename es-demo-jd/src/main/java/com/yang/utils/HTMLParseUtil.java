package com.yang.utils;

import com.yang.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HTMLParseUtil {
    public static void main(String[] args) throws IOException {
        List<Content> java = new HTMLParseUtil().parseJD("西游记");
        java.forEach(System.out::println);
    }

    public List<Content> parseJD(String keywords) throws IOException {
        String url = "https://search.jd.com/Search?keyword=" + keywords+"&enc=utf-8";
        Document document = Jsoup.parse(new URL(url), 30000);
        Element element = document.getElementById("J_goodsList");
        Elements elements = element.getElementsByTag("li");
        List<Content> goodsList = new ArrayList<>();
        for (Element el : elements) {
            //关于这种图片，特别多的图片都是延时加载的
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();
            goodsList.add(new Content(title, price, img));
        }
        return goodsList;
    }
}
