public class Payment {
    User user;
    int amount;
    boolean status;
    enum PaymentMode {
        CREDIT_CARD,
        UPI
    }

     Payment(User user,int amount,PaymentMode mode){
        this.user=user;
        this.amount=amount;
    }

    void Success(){
        this.status=true;
        System.out.println("Payment Successful");
        return;
    }



}
