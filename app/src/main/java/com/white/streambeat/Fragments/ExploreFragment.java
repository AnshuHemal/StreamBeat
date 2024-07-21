package com.white.streambeat.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.white.streambeat.Adapters.SearchAdapter;
import com.white.streambeat.Models.SharedViewModel;
import com.white.streambeat.R;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private RecyclerView recyclerViewSearchResults;
    private SearchAdapter searchAdapter;
    private List<Object> searchResults;
    private EditText searchView;
    private LinearLayout no_keyword_found;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        FragmentManager fragmentManager = getParentFragmentManager();
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        recyclerViewSearchResults = view.findViewById(R.id.recyclerViewSearchResults);
        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));

        no_keyword_found = view.findViewById(R.id.llNoKeywordFound);

        no_keyword_found.setVisibility(View.INVISIBLE);

        searchView = view.findViewById(R.id.searchView);
        searchResults = new ArrayList<>();
        searchAdapter = new SearchAdapter(getContext(), fragmentManager, searchResults);
        recyclerViewSearchResults.setAdapter(searchAdapter);


        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                search(query);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Observe search results from ViewModel
        sharedViewModel.getSearchResults().observe(getViewLifecycleOwner(), objects -> {
            if (objects != null) {
                searchAdapter.updateData(objects);
                Log.d("ExploreFragment", "Search results updated: " + searchResults.size() + " items");

                if (searchResults.isEmpty()) {
                    no_keyword_found.setVisibility(View.GONE);
                    recyclerViewSearchResults.setVisibility(View.GONE);
                } else {
                    recyclerViewSearchResults.setVisibility(View.VISIBLE);
                    no_keyword_found.setVisibility(View.GONE);
                }
            } else {
                no_keyword_found.setVisibility(View.GONE);
                recyclerViewSearchResults.setVisibility(View.INVISIBLE);
            }
        });

        return view;
    }

    private void search(String query) {
        if (query.isEmpty()) {
            sharedViewModel.clearSearchResults();
        } else {
            // Trigger search in sharedViewModel
            sharedViewModel.search(query);
            Log.d("ExploreFragment", "Performing search with query: " + query);
        }
    }

    public void updateCurrentlyPlayingPosition(int position) {
        if (searchAdapter != null) {
            searchAdapter.setCurrentlyPlayingPosition(position);
        }
    }
}