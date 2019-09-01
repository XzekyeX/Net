package net.fimitek.network;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
 * 
 * @author Mikko Tekoniemi
 *
 */
public class Test {

	public static List<FolderNode> getSubModules(FolderNode parent) {
		List<FolderNode> result = new ArrayList<FolderNode>();
		
		return result;
	}

	public static void main(String[] args) {

		FolderNode cfolder = new FolderNode("C", "C:/");
		List<FolderNode> nodes = getSubModules(cfolder);
		for (FolderNode n : nodes) {
			System.out.println("n: " + n);
		}

	}

}
