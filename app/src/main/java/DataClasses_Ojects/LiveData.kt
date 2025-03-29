package DataClasses_Ojects

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Runnable


class LiveData<T>: ViewModel{

    private var _value:MutableLiveData<T> = MutableLiveData<T>()

    val value: LiveData<T>
        get() {
            return _value
        }

    constructor(initialValue:T):super(){
        _value.value=initialValue
    }
    val handler = Handler(Looper.getMainLooper())

   public fun Update(newValue:T){
        _value.value=newValue
    }


}