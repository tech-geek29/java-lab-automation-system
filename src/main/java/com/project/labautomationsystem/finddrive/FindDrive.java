package com.project.labautomationsystem.finddrive;

import java.io.*;
import java.net.UnknownHostException;

import com.project.labautomationsystem.client.Client;
import com.project.labautomationsystem.server.Server;

/**
 *
 * @author rishabh
 * 
 */
public class FindDrive {
	PrintWriter writer;

	public String find() throws UnknownHostException {
		String[] letters = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I" };
		File[] drives = new File[letters.length];
		boolean[] isDrive = new boolean[letters.length];

		// init the file objects and the initial drive state
		for (int i = 0; i < letters.length; ++i) {
			drives[i] = new File(letters[i] + ":/");

			isDrive[i] = drives[i].canRead();
		}
		// loop indefinitely
		while (true) {
			// check each drive
			for (int i = 0; i < letters.length; ++i) {
				boolean pluggedIn = drives[i].canRead();

				// if the state has changed output a message
				if (pluggedIn != isDrive[i]) {
					if (pluggedIn) {
						System.out.println("Drive " + letters[i] + " has been plugged in");
						Client c = new Client();
						String o = c.getInetAddress();
						Server s = new Server();
						s.h(o);

					} else {
						System.out.println("Drive " + letters[i] + " has been unplugged");
						Client c = new Client();
						String o = c.getInetAddress();
						Server s = new Server();
						s.hh(o);

					}
					isDrive[i] = pluggedIn;
				}
			}

			// wait before looping
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				/* do nothing */ }

		}
	}
}
