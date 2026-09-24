package com.example.agrofastsolutions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MarketPriceAdapter extends RecyclerView.Adapter<MarketPriceAdapter.ViewHolder> {

    private final List<MarketPrice> items = new ArrayList<>();

    /**
     * Resolved from the host Activity's theme at construction time.
     * - Light mode: white (from ?attr/colorOnPrimary on green card)
     * - Dark mode: black (from ?attr/colorOnPrimary on yellowish-green card)
     *
     * Direction is conveyed by the arrow glyph (▲ / ▼ / —), not color.
     */
    private final int onPrimaryColor;

    public MarketPriceAdapter(List<MarketPrice> initial, int onPrimaryColor) {
        if (initial != null) items.addAll(initial);
        this.onPrimaryColor = onPrimaryColor;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_market_price, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        MarketPrice item = items.get(position);

        h.tvCropName.setText(item.getCropName() != null ? item.getCropName() : "—");
        h.tvRegion.setText(String.format(Locale.getDefault(), "📍 %s",
                item.getRegion() != null ? item.getRegion() : "—"));

        if (item.getUnit() != null && !item.getUnit().isEmpty()) {
            h.tvUnit.setText(item.getUnit());
            h.tvUnit.setVisibility(View.VISIBLE);
        } else {
            h.tvUnit.setVisibility(View.GONE);
        }

        if (item.getPrice() != null) {
            h.tvPrice.setText(String.format(Locale.getDefault(),
                    "TZS %,.0f", item.getPrice()));
            h.tvPrice.setVisibility(View.VISIBLE);
        } else {
            h.tvPrice.setVisibility(View.GONE);
        }

        double low  = item.getPriceLow()  != null ? item.getPriceLow()  : 0;
        double high = item.getPriceHigh() != null ? item.getPriceHigh() : 0;
        h.tvPriceRange.setText(String.format(Locale.getDefault(),
                "Range: TZS %,.0f – %,.0f", low, high));

        bindChange(h.tvWeeklyChange,  item.getWeeklyChange(),  "Weekly");
        bindChange(h.tvMonthlyChange, item.getMonthlyChange(), "Monthly");

        String date = item.getPublishedDate();
        h.tvDate.setText("Updated: " +
                (date != null && !date.isEmpty() ? formatDate(date) : "—"));
    }

    /**
     * Both up and down use onPrimaryColor so text is always readable on the
     * card. Direction is signaled by the arrow character only.
     */
    private void bindChange(TextView view, Double change, String label) {
        String arrow;
        if (change == null) {
            arrow = "—";
        } else if (change > 0) {
            arrow = "▲";
        } else if (change < 0) {
            arrow = "▼";
        } else {
            arrow = "—";
        }

        double magnitude = (change != null) ? Math.abs(change) : 0;

        String text = String.format(Locale.getDefault(),
                "%s: %s %.1f%%", label, arrow, magnitude);

        view.setText(text);
        view.setTextColor(onPrimaryColor);
    }

    private String formatDate(String iso) {
        if (iso == null || iso.isEmpty()) return "—";
        try {
            String ymd = iso.length() >= 10 ? iso.substring(0, 10) : iso;
            java.text.SimpleDateFormat in  = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("MMM d, yyyy", Locale.US);
            java.util.Date d = in.parse(ymd);
            return d != null ? out.format(d) : ymd;
        } catch (Exception e) {
            return iso;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setData(List<MarketPrice> newList) {
        items.clear();
        if (newList != null) items.addAll(newList);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCropName, tvRegion, tvUnit, tvPrice, tvPriceRange,
                tvWeeklyChange, tvMonthlyChange, tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCropName     = itemView.findViewById(R.id.tvCropName);
            tvRegion       = itemView.findViewById(R.id.tvRegion);
            tvUnit         = itemView.findViewById(R.id.tvUnit);
            tvPrice        = itemView.findViewById(R.id.tvPrice);
            tvPriceRange   = itemView.findViewById(R.id.tvPriceRange);
            tvWeeklyChange = itemView.findViewById(R.id.tvWeeklyChange);
            tvMonthlyChange= itemView.findViewById(R.id.tvMonthlyChange);
            tvDate         = itemView.findViewById(R.id.tvDate);
        }
    }
}