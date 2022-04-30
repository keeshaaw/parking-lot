import domain.ParkingLotObjects.{EmptyParkingSpot, OccupiedParkingSpot, ParkingHistory, ParkingLot}
import domain.VehicleObjects.{HatchbackCar, TwoWheeler, Vehicle}
import sample_data.DataSeeder
import service.ParkingLotService.{ExitResponse, ParkingFailedResponse, ParkingResponseLike, ParkingSuccessResponse}
import service.{ParkingHistoryServiceImpl, ParkingLotServiceImpl}

import java.time.LocalTime

object Main {

  def main(args: Array[String]): Unit = {

    val parkingLotService = ParkingLotServiceImpl
    val parkingHistoryService = ParkingHistoryServiceImpl
    val businessName = "Udaan"

    //Seeding parking lot data
    val parkingLot = DataSeeder.initializeParkingLotRepo(businessName)
    printCurrentStatusOfParkingLot(businessName, parkingLot)

    val twoWheeler1 = Vehicle(TwoWheeler, "KA-53-EN-8745")
    val twoWheeler2 = Vehicle(TwoWheeler, "KA-55-EN-4940")
    val twoWheeler3 = Vehicle(TwoWheeler, "KA-41-EN-1540")

    //Parking first two wheeler
    printParkingResponse(businessName, parkingLotService.parkVehicle(twoWheeler1, businessName))
    //Parking second two wheeler
    printParkingResponse(businessName, parkingLotService.parkVehicle(twoWheeler2, businessName))
    //Parking third two wheeler. This will fail to park as there are no parking spots left after parking 2 two wheelers
    printParkingResponse(businessName, parkingLotService.parkVehicle(twoWheeler3, businessName))

    printCurrentStatusOfParkingLot(businessName, parkingLot)

    printExistResponse(businessName, parkingLotService.exitVehicle(twoWheeler1, businessName, LocalTime.now().plusHours(3)))

    printCurrentStatusOfParkingLot(businessName, parkingLot)

    printParkingResponse(businessName, parkingLotService.parkVehicle(twoWheeler3, businessName))

    printCurrentStatusOfParkingLot(businessName, parkingLot)

    val hatchbackCar1 = Vehicle(HatchbackCar, "KA-53-CA-4565")

    printParkingResponse(businessName, parkingLotService.parkVehicle(hatchbackCar1, businessName))
    printExistResponse(businessName, parkingLotService.exitVehicle(hatchbackCar1, businessName, LocalTime.now().plusHours(3)))
    printParkingResponse(businessName, parkingLotService.parkVehicle(hatchbackCar1, businessName))
    printExistResponse(businessName, parkingLotService.exitVehicle(hatchbackCar1, businessName, LocalTime.now().plusHours(5)))
    printParkingHistories(parkingHistoryService.getHistoriesForVehicle(hatchbackCar1))
  }

  def printCurrentStatusOfParkingLot(businessName: String, parkingLot: ParkingLot) = {
    println(s"================== Current Status for: $businessName ================")
    parkingLot.parkingSpots.foreach {
      case EmptyParkingSpot(supportedVehicle, spotName) =>
        println(s"Spot: $spotName, Status: Empty, SupportedVehicle: $supportedVehicle")
      case OccupiedParkingSpot(_, spotName, parkedVehicle, parkingStartTime) =>
        println(s"Spot: $spotName, Status: Occupied, Parked Vehicle: ${parkedVehicle.vehicleType}" +
          s" ${parkedVehicle.number}, Parking Start Time: ${parkingStartTime.toString}")
    }
    println("==============================================================\n")
  }

  def printParkingResponse(businessName: String, parkingResponseLike: ParkingResponseLike): Unit = {
    parkingResponseLike match {
      case ParkingSuccessResponse(occupiedParkingSpot) =>
        val parkedVehicle = occupiedParkingSpot.parkedVehicle
        println(s"[PARKING] Status: Parked, Vehicle: ${parkedVehicle.vehicleType} ${parkedVehicle.number} " +
          s"Spot: ${occupiedParkingSpot.spotName}, Business Name: $businessName, " +
          s"Start Time: ${occupiedParkingSpot.parkingStartTime}\n")

      case ParkingFailedResponse(reason) =>
        println(s"[PARKING] Status: Failed, Reason: $reason\n")
    }
  }

  def printExistResponse(businessName: String, existResponse: ExitResponse): Unit = {
    println(s"[EXITING] Status: Exited, Vehicle: ${existResponse.vehicleNumber}, Spot: ${existResponse.spotName}, " +
      s"Business Name: $businessName, Cost: ${existResponse.totalCost}\n")
  }

  def printParkingHistories(parkingHistories: List[ParkingHistory]): Unit = {
    println(s"===================== Parking History =====================")
    parkingHistories.foreach { ph =>
      println(s"Vehicle: ${ph.vehicleNumber}, Business Name: ${ph.businessName}, Spot: ${ph.spotName} "+
        s"Start Time: ${ph.startTime}, End Time: ${ph.endTime.getOrElse("N/A")}, Cost: ${ph.cost.getOrElse("N/A")}")
    }
    println(s"===========================================================")
  }
}