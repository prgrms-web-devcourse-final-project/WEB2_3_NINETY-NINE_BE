package com.example.onculture.global.utils.image;

import java.util.ArrayList;
import java.util.List;

public class ImageUrlUtil {
    public static String joinImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        return String.join(", ", imageUrls);
    }

    public static List<String> splitImageUrls(String imageUrlsString) {
        List<String> imageUrlList = new ArrayList<>();
        if (imageUrlsString == null || imageUrlsString.trim().isEmpty()) {
            return imageUrlList;
        }

        String[] urls = imageUrlsString.split(",");
        for (String url : urls) {
            String trimmedUrl = url.trim();
            if (!trimmedUrl.isEmpty()) {
                imageUrlList.add(trimmedUrl);
            }
        }
        return imageUrlList;
    }
}


