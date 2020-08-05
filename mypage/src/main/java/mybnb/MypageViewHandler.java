package mybnb;

import mybnb.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MypageViewHandler {


    @Autowired
    private MypageRepository mypageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenBooked_then_CREATE_1 (@Payload Booked booked) {
        try {
            if (booked.isMe()) {
                System.out.println("Booked " + booked.toJson());

                // view 객체 생성
                Mypage mypage = new Mypage();

                // view 객체에 이벤트의 Value 를 set 함
                mypage.setBookId(booked.getId());
                mypage.setRoomId(booked.getRoomId());
                mypage.setName(booked.getName());
                mypage.setPrice(booked.getPrice());
                mypage.setAddress(booked.getAddress());
                mypage.setHost(booked.getHost());
                mypage.setGuest(booked.getGuest());
                mypage.setUsedate(booked.getUsedate());
                mypage.setStatus(booked.getStatus());
                BigDecimal charge = BigDecimal.ZERO;
                charge = BigDecimal.valueOf(mypage.getPrice()).multiply(BigDecimal.valueOf(0.01));
                mypage.setCharge(charge.floatValue());

                // view 레파지 토리에 save
                mypageRepository.save(mypage);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenBookCanceled_then_UPDATE_1(@Payload BookCanceled bookCanceled) {
        try {
            if (bookCanceled.isMe()) {
                // view 객체 조회
                List<Mypage> mypageList = mypageRepository.findByBookId(bookCanceled.getId());
                for(Mypage mypage : mypageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    mypage.setStatus(bookCanceled.getStatus());
                    mypage.setCharge(Float.valueOf(0));
                    // view 레파지 토리에 save
                    mypageRepository.save(mypage);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayCanceled_then_UPDATE_2(@Payload PayCanceled payCanceled) {
        try {
            if (payCanceled.isMe()) {
                // view 객체 조회
                List<Mypage> mypageList = mypageRepository.findByBookId(payCanceled.getBookId());
                for(Mypage mypage : mypageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    mypage.setStatus(payCanceled.getStatus());
                    // view 레파지 토리에 save
                    mypageRepository.save(mypage);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenReviewRegistered_then_UPDATE_3(@Payload ReviewRegistered reviewRegistered) {
        try {
            if (reviewRegistered.isMe()) {
                // view 객체 조회
                List<Mypage> mypageList = mypageRepository.findByBookId(reviewRegistered.getBookId());
                for(Mypage mypage : mypageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    mypage.setScore(reviewRegistered.getScore());
                    mypage.setStatus(reviewRegistered.getStatus());
                    // view 레파지 토리에 save
                    mypageRepository.save(mypage);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 개인과제
    @StreamListener(KafkaProcessor.INPUT)
    public void whenReviewRegistered_then_UPDATE_4(@Payload CommissionCharged commissionCharged) {
        try {
            if (commissionCharged.isMe()) {
                // view 객체 조회
                List<Mypage> mypageList = mypageRepository.findByBookId(commissionCharged.getBookId());
                for(Mypage mypage : mypageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    mypage.setStatus(commissionCharged.getStatus());
                    // view 레파지 토리에 save
                    mypageRepository.save(mypage);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}