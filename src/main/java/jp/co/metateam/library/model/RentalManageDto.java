package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import jp.co.metateam.library.values.RentalStatus;

/**
 * 貸出管理DTO
 */
@Getter
@Setter
public class RentalManageDto {

    private Long id;

    @NotEmpty(message="在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message="社員番号は必須です")
    private String employeeId;

    @NotNull(message="貸出ステータスは必須です")
    private Integer status;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="貸出予定日は必須です")
    private Date expectedRentalOn;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="返却予定日は必須です")
    private Date expectedReturnOn;

    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;

    public Optional<String> statusCheckEdit(Integer formerStatus, Integer status, Date expectedRental, Date expectedReturn) {
        if(formerStatus != status){
            Date date;
            date = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
            if ((formerStatus == RentalStatus.RENT_WAIT.getValue() && status == RentalStatus.RETURNED.getValue()) ||  //貸出待ちから返却済み
                (formerStatus == RentalStatus.RENTAlING.getValue() && status == RentalStatus.RENT_WAIT.getValue()) || //貸出中から貸出待ち
                (formerStatus == RentalStatus.RENTAlING.getValue() && status == RentalStatus.CANCELED.getValue()) ||  //貸出中からキャンセル
                (formerStatus == RentalStatus.RETURNED.getValue() && status == RentalStatus.RENT_WAIT.getValue()) ||  //返却済みから貸出待ち
                (formerStatus == RentalStatus.RETURNED.getValue() && status == RentalStatus.RENTAlING.getValue()) ||  //返却済みから貸出中
                (formerStatus == RentalStatus.RETURNED.getValue() && status == RentalStatus.CANCELED.getValue()) ||   //返却済みからキャンセル
                (formerStatus == RentalStatus.CANCELED.getValue() && status == RentalStatus.RENT_WAIT.getValue()) ||  //キャンセルから貸出待ち
                (formerStatus == RentalStatus.CANCELED.getValue() && status == RentalStatus.RENTAlING.getValue()) ||  //キャンセルから貸出中
                (formerStatus == RentalStatus.CANCELED.getValue() && status == RentalStatus.RETURNED.getValue()) )    //キャンセルから返却済み
                {
                    return Optional.of("そのステータスの変更はできません");
             
                 } 

                 
                else if (formerStatus ==RentalStatus.RENT_WAIT.getValue() && status == RentalStatus.RENTAlING.getValue()) //貸出待ちから貸出中
            {
                if(!(expectedRental.compareTo(date) == 0)){
                    return Optional.of("日付が不正です");
                }

            } else if (formerStatus ==RentalStatus.RENTAlING.getValue() && status == RentalStatus.RETURNED.getValue()) //貸出中から返却済み
            {
                if(!(expectedReturn.compareTo(date) == 0)){
                    return Optional.of("日付が不正です");
                }
            }

            return Optional.empty();
        
        } else {
            return Optional.empty();
        }
       
        }
    

    public Optional<String> statusCheckAdd(Integer status, Date expectedRental) {
        Date date;
        date = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (status == RentalStatus.RENTAlING.getValue()) //貸出中のステータスが選択されている
            {
                if(!(expectedRental.compareTo(date) == 0)){
                    return Optional.of("日付が不正です");
                }
            }  
        else if(status == RentalStatus.RETURNED.getValue() ||
                status == RentalStatus.CANCELED.getValue())
            {
                return Optional.of("そのステータスでは登録できません");
            }
            return Optional.empty();
            }        
        
}
