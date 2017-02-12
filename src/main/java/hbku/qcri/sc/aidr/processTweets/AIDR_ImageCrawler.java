package hbku.qcri.sc.aidr.processTweets;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.omg.CORBA._IDLTypeStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AIDR_ImageCrawler {

	

	/**
	 * Persisting To Postgres
	 *
	 * @param message
	 */
	protected static final Logger log = LoggerFactory.getLogger(AIDR_ImageCrawler.class);

	
	public static Tweet parseDataFeed(String message) {
	
		String imageUrl = "";
		JSONObject msgJson = new JSONObject(message);

		String _id = msgJson.get("id").toString();
		if (_id == null) {
			return null;
		}
		JSONObject entities = msgJson.getJSONObject("entities");
		// System.out.println(entities.toString());
		if (entities != null && entities.has("media") && entities.getJSONArray("media") != null
				&& entities.getJSONArray("media").length() > 0
				&& entities.getJSONArray("media").getJSONObject(0).getString("type") != null
				&& entities.getJSONArray("media").getJSONObject(0).getString("type").equals("photo")) {
			// System.out.println("Parse incoming tweets for image
			// info...");
			// String imgType =
			// entities.getJSONArray("media").getJSONObject(0).getString("type");
			imageUrl = entities.getJSONArray("media").getJSONObject(0).getString("media_url");
			if (imageUrl == null) {
				return null;
			}
		}
		
		return new Tweet(_id, imageUrl);
	}

	
	public static List<Tweet> getUniqUrls(String json_file) throws IOException {
		FileInputStream fstream = new FileInputStream(json_file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		List<String> urls = new ArrayList<String>();
		List<Tweet> twts = new ArrayList<Tweet>();
		Tweet twt = null;
		String strLine;
		String _url = "";
		
		int count = 0;
		
		while ((strLine = br.readLine()) != null) {
			twt = parseDataFeed(strLine); // Get the tweet
			if (twt != null) {
				_url = twt._url;
				if (_url.trim() != ""){
					count +=1;
					if(!urls.contains(_url) ){
						urls.add(_url);
						twts.add(twt);
					}
				}
				
			}
		}

		log.info("Number of image URLs: " + count);
		
		br.close();
		return twts;
	}

	public static void main(String args[]) throws ParseException, IOException {
		CommandLineParser parser = new BasicParser();
		Options options = new Options();
		options.addOption("f", "folder", true, "Save image to the folder under the name of collection");
		options.addOption("j", "json_file", true, "Json file of the collection");

		CommandLine commandLine = parser.parse(options, args);

		String json_file = commandLine.getOptionValue('j', "170206130008_z_images_20170206_vol-1.json");
		String folder = commandLine.getOptionValue('f', "./_o_n/");

		log.info("-------------------------------------------------");
		List<Tweet> twts = getUniqUrls(json_file);
		int n = twts.size();
		log.info("Number of uniq URLs: " + n);
		log.info("Starting to crawl images...!");

		String outFile = "";
		String imgName = "";
		String formatName = "";
		
		for (int i = 0; i < n; i++) {
	
			URL _url = new URL(twts.get(i).get_url());
			imgName = twts.get(i).get_id();
			formatName = FilenameUtils.getExtension(_url.getPath());	
			outFile = folder + "/" + imgName + "." + formatName;
			log.info(_url.toString());
			//log.info(formatName);
			
			File outputfile = new File(outFile);
			try {
				BufferedImage image = ImageIO.read(_url);
				if (image != null) {
					ImageIO.write(image, formatName, outputfile);
				}
			} catch (IOException e) {
			}
			if (i % 1000 == 0) {
				log.info(" -Done " + i + " urls...!");
			}
			
		}
		log.info("Fnished...!");

	}

}
