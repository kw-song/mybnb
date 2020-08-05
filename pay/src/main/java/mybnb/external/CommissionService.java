
package mybnb.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="commssion", url="${api.url.commission}")
public interface CommissionService {

    @RequestMapping(method= RequestMethod.POST, path="/commissions")
    public void commissionRequest(@RequestBody Commission commission);

}