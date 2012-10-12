package se.chalmers.watchme.ui;

import se.chalmers.watchme.R;
import se.chalmers.watchme.activity.MainActivity;
import se.chalmers.watchme.activity.MovieDetailsActivity;
import se.chalmers.watchme.activity.TagMovieListActivity;
import se.chalmers.watchme.database.DatabaseAdapter;
import se.chalmers.watchme.database.TagsTable;
import se.chalmers.watchme.database.WatchMeContentProvider;
import se.chalmers.watchme.model.Movie;
import se.chalmers.watchme.model.Tag;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class TagListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private SimpleCursorAdapter adapter;
	private DatabaseAdapter db;
	
	@Override
	public void onActivityCreated(Bundle b) {
		super.onActivityCreated(b);
		Thread.currentThread().setContextClassLoader(getActivity().getClassLoader());
		
		String[] from = new String[] { TagsTable.COLUMN_TAG_ID, TagsTable.COLUMN_NAME };
		int[] to = new int[] { android.R.id.text1 , android.R.id.text1 };
		
		getActivity().getSupportLoaderManager().initLoader(1, null, this);
		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1 , null, from, to, 0);
		setListAdapter(adapter);
		
		// Set up listener to delete a Tag.
		this.getListView().setOnItemLongClickListener(new OnDeleteListener());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.tag_list_fragment_view, container, false);
	}
	
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		
		
		String[] projection = { TagsTable.COLUMN_TAG_ID, TagsTable.COLUMN_NAME };
	    CursorLoader cursorLoader = new CursorLoader(getActivity(),
	    		WatchMeContentProvider.CONTENT_URI_TAGS, projection, null, null, null);
	    return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		
		adapter.swapCursor(arg1);		
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
		// data is not available anymore, delete reference
	    adapter.swapCursor(null);
		
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		/**
		 * When a tag is clicked, create a cursor pointing at movies containing
		 * that tag. Then send it to TagMovieListActivity using intent.putExtra()
		 */
		
		Intent intent = new Intent(getActivity(), TagMovieListActivity.class);
		intent.putExtra(MainActivity.TAG_ID, id);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
			
	}
	
	 /**
     * The listener for when the user does a long-tap on an item in the list.
     * 
     * The Movie object in the list is removed if the user confirms that he wants to remove the Movie.
     * 
     * @author Johan
     * @author lisastenberg
     */
    private class OnDeleteListener implements OnItemLongClickListener {
    	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			db = new DatabaseAdapter(getActivity().getContentResolver());
			
    		final Tag tag = db.getTag(id);
    		
            AlertDialog.Builder alertbox = new AlertDialog.Builder(getActivity());
            alertbox.setMessage("Are you sure you want to delete the tag \"" + tag.getName() + "\"?");           
            alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                	
                	db = new DatabaseAdapter(getActivity().getContentResolver());
                	db.removeTag(tag);
                	
                    Toast.makeText(getActivity().getApplicationContext(), "\"" + tag.getName() + "\" was deleted" , Toast.LENGTH_SHORT).show();
                }
            });
            alertbox.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    
                }
            });
            
            alertbox.show();
			return true;
		}    	
	}
}
