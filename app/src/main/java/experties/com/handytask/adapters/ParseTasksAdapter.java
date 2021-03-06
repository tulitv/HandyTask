package experties.com.handytask.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseFile;
import com.parse.ParseImageView;

import java.util.List;

import experties.com.handytask.R;
import experties.com.handytask.activities.ParseTaskListListener;
import experties.com.handytask.activities.ShowTasksActivity;
import experties.com.handytask.helpers.FragmentHelpers;
import experties.com.handytask.models.ParseTask;
import experties.com.handytask.models.TaskItem;

/**
 * Created by vincetulit on 3/8/15.
 */
public class ParseTasksAdapter extends ArrayAdapter<ParseTask> {

    public ParseTasksAdapter(Context context, List<ParseTask> items) {
        super(context, R.layout.parse_task, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        ParseTask parseTask = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.parse_task, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.ivMainTaskPhoto = (ParseImageView) convertView.findViewById(R.id.ivMainTaskPhoto);
            viewHolder.tvBriefDescription = (TextView) convertView.findViewById(R.id.tvBriefDescription);
            viewHolder.tvRelativeTime = (TextView) convertView.findViewById(R.id.tvRelativeTime);
            viewHolder.tvLocation = (TextView) convertView.findViewById(R.id.tvLocation);
            viewHolder.tvRelativeDistance = (TextView) convertView.findViewById(R.id.tvRelativeDistance);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tvBriefDescription.setText(parseTask.getTitle());
        viewHolder.tvRelativeTime.setText(FragmentHelpers.getRelativeTime(parseTask.getCreatedAt()));
        viewHolder.tvLocation.setText(parseTask.getCity() + "," + parseTask.getState());
        LatLng p = new LatLng(parseTask.getLatitude(), parseTask.getLongitude());
        String relativeDistance = String.format("%.1f", parseTask.getRelativeDistance());
        viewHolder.tvRelativeDistance.setText(relativeDistance + " miles");

        ParseFile file = parseTask.getPhoto1();
        if (file!=null) {
            viewHolder.ivMainTaskPhoto.setParseFile(file);
            viewHolder.ivMainTaskPhoto.loadInBackground();
        } else {
            viewHolder.ivMainTaskPhoto.setImageResource(R.drawable.no_image_avail);
        }

        return convertView;
    }

    private static class ViewHolder {
        public ParseImageView ivMainTaskPhoto;
        public TextView tvBriefDescription;
        public TextView tvRelativeTime;
        public TextView tvLocation;
        public TextView tvRelativeDistance;
    }
}
