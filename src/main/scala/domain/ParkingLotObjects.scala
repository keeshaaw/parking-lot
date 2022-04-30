package domain

import domain.RateCardObjects.RateCard
import domain.VehicleObjects.{Vehicle, VehicleType}

import java.time.LocalTime
import scala.collection.mutable

object ParkingLotObjects {

  case class ParkingLot(parkingSpots: mutable.ArrayBuffer[ParkingSpotLike], rateCard: RateCard)

  sealed trait ParkingSpotLike {
    def supportedVehicle: VehicleType
    def spotName: String
    def isAvailable: Boolean
    def underlingParkedVehicle: Option[Vehicle]
    def underlingParkingStartTime: Option[LocalTime]
  }

  case class EmptyParkingSpot(supportedVehicle: VehicleType, spotName: String) extends ParkingSpotLike {
    override val isAvailable: Boolean = true
    override val underlingParkedVehicle: Option[Vehicle] = None
    override val underlingParkingStartTime: Option[LocalTime] = None
  }

  case class OccupiedParkingSpot(supportedVehicle: VehicleType, spotName: String,
                                 parkedVehicle: Vehicle, parkingStartTime: LocalTime) extends ParkingSpotLike {
    override val isAvailable: Boolean = false
    override val underlingParkedVehicle: Option[Vehicle] = Some(parkedVehicle)
    override val underlingParkingStartTime: Option[LocalTime] = Some(parkingStartTime)

    def toEmptyParkingSpot: EmptyParkingSpot = EmptyParkingSpot(supportedVehicle, spotName)
  }

  case class ParkingHistory(supportedVehicle: VehicleType, vehicleNumber: String, businessName: String,
                            spotName: String, startTime: LocalTime, endTime: Option[LocalTime],
                            cost: Option[Double]) {

    def withEndTimeAndCost(endTime: LocalTime, cost: Double): ParkingHistory = {
      this.copy(endTime = Some(endTime), cost = Some(cost))
    }
  }

}
