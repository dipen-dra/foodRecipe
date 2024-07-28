package com.example.easyfood.mvvm

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.easyfood.data.db.MealsDatabase
import com.example.easyfood.data.db.Repository
import com.example.easyfood.data.pojo.MealDB
import com.example.easyfood.data.pojo.MealDetail
import com.example.easyfood.data.pojo.RandomMealResponse
import com.example.easyfood.data.retrofit.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailsMVVM(application: Application) : AndroidViewModel(application) {
    private val mutableMealDetail = MutableLiveData<List<MealDetail>>()
    private val mutableMealBottomSheet = MutableLiveData<List<MealDetail>>()
    private val repository: Repository
    private val firebaseDatabase = FirebaseDatabase.getInstance().getReference("favorite_meals")

    init {
        val mealDao = MealsDatabase.getInstance(application).dao()
        repository = Repository(mealDao)
    }

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun insertMeal(meal: MealDB) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertFavoriteMeal(meal)
            // Save meal to Firebase with user ID
            firebaseDatabase.child(currentUserId).child(meal.mealId.toString()).setValue(meal)
        }
    }

    fun deleteMeal(meal: MealDB) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteMeal(meal)
        // Remove meal from Firebase with user ID
        firebaseDatabase.child(currentUserId).child(meal.mealId.toString()).removeValue()
    }

    fun getMealById(id: String) {
        RetrofitInstance.foodApi.getMealById(id).enqueue(object : Callback<RandomMealResponse> {
            override fun onResponse(call: Call<RandomMealResponse>, response: Response<RandomMealResponse>) {
                val meals = response.body()?.meals
                mutableMealDetail.value = meals ?: emptyList()
            }

            override fun onFailure(call: Call<RandomMealResponse>, t: Throwable) {
                Log.e(TAG, t.message.toString())
            }
        })
    }

    fun isMealSavedInDatabase(mealId: String): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
            val meal = repository.getMealById(mealId)
            result.postValue(meal != null)
        }
        return result
    }

    fun deleteMealById(mealId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMealById(mealId)
            // Remove meal from Firebase with user ID
            firebaseDatabase.child(currentUserId).child(mealId).removeValue()
        }
    }

    fun getMealByIdBottomSheet(id: String) {
        RetrofitInstance.foodApi.getMealById(id).enqueue(object : Callback<RandomMealResponse> {
            override fun onResponse(call: Call<RandomMealResponse>, response: Response<RandomMealResponse>) {
                val meals = response.body()?.meals
                mutableMealBottomSheet.value = meals ?: emptyList()
            }

            override fun onFailure(call: Call<RandomMealResponse>, t: Throwable) {
                Log.e(TAG, t.message.toString())
            }
        })
    }

    fun observeMealDetail(): LiveData<List<MealDetail>> = mutableMealDetail

    fun observeMealBottomSheet(): LiveData<List<MealDetail>> = mutableMealBottomSheet

    fun observeUserMeals(): LiveData<List<MealDB>> {
        val result = MutableLiveData<List<MealDB>>()
        firebaseDatabase.child(currentUserId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val meals = snapshot.children.mapNotNull { it.getValue(MealDB::class.java) }
                result.postValue(meals)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.message)
            }
        })
        return result
    }

    companion object {
        private const val TAG = "DetailsMVVM"
    }
}
