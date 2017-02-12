package hbku.qcri.sc.aidr.processTweets;

public class Tweet {
	public String _id = "";
	public String _url = "";
	
	public Tweet(String _id, String _url) {
		//super();
		this._id = _id;
		this._url = _url;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String get_url() {
		return _url;
	}

	public void set_url(String _url) {
		this._url = _url;
	}
	
}
