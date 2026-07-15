const { setGlobalOptions } = require("firebase-functions");
const { onRequest } = require("firebase-functions/https");
const admin = require("firebase-admin");

admin.initializeApp();

setGlobalOptions({
  maxInstances: 10,
});


exports.sendRideNotification = onRequest(async (req, res) => {

  try {

    const {
      driverToken,
      riderKey,
      requestId,
      pickupLocation
    } = req.body;

console.log("Request Body:", req.body);
console.log("driverToken:", driverToken);
console.log("riderKey:", riderKey);
console.log("requestId:", requestId);
console.log("pickupLocation:", pickupLocation);
console.log("Request Body:", JSON.stringify(req.body));


    if (!driverToken) {
      res.status(400).send({
        success: false,
        message: "Driver token missing"
      });
      return;
    }


    const message = {

      token: driverToken,

      data: {

  title: "RequestDriver",

  body: "You have a new ride request",

  RiderKey: riderKey,

  requestId: requestId,

  PickUpLocation: pickupLocation

},

      android: {

        priority: "high"

      }

    };


    const response = await admin.messaging()
      .send(message);


    console.log(
      "FCM message sent:",
      response
    );


    res.status(200).send({

      success: true,

      messageId: response

    });


  } catch(error) {


    console.error(
      "FCM Error:",
      error
    );


    res.status(500).send({

      success:false,

      error:error.message

    });

  }

});

exports.sendAcceptNotification = onRequest(async (req, res) => {
    try {

        const { riderToken, driverKey, requestId } = req.body;

        if (!riderToken || !driverKey || !requestId) {
            return res.status(400).json({
                success: false,
                message: "Missing required parameters."
            });
        }

     const message = {
         token: riderToken,

         data: {
             title: "REQUEST_DRIVER_ACCEPT",
             body: "Driver accepted your request",
             driverKey: driverKey,
             requestId: requestId
         }
     };


        const response = await admin.messaging().send(message);

        return res.status(200).json({
            success: true,
            message: "Accept notification sent successfully.",
            response: response
        });

    } catch (error) {

        return res.status(500).json({
            success: false,
            message: error.message
        });

    }
});

exports.sendRideCancellationNotification = onRequest(async (req, res) => {
  try {

    const { requestId, cancelledBy } = req.body;

    if (!requestId || !cancelledBy) {
      return res.status(400).json({
        success: false,
        message: "requestId and cancelledBy are required."
      });
    }

    // Read Ride Request
    const rideSnapshot = await admin.database()
      .ref("RideRequests")
      .child(requestId)
      .once("value");

    if (!rideSnapshot.exists()) {
      return res.status(404).json({
        success: false,
        message: "Ride request not found."
      });
    }

    const ride = rideSnapshot.val();

    let receiverId = "";

    if (cancelledBy === "RIDER") {
      receiverId = ride.driverId;
    } else if (cancelledBy === "DRIVER") {
      receiverId = ride.riderId;
    } else {
      return res.status(400).json({
        success: false,
        message: "Invalid cancelledBy value."
      });
    }

    // Read receiver FCM token
    const tokenSnapshot = await admin.database()
      .ref("Token")
      .child(receiverId)
      .once("value");

    if (!tokenSnapshot.exists()) {
      return res.status(404).json({
        success: false,
        message: "Receiver token not found."
      });
    }

    const receiverToken = tokenSnapshot.val().token;

    const message = {

      token: receiverToken,

      android: {
        priority: "high"
      },

      data: {
        title: "RIDE_CANCELLED",
        body: "Ride has been cancelled",
        requestId: requestId,
        cancelledBy: cancelledBy
      }

    };

    const response = await admin.messaging().send(message);

    return res.status(200).json({
      success: true,
      message: "Ride cancellation notification sent successfully.",
      response: response
    });

  } catch (error) {

    console.error("Cancellation Error:", error);

    return res.status(500).json({
      success: false,
      message: error.message
    });

  }
});

exports.sendTripStartedNotification = onRequest(async (req, res) => {
  try {

    const { riderToken, requestId } = req.body;

    if (!riderToken) {
      return res.status(400).json({
        success: false,
        message: "Missing riderToken"
      });
    }

    const message = {

      token: riderToken,

      data: {
        title: "TRIP_STARTED",
        body: "Your trip has started.",
        requestId: requestId || ""
      }

    };

    const response = await admin.messaging().send(message);

    return res.status(200).json({
      success: true,
      message: "Trip Started notification sent successfully.",
      response: response
    });

  } catch (error) {

    console.error("Trip Started Error:", error);

    return res.status(500).json({
      success: false,
      message: error.message
    });

  }
});

exports.sendTripCompletedNotification = onRequest(async (req, res) => {
  try {

    const { riderToken, requestId } = req.body;

    if (!riderToken) {
      return res.status(400).json({
        success: false,
        message: "Missing riderToken"
      });
    }

    const message = {

      token: riderToken,

      data: {
        title: "TRIP_COMPLETED",
        body: "Your trip has been completed.",
        requestId: requestId || ""
      }

    };

    const response = await admin.messaging().send(message);

    return res.status(200).json({
      success: true,
      message: "Trip Completed notification sent successfully.",
      response: response
    });

  } catch (error) {

    console.error("Trip Completed Error:", error);

    return res.status(500).json({
      success: false,
      message: error.message
    });

  }
});