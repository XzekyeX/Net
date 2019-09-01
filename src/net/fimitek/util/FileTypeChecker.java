package net.fimitek.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * 
 * @author Mikko Tekoniemi
 *
 */
public class FileTypeChecker {
	public static void moveSubSubFiles(File folder) {
		File[] files = folder.listFiles();
		if (files != null) for (File file : files) {
			if (file != null) {
				if (file.isDirectory()) {
					// log.add("folder: " + file);
					File[] subFiles = file.listFiles();
					if (subFiles != null) {
						for (File sub : subFiles) {
							if (sub.isDirectory()) {
								// log.add("subFolder: " + sub);
								File[] ssf = sub.listFiles();
								if (ssf != null) {
									for (File ssfile : ssf) {
										if (!ssfile.isDirectory()) moveFileAndCheckFileType(ssfile, folder);
									}
								}
							} else {
								// log.add("subFile: " + sub);
								moveFileAndCheckFileType(sub, folder);
							}
						}
					}
				} else {
					// log.add("file: " + file);
				}
			}
		}
	}

	public static void moveFileTo(File from, File to) {
		try {
			File f = new File(to.getAbsolutePath() + "/" + from.getName());
			boolean renamed = from.renameTo(f);
			System.out.println("Moved: " + renamed + ", " + from + " moved to: " + f);
		} catch (SecurityException e) {
			System.err.println("method denies write access to either the old or new pathnames");
		}
	}

	public static void renameFileTo(File from, File to, String ends) {
		try {
			File f = new File(to.getAbsolutePath() + "/" + from.getName() + ends);
			boolean renamed = from.renameTo(f);
			System.out.println("Moving and renaming: " + renamed + ", " + from + " moved and renamed to: " + f);
		} catch (SecurityException e) {
			System.err.println("method denies write access to either the old or new pathnames");
		}
	}

	public static void moveFileAndCheckFileType(File from, File to) {
		if (/* file.getName().startsWith("f_") && */ !(from.getName().endsWith(".png") || from.getName().endsWith(".jpg") || from.getName().endsWith(".gif") || from.getName().endsWith(".mp4") || from.getName().endsWith(".webp") || from.getName().endsWith(".html") || from.getName().endsWith(".woff"))) {
			String line = readFileFirstLine(from);
			if (line.contains("PNG")) {
				renameFileTo(from, to, ".png");
			} else if (line.contains("JPEG") || line.contains("JFIF")) {
				renameFileTo(from, to, ".jpg");
			} else if (line.contains("GIF")) {
				renameFileTo(from, to, ".gif");
			} else if (line.contains("ftypisom") || line.contains("FFmpeg")) {
				renameFileTo(from, to, ".mp4");
			} else if (line.contains("WEBP")) {
				renameFileTo(from, to, ".webp");
			} else if (line.contains("DOCTYPE")) {
				renameFileTo(from, to, ".html");
			} else if (line.contains("wOF2")) {
				renameFileTo(from, to, ".woff");
			}
		} else {
			moveFileTo(from, to);
		}
	}

	public static void checkFileTypesAndRename(File folder) {
		File[] files = folder.listFiles();
		System.out.println("files: " + files.length);
		// List<String> res = new ArrayList<String>();
		if (files != null) for (File file : files) {
			if (file != null) {
				if (file.isDirectory()) {
					// res.add(file.getName());
				} else {
					System.out.println(file.getName());
					if (/* file.getName().startsWith("f_") && */ !(file.getName().endsWith(".png") || file.getName().endsWith(".jpg") || file.getName().endsWith(".gif") || file.getName().endsWith(".mp4") || file.getName().endsWith(".webp") || file.getName().endsWith(".html") || file.getName().endsWith(".woff"))) {
						String line = readFileFirstLine(file);
						if (line.contains("PNG")) {
							renameFile(file, ".png");
						} else if (line.contains("JPEG") || line.contains("JFIF")) {
							renameFile(file, ".jpg");
						} else if (line.contains("GIF")) {
							renameFile(file, ".gif");
						} else if (line.contains("ftypisom") || line.contains("FFmpeg")) {
							renameFile(file, ".mp4");
						} else if (line.contains("WEBP")) {
							renameFile(file, ".webp");
						} else if (line.contains("DOCTYPE")) {
							renameFile(file, ".html");
						} else if (line.contains("wOF2")) {
							renameFile(file, ".woff");
						}
					}
				}
			} else System.out.println("file is null!");
		}
	}

	public static void renameFile(File file, String ends) {
		try {
			File rf = new File(file.getAbsolutePath() + ends);
			boolean renamed = file.renameTo(rf);
			System.out.println("Renaming: " + renamed + ", " + file.getName() + " renamed to: " + rf.getName());
		} catch (SecurityException e) {
			System.err.println("method denies write access to either the old or new pathnames");
		}
	}

	public static List<String> readFile(String path, String prefix) {
		List<String> result = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = "";
			while ((line = br.readLine()) != null) {
				String str = line.trim();
				if (str.startsWith(prefix)) result.add(str);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String readFileFirstLine(File file) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			// System.out.println("Reading file: " + file.getName());
			String line = br.readLine();
			// System.out.println("Line: " + line);
			br.close();
			return line;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static void saveFile(List<String> values, String filename) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			for (String s : values) {
				bw.write(s + "\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveFile(List<String> values, String path, String filename) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path, filename)));
			for (String s : values) {
				bw.write(s + "\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		File folder = new File("C:/");
		checkFileTypesAndRename(folder);
	}
}
