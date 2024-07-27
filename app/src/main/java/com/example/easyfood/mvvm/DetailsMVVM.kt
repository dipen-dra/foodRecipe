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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailsMVVM(application: Application) : AndroidViewModel(application) {
    private val mutableMealDetail = MutableLiveData<List<MealDetail>>()
    private val mutableMealBottomSheet = MutableLiveData<List<MealDetail>>()
    private val allMeals: LiveData<List<MealDB>>
    private val repository: Repository

    init {
        val mealDao = MealsDatabase.getInstance(application).dao()
        repository = Repository(mealDao)
        allMeals = repository.mealList
    }

    fun insertMeal(meal: MealDB) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertFavoriteMeal(meal)
        }
    }

    fun deleteMeal(meal: MealDB) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteMeal(meal)
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

    fun observeSaveMeal(): LiveData<List<MealDB>> = allMeals

    companion object {
        private const val TAG = "DetailsMVVM"
    }
}
