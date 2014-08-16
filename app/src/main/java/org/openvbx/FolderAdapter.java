package org.openvbx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FolderAdapter extends ArrayAdapter<Folder> {
	public ArrayList<Folder> folders;
	private int ViewResourceId;
	private LayoutInflater inflater;

	public FolderAdapter(Context context, int textViewResourceId, ArrayList<Folder> folders) {
		super(context, textViewResourceId, folders);
		this.folders = folders;
		this.ViewResourceId = textViewResourceId;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
      View view = convertView;
      if (view == null) {
         view = inflater.inflate(ViewResourceId, null);
      }
      Folder folder = folders.get(position);
      if (folder != null) {
         if ("inbox".equals(folder.getType())){
            ((ImageView) view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_action_inbox);
         }

         ((TextView) view.findViewById(R.id.name)).setText(folder.getName());
         TextView msgCount = (TextView) view.findViewById(R.id.msgCount);
         StringBuilder bld = new StringBuilder("Total:");
         bld.append(folder.getCount());
         bld.append(" - New:");
         bld.append(folder.getUnread());
         msgCount.setText(bld.toString());
         if (folder.getUnread() < 1 && folder.getCount() < 1) {
            msgCount.setVisibility(View.GONE);
         }

      }
      return view;
   }

	@Override
	public int getCount() {
		return folders.size();
	}

	public Folder get(int position){
		return folders.get(position);
	}
}
