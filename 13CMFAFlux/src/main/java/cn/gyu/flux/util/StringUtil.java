package cn.gyu.flux.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    public static List<String> parseContentWithTag(String contents, String startTag, String endTag) {
        List<String> results = new ArrayList<>();
        int firstPos = 0;
        while (true) {
            firstPos = contents.indexOf(startTag, firstPos);
            int lastPos = contents.indexOf(endTag, firstPos);
            if(firstPos == -1 || lastPos == -1) {
                break;
            }
            firstPos = firstPos + startTag.length();
            results.add(contents.substring(firstPos, lastPos));
            firstPos = lastPos + endTag.length();
        }
        return results;
    }

    public static double nextDouble(double min, double max) {
        Random random = new Random();
        return min + (max - min) * random.nextDouble();
    }

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static String removeCoefficient(String str) {
        return str.replaceAll("^\\d+", "").trim();
    }

    public static void removeStringBufferEndTag(StringBuffer sb, String tag) {
        if(sb.length() < tag.length() || !sb.toString().endsWith(tag)) {
            return;
        }
        sb.delete(sb.length() - tag.length(), sb.length());
    }

    public static void removeStringBufferEndTag(String tag, StringBuffer... sbs) {
        for(StringBuffer sb : sbs) {
            removeStringBufferEndTag(sb, tag);
        }
    }

    public static String moveBeginningNumberToEnd(String str) {
        String beginNumbers = "";
        try {
            String regex = "^\\d+";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(str);
            if(m.find()) {
                beginNumbers = m.group();
            }
        }catch(Exception e) {
        }

        String result = removeCoefficient(str).trim();
        result = result + beginNumbers;
        return result;
    }

}
