package repository

import domain.ParkingLotObjects.{ParkingHistory, ParkingLot}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait ParkingLotRepositoryLike {
  protected def parkingLotData: mutable.Map[String, ParkingLot]
  protected def parkingHistories: mutable.Map[String, mutable.ArrayBuffer[ParkingHistory]]

  def addParkingLot(businessName: String, parkingLot: ParkingLot): Unit

  def getParkingLot(businessName: String): Option[ParkingLot]

  def getHistoryRepo(): mutable.Map[String, ArrayBuffer[ParkingHistory]]
}

object InMemoryParkingLotRepository extends ParkingLotRepositoryLike {

  override protected val parkingLotData: mutable.Map[String, ParkingLot] = mutable.Map.empty
  override protected val parkingHistories: mutable.Map[String, ArrayBuffer[ParkingHistory]] = mutable.Map.empty

  override def addParkingLot(businessName: String, parkingLot: ParkingLot): Unit = {
    if (parkingLotData.contains(businessName))
      throw new Exception(s"Trying to add an existing parking lot for business: $businessName")
    this.parkingLotData.addOne(businessName, parkingLot)
  }

  override def getParkingLot(businessName: String): Option[ParkingLot] = {
    parkingLotData.get(businessName)
  }

  override def getHistoryRepo(): mutable.Map[String, ArrayBuffer[ParkingHistory]] = parkingHistories
}
