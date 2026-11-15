const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_owner_789";
const ADMIN_EMAIL = "lala.kushal@gmail.com";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate() {
  return new Date(Date.now() - 1000);
}

function validOrderPayload(uid, overrides = {}) {
  const ts = pastDate();
  return {
    userId: uid,
    userEmail: "customer@example.com",
    placedAtMillis: Date.now(),
    formattedDate: "14 Nov, 08:30 PM",
    status: "PENDING",
    items: [
      {
        menuItemId: "biryani_hyderabadi_chicken",
        name: "Hyderabadi Chicken Dum Biryani",
        quantity: 1,
        unitPriceInr: 329,
        isVeg: false,
        customization: "Authentic Dum",
      },
    ],
    totalInr: 365,
    discountInr: 0,
    deliveryAddress: "24 MG Road, Indiranagar, Bengaluru",
    recipientName: "Rohan Verma",
    phoneNumber: "9876543210",
    deliveryInstruction: "Ring bell on arrival",
    kitchenNote: "Less spicy",
    paymentMethod: "CASH_ON_DELIVERY",
    estimatedMinutes: 35,
    createdAt: ts,
    updatedAt: ts,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write orders", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("orders").get());
  await assertFails(
    unauthDb.collection("orders").doc("MFD-1001").set(validOrderPayload(ALICE_UID))
  );
});

test("Customer: can create own PENDING order and query own orders", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    aliceDb.collection("orders").doc("MFD-1001").set(validOrderPayload(ALICE_UID))
  );

  await assertSucceeds(
    aliceDb.collection("orders").where("userId", "==", ALICE_UID).get()
  );
});

test("Customer: cannot list all orders without userId filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("orders").get());
});

test("Customer: cannot read or update another customer's order", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("orders").doc("MFD-2001").set(validOrderPayload(BOB_UID))
  );

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("orders").doc("MFD-2001").get());
  await assertFails(
    aliceDb.collection("orders").doc("MFD-2001").update({
      status: "ACCEPTED",
      updatedAt: pastDate(),
    })
  );
});

test("Shadow update & identity spoofing: rejects ghost fields, spoofed userId, non-PENDING create, invalid phone", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Ghost field
  await assertFails(
    aliceDb.collection("orders").doc("MFD-1002").set(
      validOrderPayload(ALICE_UID, { isVerified: true })
    )
  );
  // Spoofed userId
  await assertFails(
    aliceDb.collection("orders").doc("MFD-1003").set(validOrderPayload(BOB_UID))
  );
  // Status shortcut on create
  await assertFails(
    aliceDb.collection("orders").doc("MFD-1004").set(
      validOrderPayload(ALICE_UID, { status: "DELIVERED" })
    )
  );
  // Invalid 10-digit phone number
  await assertFails(
    aliceDb.collection("orders").doc("MFD-1005").set(
      validOrderPayload(ALICE_UID, { phoneNumber: "12345" })
    )
  );
});

test("Email spoofing test: unverified admin email cannot list all orders", async () => {
  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();

  await assertFails(spoofDb.collection("orders").get());
});

test("Bootstrapped Admin: can list all customer orders and update status, including after customer update", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("orders").doc("MFD-3001").set(validOrderPayload(BOB_UID))
  );

  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  await assertSucceeds(adminDb.collection("orders").get());
  await assertSucceeds(
    adminDb.collection("orders").doc("MFD-3001").update({
      status: "ACCEPTED",
      updatedAt: pastDate(),
    })
  );
  await assertSucceeds(
    adminDb.collection("orders").doc("MFD-3001").update({
      status: "DELIVERED",
      updatedAt: pastDate(),
    })
  );
});

test("Terminal state locking: non-admin cannot mutate DELIVERED order or tamper with totalInr", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("orders").doc("MFD-4001").set(validOrderPayload(ALICE_UID))
  );

  // Tamper totalInr fails
  await assertFails(
    aliceDb.collection("orders").doc("MFD-4001").update({
      totalInr: 1,
      updatedAt: pastDate(),
    })
  );

  // Transition to DELIVERED
  await assertSucceeds(
    aliceDb.collection("orders").doc("MFD-4001").update({
      status: "DELIVERED",
      updatedAt: pastDate(),
    })
  );

  // Non-admin cannot reopen DELIVERED order
  await assertFails(
    aliceDb.collection("orders").doc("MFD-4001").update({
      status: "PENDING",
      updatedAt: pastDate(),
    })
  );
});

test("Delete is strictly denied on orders", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("orders").doc("MFD-5001").set(validOrderPayload(ALICE_UID))
  );
  await assertFails(aliceDb.collection("orders").doc("MFD-5001").delete());
});
