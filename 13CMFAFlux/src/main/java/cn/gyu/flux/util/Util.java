package cn.gyu.flux.util;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

	public static double roundNumber(double number, int scale) {
		BigDecimal b = new BigDecimal(number);
		return b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static double formatNumber(double number) {
		double base = 1e8;
		double decimalBase = 1e8;
		if(number > base) {
			return base;
		} else if(number < -base) {
			return -base;
		}
		return Math.round(number * decimalBase) / decimalBase;
	}

	public static void writeContentToFile(String fileName, String content) {
		File file = new File(fileName);
		if(file.exists()) {
			file.delete();
		}
		try {
			FileWriter fw = new FileWriter(file);
			fw.write(content);
			fw.close();
		} catch (Exception e) {
		}
	}

	public static List<String> readFileContent(String file) {
		List<String> result = new ArrayList<>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String content;
			while ((content = reader.readLine()) != null) {
				if(!isBlank(content)) {
					result.add(content);
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
			result.clear();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return result;
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

	public static String parsePath(String fileName) {
		String result;
		int lastSeparatorIndex = fileName.lastIndexOf(File.separator);
		result = fileName.substring(0, lastSeparatorIndex);
		return result;
	}

	public static double parseCoefficient(String str) {
		double result = 1.0;
		try {
			String regex = "^\\d+(\\.\\d+)?";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(str);
			if(m.find()) {
				result = Double.parseDouble(m.group());
			}
		}catch(Exception e) {
			result = 1.0;
		}
		return result;
	}

	public static String removeCoefficient(String str) {
		return str.replaceAll("^\\d+(\\.\\d+)?", "").trim();
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

	public static String insertCommaToString(String content) {
		String[] splits = content.split("");
		StringBuffer sb = new StringBuffer();
		for(int index = 0; index < splits.length; index++) {
			sb.append(splits[index]);
			if(index < splits.length - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

}
