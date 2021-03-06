/**
 *	WatchMeContentProvider.java
 *
 * The Content Provider for the WatchMe application.
 *
 *  @author lisastenberg
 *	@copyright (c) 2012 Johan Brook, Robin Andersson, Lisa Stenberg, Mattias Henriksson
 *	@license MIT
 */

package se.chalmers.watchme.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class WatchMeContentProvider extends ContentProvider {

	private DatabaseHelper db;

	/**
	 * The authority of the Content Provider
	 */
	public static final String AUTHORITY = "se.chalmers.watchme.database."
			+ "providers.WatchMeContentProvider";

	private static final String BASE_PATH_MOVIES = "movies";
	private static final String BASE_PATH_TAGS = "tags";
	private static final String BASE_PATH_HAS_TAG = "hastag";

	private static final int MOVIES = 10;
	private static final int MOVIES_ID = 20;
	private static final int TAGS = 30;
	private static final int TAGS_ID = 40;
	private static final int HAS_TAG = 50;

	/**
	 * The Uri that affects the Movie table.
	 */
	public static final Uri CONTENT_URI_MOVIES = Uri.parse("content://"
			+ AUTHORITY + "/" + BASE_PATH_MOVIES);
	/**
	 * The Uri that affects the Tags table.
	 */
	public static final Uri CONTENT_URI_TAGS = Uri.parse("content://"
			+ AUTHORITY + "/" + BASE_PATH_TAGS);
	/**
	 * The Uri that affects the HasTag table.
	 */
	public static final Uri CONTENT_URI_HAS_TAG = Uri.parse("content://"
			+ AUTHORITY + "/" + BASE_PATH_HAS_TAG);

	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/watchme";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/watchme";

	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sUriMatcher.addURI(AUTHORITY, BASE_PATH_MOVIES, MOVIES);
		sUriMatcher.addURI(AUTHORITY, BASE_PATH_MOVIES + "/#", MOVIES_ID);
		sUriMatcher.addURI(AUTHORITY, BASE_PATH_TAGS, TAGS);
		sUriMatcher.addURI(AUTHORITY, BASE_PATH_TAGS + "/#", TAGS_ID);
		sUriMatcher.addURI(AUTHORITY, BASE_PATH_HAS_TAG, HAS_TAG);
	};

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase sqlDB = db.getWritableDatabase();

		int deletedRows;
		switch (sUriMatcher.match(uri)) {
		case MOVIES:
			/*
			 * selection is supposed to contain "_id = <movieId>" therefore
			 * movieSel is supposed to contain: " = <movieId>"
			 */
			String movieSel = selection.split(MoviesTable.COLUMN_MOVIE_ID)[1];
			
			// Query for all tags that is attached to the movie.
			Cursor movieCursor = sqlDB.query(HasTagTable.TABLE_HAS_TAG, null,
					HasTagTable.COLUMN_MOVIE_ID + movieSel, null, null, null,
					null);
			movieCursor.getCount();

			// Delete the movie
			deletedRows = sqlDB.delete(MoviesTable.TABLE_MOVIES, selection,
					selectionArgs);

			/*
			 * If the movie had any attached tag: detach them
			 */
			while (movieCursor.moveToNext()) {
				String tagSel = " = " + movieCursor.getString(1);

				Cursor tagCursor = sqlDB.query(HasTagTable.TABLE_HAS_TAG, null,
						HasTagTable.COLUMN_TAG_ID + tagSel, null, null, null,
						null);

				if (!tagCursor.moveToFirst()) {
					// If the tag isn't connected to any Movie, delete it.
					sqlDB.delete(TagsTable.TABLE_TAGS, TagsTable.COLUMN_TAG_ID
							+ tagSel, null);
				}
				tagCursor.close();
			}
			movieCursor.close();

			break;
		case MOVIES_ID:
			selection = selection + MoviesTable.COLUMN_MOVIE_ID + " = " 
			+ uri.getLastPathSegment();
			deletedRows = sqlDB.delete(MoviesTable.TABLE_MOVIES, selection, 
					selectionArgs);
			break;
		case TAGS:
			deletedRows = sqlDB.delete(TagsTable.TABLE_TAGS, selection,
					selectionArgs);
			break;
		case TAGS_ID:
			selection = selection + TagsTable.COLUMN_TAG_ID + " = " + 
			uri.getLastPathSegment();
			deletedRows = sqlDB.delete(TagsTable.TABLE_TAGS, selection, 
					selectionArgs);
			break;
		case HAS_TAG:
			// In practice this is the same as detaching a tag from a movie.
			deletedRows = sqlDB.delete(HasTagTable.TABLE_HAS_TAG, selection,
					selectionArgs);
			/*
			 * selection is supposed to contain 
			 * "movieid = <movieid> AND tagid = <tagid>" therefore
			 * 
			 * tagSelection[0] is supposed to contain "movieid = <movieid> AND"
			 * 
			 * tagSelection[1] is supposed to contain: " = <tagId>"
			 */
			String tagSelection = selection.split(HasTagTable.COLUMN_TAG_ID)[1];

			/*
			 * Query for all movies attached to the tag.
			 */
			Cursor tagCursor = sqlDB.query(HasTagTable.TABLE_HAS_TAG, null,
					HasTagTable.COLUMN_TAG_ID + tagSelection, null, null, null,
					null);

			// If the tag isn't connected to any Movie, delete it.
			if (!tagCursor.moveToFirst()) {
				sqlDB.delete(TagsTable.TABLE_TAGS, TagsTable.COLUMN_TAG_ID
						+ tagSelection, null);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);

		}

		// Notify Observers
		getContext().getContentResolver()
				.notifyChange(CONTENT_URI_MOVIES, null);
		getContext().getContentResolver().notifyChange(CONTENT_URI_TAGS, null);
		getContext().getContentResolver().notifyChange(CONTENT_URI_HAS_TAG,
				null);
		return deletedRows;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase sqlDB = db.getWritableDatabase();

		long id = 0;
		switch (sUriMatcher.match(uri)) {
		case MOVIES:

			String movieTitle = values.getAsString(MoviesTable.COLUMN_TITLE);
			Cursor movieCursor = sqlDB.query(MoviesTable.TABLE_MOVIES, null,
					MoviesTable.COLUMN_TITLE + " = \"" + movieTitle + "\"",
					null, null, null, null);

			// If the Movie doesn't exist, create it.
			if (movieCursor.getCount() == 0) {
				id = sqlDB.insert(MoviesTable.TABLE_MOVIES, null, values);
			}

			break;
		case HAS_TAG:
			/* Check if the Tag exists. If it doesn't exist 
			 * insert into database
			 */
			String tagName = values.getAsString(TagsTable.COLUMN_NAME);
			Cursor tagCursor = sqlDB.query(TagsTable.TABLE_TAGS, null,
					TagsTable.COLUMN_NAME + " = \"" + tagName + "\"", null,
					null, null, null);

			if (tagCursor.moveToFirst()) {
				// If the Tag already exist. Get the Id.
				id = Long.parseLong(tagCursor.getString(0));

				/*
				 * Check if the tag is already attached to the movie. Return 0
				 * as id.
				 */
				Cursor cursor = sqlDB.query(
						HasTagTable.TABLE_HAS_TAG, null,
						HasTagTable.COLUMN_MOVIE_ID + " = "
								+ values.getAsLong(MoviesTable.COLUMN_MOVIE_ID)
								+ " AND " + HasTagTable.COLUMN_TAG_ID + " = "
								+ id, null, null, null, null);
				if (cursor.moveToFirst()) {
					id = 0;
					break;
				}
			} else {
				ContentValues tagValues = new ContentValues();
				tagValues.put(TagsTable.COLUMN_NAME, tagName);

		        id = sqlDB.insert(TagsTable.TABLE_TAGS, null, tagValues); 
			}

			tagCursor.close();

			String sql = "INSERT INTO " + HasTagTable.TABLE_HAS_TAG
					+ " VALUES("
					+ values.getAsLong(MoviesTable.COLUMN_MOVIE_ID) + ", " + id
					+ ")";
			sqlDB.execSQL(sql);

			break;
		case TAGS:
			throw new UnsupportedOperationException("A tag can't exist "
					+ "without being attached to a movie");
		default:
			throw new IllegalArgumentException("Unknown URI" + uri);
		}

		// Notify Observers
		getContext().getContentResolver()
				.notifyChange(CONTENT_URI_MOVIES, null);
		getContext().getContentResolver().notifyChange(CONTENT_URI_TAGS, null);
		getContext().getContentResolver().notifyChange(CONTENT_URI_HAS_TAG,
				null);
		return Uri.parse(BASE_PATH_MOVIES + "/" + id);
	}

	@Override
	public boolean onCreate() {
		db = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase sqlDB = db.getReadableDatabase();
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		switch (sUriMatcher.match(uri)) {
		case MOVIES:
			queryBuilder.setTables(MoviesTable.TABLE_MOVIES);
			break;
		case MOVIES_ID:
			selection = selection + MoviesTable.COLUMN_MOVIE_ID + " = "
					+ uri.getLastPathSegment();
			queryBuilder.setTables(MoviesTable.TABLE_MOVIES);
			break;
		case TAGS:
			queryBuilder.setTables(TagsTable.TABLE_TAGS);
			break;
		case TAGS_ID:
			selection = selection + TagsTable.COLUMN_TAG_ID + " = "
					+ uri.getLastPathSegment();
			queryBuilder.setTables(TagsTable.TABLE_TAGS);
			break;
		case HAS_TAG:
			/*
			 * Join the tables Movies, HasTag and Tags to be able to query
			 * for any attribute in any table.
			 */
			String tables = MoviesTable.TABLE_MOVIES + " LEFT OUTER JOIN "
					+ HasTagTable.TABLE_HAS_TAG + " ON "
					+ MoviesTable.TABLE_MOVIES + "."
					+ MoviesTable.COLUMN_MOVIE_ID + " = "
					+ HasTagTable.TABLE_HAS_TAG + "."
					+ HasTagTable.COLUMN_MOVIE_ID + " LEFT OUTER JOIN "
					+ TagsTable.TABLE_TAGS + " ON " + HasTagTable.TABLE_HAS_TAG
					+ "." + HasTagTable.COLUMN_TAG_ID + " = "
					+ TagsTable.TABLE_TAGS + "." + TagsTable.COLUMN_TAG_ID;

			queryBuilder.setTables(tables);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		Cursor cursor = queryBuilder.query(sqlDB, projection, selection,
				selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase sqlDB = db.getWritableDatabase();

		int updatedRows;
		switch (sUriMatcher.match(uri)) {
		case MOVIES:
			// Nothing need to be added to selection
			updatedRows = sqlDB.update(MoviesTable.TABLE_MOVIES, values,
					selection, selectionArgs);
			break;
		case MOVIES_ID:
			selection = selection + MoviesTable.COLUMN_MOVIE_ID + " = "
					+ uri.getLastPathSegment();
			updatedRows = sqlDB.update(MoviesTable.TABLE_MOVIES, values,
					selection, selectionArgs);
			break;
		case TAGS:
			// Nothing need to be added to selection
			updatedRows = sqlDB.update(TagsTable.TABLE_TAGS, values, selection,
					selectionArgs);
			break;
		case TAGS_ID:
			selection = selection + TagsTable.COLUMN_TAG_ID + " = "
					+ uri.getLastPathSegment();
			updatedRows = sqlDB.update(TagsTable.TABLE_TAGS, values, selection,
					selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Notify Observers
		getContext().getContentResolver()
				.notifyChange(CONTENT_URI_MOVIES, null);
		getContext().getContentResolver().notifyChange(CONTENT_URI_TAGS, null);
		getContext().getContentResolver().notifyChange(CONTENT_URI_HAS_TAG,
				null);
		return updatedRows;
	}

}
