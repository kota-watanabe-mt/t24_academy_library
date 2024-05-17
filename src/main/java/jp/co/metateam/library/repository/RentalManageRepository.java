package jp.co.metateam.library.repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
    List<RentalManage> findAll();

	Optional<RentalManage> findById(Long id);

// 在庫テーブルから在庫管理番号が一致していて在庫ステータスが利用可の数をカウントする(0or1)   
 
@Query("select count (*) from Stock where id = ?1 AND status = 0")
    Integer count(String id);

//  貸出管理テーブルから在庫管理番号が一致し、貸出管理番号は一致しない、貸出ステータスが0貸出待ち、1貸出中の貸出予定日、返却予定日を取得する   
@Query("select count (*) from RentalManage where stock.id = ?1 AND id != ?2 AND status IN (0, 1) AND (expectedRentalOn > ?3 OR expectedReturnOn < ?4)")
    Integer rentalPeriod(String stockId, Long id, Date expected_return_on, Date expected_rental_on);

@Query("select count (*) from RentalManage where stock.id = ?1 AND id != ?2 AND status IN (0, 1)")
    Integer test(String stockId, Long id);
    
@Query("select count (*) from RentalManage where stock.id = ?1 AND status IN (0, 1) AND (expectedRentalOn > ?2 OR expectedReturnOn < ?3)")
    Integer rentalPeriodAdd(String stockId, Date expected_return_on, Date expected_rental_on);

@Query("select count (*) from RentalManage where stock.id = ?1 AND status IN (0, 1)")
    Integer testAdd(String stockId);

}
