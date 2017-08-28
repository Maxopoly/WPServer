package com.github.maxopoly.WPServer.model;

import com.github.maxopoly.WPCommon.model.Chest;
import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.WPItem;
import com.github.maxopoly.WPServer.Main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChestManagement {

	private static ChestManagement instance;
	private boolean dirty;

	public static ChestManagement getInstance() {
		if (instance == null) {
			instance = new ChestManagement();
		}
		return instance;
	}

	private static final String saveFilePath = "chests.json";
	private static final String backupFilePath = "chestsBackup.json";
	private static final int saveIntervall = 120;

	private Map<WPItem, Set<Chest>> items;
	private Map<Location, Chest> chests;

	private ChestManagement() {
		this.items = new HashMap<WPItem, Set<Chest>>();
		this.chests = new HashMap<Location, Chest>();
		ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
		exec.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				saveToFile();

			}
		}, saveIntervall, saveIntervall, TimeUnit.SECONDS);
		loadFromFile();
		dirty = false;
	}

	public synchronized void updateContent(Chest chest) {
		dirty = true;
		Chest preExisting = chests.get(chest.getLocation());
		if (preExisting == null) {
			chests.put(chest.getLocation(), chest);
			preExisting = chest;
		}
		preExisting.setContent(chest.getContent());
		for (WPItem item : chest.getContent()) {
			if (item.isRepairable()) {
				item.setDurability(0);
			}
			Set<Chest> chests = items.get(item);
			if (chests == null) {
				chests = new HashSet<Chest>();
				items.put(item, chests);
			}
			chests.add(preExisting);
		}
	}

	private synchronized List<Chest> getChestsForItem(WPItem item) {
		Set<Chest> unfiltered = items.get(item);
		if (unfiltered == null) {
			return new LinkedList<Chest>();
		}
		List<Chest> copy = new LinkedList<Chest>(unfiltered);
		Iterator<Chest> iter = copy.iterator();
		while (iter.hasNext()) {
			Chest chest = iter.next();
			if (chest.getAmount(item) == 0) {
				iter.remove();
			}
		}
		return copy;
	}

	public synchronized Map<Chest, List<WPItem>> getChestsForSimilarItems(WPItem inputItem) {
		Map<Chest, List<WPItem>> locs = new HashMap<Chest, List<WPItem>>();
		if (inputItem.isRepairable()) {
			inputItem.setDurability(0);
		}
		WPItem[] toLookUp = new WPItem[] { new WPItem(inputItem.getID(), 1, inputItem.getDurability(), false, false),
				new WPItem(inputItem.getID(), 1, inputItem.getDurability(), true, false),
				new WPItem(inputItem.getID(), 1, inputItem.getDurability(), false, true) };
		for (WPItem item : toLookUp) {
			List<Chest> chests = getChestsForItem(item);
			if (chests.size() == 0) {
				continue;
			}
			for (Chest chest : chests) {
				List<WPItem> itemList = locs.get(chest);
				if (itemList == null) {
					itemList = new LinkedList<WPItem>();
					locs.put(chest, itemList);
				}
				itemList.add(new WPItem(item.getID(), chest.getAmount(item), item.getDurability(), item.isCompacted(),
						item.isEnchanted()));
			}
		}
		return locs;
	}

	public synchronized void loadFromFile() {
		Logger logger = Main.getLogger();
		File file = new File(saveFilePath);
		if (!file.exists()) {
			logger.warn("Tried to load chest content from file, but file did not exist");
			return;
		}
		StringBuilder sb = new StringBuilder();
		try (BufferedReader buff = new BufferedReader(new FileReader(file));) {
			for (String line = buff.readLine(); line != null; line = buff.readLine()) {
				sb.append(line);
			}
		} catch (FileNotFoundException e) {
			logger.warn("Failed to find chest content save file", e);
			return;
		} catch (IOException e) {
			logger.warn("IOException while loading chest content save file", e);
			return;
		}
		JSONObject json = new JSONObject(sb.toString());
		JSONArray chestArray = json.getJSONArray("chests");
		for (int i = 0; i < chestArray.length(); i++) {
			updateContent(new Chest(chestArray.getJSONObject(i)));
		}
		logger.info("Loaded " + chests.size() + " chests from save file");
	}

	public synchronized void saveToFile() {
		if (!dirty) {
			return;
		}
		File saveFile = new File(saveFilePath);
		File backUpFile = new File(backupFilePath);
		if (saveFile.exists() && backUpFile.exists()) {
			backUpFile.delete();
			saveFile.renameTo(backUpFile);
		}
		List<Chest> toSave = new LinkedList<Chest>(chests.values());
		JSONObject json = new JSONObject();
		JSONArray chestArray = new JSONArray();
		for (Chest chest : toSave) {
			chestArray.put(chest.serialize());
		}
		json.put("chests", chestArray);
		try (FileWriter writer = new FileWriter(saveFile)) {
			writer.write(json.toString());
		} catch (IOException e) {
			Main.getLogger().error("Failed to save chest content to save file", e);
			return;
		}
		Main.getLogger().info(
				"Successfully saved chest content to save file, total of " + chests.size() + " chests tracked");
		dirty = false;
	}
}
