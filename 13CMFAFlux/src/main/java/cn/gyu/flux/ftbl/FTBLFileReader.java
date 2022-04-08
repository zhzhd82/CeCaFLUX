package cn.gyu.flux.ftbl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FTBLFileReader {

	public static List<String> readBlock(String block, File file) {
		List<String> result = new ArrayList<>();
		BufferedReader reader = null;
		boolean isBlockStart = false;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString;
			while ((tempString = reader.readLine()) != null) {
				if (tempString.trim().length() == 0 || tempString.startsWith("//")) {
					continue;
				}
				if (!isBlockStart && tempString.startsWith(block)) {
					isBlockStart = true;
					continue;
				}
				if (isBlockStart) {
					if (!tempString.startsWith("\t")) {
						break;
					}
					String[] splits = tempString.split("\t");
					for (String str : splits) {
						if (str.length() > 0) {
							if (!str.startsWith("//")) {
								result.add(tempString);
							}
							break;
						}
					}
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

	public static List<String> readSubBlock(String block, String subBlock, File file) {
		return readSubBlock(block,subBlock,2,file);
	}

	public static List<String> readSubBlock(String block, String subBlock, int subBlockIndex, File file) {
		List<String> result = new ArrayList<>();
		BufferedReader reader = null;
		boolean isBlockStart = false;
		boolean isSubBlockStart = false;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString;
			while ((tempString = reader.readLine()) != null) {
				if (tempString.trim().length() == 0 || tempString.startsWith("//")) {
					continue;
				}
				if (!isBlockStart && tempString.startsWith(block)) {
					isBlockStart = true;
					continue;
				}
				if (isBlockStart) {
					if (!tempString.startsWith("\t")) {
						break;
					}
					String[] splits = tempString.split("\t");
					if(!isSubBlockStart && splits[subBlockIndex-1].startsWith(subBlock)) {
						isSubBlockStart = true;
						continue;
					}
					if(isSubBlockStart) {
						if(splits[subBlockIndex-1].trim().length() != 0) {
							break;
						}
						for (String str : splits) {
							if (str.length() > 0) {
								if (!str.startsWith("//")) {
									result.add(tempString);
								}
								break;
							}
						}
					}
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

}
