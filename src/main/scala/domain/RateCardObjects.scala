package domain

import domain.VehicleObjects.VehicleType

import java.time.{Duration, LocalTime}

object RateCardObjects {
  sealed trait ParkingPricingStrategy {
    def computeParkingCost(startTime: LocalTime, endTime: LocalTime): Double
  }

  case class FlatRatePerHour(hourlyRate: Double) extends ParkingPricingStrategy {

    def computeParkingCost(startTime: LocalTime, endTime: LocalTime): Double = {
      Duration.between(startTime, endTime).toHours * hourlyRate
    }
  }

  case class DynamicPricing(firstNHourRate: FirstNHourRate,
                            hourlyRateAfterFirstHour: Double) extends ParkingPricingStrategy {

    def computeParkingCost(startTime: LocalTime, endTime: LocalTime): Double = {
      val totalDurationInParking = Duration.between(startTime, endTime).toHours

      if(totalDurationInParking <= firstNHourRate.tilHouNumber) {
        totalDurationInParking * firstNHourRate.rate
      } else {
        val costForFirstNHours = firstNHourRate.tilHouNumber * firstNHourRate.rate

        val remainingHoursAfterFirstNHours = totalDurationInParking - firstNHourRate.tilHouNumber
        val costAfterFirstNHours = remainingHoursAfterFirstNHours * hourlyRateAfterFirstHour

        costForFirstNHours + costAfterFirstNHours
      }
    }
  }

  case class FirstNHourRate(tilHouNumber: Int, rate: Double)

  case class RateCard(rates: Map[VehicleType, ParkingPricingStrategy]) {
    def getPricingStrategy(vehicleType: VehicleType): ParkingPricingStrategy = rates
      .getOrElse(vehicleType, throw new Exception(s"Could not find any rate for $vehicleType"))
  }
}
