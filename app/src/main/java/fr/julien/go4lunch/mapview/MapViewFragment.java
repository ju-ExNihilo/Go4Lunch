package fr.julien.go4lunch.mapview;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import fr.julien.go4lunch.databinding.FragmentMapViewBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapViewFragment extends Fragment{

    private FragmentMapViewBinding binding;
    private NavController navController;

    public MapViewFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMapViewBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setUpNavigation();
    }

    public void setUpNavigation(){
        NavigationUI.setupWithNavController(binding.bottomNavigationMapView, navController);
    }

}
