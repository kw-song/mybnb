package mybnb;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name="Payment_table")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long bookId;
    private Long roomId;
    private String name;
    private Long price;
    private String address;
    private String host;
    private String guest;
    private String usedate;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @PostPersist
    public void onPostPersist(){

        if("PayApproved".equals(getStatus())) {
            PayApproved payApproved = new PayApproved();
            BeanUtils.copyProperties(this, payApproved);
            payApproved.setStatus(getStatus());
            payApproved.publishAfterCommit();

            // 결제이력을 저장한 후 적당한 시간 끌기
            /*
            try {
                Thread.currentThread().sleep((long) (400 + Math.random() * 220));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
            mybnb.external.Commission commission = new mybnb.external.Commission();
            commission.setPayId(getId());
            commission.setPrice(getPrice());
            commission.setBookId(getBookId());
            BigDecimal charge = BigDecimal.ZERO;
            charge = BigDecimal.valueOf(getPrice()).multiply(BigDecimal.valueOf(0.01));
            commission.setCharge(charge.floatValue());
            commission.setStatus("CommissionRequested");

            // mappings goes here
            try {
                PayApplication.applicationContext.getBean(mybnb.external.CommissionService.class)
                        .commissionRequest(commission);
            }catch(Exception e) {
                throw new RuntimeException("수수료 서비스 호출 실패입니다.");
            }

        }
        else if("PayCanceled".equals(getStatus())) {
            PayCanceled payCanceled = new PayCanceled();
            BeanUtils.copyProperties(this, payCanceled);
            payCanceled.setStatus(getStatus());
            payCanceled.publishAfterCommit();
        }
    }

}
