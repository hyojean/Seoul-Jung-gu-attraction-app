package ddwu.mobile.finalproject.ma01_20181801;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlaceDAO {

    @Query("SELECT * FROM place_table")
    List<Place> getAllPlaces();

    @Query("SELECT * FROM place_table WHERE _id = :id")
    Place getPlaceById(int id);

    @Query("SELECT * FROM place_table WHERE title = :title")
    Place getPlaceByTitle(String title);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPlace(Place place);

}

