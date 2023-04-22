package com.moonspace.thelastfive;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moonspace.thelastfive.helpers.DateHelper;
import com.moonspace.thelastfive.helpers.FormatHelper;
import com.moonspace.thelastfive.helpers.MathHelper;
import com.moonspace.thelastfive.models.MainModel;
import com.moonspace.thelastfive.models.WeightEntry;

class CustomHistoryListAdapter extends ArrayAdapter<WeightEntry> implements View.OnClickListener
{
    private final MainModel dataSet;
    private final Context context;
    private Double nextWeight = 0.0;

    CustomHistoryListAdapter(MainModel data, Context context)
    {
        super(context, R.layout.activity_history_item, data.getWeightEntryList());
        this.dataSet = data;
        this.context = context;
    }

    @Override
    public void onClick(View v)
    {
        // Do nothing... (maybe we'll add the ability to view details later?)
    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent)
    {
        double weightDelta;

        // Get the data item for this position
        WeightEntry dataModel = getItem(position);

        assert dataModel != null;

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder;

        if (convertView == null)
        {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.activity_history_item, parent, false);

            viewHolder.txtDate = convertView.findViewById(R.id.txtDate);
            viewHolder.txtWeight = convertView.findViewById(R.id.txtWeight);
            viewHolder.txtWeightDelta = convertView.findViewById(R.id.txtWeightDelta);
            viewHolder.txtNotes = convertView.findViewById(R.id.txtNotes);
            viewHolder.imgSelfie = convertView.findViewById(R.id.imgSelfie);
            viewHolder.btnDelete = convertView.findViewById(R.id.btnDelete);
            viewHolder.btnEdit = convertView.findViewById(R.id.btnEdit);

            viewHolder.btnDelete.setOnClickListener(v -> deleteItem((Long) v.getTag()));

            viewHolder.btnEdit.setOnClickListener(v -> editItem((Long) v.getTag()));

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();

            // Clear viewHolder values
            viewHolder.id = 0L;
            viewHolder.txtDate.setText(null);
            viewHolder.txtWeight.setText(null);
            viewHolder.txtWeightDelta.setText(null);
            viewHolder.txtNotes.setText(null);
            viewHolder.imgSelfie.setImageBitmap(null);
        }

        if (position < (dataSet.getWeightEntryList().size() - 1))
        {
            nextWeight = dataSet.getWeightEntryList().get(position + 1).getWeight();
        }

        weightDelta = dataModel.getWeight() - nextWeight;

        viewHolder.id = dataModel.getId();

        viewHolder.btnDelete.setTag(viewHolder.id);
        viewHolder.btnEdit.setTag(viewHolder.id);

        viewHolder.txtDate.setText(DateHelper.getFormattedDate(dataModel.getDate()));
        viewHolder.txtWeight.setText(FormatHelper.doubleToDefaultLocaleString(dataModel.getWeight()));

        if (weightDelta < 0)
        {
            viewHolder.txtWeightDelta.setText(String.format("%s%s", context.getString(R.string.weight_down_indicator), MathHelper.roundToNearestTenth(Math.abs(weightDelta))));
            viewHolder.txtWeightDelta.setTextColor(Color.parseColor("#04af70"));
            viewHolder.txtWeightDelta.setTypeface(null, Typeface.BOLD);
        }
        else if (weightDelta == 0)
        {
            viewHolder.txtWeightDelta.setText(" 0.0");
            viewHolder.txtWeightDelta.setTextColor(Color.LTGRAY);
        }
        else if (weightDelta > 0)
        {
            viewHolder.txtWeightDelta.setText(String.format("%s%s", context.getString(R.string.weight_up_indicator), MathHelper.roundToNearestTenth(weightDelta)));
            viewHolder.txtWeightDelta.setTextColor(Color.RED);
        }

        viewHolder.txtNotes.setText(dataModel.getNotes());

        // Display the image
        byte[] image = dataModel.getPhotoImage();

        if (image != null)
        {
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            viewHolder.imgSelfie.setImageBitmap(imageBitmap);
        }
        else
        {
            viewHolder.imgSelfie.setImageBitmap(null);
        }

        // Return the completed view to render on screen
        return convertView;
    }

    /**
     * Deletes the selected weight entry.
     *
     * @param id the ID of the weight entry to be deleted.
     */
    private void deleteItem(Long id)
    {
        // Let's prompt the user to make sure they actually want to delete this item
        new AlertDialog.Builder(context)
                .setTitle("Delete Item")
                .setMessage("Delete the selected item?")
                .setIcon(android.R.drawable.ic_delete)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {

                    WeightEntry weightEntry = dataSet.getWeightEntryById(id);

                    if (weightEntry != null)
                    {
                        weightEntry.setIsDeleted(true);

                        dataSet.saveModel();

                        notifyDataSetChanged();

                        Toast.makeText(context, "Item Deleted", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(android.R.string.no, null).show();
    }

    /**
     * Display the Activity for entering a new weight entry.
     *
     * @param id the ID of the weight entry to be modified.
     */
    private void editItem(Long id)
    {
        Intent intent = new Intent(context, NewEntryActivity.class);

        intent.putExtra("ENTRY_ID", id);

        // Needed since we're calling an Activity outside of an Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        this.getContext().startActivity(intent);
    }

    // View lookup cache
    private static class ViewHolder
    {
        Long id;
        TextView txtDate;
        TextView txtWeight;
        TextView txtWeightDelta;
        TextView txtNotes;
        ImageButton btnDelete;
        ImageButton btnEdit;
        ImageView imgSelfie;
    }
}
