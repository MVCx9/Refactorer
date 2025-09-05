package main.neo.algorithms.cognitivecomplexity.sonar;

import java.util.List;

public class Analysis {

	private String key;
	private String date;
	private List<Event> events = null;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

}