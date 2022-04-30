package sample_data

import domain.ParkingLotObjects.{EmptyParkingSpot, ParkingLot, ParkingSpotLike}
import domain.RateCardObjects._
import domain.VehicleObjects.{HatchbackCar, SUVCar, TwoWheeler, VehicleType}
import repository.InMemoryParkingLotRepository

import scala.collection.mutable.ArrayBuffer

object DataSeeder {

  private def getSampleRateCard(): RateCard = {
    val twoWheelerRateCard: (VehicleType, ParkingPricingStrategy) = TwoWheeler -> FlatRatePerHour(50)

    val hatchbackCarRateCard: (VehicleType, ParkingPricingStrategy) = {

      val dynamicRate = {
        val firstNHourRate = FirstNHourRate(2, 100)
        DynamicPricing(firstNHourRate, 80)
      }

      HatchbackCar -> dynamicRate
    }

    val suvCarRateCard = {
      val dynamicRate = {
        val firstNHourRate = FirstNHourRate(2, 150)
        DynamicPricing(firstNHourRate, 100)
      }

      SUVCar -> dynamicRate
    }
    RateCard(Map(twoWheelerRateCard, hatchbackCarRateCard, suvCarRateCard))
  }

  private def getSampleParkingLot(): ParkingLot = {
    val allVehicleTypes = Array[VehicleType](TwoWheeler, HatchbackCar, SUVCar)

    val numberOfParkingSpots = 6
    val allEmptyParkingSpots: ArrayBuffer[ParkingSpotLike] = ArrayBuffer() ++ (1 to numberOfParkingSpots).map { index =>
      val vehicleType = allVehicleTypes(index % allVehicleTypes.length)
      EmptyParkingSpot(vehicleType, s"SPOT-$index"): ParkingSpotLike
    }
    ParkingLot(allEmptyParkingSpots, getSampleRateCard())
  }

  def initializeParkingLotRepo(businessName: String): ParkingLot = {
    val sampleParkingLot = getSampleParkingLot()
    InMemoryParkingLotRepository.addParkingLot(businessName, sampleParkingLot)
    sampleParkingLot
  }

}
