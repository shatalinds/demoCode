...........................
    class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rlItem) RelativeLayout rlItem;
        @BindView(R.id.ivItemIcon) ImageView ivItemIcon;
        @BindView(R.id.tvItemText) TextView tvItemText;
        @BindView(R.id.rbItemSelect) RadioButton rbItemSelect;
        @BindView(R.id.llLine) LinearLayout llLine;

        public ItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mRadioList.add(rbItemSelect);
        }
    }
...........................