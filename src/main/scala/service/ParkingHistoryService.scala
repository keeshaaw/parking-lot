package service

import domain.ParkingLotObjects.ParkingHistory
import domain.VehicleObjects.Vehicle
import repository.{InMemoryParkingLotRepository, ParkingLotRepositoryLike}

import java.time.LocalTime
import scala.collection.mutable.ArrayBuffer

trait ParkingHistoryService {

  protected def parkingLotRepo: ParkingLotRepositoryLike

  def addHistoryOnParking(history: ParkingHistory): Unit

  def updateParkingHistoryOnExit(vehicleNumber: String, exitTime: LocalTime, cost: Double): Unit

  def getHistoriesForVehicle(vehicle: Vehicle): List[ParkingHistory]
}

object ParkingHistoryServiceImpl extends ParkingHistoryService {

  override protected val parkingLotRepo: InMemoryParkingLotRepository.type = InMemoryParkingLotRepository
  private val parkingHistoryRepo = parkingLotRepo.getHistoryRepo()

  override def addHistoryOnParking(history: ParkingHistory): Unit =
    parkingHistoryRepo.get(history.vehicleNumber) match {
      case Some(value) => value.addOne(history)
      case None =>
        val parkingHistories = ArrayBuffer(history)
        parkingHistoryRepo.put(history.vehicleNumber, parkingHistories)
    }

  override def updateParkingHistoryOnExit(vehicleNumber: String, exitTime: LocalTime, cost: Double): Unit = {
    parkingHistoryRepo.get(vehicleNumber) match {
      case Some(histories) =>
        if (histories.last.vehicleNumber == vehicleNumber) {
          val updatedHistory = histories.last.withEndTimeAndCost(exitTime, cost)
          histories.update(histories.length - 1, updatedHistory)
        } else {
          throw new Exception(s"The last record must for the vehicle number: $vehicleNumber in $histories")
        }
      case None => throw new Exception(s"Parking history must exist for vehicle number: $vehicleNumber")
    }
  }


  override def getHistoriesForVehicle(vehicle: Vehicle): List[ParkingHistory] =
    parkingHistoryRepo.get(vehicle.number).toList.flatten
}
