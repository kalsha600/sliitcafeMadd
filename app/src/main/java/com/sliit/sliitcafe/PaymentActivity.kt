package com.sliit.sliitcafe

import adapters.RecyclerSavedCardsAdapter
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import datamodels.SavedCardItem
import services.DatabaseHandler

class PaymentActivity : AppCompatActivity(), RecyclerSavedCardsAdapter.OnItemClickListener {

    private lateinit var totalPaymentTV: TextView
    private lateinit var confirmPaymentBtn: Button

    private lateinit var paymentCreditDebitBtn: Button


    private lateinit var cashPaymentRB: RadioButton
    private lateinit var creditDebitRB: RadioButton



    private lateinit var creditDebitSection: LinearLayout




    var totalItemPrice = 0.0F
    var totalTaxPrice = 0.0F
    var subTotalPrice = 0.0F

    var takeAwayTime = ""



    private var enteredCreditDebitCard = ""


    private lateinit var savedCardRecyclerView: RecyclerView
    private lateinit var savedCardsRecyclerAdapter: RecyclerSavedCardsAdapter
    private val savedCardItems = ArrayList<SavedCardItem>()

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_alert)
            .setTitle("Alert!")
            .setMessage("Do you want to cancel the payment?")
            .setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
                super.onBackPressed()
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.dismiss()
            })
            .create().show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        totalItemPrice = intent.getFloatExtra("totalItemPrice", 0.0F)
        totalTaxPrice = intent.getFloatExtra("totalTaxPrice", 0.0F)
        subTotalPrice = intent.getFloatExtra("subTotalPrice", 0.0F)

        takeAwayTime = intent?.getStringExtra("takeAwayTime").toString()

        totalPaymentTV = findViewById(R.id.total_payment_tv)
        totalPaymentTV.text = "\$%.2f".format(subTotalPrice)

        cashPaymentRB = findViewById(R.id.cash_payment_radio_btn)

        creditDebitRB = findViewById(R.id.credit_debit_card_radio_btn)


        creditDebitSection = findViewById(R.id.credit_debit_section_ll)


        setupPaymentButtons()




        findViewById<ImageView>(R.id.payment_go_back_iv).setOnClickListener { onBackPressed() }
    }




    private fun setupPaymentButtons() {
        confirmPaymentBtn = findViewById(R.id.confirm_payment_btn)

        paymentCreditDebitBtn = findViewById(R.id.payment_credit_debit_card_btn)



        paymentCreditDebitBtn.text = "Pay \$%.2f".format(subTotalPrice)


        confirmPaymentBtn.setOnClickListener { orderDone() }

        paymentCreditDebitBtn.setOnClickListener { doCreditDebitCardPayment() }

    }



    private fun doCreditDebitCardPayment() {
        val cardNumberET = findViewById<EditText>(R.id.payment_credit_debit_card_number_et)
        val cardExpiryDateET = findViewById<EditText>(R.id.payment_credit_debit_expiry_date_et)
        val cardCVVET = findViewById<EditText>(R.id.payment_credit_debit_cvv_et)
        val cardHolderNameET = findViewById<EditText>(R.id.payment_credit_debit_card_holder_name_et)

        var allTrue = true
        if(cardNumberET.length() != 16) {
            cardNumberET.error = "Invalid Card Number"
            allTrue = false
        }
        if(cardExpiryDateET.length() != 5) {
            cardExpiryDateET.error = "Invalid Date Format"
            allTrue = false
        }
        if(cardCVVET.length() != 3) {
            cardCVVET.error = "Invalid CVV Number"
            allTrue = false
        }
        if(cardHolderNameET.text.isEmpty()) {
            cardHolderNameET.error = "Name is required"
            allTrue = false
        }

        if(!allTrue) return

        //Don't Save CVV
        if(findViewById<SwitchCompat>(R.id.payment_credit_debit_saved_card_switch).isChecked) {
            val cardItem = SavedCardItem(
                cardNumberET.text.toString(),
                cardHolderNameET.text.toString(),
                cardExpiryDateET.text.toString()
            )
            val result = DatabaseHandler(this).insertSavedCardDetails(cardItem)
            // if result is -1, then Card is already saved, if 1 then it is saved in database
        }
        enteredCreditDebitCard = "XXXX${cardNumberET.text.substring(12,16)}, ${cardHolderNameET.text}"
        orderDone()
    }

    fun chooseModeOfPayment(view: View) {
        var payMethod = ""
        payMethod = if(view is RadioButton) {
            ((view.parent as LinearLayout).getChildAt(1) as TextView).text.toString()
        } else {
            (((view as CardView).getChildAt(0) as LinearLayout).getChildAt(1) as TextView).text.toString()
        }

        cashPaymentRB.isChecked = false
        creditDebitRB.isChecked = false


        creditDebitSection.visibility = ViewGroup.GONE

        savedCardRecyclerView.visibility = ViewGroup.GONE

        confirmPaymentBtn.visibility = ViewGroup.INVISIBLE

        when(payMethod) {
            getString(R.string.cash_payment) -> {
                cashPaymentRB.isChecked = true
                confirmPaymentBtn.visibility = ViewGroup.VISIBLE
            }


            getString(R.string.credit_or_debit_card) -> {
                creditDebitRB.isChecked = true
                creditDebitSection.visibility = ViewGroup.VISIBLE
            }

        }
    }

    private fun orderDone() {
        var paymentMethod = ""
        when {
            cashPaymentRB.isChecked -> paymentMethod = "Pending: Cash Payment"

            creditDebitRB.isChecked -> paymentMethod = "Paid: $enteredCreditDebitCard"

        }

        val intent = Intent(this, OrderDoneActivity::class.java)
        intent.putExtra("totalItemPrice", totalItemPrice)
        intent.putExtra("totalTaxPrice", totalTaxPrice)
        intent.putExtra("subTotalPrice", subTotalPrice)
        intent.putExtra("takeAwayTime", takeAwayTime)
        intent.putExtra("paymentMethod", paymentMethod)

        startActivity(intent)
        finish()
    }

    override fun onItemClick(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onItemPayButtonClick(position: Int) {
        TODO("Not yet implemented")
    }


}