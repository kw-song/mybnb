package mybnb;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommissionRepository extends CrudRepository<Commission, Long>{

    List<Commission> findByBookId(Long bookId);

}