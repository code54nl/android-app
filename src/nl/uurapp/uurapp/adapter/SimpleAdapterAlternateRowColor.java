package nl.uurapp.uurapp.adapter;


import java.util.HashMap;
import java.util.List;
 
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;


public class SimpleAdapterAlternateRowColor extends SimpleAdapter {
	
    private static final int evenColor = Color.parseColor ("#F5F5F5");
    private static final int oddColor = Color.parseColor ("White");
    
    
    public SimpleAdapterAlternateRowColor(Context context, 
    		List<HashMap<String, String>> items, int resource, String[] from, int[] to) {
        super(context, items, resource, from, to);
    }
    
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        if (position % 2 == 0) { // we're on an even row
          view.setBackgroundColor(evenColor);
        } else {
           view.setBackgroundColor(oddColor);
        }
       return view;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = super.getView(position, convertView, parent);
      if (position % 2 == 0) { // we're on an even row
         view.setBackgroundColor(evenColor);
       } else {
         view.setBackgroundColor(oddColor);
       }      
      return view;
    }

}
