package com.sr.trackcrack.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sr.trackcrack.R;
import com.sr.trackcrack.databinding.FragmentHistoryBinding;
import com.sr.trackcrack.ui.map.MapFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HistoryFragment extends Fragment implements InspectionAdapter.OnItemClickListener {

    private FragmentHistoryBinding binding;
    private RecyclerView recyclerView;
    private InspectionAdapter adapter;
    private List<Inspection> inspectionList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HistoryViewModel historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.inspectionHistoryRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new InspectionAdapter(inspectionList, this); // Pass 'this' as the listener
        recyclerView.setAdapter(adapter);

        loadInspectionData();

        return root;
    }

    private void loadInspectionData() {
        inspectionList.add(new Inspection("12345", "2024-09-16", 3, false));
        inspectionList.add(new Inspection("12346", "2024-09-14", 5, true));
        inspectionList.add(new Inspection("12347", "2024-09-13", 2, false));
        inspectionList.add(new Inspection("12348", "2024-09-12", 4, true));
        inspectionList.add(new Inspection("12349", "2024-09-11", 1, false));
        inspectionList.add(new Inspection("12350", "2024-09-10", 3, true));
        inspectionList.add(new Inspection("12351", "2024-09-09", 5, false));
        inspectionList.add(new Inspection("12352", "2024-09-08", 2, true));
        inspectionList.add(new Inspection("12353", "2024-09-07", 4, false));
        inspectionList.add(new Inspection("12354", "2024-09-06", 1, true));

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(Inspection inspection) {
        // Get NavController from the host activity or fragment
        NavController navController = Navigation.findNavController(getActivity(),R.id.nav_host_fragment_content_main);


        // Set toolbar title to inspection ID

        // Create a bundle to pass data to MapFragment
        Bundle args = new Bundle();
        args.putString("inspection_date", inspection.getDate());;
        // Navigate to MapFragment with arguments
        navController.navigate(R.id.mapFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}