package domain

object VehicleObjects {

  sealed abstract class VehicleType(asString: String) {
    override def toString: String = asString
  }
  case object TwoWheeler extends VehicleType("Two Wheeler")
  case object HatchbackCar extends VehicleType("Hatchback Car")
  case object SUVCar extends VehicleType("SUV Car")

  case class Vehicle(vehicleType: VehicleType, number: String)
}
