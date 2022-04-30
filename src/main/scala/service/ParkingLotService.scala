package service

import domain.ParkingLotObjects.{EmptyParkingSpot, OccupiedParkingSpot, ParkingHistory}
import domain.VehicleObjects.Vehicle
import repository.{InMemoryParkingLotRepository, ParkingLotRepositoryLike}
import service.ParkingLotService.{ExitResponse, ParkingFailedResponse, ParkingResponseLike, ParkingSuccessResponse}

import java.time.LocalTime

trait ParkingLotService {

  protected def parkingLotRepo: ParkingLotRepositoryLike
  protected def parkingHistoryService: ParkingHistoryService

  def parkVehicle(vehicle: Vehicle, businessName: String): ParkingResponseLike

  def exitVehicle(vehicle: Vehicle, businessName: String, endTime: LocalTime): ExitResponse
}

object ParkingLotServiceImpl extends ParkingLotService {

  override protected val parkingLotRepo: ParkingLotRepositoryLike = InMemoryParkingLotRepository
  override protected val parkingHistoryService: ParkingHistoryService = ParkingHistoryServiceImpl

  override def parkVehicle(vehicle: Vehicle, businessName: String): ParkingResponseLike = {

    parkingLotRepo.getParkingLot(businessName) match {
      case Some(parkingLot) =>
        parkingLot.parkingSpots.zipWithIndex.collectFirst { case (spot: EmptyParkingSpot, index) if spot.supportedVehicle == vehicle.vehicleType =>
          OccupiedParkingSpot(vehicle.vehicleType, spot.spotName, vehicle, LocalTime.now()) -> index
        } match {
          case Some((occupiedParkingSpot, index)) =>
            parkingLot.parkingSpots.update(index, occupiedParkingSpot)

            parkingHistoryService.addHistoryOnParking(ParkingHistory(occupiedParkingSpot.supportedVehicle,
              occupiedParkingSpot.parkedVehicle.number, businessName, occupiedParkingSpot.spotName,
              occupiedParkingSpot.parkingStartTime, endTime = None, cost = None))

            ParkingSuccessResponse(occupiedParkingSpot)
          case None => ParkingFailedResponse("No parking spot available")
        }
      case None => throw new Exception(s"No parking lot available for business name: $businessName")
    }
  }

  override def exitVehicle(vehicle: Vehicle, businessName: String, exitTime: LocalTime): ExitResponse = {
    parkingLotRepo.getParkingLot(businessName) match {
      case Some(parkingLot) => parkingLot.parkingSpots.zipWithIndex.collectFirst { case (spot: OccupiedParkingSpot, index) if spot.parkedVehicle.number == vehicle.number =>

        val emptyParkingSpot = spot.toEmptyParkingSpot

        parkingLot.parkingSpots.update(index, emptyParkingSpot)
        val cost = parkingLot.rateCard.getPricingStrategy(vehicle.vehicleType).computeParkingCost(spot.parkingStartTime, exitTime)

        parkingHistoryService.updateParkingHistoryOnExit(vehicle.number, exitTime, cost)

        ExitResponse(spot.spotName, vehicle.number, exitTime, cost)
      }.getOrElse(throw new Exception(s"$vehicle is not parked"))
      case None => throw new Exception(s"No parking lot available for business name: $businessName")
    }
  }

}


object ParkingLotService {

  sealed trait ParkingResponseType
  case object ParkingDone extends ParkingResponseType
  case object ParkingFailed extends ParkingResponseType

  sealed trait ParkingResponseLike {
    def responseType: ParkingResponseType
  }

  case class ParkingSuccessResponse(occupiedParkingSpot: OccupiedParkingSpot) extends ParkingResponseLike {
    override final val responseType: ParkingResponseType = ParkingDone
  }

  case class ParkingFailedResponse(reason: String) extends ParkingResponseLike {
    override final val responseType: ParkingResponseType = ParkingFailed
  }

  case class ExitResponse(spotName: String, vehicleNumber: String, exitTime: LocalTime,
                          totalCost: Double)

}
