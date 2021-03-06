package com.zohar.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CrimeListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE = "saved_subtitle_visible";

    private List<Crime> mCrimes;
    private CrimeAdapter mCrimeAdapter;
    private RecyclerView mCrimeRecyclerView;

    private final int REQUEST_CRIME = 1;
    private Crime mCrime;
    private boolean mSubtitleVisiable; //子标题是否显示

    private Callback mCallback;

    interface Callback {
        public void onCrimeSelect(Crime crime);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mCrimeRecyclerView.setLayoutManager(layoutManager);


        if (savedInstanceState != null) {
            mSubtitleVisiable = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();

        ItmeCallback itemCallback = new ItmeCallback(mCrimeAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemCallback);
        itemTouchHelper.attachToRecyclerView(mCrimeRecyclerView);

        return view;

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisiable);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (Callback) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    public void updateUI() {
        CrimeLab crimeLab = CrimeLab.getInstance(getContext());
        mCrimes = crimeLab.getCrimes();

        if (mCrimeAdapter == null) {
            mCrimeAdapter = new CrimeAdapter(mCrimes);
            mCrimeRecyclerView.setAdapter(mCrimeAdapter);
        } else {
            mCrimeAdapter.setCrimes(mCrimes);
            mCrimeAdapter.notifyDataSetChanged();
        }



        updateSubtitle();
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mCrimeTitleTextView;
        TextView mCrimeDate;
        ImageView mSolvedImageView;
        View view;

        private Crime mCrime;

        public CrimeHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            mCrimeDate = itemView.findViewById(R.id.crime_date);
            mCrimeTitleTextView = itemView.findViewById(R.id.crime_tile);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved);

            itemView.setOnClickListener(this);
        }

        private void bind(Crime crime) {
            mCrime = crime;
            mCrimeTitleTextView.setText(crime.getTitle());
            Date date = crime.getDate();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd E HH:mm:ss", Locale.CHINA);
            mCrimeDate.setText(format.format(date));
            mSolvedImageView.setVisibility(mCrime.isSolved() ? View.VISIBLE : View.GONE);
        }


        @Override
        public void onClick(View v) {
            mCallback.onCrimeSelect(mCrime);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CRIME) {

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subTitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisiable) {
            subTitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subTitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.getInstance(getContext()).addCrime(crime);
                updateUI();
                mCallback.onCrimeSelect(crime);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisiable = !mSubtitleVisiable;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.getInstance(getContext());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subttitle_plurals, crimeCount, crimeCount);

        if (!mSubtitleVisiable) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> implements IOperationData {

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            //getItemViewType(i)
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_list_crime, viewGroup, false);
            CrimeHolder holder = new CrimeHolder(view);

            return holder;
        }

        @Override
        public void onBindViewHolder(CrimeHolder viewHolder, int i) {
            mCrime = mCrimes.get(i);
            viewHolder.bind(mCrime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public void onItemMove(int fromPosition, int toPosition) {

        }

        @Override
        public void onItemRemove(int position) {
            mCrimes.remove(position);
            // 数据库删除当前的item
            CrimeLab.getInstance(getContext()).deleteCrime(mCrime);
            notifyItemRemoved(position);
        }
    }

    /**
     * item 侧滑
     */
    class ItmeCallback extends ItemTouchHelper.Callback {

        private CrimeAdapter mCrimeAdapter;

        public ItmeCallback(CrimeAdapter adapter) {
            mCrimeAdapter = adapter;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            //设置移动标志，说明可以从什么位置可以滑动
            return ItemTouchHelper.LEFT;
        }

        @Override
        public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            mCrimeAdapter.onItemRemove(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                //滑动时改变Item的透明度
                final float alpha = 1 - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                viewHolder.itemView.setAlpha(alpha);
            }

        }
    }
}
