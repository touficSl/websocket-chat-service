package com.ts.messenger;

import com.ts.messenger.utils.AESencrp;

public class TEST {

	public static void main(String [] args) throws Exception {
		String encrypted = AESencrp.encrypt("https://static.remove.bg/remove-bg-web/eb1bb48845c5007c3ec8d72ce7972fc8b76733b1/assets/start-1abfb4fe2980eabfbbaaa4365a0692539f7cd2725f324f904565a9a744f8e214.jpg");
	
		System.out.println(encrypted);
		
		String decrypt = AESencrp.decrypt("/DGAIKBDppUXAvhBbr7V86J5JKs87BAGUVA97eXforLm4w90f82fVbRceRyAzWNnoEIRXxu0tynwbCrgBSt5Ac7Isq3eNazHE/5AjCEUvlbJCfvLDbpZJYI6w5Q/Tm0hBXxU4KHifxvqGYUHeCBeoli8tg6/1B8f7OHFq1RtBHl1s2a7i/ja4Yj+kFta3BZ0FamOD1gOYfh3TD8HJISa4YAbgKjwDyQz6I8tzgXVGmA=");

		System.out.println(decrypt);
	}
}
