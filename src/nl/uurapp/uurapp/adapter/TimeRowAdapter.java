package nl.uurapp.uurapp.adapter;

import java.util.List;

import nl.uurapp.uurapp.R;
import nl.uurapp.uurapp.ApplicationEx.TimeRow;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TimeRowAdapter extends ArrayAdapter<TimeRow> {

	
    private Context context; 
    private int layoutResourceId;    
    private List<TimeRow> listTimeRow;	
    private static final int evenColor = Color.parseColor ("#F5F5F5");
    private static final int oddColor = Color.parseColor ("White");
    
	public TimeRowAdapter(Context context, int layoutResourceId, List<TimeRow> listTimeRow) {
		super(context, layoutResourceId, listTimeRow);

        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.listTimeRow = listTimeRow;
	}
	
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ListRowHolder holder = null;
        
        if(v == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            v = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new ListRowHolder();
            holder.tvStart = (TextView)v.findViewById(R.id.rowStart);
            holder.tvEnd = (TextView)v.findViewById(R.id.rowEnd);
            holder.tvOrganizationAbbrv = (TextView)v.findViewById(R.id.rowOrganizationAbbrv);
            holder.tvOrganization = (TextView)v.findViewById(R.id.rowOrganization);
            holder.tvDescription = (TextView)v.findViewById(R.id.rowDescription);
            holder.tvActivityAbbrv = (TextView)v.findViewById(R.id.rowActivityAbbrv);
            holder.tvActivity = (TextView)v.findViewById(R.id.rowActivity);
            holder.tvProjectAbbrv = (TextView)v.findViewById(R.id.rowProjectAbbrv);
            holder.tvProject = (TextView)v.findViewById(R.id.rowProject);
            v.setTag(holder);
        }
        else
        {
            holder = (ListRowHolder)v.getTag();
        }
        
        TimeRow timeRow = listTimeRow.get(position);
        if (timeRow.getStartTime().equals("")) {
        		holder.tvStart.setText(timeRow.getHours());
        		holder.tvEnd.setText("uur");
        }
        else {
        	holder.tvStart.setText(timeRow.getStartTime());
        	holder.tvEnd.setText(timeRow.getEndTime());
		}
        holder.tvOrganizationAbbrv.setText(timeRow.getOrganizationAbbrv());
        holder.tvOrganization.setText(timeRow.getOrganizationName()!=null?"-":timeRow.getOrganizationName());
        holder.tvDescription.setText(timeRow.getDescription());
        if (!timeRow.getDescription().equals(""))
        	holder.tvDescription.setVisibility(View.VISIBLE);
        	else holder.tvDescription.setVisibility(View.GONE);
        holder.tvActivityAbbrv.setText(timeRow.getActivityAbbrv());
        holder.tvActivity.setText(timeRow.getActivityDescription());
        holder.tvProjectAbbrv.setText(timeRow.getProjectAbbrv());
        if (timeRow.getProjectPhaseName().length() > 0)
        {
        	holder.tvProject.setText(timeRow.getProjectName() + "/" + timeRow.getProjectPhaseName());
        }
        else
        {
        	holder.tvProject.setText("-");
        }
        holder.timeRowID = timeRow.getId();
        holder.declared = timeRow.getDeclared();
        
        //set backgroud color
        if (position % 2 == 0) { // we're on an even row
            v.setBackgroundColor(evenColor);
         } else {
            v.setBackgroundColor(oddColor);
         }    
        return v;
    }
	
	public static class ListRowHolder
    {
        public TextView tvStart;
        public TextView tvEnd;
        public TextView tvOrganizationAbbrv;
        public TextView tvOrganization;
        public TextView tvDescription;
        public TextView tvActivityAbbrv;
        public TextView tvActivity;
        public TextView tvProjectAbbrv;
        public TextView tvProject;
        public String timeRowID;
        public Boolean declared;
    }

	@Override
	public int getCount() {
	    return listTimeRow.size();
	}

}
