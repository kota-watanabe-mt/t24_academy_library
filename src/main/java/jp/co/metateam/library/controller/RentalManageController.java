package jp.co.metateam.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.validation.Valid;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import lombok.extern.log4j.Log4j2;

import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.Stock;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;



/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {

    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;

    @Autowired
    public RentalManageController(
        AccountService accountService, 
        RentalManageService rentalManageService, 
        StockService stockService
    ) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    }

    /**
     * 貸出一覧画面初期表示
     * @param model
     * @return
     */
    @GetMapping("/rental/index")
    public String index(Model model) {
        // 貸出管理テーブルから全件取得
        List<RentalManage> rentalManageList = this.rentalManageService.findAll();
        // 貸出一覧画面に渡すデータをmodelに追加
        model.addAttribute("rentalManageList", rentalManageList);
        // 貸出一覧画面に遷移
        return "rental/index";
    }



    @GetMapping("/rental/add")
    public String add(Model model) {
        List<Account> accounts = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findStockAvailableAll();
        
        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());


        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }

        return "rental/add";
    }



    @PostMapping("/rental/add")
    public String register(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra, Model model){
        try{
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }

            Optional<String> s = rentalManageDto.statusCheckAdd(rentalManageDto.getStatus(), rentalManageDto.getExpectedRentalOn());

            if(!(s.isEmpty())){
                //エラーメッセージの設定
                result.addError(new FieldError("rentalManage", "status", s.get()));
                throw new Exception("Validation error.");
            }

            Optional<String> r = rentalManageService.rentalAbleAdd(rentalManageDto.getStockId(), new java.sql.Date(rentalManageDto.getExpectedRentalOn().getTime()), new java.sql.Date(rentalManageDto.getExpectedReturnOn().getTime()));
            if(!(r.isEmpty())){
                //エラーメッセージの設定
                result.addError(new FieldError("rentalManageDto", "status", r.get()));
                throw new Exception("Validation error.");
            }
            //登録処理
            this.rentalManageService.save(rentalManageDto);

            return "redirect:/rental/index";
           } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            return "redirect:/rental/add";
        }
    }

    @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model) {
        List<Account> accounts = this.accountService.findAll();
        List <Stock> stockList = this.stockService.findStockAvailableAll();
     
        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        
     
        if (!model.containsAttribute("rentalManageDto")) {
            RentalManageDto rentalManageDto = new RentalManageDto();
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
     
            rentalManageDto.setId(rentalManage.getId());
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
            rentalManageDto.setStockId(rentalManage.getStock().getId());
            rentalManageDto.setStatus(rentalManage.getStatus());
            
     
            model.addAttribute("rentalManageDto", rentalManageDto);
        }
        return "rental/edit";
    }
 
 
    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra, Model model) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));

            Optional<String> s = rentalManageDto.statusCheckEdit(rentalManage.getStatus(), rentalManageDto.getStatus(), rentalManageDto.getExpectedRentalOn(),rentalManageDto.getExpectedReturnOn());

            if(!(s.isEmpty())){
                //エラーメッセージの設定
                result.addError(new FieldError("rentalManage", "status", s.get()));
                throw new Exception("Validation error.");
            }

            // 貸出可否チェック
            Optional<String> r = rentalManageService.rentalAble(rentalManageDto.getStockId(), Long.valueOf(id), new java.sql.Date(rentalManageDto.getExpectedRentalOn().getTime()), new java.sql.Date(rentalManageDto.getExpectedReturnOn().getTime()));
            if(!(r.isEmpty())){
                //エラーメッセージの設定
                result.addError(new FieldError("rentalManageDto", "status", r.get()));
                throw new Exception("Validation error.");
            }

            // 更新処理
            rentalManageService.update(Long.valueOf(id), rentalManageDto);
            
            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);


            return String.format("redirect:/rental/%s/edit", id);
        }
    }
}

// java.util.Date d = rentalManageDto.getExpectedRentalOn();
            // Calendar cal = Calendar.getInstance();
            // cal.setTime(d);
            // cal.set(Calendar.HOUR_OF_DAY, 0);
            // cal.set(Calendar.MINUTE, 0);
            // cal.set(Calendar.SECOND, 0);
            // cal.set(Calendar.MILLISECOND, 0);
            // java.sql.Date d2 = new java.sql.Date(cal.getTimeInMillis());

            // java.util.Date d3 = rentalManageDto.getExpectedReturnOn();
            // Calendar cal1 = Calendar.getInstance();
            // cal1.setTime(d3);
            // cal1.set(Calendar.HOUR_OF_DAY, 0);
            // cal1.set(Calendar.MINUTE, 0);
            // cal1.set(Calendar.SECOND, 0);
            // cal1.set(Calendar.MILLISECOND, 0);
            // java.sql.Date d4 = new java.sql.Date(cal1.getTimeInMillis());