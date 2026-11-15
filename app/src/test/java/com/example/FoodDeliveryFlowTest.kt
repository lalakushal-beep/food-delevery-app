package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FoodDeliveryFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun fullUserJourney_addCartNavigationQuantityCheckoutPlaceOrderConfirmation_works() {
        // 1. Home screen is displayed with Admin Panel button
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("home_admin_panel_button").assertIsDisplayed()
        composeRule.onNodeWithTag("home_admin_panel_button").performClick()
        composeRule.onNodeWithTag("admin_orders_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("admin_back_button").performClick()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()

        // 2. Scroll to a dish and scroll one more card past it
        composeRule.onNodeWithTag("home_screen")
            .performScrollToNode(hasTestTag("dish_card_thali_maharaja"))
        composeRule.onNodeWithTag("home_screen")
            .performScrollToNode(hasTestTag("dish_card_biryani_hyderabadi_chicken"))

        // Click Add to Cart on Maharaja Thali
        composeRule.onNodeWithTag("menu_thali_maharaja_add_button").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("floating_cart_bar").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("1 item added").assertIsDisplayed()

        // 3. Increase and decrease quantity from Home card
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("menu_thali_maharaja_increment_button").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("menu_thali_maharaja_increment_button").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithText("2 items added").assertIsDisplayed() }.isSuccess
        }

        composeRule.onNodeWithTag("menu_thali_maharaja_decrement_button").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithText("1 item added").assertIsDisplayed() }.isSuccess
        }

        // 4. Bottom navigation: Menu, Orders, Home, Cart
        composeRule.onNodeWithTag("nav_tab_menu").performClick()
        composeRule.onNodeWithTag("menu_screen").assertIsDisplayed()

        // Also test Menu category chip and Add to Cart on Menu screen
        composeRule.onNodeWithTag("menu_category_row")
            .performScrollToNode(hasTestTag("menu_category_pizza"))
        composeRule.onNodeWithTag("menu_category_pizza").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("menu_grid")
            .performScrollToNode(hasTestTag("menu_pizza_tandoori_paneer_add_button"))
        composeRule.onNodeWithTag("menu_pizza_tandoori_paneer_add_button").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithText("2 items added").assertIsDisplayed() }.isSuccess
        }

        composeRule.onNodeWithTag("nav_tab_orders").performClick()
        composeRule.onNodeWithTag("orders_screen").assertIsDisplayed()

        composeRule.onNodeWithTag("nav_tab_home").performClick()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()

        // 5. Open Cart via Floating Cart Bar
        composeRule.onNodeWithTag("floating_cart_bar").performClick()
        composeRule.onNodeWithTag("cart_screen").assertIsDisplayed()

        // 6. Increase & decrease inside CartScreen
        composeRule.onNodeWithTag("cart_list")
            .performScrollToNode(hasTestTag("cart_inc_thali_maharaja"))
        composeRule.onNodeWithTag("cart_inc_thali_maharaja").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithTag("cart_qty_thali_maharaja").assertTextEquals("2") }.isSuccess
        }
        composeRule.onNodeWithTag("cart_dec_thali_maharaja").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithTag("cart_qty_thali_maharaja").assertTextEquals("1") }.isSuccess
        }

        // 7. Checkout button
        composeRule.onNodeWithTag("proceed_to_checkout_button").performClick()
        composeRule.onNodeWithTag("checkout_screen").assertIsDisplayed()

        // Verify 10-digit mobile validation works, then enter valid 10-digit number
        composeRule.onNodeWithTag("checkout_phone_input").performScrollTo()
        composeRule.onNodeWithTag("checkout_phone_input").performTextReplacement("98765")
        composeRule.onNodeWithTag("place_order_button").performClick()
        composeRule.onNodeWithTag("checkout_validation_error").assertIsDisplayed()

        composeRule.onNodeWithTag("checkout_name_input").performScrollTo()
        composeRule.onNodeWithTag("checkout_name_input").performTextReplacement("Rohan Verma")
        composeRule.onNodeWithTag("checkout_phone_input").performScrollTo()
        composeRule.onNodeWithTag("checkout_phone_input").performTextReplacement("9876543210")
        composeRule.onNodeWithTag("checkout_address_input").performScrollTo()
        composeRule.onNodeWithTag("checkout_address_input").performTextReplacement("24 MG Road, Indiranagar, Bengaluru 560038")
        composeRule.onNodeWithTag("checkout_landmark_input").performScrollTo()
        composeRule.onNodeWithTag("checkout_landmark_input").performTextReplacement("Near Indiranagar Metro")
        composeRule.onNodeWithTag("checkout_list")
            .performScrollToNode(hasTestTag("payment_option_CASH_ON_DELIVERY"))
        composeRule.onNodeWithTag("payment_option_CASH_ON_DELIVERY").performClick()

        // 8. Place Order button
        composeRule.onNodeWithTag("place_order_button").performClick()

        // 9. Order Confirmation screen appears with Pending status
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("order_confirmation_screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("order_confirmation_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Order Confirmed!").assertIsDisplayed()
        composeRule.onNodeWithTag("order_confirmation_screen")
            .performScrollToNode(hasTestTag("track_orders_button"))
        composeRule.onNodeWithTag("track_orders_button").performClick()

        // 10. My Orders displays the new order with status Pending & Admin Dashboard updates sync back to My Orders
        composeRule.onNodeWithTag("orders_screen").assertIsDisplayed()
        composeRule.onAllNodesWithText("Pending")[0].assertIsDisplayed()
        composeRule.onNodeWithTag("open_admin_orders_button").performClick()
        composeRule.onNodeWithTag("admin_orders_screen").assertIsDisplayed()

        // Verify Admin Dashboard KPIs (Total Orders & Daily Sales) and customer details
        composeRule.onNodeWithTag("admin_kpi_total_orders").assertIsDisplayed()
        composeRule.onNodeWithTag("admin_kpi_daily_sales").assertIsDisplayed()
        composeRule.onNodeWithTag("admin_orders_list")
            .performScrollToNode(hasText("Rohan Verma"))
        composeRule.onNodeWithText("Rohan Verma").assertIsDisplayed()
        composeRule.onNodeWithText("9876543210").assertIsDisplayed()

        // Accept the Pending order
        composeRule.onNodeWithTag("admin_orders_list")
            .performScrollToNode(hasText("Accept"))
        composeRule.onNodeWithText("Accept").performScrollTo()
        composeRule.onNodeWithText("Accept").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithText("Move to: Preparing").assertIsDisplayed() }.isSuccess
        }
        composeRule.onNodeWithText("Move to: Preparing").performScrollTo()
        composeRule.onNodeWithText("Move to: Preparing").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithText("Move to: Ready").assertIsDisplayed() }.isSuccess
        }

        // Navigate back to Customer My Orders screen and verify status updated to Preparing
        composeRule.onNodeWithTag("admin_back_button").performClick()
        composeRule.onNodeWithTag("orders_screen").assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithText("Chef is freshly cooking your meal").assertIsDisplayed() }.isSuccess
        }
    }
}
