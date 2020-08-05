package mybnb;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

import java.net.ConnectException;
import java.util.List;

@Entity
@Table(name="Booking_table")
public class Booking {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private Long roomId;
    private String name;
    private Long price;
    private String address;
    private String host;
    private String guest;
    private String usedate;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getGuest() {
        return guest;
    }

    public void setGuest(String guest) {
        this.guest = guest;
    }

    public String getUsedate() {
        return usedate;
    }

    public void setUsedate(String usedate) {
        this.usedate = usedate;
    }

    @PostPersist
    public void onPostPersist(){
        // 예약시 결제까지 트랜잭션을 통합을 위해 결제 서비스 직접 호출
        {
            mybnb.external.Payment payment = new mybnb.external.Payment();
            payment.setBookId(getId());
            payment.setRoomId(getRoomId());
            payment.setGuest(getGuest());
            payment.setPrice(getPrice());
            payment.setName(getName());
            payment.setHost(getHost());
            payment.setAddress(getAddress());
            payment.setUsedate(getUsedate());
            payment.setStatus("PayApproved");

            // mappings goes here
            try {
                BookingApplication.applicationContext.getBean(mybnb.external.PaymentService.class)
                        .pay(payment);
            }catch(Exception e) {
                throw new RuntimeException("결제서비스 호출 실패입니다.");
            }
        }
        
        // 결제까지 완료되면 최종적으로 예약 완료 이벤트 발생
        Booked booked = new Booked();
        BeanUtils.copyProperties(this, booked);
        booked.setStatus("Booked");
        booked.publishAfterCommit();
    }

    @PostRemove
    public void onPostRemove(){
        BookCanceled bookCanceled = new BookCanceled();
        BeanUtils.copyProperties(this, bookCanceled);
        bookCanceled.setStatus("BookCanceled");
        bookCanceled.publishAfterCommit();

    }

}
