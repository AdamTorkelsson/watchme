/**
 * A class that represents a Movie.
 * 
 * A Movie contains information about its title, rating, note and tags.
 * The higher rating a movie has, the more you want to see it. 
 * 
 * @author lisastenberg
 */

package se.chalmers.watchme.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Movie implements Serializable {
	
	/**
	 * The JSON key for a movie's title
	 */
	public static final String JSON_KEY_NAME = "original_name";
	/**
	 * The JSON key for a movie's ID
	 */
	public static final String JSON_KEY_ID = "id";
	/**
	 * The JSON key for a movie's release date
	 */
	public static final String JSON_KEY_DATE = "released";
	/**
	 * Use to check if a Movie hasn't an API id set.
	 */
	public static final int NO_API_ID = -1;
	
	private String title, note;
	private int apiID;
	private int rating;
	private long id;
	private Calendar releaseDate;
	private List<Tag> tags;
	
	private Map<PosterSize, String> posters;
	
	/**
	 * Supported poster sizes for a Movie.
	 * 
	 * @author Johan
	 */
	public enum PosterSize {
		MID("mid"), THUMB("thumb");
		
		private final String size;
		
		private PosterSize(String size) {
			this.size = size;
		}
		
		public String getSize() {
			return this.size;
		}
	}
	
	/**
	 * Creates a movie with the given title, release date set as current date,
	 * rating 0 and an empty note.
	 * @param title The title of the Movie.
	 */
	public Movie(String title) {
		this(title, Calendar.getInstance(), 0, "");
	}
	
	/**
	 * Creates a movie with the given title, rating and note.
	 * @param title The title of the Movie.
	 * @param rating The rating.
	 * @param note The added note.
	 * @param releaseDate The release date
	 */
	public Movie(String title, Calendar releaseDate, int rating, String note) {
		this.title = title;
		this.rating = rating;
		this.note = note;
		this.releaseDate = releaseDate;
		
		this.apiID = -1;
		this.posters = new HashMap<PosterSize, String>();
		
		// Set the time of day to 00.00.00 to allow for easier testing
		this.releaseDate.set(releaseDate.get(Calendar.YEAR),
				releaseDate.get(Calendar.MONTH),
				releaseDate.get(Calendar.DAY_OF_MONTH),
				0, 0, 0);
		
		tags = new LinkedList<Tag>();
	}
	
	/**
	 * Adds a tag to the list with tags.
	 * 
	 * @param tag The tag you want to add.
	 */
	public void addTag(Tag tag) {
		tags.add(tag);
	}
	
	/**
	 * Appends a list of tags to the existing list of tags
	 * 
	 * @param tags The list of tags to be added
	 */
	public void addTags(List<Tag> tags){
		this.tags.addAll(tags);
	}
	
	/**
	 * Removes a tag from the list with tags.
	 * 
	 * @param tag The tag you want to remove.
	 * @return true if the removal went through.
	 */
	public boolean removeTag(Tag tag) {
		return tags.remove(tag);
	}
	
	/**
	 * @return A list of tags connected to the Movie.
	 */
	public List<Tag> getTags() {
		return tags;
	}
	
	/**
	 * @return the ID of the Movie.
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Sets the id of the Movie.
	 * @param id the Id you want to set.
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * @return The title of the Movie.
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @return The note of the Movie.
	 */
	public String getNote() {
		return note;
	}
	
	/**
	 * Set the note of the Movie to the given parameter.
	 * @param note The new note.
	 */
	public void setNote(String note) {
		this.note = note;
	}
	
	/**
	 * @return The rating of the Movie.
	 */
	public int getRating() {
		return rating;
	}
	
	/**
	 * Change the rating of the Movie.
	 * @param rating The new rating.
	 */
	public void setRating(int rating) {
		this.rating = rating;
	}
	
	/**
	 * Returns the movie's release date represented by a Calendar object
	 * @return The movie�s release date
	 */
	public Calendar getDate() {
		return this.releaseDate;
	}
	
	/**
	 * Sets the movie's release date represented by a Calendar object
	 * 
	 * @param releaseDate The movie's release date
	 */
	public void setDate(Calendar releaseDate) {
		this.releaseDate = releaseDate;
	}
	
	/**
	 * Get the API id of this movie.
	 * 
	 * @return The IMDB id
	 */
	public int getApiID() {
		return this.apiID;
	}
	
	/**
	 * Set the IMDb id.
	 * 
	 * @param id The IMDb id
	 */
	public void setApiID(int id) {
		this.apiID = id;
	}
	
	/**
	 * Checks whether the IMDb API id is set. 
	 * 
	 * @return True if this Movie has an API id,
	 * otherwise false.
	 */
	public boolean hasApiIDSet() {
		return this.apiID != NO_API_ID;
	}
	
	/**
	 * Set a poster URL for a certain size.
	 * 
	 * @param url The URL to the image
	 * @param size The size
	 */
	public void setPosterURL(String url, PosterSize size) {
		this.posters.put(size, url);
	}
	
	/**
	 * Get the poster URL for a certain size
	 * 
	 * @param size The size. See {@link PosterSize}.
	 * @return The URL to the poster. Null if the size doesn't
	 * exist
	 */
	public String getPosterURL(PosterSize size) {
		return this.posters.get(size);
	}
	
	/**
	 * Get all poster URLs.
	 * 
	 * @return The URLs in a map for all sizes
	 */
	public Map<PosterSize, String> getPosterURLs() {
		return this.posters;
	}
	
	/**
	 * Set all posters to this Movie.
	 * 
	 * @param posters A map of posters with sizes and URLs
	 */
	public void setPosters(Map<PosterSize, String> posters) {
		this.posters.clear();
		this.posters.putAll(posters);
	}
	
	@Override
	public String toString() {
		return title;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		} else if(o == null) {
			return false;
		} else if(this.getClass() != o.getClass()) {
			return false;
		} else {
			Movie tmp = (Movie)o;
			return this.title == tmp.title;
		}
	}
}
