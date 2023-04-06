package u.scooters.attack.main

import android.util.Log
import java.util.concurrent.ConcurrentSkipListSet

object Statistics {
    var isUseAverageAsDefault = 0

    var isScooterLocked = false
    var isCruiseActive = false
    var isLightActive = false
    @JvmStatic
    var recoveryMode = 0 //0=weak,1=medium,2=strong
    private var maxPower = 0.0
    private var minPower = -0.0
    var recovered = 0.0 //Ampere hours
        private set
    var spent = 0.0 //Ampere hours
        private set

    //calculateEnergy();
    @JvmStatic
    var currentVoltage = 0.0
    private var currentAmpere = 0.0
    private var lastTimeStamp: Long = 0
    var currDiff = 0.0
        private set
    var requestsSent = 1
        private set
    var responseReceived = 1
        private set
    @JvmStatic
    var batteryLife = 0
    @JvmStatic
    var remainingCapacity = 7800 //Nennkapazität maH
    @JvmStatic
    var batteryTemperature = 0
    @JvmStatic
    var motorTemperature = 0.0
    @JvmStatic
    var distanceTravelled = 0.0001 //km
    var currentSpeed = 0.0 //km/h
        private set
    private var mampHoursPerKilometer = 0.0
    private var min_speed = 4
    private var default_efficiency = 600
    private var remainingRange = 0.0
    private val currentList = ConcurrentSkipListSet<Double>()
    private val speedList = ConcurrentSkipListSet<Double>()
    private val efficiencyList = ConcurrentSkipListSet<Double>()
    private val averageSpeedList = ConcurrentSkipListSet<Double>()
    fun getDefault_efficiency(): Int {
        return default_efficiency
    }

    fun setDefault_efficiency(new_default_efficiency: Int) {
        var new_default_efficiency = new_default_efficiency
        Log.d("stat", "default:$new_default_efficiency")
        if (new_default_efficiency == -1) {
            new_default_efficiency = averageEfficiency
            isUseAverageAsDefault = 1
        } else if (new_default_efficiency == -2) {
            new_default_efficiency = limitedAverageEfficiency
            isUseAverageAsDefault = 2
        } else {
            isUseAverageAsDefault = 0
        }
        default_efficiency = new_default_efficiency
    }

    fun getMin_speed(): Int {
        return min_speed
    }

    fun setMin_speed(min_speed: Int) {
        Log.d("stat", "min_speed:$min_speed")
        Statistics.min_speed = min_speed
    }

    private fun calculateEnergy() {
        val now = System.currentTimeMillis()
        var diff = (now - lastTimeStamp).toDouble()
        if (diff > 10000) {
            diff = 500.0
        }
        //currDiff = diff;
        diff /= 1000.0
        var power = averagedCurrent
        if (java.lang.Double.isNaN(power)) {
            power = getCurrentAmpere()
        }

        //Log.d("Stat","seconds:"+diff+" "+testTime+" power:"+power);
        if (power < 0) {
            recovered += power / 60 / 60 * diff
        } else if (power > 0) {
            spent += power / 60 / 60 * diff
        }
        //lastTimeStamp = now;
        if (power == 0.0) {
            power = getCurrentAmpere()
        }
        //Log.d("Stat", "calculateEnergy: "+power+ " speed: "+currentSpeed);
        if (currentSpeed >= min_speed) {
            mampHoursPerKilometer = power * 1000 / currentSpeed
            if (mampHoursPerKilometer < 0.01) {
                mampHoursPerKilometer = 0.01
            }
            remainingRange = remainingCapacity / mampHoursPerKilometer
            if (remainingRange < 0.01) {
                remainingRange = 0.0
            }
            efficiencyList.add(getMampHoursPerKilometer())
        } else {
            if (isUseAverageAsDefault == 1) {
                default_efficiency = averageEfficiency
            } else if (isUseAverageAsDefault == 2) {
                default_efficiency = limitedAverageEfficiency
            }
            mampHoursPerKilometer = default_efficiency.toDouble()
            remainingRange = remainingCapacity / mampHoursPerKilometer
        }
    }

    fun resetPowerStats() {
        maxPower = 0.0
        minPower = 0.0
        recovered = 0.0
        spent = 0.0
    }

    fun resetRequestStats() {
        requestsSent = 1
        responseReceived = 1
    }

    val power: Double
        get() = currentAmpere * currentVoltage
    val averagedPower: Double
        get() {
            var currentSum = 0.0
            for (d in currentList) {
                currentSum += d
            }
            var averageCurrent = currentSum / currentList.size
            if (java.lang.Double.isNaN(averageCurrent)) {
                averageCurrent = 0.0
            }
            return averageCurrent * currentVoltage
        }
    val averagedCurrent: Double
        get() {
            var currentSum = 0.0
            for (d in currentList) {
                currentSum += d
            }
            var averageCurrent = currentSum / currentList.size
            if (java.lang.Double.isNaN(averageCurrent)) {
                averageCurrent = 0.0
            }
            return averageCurrent
        }

    //just started app, no values yet
    val averageEfficiency: Int
        get() {
            if (efficiencyList.size == 0) {
                return 600 //just started app, no values yet
            }
            val sum = efficiencyList.stream().mapToDouble { obj: Double -> obj }
                .sum()
            val avgEff = sum / efficiencyList.size
            return avgEff.toInt()
        }

    //double sum =efficiencyList.stream().mapToDouble(Double::doubleValue).sum();
    //double avgEff = (sum / efficiencyList.size());
    val limitedAverageEfficiency: Int
        get() {
            if (efficiencyList.size == 0) {
                return 600
            }
            var currentSum = 0.0
            var i = 100
            val iterator: Iterator<*> = efficiencyList.descendingIterator()
            while (!iterator.hasNext() || i == 0) {
                currentSum += iterator.next() as Double
                i--
            }
            val averageCurrent = currentSum / (100 - i)

            //double sum =efficiencyList.stream().mapToDouble(Double::doubleValue).sum();
            //double avgEff = (sum / efficiencyList.size());
            return averageCurrent.toInt()
        }
    val averageSpeed: Double
        get() {
            val sum =
                averageSpeedList.stream().mapToDouble { obj: Double -> obj }
                    .sum()
            return sum / averageSpeedList.size
        }

    fun getMaxPower(): Double {
        return maxPower
    }

    fun setMaxPower(maxPower: Double) {
        if (Statistics.maxPower < maxPower) {
            Statistics.maxPower = maxPower
        }
    }

    fun getMinPower(): Double {
        return minPower
    }

    fun setMinPower(minPower: Double) {
        if (Statistics.minPower > minPower) {
            Statistics.minPower = minPower
        }
    }

    fun countRespnse() {
        responseReceived += 1
    }

    fun countRequest() {
        requestsSent += 1
    }

    @JvmStatic
    fun setSpeed(speed: Double) {
        speedList.add(speed)
        currentSpeed = speed
    }


    @JvmStatic
    fun round(toRound: Double, decimals: Int): Double {
        val temp = (toRound * Math.pow(10.0, decimals.toDouble())).toInt()
        val temp2 = temp.toDouble()
        return temp2 / Math.pow(10.0, decimals.toDouble())
    }

    fun getCurrentAmpere(): Double {
        return currentAmpere
    }

    @JvmStatic
    fun setCurrentAmpere(currentAmpere: Double) {
        //Log.d("Stat","Current: "+currentAmpere);
        Statistics.currentAmpere = currentAmpere
        currentList.add(currentAmpere)
        //calculateEnergy();
        setMaxPower(power)
        setMinPower(power)
        val now = System.currentTimeMillis()
        var diff = (now - lastTimeStamp).toDouble()
        if (diff > 10000) {
            diff = 500.0
        }
        if (diff > 50) {
            currDiff = diff
        }
        lastTimeStamp = now
    }

    fun getMampHoursPerKilometer(): Double {
        return round(mampHoursPerKilometer, 2)
    }

    fun getRemainingRange(): Double {
        return round(remainingRange, 1)
    }

}