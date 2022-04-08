package edu.gyu.mfa.util;

import edu.gyu.mfa.info.Argument;
import edu.gyu.mfa.info.Constant;

import java.io.*;
import java.util.*;

public class Util {

	public static String paddingPrefixWithZero(String src, int length) {
		String result = src;
		if (src.length() < length) {
			int diff = length - src.length();
			for (int i = 0; i < diff; i++) {
				result = "0" + result;
			}
		}
		return result;
	}

	public static String joinNames(String spliter, String... names) {
		StringBuffer sb = new StringBuffer();
		int len = names.length;
		for(int index = 0; index < len; index++) {
			sb.append(names[index]);
			if(index < len - 1) {
				sb.append(spliter);
			}
		}
		return sb.toString();
	}
	
	public static String joinNames(String spliter, int... names) {
		StringBuffer sb = new StringBuffer();
		int len = names.length;
		for(int index = 0; index < len; index++) {
			sb.append(names[index]);
			if(index < len - 1) {
				sb.append(spliter);
			}
		}
		return sb.toString();
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

	public static double formatNumber(double number) {
		double base = 1e8;
		double decimalBase = 1e12;
		if(number > base) {
			return base;
		} else if(number < -base) {
			return -base;
		}
		return Math.round(number * decimalBase) / decimalBase;
	}

	public static boolean contains(String src, String target, String splitter) {
		boolean result = true;
		String[] splits = src.split(splitter);
		for(String split : splits) {
			if(!target.contains(split)) {
				result = false;
				break;
			}
		}
		return result;
	}

	public static boolean containsSubString(String src, String target, int start, int len) {
		return src.equals(target.substring(start, start+len));
	}

	public static boolean containsSubString(String src, String target, String cPosition) {
		String[] cSplits = cPosition.split(Constant.NAME_SPLITTER);
		int start = Integer.parseInt(cSplits[0]) - 1;
		return containsSubString(src, target, start, cSplits.length);
	}

	public static int factorial(int n) {
		if(n <= 1) {
			return 1;
		}
		int result = 1;
		for(int num = 2; num <=n; num++) {
			result *= num;
		}
		return result;
	}

	public static int combination(int m, int n) {
		return factorial(n) / (factorial(m) * factorial(n - m));
	}

	public static String parsePath(String fileName) {
		String result;
		int lastSeparatorIndex = fileName.lastIndexOf(File.separator);
		result = fileName.substring(0, lastSeparatorIndex);
		return result;
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

	public static void parseArgs(String[] args) {
		Map<String, String> map = new HashMap<>();
		for(String arg : args) {
			String[] argSplits = arg.split("=");
			map.put(argSplits[0], argSplits[1]);
		}

		Argument.sampling_proc_file = map.get("flux_sampling_file");

		if(map.containsKey("python")) {
			Argument.python = map.get("python");
		}

		if(map.containsKey("cpu_cores_keep")) {
			Argument.cpu_cores_keep = Integer.parseInt(map.get("cpu_cores_keep"));
		}

		if(map.containsKey("mode") && map.get("mode").equals("fix")) {
			Argument.isAdaptive = false;
		}

		if(map.containsKey("step")) {
			Argument.step = Double.parseDouble(map.get("step"));
		}

		if(map.containsKey("tolerance_addition_factor")) {
			Argument.tolerance_addition_factor = Double.parseDouble(map.get("tolerance_addition_factor"));
		}

		if(map.containsKey("tolerance_scaling_factor")) {
			Argument.tolerance_scaling_factor = Double.parseDouble(map.get("tolerance_scaling_factor"));
		}

		if(map.containsKey("sample_lower_bound")) {
			Argument.lower_bound = Double.parseDouble(map.get("sample_lower_bound"));
		}

		if(map.containsKey("sample_upper_bound")) {
			Argument.upper_bound = Double.parseDouble(map.get("sample_upper_bound"));
			Constant.V_UB = Argument.upper_bound;
		}

	}

	public static void removeStringBufferEndTag(StringBuffer sb, String tag) {
		if(sb.length() < tag.length() || !sb.toString().endsWith(tag)) {
			return;
		}
		sb.delete(sb.length() - tag.length(), sb.length());
	}

}
