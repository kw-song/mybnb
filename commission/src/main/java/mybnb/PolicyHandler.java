package mybnb;

import mybnb.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PolicyHandler{

    @Autowired
    private CommissionRepository commissionRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCanceled_CommissionCancel(@Payload PayCanceled payCanceled){

        if(payCanceled.isMe()){
            System.out.println("##### listener CommissionCancel : " + payCanceled.toJson());
            Commission commission = new Commission();
            commission.setPayId(payCanceled.getId());
            commission.setBookId(payCanceled.getBookId());
            commission.setPrice(payCanceled.getPrice());
            BigDecimal charge = BigDecimal.ZERO;
            charge = BigDecimal.valueOf(payCanceled.getPrice()).multiply(BigDecimal.valueOf(-0.01));
            commission.setCharge(charge.floatValue());
            commission.setStatus("CommissionCanceled");
            // view 레파지 토리에 save
            commissionRepository.save(commission);
        }
    }

}
